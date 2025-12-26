import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 文件解析API
export const parseApi = {
  // 解析文本内容
  parseContent: (content, fileName) => {
    return api.post('/parse/content', { content, fileName })
  },
  // 获取解析结果
  getParseResult: (fileId) => {
    return api.get(`/parse/${fileId}`)
  }
}

// 规则API
export const ruleApi = {
  // 获取规则列表
  list: (status) => {
    return api.get('/rules', { params: { status } })
  },
  // 创建规则
  create: (rule) => {
    return api.post('/rules', rule)
  },
  // 更新规则
  update: (id, rule) => {
    return api.put(`/rules/${id}`, rule)
  },
  // 删除规则
  delete: (id) => {
    return api.delete(`/rules/${id}`)
  },
  // 发布规则
  publish: (id) => {
    return api.post(`/rules/${id}/publish`)
  },
  // 执行规则检查
  execute: (ruleIds, content, fileName) => {
    return api.post('/rules/execute', { ruleIds, content, fileName })
  }
}

// 查询API
export const queryApi = {
  // 执行查询
  execute: (dsl, fileIds) => {
    return api.post('/query', { dsl, fileIds })
  }
}

// 比对API
export const diffApi = {
  // 执行比对
  compare: (leftContent, rightContent, options) => {
    return api.post('/diff', { leftContent, rightContent, options })
  }
}

export default api
