/**
 * 环境变量配置
 */
import {CodeGenTypeEnum} from "@/utils/codeGenTypes.ts";

// 应用部署域名
export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 静态资源地址
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

// 获取部署应用的完整URL
export const getDeployUrl = (deployKey: string) => {
  // 结尾这个斜杠绝对不能少！
  // 部署产物里的资源引用是相对路径（vite 配了 base: './'，即 ./assets/xxx.js），
  // 浏览器解析相对路径时以「当前 URL 所在目录」为基准：
  //   少了斜杠 → /api/deploy/reL460 被当成文件，基准变成 /api/deploy/ → 资源全 404
  //   有斜杠   → /api/deploy/reL460/ 被当成目录，基准是 /api/deploy/reL460/ → 正常
  // 现象就是 index.html 能打开，但页面一片空白（Vue 项目的 JS/CSS 全 404）。
  // 顺手去掉域名结尾可能多出来的斜杠，避免拼出 //。
  const base = DEPLOY_DOMAIN.replace(/\/+$/, '')
  return `${base}/${deployKey}/`
}

// 获取静态资源预览URL
export const getStaticPreviewUrl = (codeGenType: string, appId: string) => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
  // 如果是 Vue 项目，浏览地址需要添加 dist 后缀
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}
