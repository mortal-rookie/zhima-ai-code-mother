/**
 * 站点合规信息（页脚展示用）
 *
 * ============ 备案号下来之后，只改这个文件 ============
 * 下面所有带 TODO 的常量都是占位值，拿到真实备案信息后直接替换字符串即可，
 * 页脚、注册页、登录页都会自动跟着变，不用去动组件。
 * ===================================================
 */

// 站点全称（页脚版权行使用）
export const SITE_NAME = '峙码 AI 零代码应用生成平台'

// 站点简称（页脚右侧的品牌字）
export const SITE_SHORT_NAME = '峙码'

/**
 * 以下两项按需求已从页脚移除，先留着备用
 * （ICP 备案的主体名称、证件地址在备案后台还要填，需要重新上页脚时直接引用即可）
 */
export const DEVELOPER_NAME = '峙码 AI 零代码应用生成平台'
export const COMPANY_ADDRESS = '待补充'

/**
 * TODO 待备案：ICP 备案号
 * 真实格式类似「沪ICP备2026XXXXXX号-1」，现在还没备案，先按格式占位，
 * 保持和参考站一致的位置与字号，拿到号以后把这一行换掉就行。
 */
export const ICP_BEIAN = ''

/**
 * TODO 待备案：公安网安备案号
 * 真实格式类似「沪公网安备3101140200XXXX号」，
 * 公安备案通过后会同时给一个 15 位编号（POLICE_BEIAN_CODE），
 * 用于拼接官方查询链接 https://beian.mps.gov.cn/#/query/webSearch?code=xxx
 */
export const POLICE_BEIAN = ''
export const POLICE_BEIAN_CODE = '000000000000000'
export const POLICE_QUERY_URL = `https://beian.mps.gov.cn/#/query/webSearch?code=${POLICE_BEIAN_CODE}`

// TODO 待补充：对外联系邮箱（写成真实邮箱后页脚会自动变成可点击的 mailto 链接）
export const CONTACT_EMAIL = ''
