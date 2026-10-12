<template>
  <div class="animated-mesh-bg" aria-hidden="true">
    <div class="mesh-gradient" />
    <canvas ref="canvasRef" class="mesh-canvas" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  /** 粒子/光斑数量 */
  blobCount: { type: Number, default: 7 }
})

const canvasRef = ref(null)
let rafId = 0
let blobs = []

function isDarkTheme() {
  const app = document.getElementById('app')
  return app?.classList.contains('dark-theme')
    || document.documentElement.classList.contains('dark-theme')
}

function createBlobs(width, height) {
  const dark = isDarkTheme()
  const palette = dark
    ? [
        [96, 165, 250, 0.35],
        [167, 139, 250, 0.3],
        [244, 114, 182, 0.28],
        [45, 212, 191, 0.22]
      ]
    : [
        [160, 196, 255, 0.55],
        [255, 200, 221, 0.5],
        [196, 181, 253, 0.45],
        [147, 197, 253, 0.4]
      ]

  blobs = []
  for (let i = 0; i < props.blobCount; i += 1) {
    const c = palette[i % palette.length]
    const radius = Math.min(width, height) * (0.22 + Math.random() * 0.18)
    blobs.push({
      x: Math.random() * width,
      y: Math.random() * height,
      radius,
      vx: (Math.random() - 0.5) * 0.35,
      vy: (Math.random() - 0.5) * 0.35,
      color: c
    })
  }
}

function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  const parent = canvas.parentElement
  const w = parent?.clientWidth || window.innerWidth
  const h = parent?.clientHeight || window.innerHeight
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.width = `${w}px`
  canvas.style.height = `${h}px`
  const ctx = canvas.getContext('2d')
  if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  if (!blobs.length) createBlobs(w, h)
}

function tick() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const w = canvas.clientWidth
  const h = canvas.clientHeight
  ctx.clearRect(0, 0, w, h)

  for (const b of blobs) {
    b.x += b.vx
    b.y += b.vy
    if (b.x < -b.radius) b.x = w + b.radius
    if (b.x > w + b.radius) b.x = -b.radius
    if (b.y < -b.radius) b.y = h + b.radius
    if (b.y > h + b.radius) b.y = -b.radius

    const [r, g, bl, a] = b.color
    const grad = ctx.createRadialGradient(b.x, b.y, 0, b.x, b.y, b.radius)
    grad.addColorStop(0, `rgba(${r},${g},${bl},${a})`)
    grad.addColorStop(1, `rgba(${r},${g},${bl},0)`)
    ctx.fillStyle = grad
    ctx.beginPath()
    ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2)
    ctx.fill()
  }

  rafId = requestAnimationFrame(tick)
}

function start() {
  resizeCanvas()
  cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(tick)
}

function onResize() {
  blobs = []
  resizeCanvas()
}

onMounted(() => {
  start()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  cancelAnimationFrame(rafId)
})
</script>

<style scoped>
.animated-mesh-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.mesh-gradient {
  position: absolute;
  inset: -20%;
  background: linear-gradient(
    120deg,
    #e8f0fe 0%,
    #fce7f3 35%,
    #ede9fe 65%,
    #dbeafe 100%
  );
  background-size: 240% 240%;
  animation: meshGradientShift 18s ease-in-out infinite;
}

:global(#app.dark-theme) .mesh-gradient {
  background: linear-gradient(
    120deg,
    #0f172a 0%,
    #1e1b4b 35%,
    #312e81 65%,
    #1e3a5f 100%
  );
}

.mesh-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

@keyframes meshGradientShift {
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
