/**
 * 「每日免费生成次数」的前端展示逻辑
 *
 * 后端 LoginUserVO 只返回两个原始字段：
 *   - dailyGenUsed：库里存的已用次数（**跨天不会自动归零**）
 *   - genResetData：这个数字对应的日期，格式 YYYY-MM-DD
 * 「今天到底用了几次」需要拿 genResetData 跟今天比一次才能得出，
 * 这段判断统一放在这里，避免每个页面各写一套。
 * 真正的拦截在后端 UserServiceImpl.consumeDailyGenQuota 里，这里只负责显示。
 */

/**
 * 每日免费生成次数。
 * ⚠️ 必须和后端 UserConstant.DEFAULT_DAILY_GEN_QUOTA 保持一致。
 * 因为后端没把这个值放进 VO，只能在两端各写一份 —— 但就算这里写错了，
 * 也只是显示不准，不会导致用户超额生成（拦截在后端）。
 */
export const DAILY_FREE_GEN_QUOTA = 2

/**
 * 取「今天」的 YYYY-MM-DD 字符串。
 *
 * 这里不能用 new Date().toISOString().slice(0, 10)：
 * toISOString 返回的是 **UTC** 日期，东八区用户在早上 8 点之前会被算成昨天，
 * 导致跨天判断整整错一天。
 * 后端用的是 LocalDate.now()（服务器本地日期），
 * 浏览器与服务器时区一致时两边结果相同；不一致时以「大概哪一天」为准，
 * 因为跨天重置的最终权威仍然是后端的判断。
 */
export const todayDateStr = (): string => {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

/**
 * 今天已经生成过几次。
 * genResetData 不是今天（或者是空的）时一律返回 0 —— 与后端
 * `!Objects.equals(genResetData, today)` 就当作 0 次的口径保持一致。
 */
export const resolveTodayGenUsed = (user: {
  dailyGenUsed?: number | null
  genResetData?: string | null
}): number => {
  if (!user.genResetData || user.genResetData !== todayDateStr()) {
    return 0
  }
  return user.dailyGenUsed ?? 0
}
