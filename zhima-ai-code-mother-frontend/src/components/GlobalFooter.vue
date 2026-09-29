<script setup lang="ts">
import { SITE_NAME, ICP_BEIAN, POLICE_BEIAN, POLICE_QUERY_URL, CONTACT_EMAIL } from '@/config/site'
</script>
<template>
  <footer class="footer">
    <span>© {{ new Date().getFullYear() }} {{ SITE_NAME }}</span>
    <nav aria-label="法律与联系">
      <RouterLink to="/user/agreement">用户协议</RouterLink
      ><RouterLink to="/user/privacy">隐私政策</RouterLink
      ><a v-if="CONTACT_EMAIL" :href="`mailto:${CONTACT_EMAIL}`">联系我们</a
      ><a
        v-if="ICP_BEIAN"
        href="https://beian.miit.gov.cn/"
        target="_blank"
        rel="noopener noreferrer"
        >{{ ICP_BEIAN }}</a
      ><a v-if="POLICE_BEIAN" :href="POLICE_QUERY_URL" target="_blank" rel="noopener noreferrer">{{
        POLICE_BEIAN
      }}</a>
    </nav>
  </footer>
</template>
<style scoped>
.footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 40px;
  font-size: 11px;
  color: #86909b;
  border-top: 1px solid var(--line);
}
nav {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
}
a {
  color: #77828e;
}
@media (max-width: 640px) {
  .footer {
    padding: 20px;
  }
}
</style>
