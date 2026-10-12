import { ref } from 'vue'

// This is a simple in-memory store to simulate a database.
// In a real application, this would be replaced by API calls to a backend.
export const notes = ref([
  {
    id: 1,
    title: '关于Vue 3学习的第一天',
    content: '今天开始学习Vue 3，组合式API感觉非常强大，特别是 `setup` 语法糖，让代码更简洁了。',
    createdAt: '2026-03-29'
  },
  {
    id: 2,
    title: 'CSS Glassmorphism 效果小记',
    content: '通过 `background`, `backdrop-filter`, 和 `border` 属性的结合，可以轻松创建出漂亮的毛玻璃效果。关键在于控制好透明度和模糊半径。',
    createdAt: '2026-03-30'
  }
]);

export function addNote(newNote) {
  notes.value.unshift({
    id: Date.now(),
    ...newNote,
    createdAt: new Date().toISOString().split('T')[0]
  });
}
