# AI工具推荐系统 - Kubernetes部署文档

## 文档信息
- **项目名称**：智能工具推荐平台
- **版本**：v1.0
- **创建日期**：2025-12-04
- **K8s版本**：v1.28+

---

## 一、集群规划

### 1.1 节点配置

**生产环境集群（最小配置）**：

| 节点类型 | 数量 | 配置 | 用途 |
|---------|------|------|------|
| **Master节点** | 3 | 4核8G | 集群管理 |
| **应用节点** | 3 | 4核8G | 运行微服务 |
| **数据节点** | 3 | 8核16G | MySQL、Redis、ES |
| **AI节点** | 1 | 8核16G + GPU | Ollama模型 |

**开发/测试环境（简化配置）**：
- 1个Master节点：2核4G
- 2个Worker节点：4核8G

### 1.2 存储规划

```yaml
存储类型：
  - 本地存储（local-path）：适用于开发环境
  - NFS：适用于文件共享
  - Ceph/Longhorn：适用于生产环境持久化存储
  - 云厂商存储：阿里云盘、AWS EBS

存储大小：
  - MySQL：100GB
  - Redis：20GB
  - Elasticsearch：200GB
  - Qdrant：50GB
  - 日志：100GB
```

---

## 二、基础环境准备

### 2.1 安装K8s集群

**使用kubeadm安装（Ubuntu 22.04）**：

```bash
# 1. 关闭swap
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# 2. 安装Docker
curl -fsSL https://get.docker.com | bash
sudo usermod -aG docker $USER

# 3. 安装kubeadm、kubelet、kubectl
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl

curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | \
  sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | \
  sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# 4. 初始化Master节点
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# 5. 配置kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 6. 安装网络插件（Calico）
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml

# 7. Worker节点加入集群（在Worker节点执行）
kubeadm join <master-ip>:6443 --token <token> \
  --discovery-token-ca-cert-hash sha256:<hash>
```

### 2.2 安装必要组件

**Ingress Controller（Nginx）**：
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.5/deploy/static/provider/cloud/deploy.yaml
```

**Metrics Server（监控）**：
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

**StorageClass（存储）**：
```bash
# 使用local-path-provisioner（开发环境）
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.26/deploy/local-path-storage.yaml

# 设置为默认StorageClass
kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

---

## 三、命名空间和资源配额

### 3.1 创建命名空间

```yaml
# namespaces.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: tool-recommend
  labels:
    name: tool-recommend

---
apiVersion: v1
kind: Namespace
metadata:
  name: tool-recommend-middleware
  labels:
    name: tool-recommend-middleware
```

```bash
kubectl apply -f namespaces.yaml
```

### 3.2 资源配额

```yaml
# resource-quota.yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: app-quota
  namespace: tool-recommend
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    pods: "50"
```

---

## 四、ConfigMap和Secret

### 4.1 应用配置

```yaml
# configmap-app.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: tool-recommend
data:
  # 数据库配置
  DB_HOST: "mysql-service.tool-recommend-middleware.svc.cluster.local"
  DB_PORT: "3306"
  DB_NAME: "tool_recommend"

  # Redis配置
  REDIS_HOST: "redis-service.tool-recommend-middleware.svc.cluster.local"
  REDIS_PORT: "6379"

  # Elasticsearch配置
  ES_HOST: "elasticsearch-service.tool-recommend-middleware.svc.cluster.local"
  ES_PORT: "9200"

  # Kafka配置
  KAFKA_BROKERS: "kafka-service.tool-recommend-middleware.svc.cluster.local:9092"

  # AI服务配置
  OLLAMA_URL: "http://ollama-service:11434"

  # 应用配置
  SPRING_PROFILES_ACTIVE: "prod"
  LOG_LEVEL: "INFO"
```

### 4.2 敏感信息

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: tool-recommend
type: Opaque
stringData:
  DB_PASSWORD: "your_db_password"
  REDIS_PASSWORD: "your_redis_password"
  JWT_SECRET: "your_jwt_secret_key"
  QWEN_API_KEY: "your_qwen_api_key"
