import { createApp, h } from 'vue'
import Toast from '@/components/Toast.vue'

let toastInstance = null
let toastApp = null

function showToast(message, type = 'info', duration = 3000) {
  // 如果已有Toast实例，先清除
  if (toastInstance) {
    hideToast()
  }

  // 创建容器
  const container = document.createElement('div')
  document.body.appendChild(container)

  // 创建Vue应用实例
  toastApp = createApp({
    data() {
      return {
        visible: false,
        message,
        type,
        duration
      }
    },
    mounted() {
      // 下一帧显示，触发动画
      requestAnimationFrame(() => {
        this.visible = true
      })
    },
    methods: {
      handleClose() {
        this.visible = false
        setTimeout(() => {
          if (toastApp) {
            toastApp.unmount()
            toastApp = null
          }
          if (container && container.parentNode) {
            container.parentNode.removeChild(container)
          }
          toastInstance = null
        }, 300) // 等待动画完成
      }
    },
    render() {
      return h(Toast, {
        message: this.message,
        type: this.type,
        duration: this.duration,
        visible: this.visible,
        onClose: this.handleClose
      })
    }
  })

  toastInstance = toastApp.mount(container)
}

function hideToast() {
  if (toastInstance && toastInstance.handleClose) {
    toastInstance.handleClose()
  }
}

export default {
  success(message, duration = 3000) {
    showToast(message, 'success', duration)
  },
  error(message, duration = 3000) {
    showToast(message, 'error', duration)
  },
  warning(message, duration = 3000) {
    showToast(message, 'warning', duration)
  },
  info(message, duration = 3000) {
    showToast(message, 'info', duration)
  },
  hide() {
    hideToast()
  }
}
