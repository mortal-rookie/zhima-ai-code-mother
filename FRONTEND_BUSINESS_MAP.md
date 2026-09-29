# 前端重设计：业务与接口对照

核对依据：本仓库 Controller、Service 实现、DTO、常量、SSE 处理和现有前端调用。属于源码核对；尚未启动后端，也未验证数据库、Redis、模型服务、COS 与部署域名。

| 业务 | 接口（相对 /api） | 新版前端必须遵守的规则 |
| --- | --- | --- |
| 注册 | POST /user/register | 账号至少 4 位；密码至少 8 位；重复密码一致。注册限流配置为同 IP 259200 秒内 1 次。 |
| 登录、退出、会话 | POST /user/login、POST /user/logout、GET /user/get/login | Session/Cookie；普通请求及 SSE 都携带凭证。登录失效后保留原页面与需求草稿。 |
| 个人资料 | POST /user/update/my | 可修改昵称、头像地址、简介；账号不在此接口的可修改字段中。 |
| 头像上传 | POST /user/avatar/upload | multipart 字段 file；最多 5 MB；jpg/jpeg/png/webp/gif。上传仅返回地址，保存资料才写数据库。 |
| 创建应用 | POST /app/add | DTO 只有 initPrompt。AI 生成名称并自动选择 html、multi_file、vue_project；创建本身不等于已生成代码。 |
| 生成、继续修改 | GET /app/chat/gen/code | 仅作者可用；SSE 默认事件为文本增量，done 结束，business-error 返回 JSON 错误。关闭异常连接，避免自动重连重复生成。 |
| 额度 | 生成服务内部校验 | 普通用户每天 2 次，每次继续对话也扣次数；生成前扣额度，当前实现没有失败退款；管理员免额度，但仍受接口限流。日期按服务端 LocalDate.now()。 |
| 生成限流 | 生成接口注解 | 每用户 10 秒内 1 次。 |
| 应用详情 | GET /app/get/vo | 作者、管理员、已部署公开应用可以查看。公开访客不返回 initPrompt；管理员也不能代作者生成、部署或下载。 |
| 我的应用 | POST /app/my/list/page/vo | 后端锁定当前用户 ID；每页最多 20 条；支持名称、生成类型等筛选。 |
| 精选案例 | POST /app/good/list/page/vo | priority=99；每页最多 20 条。不可将精选自动等同于可复制模板或公开对话历史。 |
| 重命名 | POST /app/update | 仅作者；只更新应用名和编辑时间。 |
| 删除 | POST /app/delete | 作者或管理员；应用实体是逻辑删除。当前 Controller 未调用 deleteByAppId，不应宣称同步永久清除全部历史与磁盘文件。 |
| 对话历史 | GET /chatHistory/app/{appId} | 必须登录，作者或管理员；pageSize 1–50；lastCreateTime 为时间游标；前端反转为时间正序展示。 |
| 预览 | /static/{type}_{id}/，Vue 加 dist/index.html | Vue 在 SSE 完成后异步构建；done 不代表 dist 已就绪，保留刷新与构建等待提示。 |
| 可视化修改 | 前端 iframe 选中元素 + 原生成接口 | 将选中元素描述写入提示词，由 AI 修改；没有独立的直接保存页面接口。 |
| 部署 | POST /app/deploy | 仅作者；Vue 先构建；生成或复用 deployKey；复制到部署目录；返回带结尾斜杠的 URL；异步截图更新封面。 |
| 下载代码 | GET /app/download/{appId} | 仅作者；要求生成目录存在；ZIP 排除 node_modules、dist、.git、.env 等。不能将 JSON 业务错误当 ZIP 下载。 |
| 管理员 | /user/add、get、delete、update、list/page/vo；/app/admin/*；/chatHistory/admin/list/page/vo | 保留用户管理、应用管理、精选设置、对话管理入口；普通用户隐藏入口，后端再次鉴权。 |

## 参考图的能力边界

- 模板页可以实现为本地「需求模板」，点击后将真实提示词填入新建页，用户可修改后生成。没有持久化模板、克隆应用接口。
- 风格选择可以追加到 initPrompt，不应伪造独立 style/codeGenType 请求参数。
- 未发现设计文件导入、外部数据库连接、订阅支付和独立资源库 CRUD 接口。不将参考图中这些文案实现为虚假操作。
- 资源库可承载使用指南和现有开源仓库链接。

## 视觉方案

以用户提供的四屏参考图为准。暖白 #F8F7F5、墨蓝 #192938、雾蓝 #DBE9F2、交互蓝 #426BE8、暖桃 #F7E7D7、次级文字 #75808D；衬线中文标题与无衬线正文；左侧导航、左对齐主内容、柔和玻璃面板、雾蓝曲线背景。首页输入框与新建页共用生成逻辑。

布局：`品牌页：顶部导航 → 左侧标题/右侧玻璃构图 → 开始创作`；`工作台：左侧导航 | 问候标题 → 需求输入 → 快捷模板 → 真实应用列表`；`模板：左侧导航 | 标题/搜索 → 分类 → 模板卡片`；`新建：左侧导航 | 说明/玻璃构图 → 需求输入 → 风格选择`。

复核：保留参考图指定的暖白、衬线与圆角；用蓝色而非其他默认强调色；去掉原站黑黄主题与英文巨大标题；卡片只用于模板、应用和输入这三类实际交互对象。
