// 图标名称映射（用于动态导入）
export const toolIconNames: Record<string, string> = {
  'Braces': 'Braces',
  'FileJson': 'FileJson',
  'Code2': 'Code2',
  'Search': 'Search',
  'Clock': 'Clock',
  'Binary': 'Binary',
  'ImageMinus': 'ImageMinus',
  'ImagePlus': 'ImagePlus',
  'Image': 'Image',
  'QrCode': 'QrCode',
  'FileType': 'FileType',
  'FileMinus': 'FileMinus',
  'FileEdit': 'FileEdit',
  'Hash': 'Hash',
  'Fingerprint': 'Fingerprint',
  'Link': 'Link',
  'Key': 'Key',
  'GitCompare': 'GitCompare',
  'Text': 'Text',
  'Type': 'Type',
  'Dices': 'Dices',
  'Scale': 'Scale',
}

// 工具分类
export const categories = [
  { id: 'dev', name: '开发工具', icon: 'Code', description: 'JSON格式化、代码压缩、正则测试等开发常用工具' },
  { id: 'image', name: '图片工具', icon: 'Image', description: '图片压缩、格式转换、Base64编码等图片处理工具' },
  { id: 'document', name: '文档转换', icon: 'FileText', description: 'PDF转换、Word转换、Markdown编辑等文档工具' },
  { id: 'crypto', name: '加密工具', icon: 'Lock', description: 'MD5加密、Base64、URL编码等加密解密工具' },
  { id: 'text', name: '文本工具', icon: 'Type', description: '文本对比、字数统计、大小写转换等文本处理工具' },
  { id: 'number', name: '数字工具', icon: 'Calculator', description: '进制转换、单位换算、随机数生成等数字工具' },
]

