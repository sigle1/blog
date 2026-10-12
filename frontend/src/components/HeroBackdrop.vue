<template>
  <div class="hero-backdrop-slideshow">
    <div
      v-for="(item, index) in items"
      :key="`${variant}-${index}`"
      class="slide-wrap"
    >
      <video
        v-if="isVideoBannerUrl(item)"
        :ref="(el) => bindVideoRef(index, el)"
        class="slide-media"
        :class="[
          variant === 'intro' ? 'slide-media--intro' : 'slide-media--banner',
          { 'slide-media--active': activeIndex === index }
        ]"
        :src="item"
        muted
        loop
        playsinline
        preload="metadata"
      />
      <img
        v-else
        :src="item"
        alt=""
        class="slide-media"
        :class="[
          variant === 'intro' ? 'slide-media--intro' : 'slide-media--banner',
          { 'slide-media--active': activeIndex === index }
        ]"
      >
    </div>
  </div>
</template>

<script setup>
import { watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { isVideoBannerUrl } from '@/utils/image'

const props = defineProps({
  items: { type: Array, default: () => [] },
  activeIndex: { type: Number, default: 0 },
  /** intro：首次引导全屏；banner：首页顶栏大图 */
  variant: { type: String, default: 'banner' }
})

const videoRefs = new Map()

function bindVideoRef(index, el) {
  if (el) videoRefs.set(index, el)
  else videoRefs.delete(index)
}

async function syncPlayback() {
  await nextTick()
  for (const [i, el] of videoRefs) {
    if (i === props.activeIndex) {
      try {
        await el.play()
      } catch {
        /* 若浏览器仍拦截自动播放，静音循环通常也可通过用户交互后恢复 */
      }
    } else {
      el.pause()
    }
  }
}

watch(
  () => [props.activeIndex, props.items],
  () => syncPlayback(),
  { deep: true }
)

onMounted(() => syncPlayback())

onBeforeUnmount(() => {
  for (const el of videoRefs.values()) {
    try {
      el.pause()
    } catch {
      /* ignore */
    }
  }
  videoRefs.clear()
})
</script>

<style scoped>
.hero-backdrop-slideshow {
  width: 100%;
  height: 100%;
}

.slide-wrap {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.slide-media {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.slide-media--active {
  opacity: 1;
}

.slide-media--intro {
  transform: scale(1.06);
  animation: introZoom 16s ease-in-out infinite;
}

.slide-media--banner {
  animation: flow 20s ease-in-out infinite;
}

@keyframes introZoom {
  0% {
    transform: scale(1.06);
  }
  50% {
    transform: scale(1.12);
  }
  100% {
    transform: scale(1.06);
  }
}

@keyframes flow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
