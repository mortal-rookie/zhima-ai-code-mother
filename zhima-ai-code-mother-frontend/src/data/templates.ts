export type PromptTemplate = { id:string; name:string; category:string; description:string; prompt:string; art:string }
export const promptTemplates: PromptTemplate[] = [
  { id:'projects', name:'项目管理', category:'办公', description:'任务 · 协作 · 进度', art:'blue', prompt:'创建一个简洁的项目管理网站，包含项目列表、任务看板、任务详情和进度统计。支持在页面中新增、编辑和删除任务，使用浏览器本地存储保存演示数据。' },
  { id:'knowledge', name:'知识库', category:'学习', description:'文档 · 分享 · 沉淀', art:'peach', prompt:'创建一个知识库网站，包含文档目录、分类标签、搜索、文章详情和收藏功能。使用浏览器本地存储保存收藏和笔记，并提供完整示例内容。' },
  { id:'dashboard', name:'数据看板', category:'数据', description:'可视化 · 分析 · 报表', art:'violet', prompt:'创建一个数据可视化看板，包含关键指标、趋势图、分类分布和明细表格，支持日期筛选。使用明确标注的演示数据，不接入外部数据库。' },
  { id:'assistant', name:'AI 助手界面', category:'AI', description:'对话 · 历史 · 快捷指令', art:'mint', prompt:'创建一个 AI 助手的交互界面原型，包含对话窗口、历史列表和快捷指令，使用演示回复展示交互。明确标注为演示，不声称已连接真实模型接口。' },
  { id:'courses', name:'课程管理', category:'学习', description:'课程 · 学生 · 成绩', art:'blue', prompt:'创建一个课程管理网站，包含课程列表、学生管理、成绩录入和成绩统计，支持页面筛选和编辑，使用浏览器本地存储保存演示数据。' },
  { id:'portfolio', name:'个人作品集', category:'生活', description:'作品 · 简介 · 联系', art:'peach', prompt:'创建一个优雅的个人作品集网站，包含个人介绍、作品展示、作品详情和联系信息。使用简洁布局、清晰排版和响应式设计，提供可替换的示例内容。' },
  { id:'expenses', name:'个人记账', category:'生活', description:'收支 · 分类 · 趋势', art:'mint', prompt:'创建一个个人记账网站，支持录入收支、按分类筛选、月度统计和趋势展示。使用浏览器本地存储保存数据，提供少量标注清晰的演示记录。' },
  { id:'blog', name:'轻量博客', category:'生活', description:'文章 · 标签 · 阅读', art:'violet', prompt:'创建一个轻量博客网站，包含文章列表、详情页、分类标签和关键词搜索。提供示例文章，采用舒适的阅读排版，支持手机访问。' },
]