// 工具列表
export const tools = [
  // 开发工具
  {
    id: 'json-formatter',
    name: 'JSON格式化',
    description: 'JSON数据的格式化、压缩、转义等操作',
    category: 'dev',
    icon: 'Braces',
    isVip: false,
    visits: 125432,
    rating: 4.8,
    reviewCount: 328,
    tags: ['热门', '常用'],
  },
  {
    id: 'json-to-yaml',
    name: 'JSON转YAML',
    description: 'JSON与YAML格式互相转换',
    category: 'dev',
    icon: 'FileJson',
    isVip: false,
    visits: 45678,
    rating: 4.6,
    reviewCount: 128,
    tags: [],
  },
  {
    id: 'code-beautify',
    name: '代码美化',
    description: 'HTML/CSS/JavaScript代码格式化',
    category: 'dev',
    icon: 'Code2',
    isVip: true,
    visits: 89321,
    rating: 4.7,
    reviewCount: 256,
    tags: ['VIP'],
  },
  {
    id: 'regex-tester',
    name: '正则测试',
    description: '在线正则表达式测试工具',
    category: 'dev',
    icon: 'Search',
    isVip: false,
    visits: 67890,
    rating: 4.5,
    reviewCount: 189,
    tags: [],
  },
  {
    id: 'timestamp-converter',
    name: '时间戳转换',
    description: 'Unix时间戳与日期时间互转',
    category: 'dev',
    icon: 'Clock',
    isVip: false,
    visits: 98765,
    rating: 4.9,
    reviewCount: 412,
    tags: ['热门'],
  },
  {
    id: 'base64-codec',
    name: 'Base64编解码',
    description: 'Base64编码和解码工具',
    category: 'dev',
    icon: 'Binary',
    isVip: false,
    visits: 76543,
    rating: 4.6,
    reviewCount: 198,
    tags: [],
  },
  
  // 图片工具
  {
    id: 'image-compress',
    name: '图片压缩',
    description: '在线图片压缩，支持JPG/PNG/GIF',
    category: 'image',
    icon: 'ImageMinus',
    isVip: false,
    visits: 156789,
    rating: 4.7,
    reviewCount: 523,
    tags: ['热门', '常用'],
  },
  {
    id: 'image-convert',
    name: '图片格式转换',
    description: '图片格式互相转换',
    category: 'image',
    icon: 'ImagePlus',
    isVip: true,
    visits: 87654,
    rating: 4.5,
    reviewCount: 234,
    tags: ['VIP'],
  },
  {
    id: 'image-to-base64',
    name: '图片转Base64',
    description: '图片转换为Base64编码',
    category: 'image',
    icon: 'Image',
    isVip: false,
    visits: 65432,
    rating: 4.4,
    reviewCount: 156,
    tags: [],
  },
  {
    id: 'qr-code',
    name: '二维码生成',
    description: '生成二维码，支持自定义样式',
    category: 'image',
    icon: 'QrCode',
    isVip: false,
    visits: 112345,
    rating: 4.8,
    reviewCount: 389,
    tags: ['热门'],
  },
  
  // 文档转换
  {
    id: 'pdf-to-word',
    name: 'PDF转Word',
    description: 'PDF文档转换为Word格式',
    category: 'document',
    icon: 'FileType',
    isVip: true,
    visits: 234567,
    rating: 4.6,
    reviewCount: 678,
    tags: ['热门', 'VIP'],
  },
  {
    id: 'pdf-compress',
    name: 'PDF压缩',
    description: '压缩PDF文件大小',
    category: 'document',
    icon: 'FileMinus',
    isVip: true,
    visits: 145678,
    rating: 4.5,
    reviewCount: 445,
    tags: ['VIP'],
  },
  {
    id: 'markdown-editor',
    name: 'Markdown编辑器',
    description: '在线Markdown编辑与预览',
    category: 'document',
    icon: 'FileEdit',
    isVip: false,
    visits: 56789,
    rating: 4.7,
    reviewCount: 234,
    tags: [],
  },
  
  // 加密工具
  {
    id: 'md5-encrypt',
    name: 'MD5加密',
    description: 'MD5哈希加密工具',
    category: 'crypto',
    icon: 'Hash',
    isVip: false,
    visits: 87654,
    rating: 4.5,
    reviewCount: 267,
    tags: [],
  },
  {
    id: 'sha-encrypt',
    name: 'SHA加密',
    description: 'SHA1/SHA256/SHA512加密',
    category: 'crypto',
    icon: 'Fingerprint',
    isVip: false,
    visits: 54321,
    rating: 4.4,
    reviewCount: 145,
    tags: [],
  },
  {
    id: 'url-codec',
    name: 'URL编解码',
    description: 'URL编码和解码工具',
    category: 'crypto',
    icon: 'Link',
    isVip: false,
    visits: 67890,
    rating: 4.6,
    reviewCount: 198,
    tags: [],
  },
  {
    id: 'aes-encrypt',
    name: 'AES加密',
    description: 'AES对称加密解密工具',
    category: 'crypto',
    icon: 'Key',
    isVip: true,
    visits: 45678,
    rating: 4.7,
    reviewCount: 123,
    tags: ['VIP'],
  },
  
  // 文本工具
  {
    id: 'text-diff',
    name: '文本对比',
    description: '文本差异对比工具',
    category: 'text',
    icon: 'GitCompare',
    isVip: false,
    visits: 34567,
    rating: 4.5,
    reviewCount: 98,
    tags: [],
  },
  {
    id: 'word-count',
    name: '字数统计',
    description: '统计文本字数、字符数、行数',
    category: 'text',
    icon: 'Text',
    isVip: false,
    visits: 78901,
    rating: 4.8,
    reviewCount: 312,
    tags: ['热门'],
  },
  {
    id: 'case-convert',
    name: '大小写转换',
    description: '英文大小写互相转换',
    category: 'text',
    icon: 'Type',
    isVip: false,
    visits: 45678,
    rating: 4.4,
    reviewCount: 156,
    tags: [],
  },
  
  // 数字工具
  {
    id: 'base-convert',
    name: '进制转换',
    description: '二进制、八进制、十进制、十六进制互转',
    category: 'number',
    icon: 'Binary',
    isVip: false,
    visits: 56789,
    rating: 4.6,
    reviewCount: 178,
    tags: [],
  },
  {
    id: 'random-number',
    name: '随机数生成',
    description: '生成随机数和随机字符串',
    category: 'number',
    icon: 'Dices',
    isVip: false,
    visits: 43210,
    rating: 4.5,
    reviewCount: 134,
    tags: [],
  },
  {
    id: 'unit-convert',
    name: '单位换算',
    description: '长度、重量、面积、体积等单位换算',
    category: 'number',
    icon: 'Scale',
    isVip: false,
    visits: 34567,
    rating: 4.3,
    reviewCount: 89,
    tags: [],
  },
]

// 获取工具列表
export function getTools(category?: string) {
  if (category && category !== 'all') {
    return tools.filter(tool => tool.category === category)
  }
  return tools
}

// 获取工具详情
export function getToolById(id: string) {
  return tools.find(tool => tool.id === id)
}

// 获取热门工具
export function getHotTools(limit: number = 8) {
  return tools
    .filter(tool => tool.tags.includes('热门'))
    .slice(0, limit)
}

// 获取最新工具
export function getNewTools(limit: number = 6) {
  return tools.slice(-limit).reverse()
}

