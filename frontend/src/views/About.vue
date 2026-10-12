<template>
  <div class="about-page">
    <div class="about-inner">
      <h1 class="about-title">关于本站</h1>

      <section v-loading="introLoading" class="about-card about-intro-card">
        <h2>自我介绍</h2>
        <p v-if="introLoadFailed" class="about-intro-hint">
          无法从服务器加载简介（请检查是否已部署后端、Nginx 是否转发 <code>/api</code>）。下方为本地默认文案。
        </p>
        <p class="about-intro">{{ introText }}</p>
      </section>

      <p class="about-lead">
        个人博客站点，前后端分离部署；以下为当前使用的主要技术，便于交流与备忘。
      </p>

      <section class="about-card">
        <h2>前端</h2>
        <ul>
          <li><strong>Vue 3</strong>（组合式 API）</li>
          <li><strong>Vite</strong> 构建与开发服务</li>
          <li><strong>Vue Router</strong> 路由</li>
          <li><strong>Element Plus</strong> 组件库</li>
          <li><strong>Pinia</strong> 状态管理（如音乐播放列表）</li>
          <li><strong>Axios</strong> 请求后端 REST 接口</li>
        </ul>
      </section>

      <section class="about-card">
        <h2>后端</h2>
        <ul>
          <li><strong>Spring Boot</strong>（Java）</li>
          <li><strong>MyBatis</strong> 与 XML Mapper 访问数据库</li>
          <li><strong>MySQL</strong> 持久化</li>
          <li><strong>JWT</strong> 等用于管理端鉴权（按项目配置）</li>
        </ul>
      </section>

      <section class="about-card">
        <h2>部署与运维</h2>
        <ul>
          <li><strong>Nginx</strong> 提供静态资源与反向代理（如 <code>/api</code> 转发至后端）</li>
          <li>服务器上可使用 <strong>宝塔</strong> 等面板管理站点与 Java 进程</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

// 便于排查：若打开关于页后控制台仍无此条，说明浏览器运行的不是当前构建，或 Vue 未挂载（如 Nginx 未回退 index.html）
console.warn('[关于页] 组件已创建，将请求 /api/about/content')

const introLoading = ref(true)
const introText = ref('')
const introLoadFailed = ref(false)

const FALLBACK_INTRO =
  '你好，欢迎来访我的博客。\n\n' +
  '这里是我的个人站点，我会记录技术笔记、生活随笔与爱好相关的内容。你可以在后台「关于页」随时修改这段自我介绍。\n\n' +
  '若你愿意交流，欢迎留言。感谢你的阅读。'

onMounted(async () => {
  introLoading.value = true
  introLoadFailed.value = false
  try {
    const res = await api.getAboutContent()
    const t = res.data && res.data.intro
    introText.value = typeof t === 'string' && t.length > 0 ? t : FALLBACK_INTRO
  } catch (e) {
    console.error('[关于页] 加载 /api/about/content 失败', e)
    introLoadFailed.value = true
    introText.value = FALLBACK_INTRO
  } finally {
    introLoading.value = false
  }
})
</script>

<style scoped>
.about-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 100px);
  margin: -24px -20px 0;
  padding: 32px 20px 56px;
  overflow: hidden;
}

.about-page::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 100% 70% at 50% -20%, rgba(129, 140, 248, 0.2), transparent 55%),
    radial-gradient(ellipse 50% 45% at 100% 80%, rgba(125, 211, 252, 0.18), transparent 50%),
    linear-gradient(165deg, #f8fafc 0%, #eef2ff 40%, #f0fdfa 100%);
}

.about-inner {
  position: relative;
  z-index: 1;
  max-width: 640px;
  margin: 0 auto;
}

.about-title {
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 12px;
  letter-spacing: -0.02em;
}

.about-lead {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.65;
  color: #475569;
}

.about-card {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 16px;
  padding: 20px 22px 22px;
  margin-bottom: 16px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}

.about-intro-card {
  margin-bottom: 20px;
}

.about-card h2 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #3730a3;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.about-intro-hint {
  margin: 0 0 12px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.55;
  color: #b45309;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 10px;
}

.about-intro-hint code {
  font-size: 0.92em;
  padding: 1px 4px;
  background: rgba(255, 255, 255, 0.8);
}

.about-intro {
  margin: 0;
  color: #334155;
  line-height: 1.75;
  font-size: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.about-card ul {
  margin: 0;
  padding-left: 1.25rem;
  color: #334155;
  line-height: 1.75;
  font-size: 15px;
}

.about-card li {
  margin-bottom: 6px;
}

.about-card code {
  font-size: 0.88em;
  padding: 2px 6px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #0f172a;
}
</style>

<style>
#app.dark-theme .about-page::before {
  background:
    radial-gradient(ellipse 100% 55% at 50% -10%, rgba(99, 102, 241, 0.22), transparent 55%),
    linear-gradient(165deg, #0f1419 0%, #151b2e 50%, #0c1220 100%);
}

#app.dark-theme .about-title {
  color: #f1f5f9;
}

#app.dark-theme .about-lead {
  color: #94a3b8;
}

#app.dark-theme .about-card {
  background: rgba(30, 41, 59, 0.88);
  border-color: rgba(71, 85, 105, 0.45);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

#app.dark-theme .about-card h2 {
  color: #a5b4fc;
  border-bottom-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .about-intro-hint {
  color: #fcd34d;
  background: rgba(120, 53, 15, 0.35);
  border-color: rgba(245, 158, 11, 0.45);
}

#app.dark-theme .about-intro-hint code {
  background: rgba(15, 23, 42, 0.6);
  color: #fde68a;
}

#app.dark-theme .about-intro {
  color: #cbd5e1;
}

#app.dark-theme .about-card ul {
  color: #cbd5e1;
}

#app.dark-theme .about-card code {
  background: rgba(15, 23, 42, 0.9);
  color: #e2e8f0;
}
</style>