```

```bash
kubectl apply -f secret.yaml
```

---

## 五、数据库部署

### 5.1 MySQL StatefulSet

```yaml
# mysql-statefulset.yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: tool-recommend-middleware
spec:
  ports:
  - port: 3306
  clusterIP: None
  selector:
    app: mysql

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: tool-recommend-middleware
spec:
  serviceName: mysql-service
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
          name: mysql
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: DB_PASSWORD
        - name: MYSQL_DATABASE
          value: "tool_recommend"
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
        - name: mysql-config
          mountPath: /etc/mysql/conf.d
      volumes:
      - name: mysql-config
        configMap:
          name: mysql-config
  volumeClaimTemplates:
  - metadata:
      name: mysql-data
    spec:
      accessModes: ["ReadWriteOnce"]
      storageClassName: "local-path"
      resources:
        requests:
          storage: 100Gi

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-config
  namespace: tool-recommend-middleware
data:
  custom.cnf: |
    [mysqld]
    character-set-server=utf8mb4
    collation-server=utf8mb4_unicode_ci
    max_connections=1000
    innodb_buffer_pool_size=2G
    slow_query_log=1
    long_query_time=1
```

### 5.2 Redis Deployment

```yaml
# redis-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: tool-recommend-middleware
spec:
  ports:
  - port: 6379
  selector:
    app: redis

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: tool-recommend-middleware
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1"
        volumeMounts:
        - name: redis-data
          mountPath: /data
      volumes:
      - name: redis-data
        persistentVolumeClaim:
          claimName: redis-pvc

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: tool-recommend-middleware
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
```

### 5.3 Elasticsearch StatefulSet

```yaml
# elasticsearch-statefulset.yaml
apiVersion: v1
kind: Service
metadata:
  name: elasticsearch-service
  namespace: tool-recommend-middleware
spec:
  ports:
  - port: 9200
    name: http
  - port: 9300
    name: transport
  clusterIP: None
  selector:
    app: elasticsearch

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: elasticsearch
  namespace: tool-recommend-middleware
spec:
  serviceName: elasticsearch-service
  replicas: 3
  selector:
    matchLabels:
      app: elasticsearch
  template:
    metadata:
      labels:
        app: elasticsearch
    spec:
      initContainers:
      - name: increase-vm-max-map
        image: busybox
        command: ["sysctl", "-w", "vm.max_map_count=262144"]
        securityContext:
          privileged: true
      containers:
      - name: elasticsearch
        image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
        ports:
        - containerPort: 9200
          name: http
        - containerPort: 9300
          name: transport
        env:
        - name: cluster.name
          value: "tool-recommend-es"
        - name: node.name
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: discovery.seed_hosts
          value: "elasticsearch-0.elasticsearch-service,elasticsearch-1.elasticsearch-service,elasticsearch-2.elasticsearch-service"
        - name: cluster.initial_master_nodes
          value: "elasticsearch-0,elasticsearch-1,elasticsearch-2"
        - name: ES_JAVA_OPTS
          value: "-Xms2g -Xmx2g"
        - name: xpack.security.enabled
          value: "false"
        resources:
          requests:
            memory: "4Gi"
            cpu: "1"
          limits:
            memory: "6Gi"
            cpu: "2"
        volumeMounts:
        - name: es-data
          mountPath: /usr/share/elasticsearch/data
  volumeClaimTemplates:
  - metadata:
      name: es-data
    spec:
      accessModes: ["ReadWriteOnce"]
      storageClassName: "local-path"
      resources:
        requests:
          storage: 200Gi
```

---

## 六、应用服务部署

### 6.1 Gateway Service

```yaml
# gateway-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
  namespace: tool-recommend
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: gateway

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway
  namespace: tool-recommend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
      - name: gateway
        image: your-registry/gateway-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        envFrom:
        - configMapRef:
            name: app-config
        - secretRef:
            name: app-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 6.2 Tool Service

```yaml
# tool-service-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: tool-service
  namespace: tool-recommend
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: 8081
  selector:
    app: tool-service

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tool-service
  namespace: tool-recommend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tool-service
  template:
    metadata:
      labels:
        app: tool-service
    spec:
      containers:
      - name: tool-service
        image: your-registry/tool-service:latest
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef:
            name: app-config
        - secretRef:
            name: app-secrets
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 6.3 AI Service

```yaml
# ai-service-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: ai-service
  namespace: tool-recommend
