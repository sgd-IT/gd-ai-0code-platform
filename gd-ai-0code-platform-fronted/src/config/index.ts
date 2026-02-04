/**
 * 应用配置
 * 从环境变量读取，支持通过 .env 文件配置
 */

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 应用预览地址（生成后的预览）
export const APP_PREVIEW_URL = import.meta.env.VITE_APP_PREVIEW_URL || 'http://localhost:8123/api/static'

// 应用部署地址（部署后的访问地址）
export const APP_DEPLOY_URL = import.meta.env.VITE_APP_DEPLOY_URL || 'http://localhost:8123/api/static/deploy'
