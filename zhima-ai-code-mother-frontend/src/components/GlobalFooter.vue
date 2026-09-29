<template>
  <a-layout-footer class="footer">
    <div class="footer-inner">
      <!-- 左侧：链接行 + 合规信息 -->
      <div class="footer-main">
        <!-- 第一行：协议链接（对应参考站「用户协议 | 隐私协议 | …」那一行） -->
        <nav class="footer-links">
          <RouterLink to="/user/agreement">用户协议</RouterLink>
          <span class="sep">|</span>
          <RouterLink to="/user/privacy">隐私协议</RouterLink>
          <template v-if="CONTACT_EMAIL">
            <span class="sep">|</span>
            <a :href="`mailto:${CONTACT_EMAIL}`">联系客服</a>
          </template>
        </nav>

        <!-- ICP 备案号：占位值见 src/config/site.ts -->
        <p class="footer-line">
          ICP备案号：
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">{{ ICP_BEIAN }}</a>
        </p>

        <p class="footer-line">
          本站为 AI 代码生成工具，生成结果不代表本站观点，请勿用于任何违法违规用途。
        </p>
        <p class="footer-line">
          本站内容归权利人所有，未经权利人许可，不得转载或复制；权利人决议不公开展示的，请通知本站删除。
        </p>

        <p class="footer-line">抵制不良信息，拒绝盗版侵权，注意自我保护，谨防受骗上当，合理安排时间，享受健康生活。</p>

        <!-- 公安网安备案：占位值见 src/config/site.ts -->
        <p class="footer-line police">
          <span class="police-badge">警</span>
          <a :href="POLICE_QUERY_URL" target="_blank" rel="noopener noreferrer">
            公安备案号：{{ POLICE_BEIAN }}
          </a>
        </p>
      </div>

      <!-- 右侧：品牌（对应参考站右侧的 logo 位置） -->
      <div class="footer-side">
        <div class="brand">
          <img class="footer-logo" src="@/assets/logo.png" alt="Logo" />
          <span class="brand-name">{{ SITE_SHORT_NAME }}</span>
        </div>
      </div>
    </div>

    <p class="copyright">© {{ new Date().getFullYear() }} {{ SITE_NAME }}</p>
  </a-layout-footer>
</template>

<script setup lang="ts">
import {
  CONTACT_EMAIL,
  ICP_BEIAN,
  POLICE_BEIAN,
  POLICE_QUERY_URL,
  SITE_NAME,
  SITE_SHORT_NAME,
} from '@/config/site.ts'
</script>

<style scoped>
/* 白底信息栏：文字色整套按浅色底重新配过，
   原来给黑底用的半透明白字/亮黄在白底上会看不见，别直接搬 */
.footer {
  background: #ffffff;
  text-align: left;
  padding: 0;
  margin-top: 0;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.footer-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 40px 24px 28px;
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 40px;
}

/* ---------- 左侧 ---------- */
.footer-main {
  min-width: 0;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 18px;
  font-size: 15px;
}

.footer-links a {
  color: #333333;
  transition: color 0.2s ease;
}

.footer-links a:hover {
  color: #c96a00;
}

.footer-links .sep {
  margin: 0 12px;
  color: #dcdcdc;
}

.footer-line {
  margin: 0 0 6px;
  color: #999999;
  font-size: 13px;
  line-height: 22px;
  word-break: break-all;
}

.footer-line a {
  color: #999999;
  transition: color 0.2s ease;
}

.footer-line a:hover {
  color: #c96a00;
}

/* 公安备案行：蓝色警徽 + 可点击的备案号 */
.footer-line.police {
  display: flex;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 0;
}

.police-badge {
  flex: none;
  width: 16px;
  height: 16px;
  margin-right: 6px;
  border-radius: 50%;
  background: linear-gradient(160deg, #1f5fd0 0%, #123c8a 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #fff;
  font-size: 10px;
  line-height: 14px;
  text-align: center;
}

/* ---------- 右侧 ---------- */
.footer-side {
  flex: none;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* logo.png 本身是黑底不透明的方图（没有 alpha 通道），
   在黑底页脚里看不出来，放到白底上就是一块黑方块，
   这里给它加圆角，做成「应用图标」那种观感，看着是有意为之 */
.footer-logo {
  width: 34px;
  height: 34px;
  object-fit: contain;
  border-radius: 8px;
}

/* 白底上亮黄 #ffdb18 对比度太低，改用深一档的金色 */
.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #e8a600;
}

.copyright {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 24px 22px;
  color: #b0b0b0;
  font-size: 12px;
}

@media (max-width: 768px) {
  .footer-inner {
    flex-direction: column;
    gap: 24px;
    padding: 28px 18px 20px;
  }

  .footer-side {
    flex-direction: row;
    align-items: center;
    width: 100%;
  }
}
</style>