spec:
  type: ClusterIP
  ports:
  - port: 8082
    targetPort: 8082
  selector:
    app: ai-service

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-service
  namespace: tool-recommend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ai-service
  template:
    metadata:
      labels:
        app: ai-service
    spec:
      containers:
      - name: ai-service
        image: your-registry/ai-service:latest
        ports:
        - containerPort: 8082
        envFrom:
        - configMapRef:
            name: app-config
        - secretRef:
            name: app-secrets
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
```

### 6.4 Ollama Service（GPU节点）

```yaml
# ollama-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: ollama-service
  namespace: tool-recommend
spec:
  type: ClusterIP
  ports:
  - port: 11434
    targetPort: 11434
  selector:
    app: ollama

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ollama
  namespace: tool-recommend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ollama
  template:
    metadata:
      labels:
        app: ollama
    spec:
      nodeSelector:
        gpu: "true"  # 选择GPU节点
      containers:
      - name: ollama
        image: ollama/ollama:latest
        ports:
        - containerPort: 11434
        env:
        - name: OLLAMA_HOST
          value: "0.0.0.0"
        - name: OLLAMA_NUM_PARALLEL
          value: "2"
        resources:
          requests:
            memory: "8Gi"
            cpu: "2"
            nvidia.com/gpu: 1
          limits:
            memory: "16Gi"
            cpu: "4"
            nvidia.com/gpu: 1
        volumeMounts:
        - name: ollama-data
          mountPath: /root/.ollama
      volumes:
      - name: ollama-data
        persistentVolumeClaim:
          claimName: ollama-pvc
```

---

## 七、Ingress配置

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tool-recommend-ingress
  namespace: tool-recommend
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.toolrecommend.com
    secretName: tool-recommend-tls
  rules:
  - host: api.toolrecommend.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway-service
            port:
              number: 8080
```

---

## 八、自动扩缩容

### 8.1 HPA（水平自动扩缩容）

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: tool-service-hpa
  namespace: tool-recommend
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: tool-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## 九、CI/CD流程

### 9.1 Jenkinsfile

```groovy
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        K8S_NAMESPACE = 'tool-recommend'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/your-org/tool-recommend.git'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/tool-service:${IMAGE_TAG}")
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials') {
                        docker.image("${DOCKER_REGISTRY}/tool-service:${IMAGE_TAG}").push()
                        docker.image("${DOCKER_REGISTRY}/tool-service:${IMAGE_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh """
                    kubectl set image deployment/tool-service \
                      tool-service=${DOCKER_REGISTRY}/tool-service:${IMAGE_TAG} \
                      -n ${K8S_NAMESPACE}

                    kubectl rollout status deployment/tool-service -n ${K8S_NAMESPACE}
                """
            }
        }
    }

    post {
        success {
            echo '部署成功！'
        }
        failure {
            echo '部署失败，请检查日志'
        }
    }
}
```

---

## 十、监控和日志

### 10.1 Prometheus配置

```yaml
# prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s

    scrape_configs:
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
```

### 10.2 日志收集（EFK）

```bash
# 安装EFK Stack
kubectl apply -f https://raw.githubusercontent.com/kubernetes/kubernetes/master/cluster/addons/fluentd-elasticsearch/fluentd-es-configmap.yaml
```

---

## 十一、常用运维命令

```bash
# 查看Pod状态
kubectl get pods -n tool-recommend

# 查看Pod日志
kubectl logs -f <pod-name> -n tool-recommend

# 进入Pod
kubectl exec -it <pod-name> -n tool-recommend -- /bin/bash

# 查看服务
kubectl get svc -n tool-recommend

# 查看Ingress
kubectl get ingress -n tool-recommend

# 扩容
kubectl scale deployment tool-service --replicas=5 -n tool-recommend

# 滚动更新
kubectl set image deployment/tool-service tool-service=new-image:tag -n tool-recommend

# 回滚
kubectl rollout undo deployment/tool-service -n tool-recommend

# 查看资源使用
kubectl top nodes
kubectl top pods -n tool-recommend
```

---

**文档版本**：v1.0
**最后更新**：2025-12-04
