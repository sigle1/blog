<template>
  <div v-if="siteIcp.text || sitePsb.text" class="site-beian-links" :class="variant">
    <a
      v-if="siteIcp.text" class="beian-link"
      :href="siteIcp.href"
      target="_blank"
      rel="noopener noreferrer"
    >{{ siteIcp.text }}</a>
    <span v-if="siteIcp.text && sitePsb.text" class="beian-sep" aria-hidden="true">|</span>
    <a
      v-if="sitePsb.text" class="beian-link psb-link"
      :href="sitePsb.href"
      target="_blank"
      rel="noopener noreferrer"
    >
      <img
        class="psb-icon"
        src="https://www.beian.gov.cn/img/logo.png"
        alt=""
        width="16"
        height="16"
      />
      {{ sitePsb.text }}
    </a>
  </div>
</template>

<script setup>
import { siteIcp, sitePsb } from '@/config/siteBeian'

defineProps({
  /** default | login | admin */
  variant: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.site-beian-links {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px 10px;
  font-size: 13px;
}

.beian-link {
  color: #666;
  text-decoration: none;
  transition: color 0.2s ease;
}

.beian-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.beian-sep {
  color: #cbd5e1;
  user-select: none;
}

.psb-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.psb-icon {
  display: block;
  flex-shrink: 0;
}

/* 登录页（深色背景） */
.site-beian-links.login .beian-link {
  color: rgba(255, 255, 255, 0.88);
}

.site-beian-links.login .beian-link:hover {
  color: #fff;
}

.site-beian-links.login .beian-sep {
  color: rgba(255, 255, 255, 0.45);
}

/* 管理端 */
.site-beian-links.admin .beian-link {
  color: #909399;
}

.site-beian-links.admin .beian-link:hover {
  color: #409eff;
}
</style>

<style>
.dark-theme .site-beian-links.default .beian-link {
  color: #9ca3af;
}

.dark-theme .site-beian-links.default .beian-link:hover {
  color: #93c5fd;
}

.dark-theme .site-beian-links.default .beian-sep {
  color: #475569;
}
</style>