// 搜索工具
export function searchTools(keyword: string) {
  const lowerKeyword = keyword.toLowerCase()
  return tools.filter(tool => 
    tool.name.toLowerCase().includes(lowerKeyword) ||
    tool.description.toLowerCase().includes(lowerKeyword)
  )
}

// 会员套餐
export const membershipPlans = [
  {
    id: 'monthly',
    name: '包月会员',
    price: 19,
    period: '月',
    popular: false,
    features: [
      '所有VIP工具无限使用',
      '单文件最大 10MB',
      '每日无限次使用',
      '去除广告',
      '优先客服支持',
    ],
  },
  {
    id: 'quarterly',
    name: '包季会员',
    price: 49,
    period: '季',
    popular: false,
    features: [
      '所有VIP工具无限使用',
      '单文件最大 10MB',
      '每日无限次使用',
      '去除广告',
      '优先客服支持',
      '新功能抢先体验',
    ],
  },
  {
    id: 'yearly',
    name: '包年会员',
    price: 99,
    period: '年',
    popular: true,
    features: [
      '所有VIP工具无限使用',
      '单文件最大 50MB',
      '每日无限次使用',
      '去除广告',
      '优先客服支持',
      '新功能抢先体验',
      '5GB云存储空间',
    ],
  },
  {
    id: 'permanent',
    name: '永久会员',
    price: 299,
    period: '永久',
    popular: false,
    features: [
      '所有VIP工具无限使用',
      '单文件最大 100MB',
      '每日无限次使用',
      '去除广告',
      'VIP专属客服',
      '新功能抢先体验',
      '10GB云存储空间',
      '终身免费更新',
    ],
  },
]

// 排行榜数据
export const rankings = {
  daily: [
    { rank: 1, toolId: 'json-formatter', visits: 5234 },
    { rank: 2, toolId: 'pdf-to-word', visits: 4891 },
    { rank: 3, toolId: 'image-compress', visits: 4567 },
    { rank: 4, toolId: 'qr-code', visits: 3987 },
    { rank: 5, toolId: 'timestamp-converter', visits: 3456 },
    { rank: 6, toolId: 'md5-encrypt', visits: 3234 },
    { rank: 7, toolId: 'base-convert', visits: 2987 },
    { rank: 8, toolId: 'word-count', visits: 2765 },
    { rank: 9, toolId: 'url-codec', visits: 2543 },
    { rank: 10, toolId: 'text-diff', visits: 2345 },
  ],
  weekly: [
    { rank: 1, toolId: 'pdf-to-word', visits: 35678 },
    { rank: 2, toolId: 'json-formatter', visits: 32456 },
    { rank: 3, toolId: 'image-compress', visits: 29876 },
    { rank: 4, toolId: 'qr-code', visits: 26543 },
    { rank: 5, toolId: 'timestamp-converter', visits: 23456 },
    { rank: 6, toolId: 'md5-encrypt', visits: 21345 },
    { rank: 7, toolId: 'base-convert', visits: 19876 },
    { rank: 8, toolId: 'word-count', visits: 18765 },
    { rank: 9, toolId: 'url-codec', visits: 17654 },
    { rank: 10, toolId: 'text-diff', visits: 16543 },
  ],
  monthly: [
    { rank: 1, toolId: 'pdf-to-word', visits: 156789 },
    { rank: 2, toolId: 'image-compress', visits: 134567 },
    { rank: 3, toolId: 'json-formatter', visits: 123456 },
    { rank: 4, toolId: 'qr-code', visits: 109876 },
    { rank: 5, toolId: 'timestamp-converter', visits: 98765 },
    { rank: 6, toolId: 'md5-encrypt', visits: 87654 },
    { rank: 7, toolId: 'base-convert', visits: 76543 },
    { rank: 8, toolId: 'word-count', visits: 65432 },
    { rank: 9, toolId: 'url-codec', visits: 54321 },
    { rank: 10, toolId: 'text-diff', visits: 43210 },
  ],
}

// 用户评价
export const reviews = [
  {
    id: 1,
    userId: 1,
    userName: '张三',
    toolId: 'json-formatter',
    rating: 5,
    content: '非常好用的工具，格式化速度很快，界面也很清爽！',
    date: '2024-01-15',
    likes: 23,
  },
  {
    id: 2,
    userId: 2,
    userName: '李四',
    toolId: 'json-formatter',
    rating: 4,
    content: '功能很实用，希望能支持更多的JSON操作功能。',
    date: '2024-01-14',
    likes: 15,
  },
  {
    id: 3,
    userId: 3,
    userName: '王五',
    toolId: 'json-formatter',
    rating: 5,
    content: '作为程序员，这个工具帮我节省了很多时间，强烈推荐！',
    date: '2024-01-13',
    likes: 31,
  },
]
