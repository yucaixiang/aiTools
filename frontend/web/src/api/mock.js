// Mock数据 - 用于测试前端功能
export const mockTools = [
  {
    id: 1,
    name: 'ChatGPT',
    tagline: '强大的AI对话助手',
    description: 'OpenAI开发的大型语言模型，可以进行自然对话、回答问题、写作、编程等。',
    logoUrl: 'https://via.placeholder.com/100?text=ChatGPT',
    websiteUrl: 'https://chat.openai.com',
    categoryId: 1,
    category: 'AI写作',
    averageRating: 4.8,
    upvoteCount: 1250,
    reviewCount: 380,
    status: 'approved',
    tags: [
      { id: 1, name: '对话' },
      { id: 2, name: '写作' },
      { id: 3, name: '编程' }
    ],
    createdAt: '2024-01-15T10:30:00'
  },
  {
    id: 2,
    name: 'Midjourney',
    tagline: 'AI艺术创作工具',
    description: '通过文字描述生成精美的艺术图片，支持多种艺术风格。',
    logoUrl: 'https://via.placeholder.com/100?text=Midjourney',
    websiteUrl: 'https://midjourney.com',
    categoryId: 2,
    category: 'AI绘画',
    averageRating: 4.9,
    upvoteCount: 980,
    reviewCount: 256,
    status: 'approved',
    tags: [
      { id: 4, name: '绘画' },
      { id: 5, name: '设计' }
    ],
    createdAt: '2024-01-20T14:20:00'
  },
  {
    id: 3,
    name: 'GitHub Copilot',
    tagline: 'AI编程助手',
    description: 'GitHub推出的AI代码补全工具，支持多种编程语言。',
    logoUrl: 'https://via.placeholder.com/100?text=Copilot',
    websiteUrl: 'https://github.com/features/copilot',
    categoryId: 3,
    category: '代码助手',
    averageRating: 4.7,
    upvoteCount: 850,
    reviewCount: 420,
    status: 'approved',
    tags: [
      { id: 6, name: '编程' },
      { id: 7, name: '自动补全' }
    ],
    createdAt: '2024-02-01T09:15:00'
  },
  {
    id: 4,
    name: 'Notion AI',
    tagline: 'AI写作和笔记工具',
    description: 'Notion集成的AI功能，帮助你写作、总结、翻译等。',
    logoUrl: 'https://via.placeholder.com/100?text=Notion',
    websiteUrl: 'https://notion.so',
    categoryId: 5,
    category: '办公效率',
    averageRating: 4.6,
    upvoteCount: 720,
    reviewCount: 198,
    status: 'approved',
    tags: [
      { id: 8, name: '笔记' },
      { id: 9, name: '写作' }
    ],
    createdAt: '2024-02-10T16:45:00'
  },
  {
    id: 5,
    name: 'Stable Diffusion',
    tagline: '开源AI图像生成',
    description: '开源的AI图像生成模型，可本地部署使用。',
    logoUrl: 'https://via.placeholder.com/100?text=SD',
    websiteUrl: 'https://stability.ai',
    categoryId: 2,
    category: 'AI绘画',
    averageRating: 4.5,
    upvoteCount: 650,
    reviewCount: 315,
    status: 'approved',
    tags: [
      { id: 4, name: '绘画' },
      { id: 10, name: '开源' }
    ],
    createdAt: '2024-02-15T11:30:00'
  },
  {
    id: 6,
    name: 'Jasper',
    tagline: 'AI营销文案助手',
    description: '专注于营销文案创作的AI工具，支持多种文案模板。',
    logoUrl: 'https://via.placeholder.com/100?text=Jasper',
    websiteUrl: 'https://jasper.ai',
    categoryId: 1,
    category: 'AI写作',
    averageRating: 4.4,
    upvoteCount: 580,
    reviewCount: 167,
    status: 'approved',
    tags: [
      { id: 11, name: '营销' },
      { id: 2, name: '写作' }
    ],
    createdAt: '2024-03-01T13:20:00'
  },
  {
    id: 7,
    name: 'Tableau',
    tagline: '数据可视化分析工具',
    description: '强大的数据分析和可视化平台，支持AI辅助分析。',
    logoUrl: 'https://via.placeholder.com/100?text=Tableau',
    websiteUrl: 'https://tableau.com',
    categoryId: 4,
    category: '数据分析',
    averageRating: 4.6,
    upvoteCount: 490,
    reviewCount: 205,
    status: 'approved',
    tags: [
      { id: 12, name: '数据' },
      { id: 13, name: '可视化' }
    ],
    createdAt: '2024-03-05T10:10:00'
  },
  {
    id: 8,
    name: 'Canva AI',
    tagline: 'AI设计工具',
    description: 'Canva的AI设计功能，一键生成设计稿。',
    logoUrl: 'https://via.placeholder.com/100?text=Canva',
    websiteUrl: 'https://canva.com',
    categoryId: 6,
    category: '设计工具',
    averageRating: 4.7,
    upvoteCount: 820,
    reviewCount: 289,
    status: 'approved',
    tags: [
      { id: 5, name: '设计' },
      { id: 14, name: '模板' }
    ],
    createdAt: '2024-03-10T15:40:00'
  },
  {
    id: 9,
    name: 'Cursor',
    tagline: 'AI代码编辑器',
    description: '专为AI编程设计的代码编辑器，内置强大的AI助手。',
    logoUrl: 'https://via.placeholder.com/100?text=Cursor',
    websiteUrl: 'https://cursor.sh',
    categoryId: 3,
    category: '代码助手',
    averageRating: 4.8,
    upvoteCount: 1100,
    reviewCount: 342,
    status: 'approved',
    tags: [
      { id: 6, name: '编程' },
      { id: 15, name: 'IDE' }
    ],
    createdAt: '2024-03-15T12:25:00'
  },
  {
    id: 10,
    name: 'Grammarly',
    tagline: 'AI英文写作助手',
    description: 'AI驱动的英文语法检查和写作改进工具。',
    logoUrl: 'https://via.placeholder.com/100?text=Grammarly',
    websiteUrl: 'https://grammarly.com',
    categoryId: 1,
    category: 'AI写作',
    averageRating: 4.5,
    upvoteCount: 670,
    reviewCount: 412,
    status: 'approved',
    tags: [
      { id: 2, name: '写作' },
      { id: 16, name: '英文' }
    ],
    createdAt: '2024-03-20T09:50:00'
  },
  {
    id: 11,
    name: 'Leonardo AI',
    tagline: 'AI艺术生成平台',
    description: '专业的AI图像生成工具，适合游戏和设计师使用。',
    logoUrl: 'https://via.placeholder.com/100?text=Leonardo',
    websiteUrl: 'https://leonardo.ai',
    categoryId: 2,
    category: 'AI绘画',
    averageRating: 4.6,
    upvoteCount: 540,
    reviewCount: 178,
    status: 'approved',
    tags: [
      { id: 4, name: '绘画' },
      { id: 17, name: '游戏' }
    ],
    createdAt: '2024-03-25T14:15:00'
  },
  {
    id: 12,
    name: 'Claude',
    tagline: 'Anthropic AI助手',
    description: 'Anthropic开发的AI助手，擅长复杂对话和分析。',
    logoUrl: 'https://via.placeholder.com/100?text=Claude',
    websiteUrl: 'https://claude.ai',
    categoryId: 1,
    category: 'AI写作',
    averageRating: 4.9,
    upvoteCount: 1450,
    reviewCount: 521,
    status: 'approved',
    tags: [
      { id: 1, name: '对话' },
      { id: 18, name: '分析' }
    ],
    createdAt: '2024-04-01T11:00:00'
  }
]

// 模拟分页 - 使用后端ToolQueryDTO的参数名
export function getMockTools(current = 1, size = 12, categoryId = null, sortBy = 'upvote') {
  let filtered = [...mockTools]

  // 分类筛选
  if (categoryId) {
    filtered = filtered.filter(t => t.categoryId === categoryId)
  }

  // 排序 - 使用后端定义的sortBy值
  if (sortBy === 'launch') {
    // 按发布时间排序
    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } else if (sortBy === 'rating') {
    // 按评分排序
    filtered.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0))
  } else {
    // 默认按点赞数排序 (upvote)
    filtered.sort((a, b) => (b.upvoteCount || 0) - (a.upvoteCount || 0))
  }

  // 分页
  const start = (current - 1) * size
  const end = start + size
  const records = filtered.slice(start, end)

  return {
    code: 200,
    message: 'success',
    data: {
      records,
      total: filtered.length,
      current,
      size
    }
  }
}
