<template>
  <div class="admin-home">
    <el-card shadow="hover" class="home-card">
        <template #header>
          <div class="card-header">
            <span>系统概览</span>
          </div>
        </template>
        <div class="home-stats">
          <el-statistic :value="128" title="总文章数">
            <template #suffix>
              <el-icon><arrow-up /></el-icon>
              <span class="text-green">+12.5%</span>
            </template>
          </el-statistic>
          <el-statistic :value="64" title="总笔记数">
            <template #suffix>
              <el-icon><arrow-up /></el-icon>
              <span class="text-green">+8.2%</span>
            </template>
          </el-statistic>
          <el-statistic :value="32" title="总爱好数">
            <template #suffix>
              <el-icon><arrow-up /></el-icon>
              <span class="text-green">+5.7%</span>
            </template>
          </el-statistic>
          <el-statistic :value="1024" title="总访问量">
            <template #suffix>
              <el-icon><arrow-up /></el-icon>
              <span class="text-green">+15.3%</span>
            </template>
          </el-statistic>
        </div>
      </el-card>
      
      <div class="home-content">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span>最近动态</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in timelineData"
              :key="index"
              :timestamp="item.time"
              :type="item.type"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
        
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行环境">Development</el-descriptions-item>
            <el-descriptions-item label="最后更新">2026-04-02</el-descriptions-item>
            <el-descriptions-item label="服务器状态">运行中</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </div>
</template>

<script>
import { ref } from 'vue'
import { ElCard, ElStatistic, ElTimeline, ElTimelineItem, ElDescriptions, ElDescriptionsItem, ElIcon } from 'element-plus'
import { ArrowUp } from '@element-plus/icons-vue'

export default {
  name: 'AdminHome',
  components: {
    ElCard,
    ElStatistic,
    ElTimeline,
    ElTimelineItem,
    ElDescriptions,
    ElDescriptionsItem,
    ElIcon,
    ArrowUp
  },
  setup() {
    const timelineData = ref([
      {
        time: '2026-04-02 14:30',
        content: '添加了新的爱好分类',
        type: 'primary'
      },
      {
        time: '2026-04-02 13:15',
        content: '发布了新的学习笔记',
        type: 'success'
      },
      {
        time: '2026-04-02 10:45',
        content: '更新了系统配置',
        type: 'warning'
      },
      {
        time: '2026-04-01 16:20',
        content: '添加了新的文章',
        type: 'info'
      }
    ])
    
    return {
      timelineData
    }
  }
}
</script>

<style scoped>
.admin-home {
  width: 100%;
}

.home-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.home-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.content-card {
  margin-bottom: 24px;
}

.text-green {
  color: #67C23A;
  font-size: 14px;
  margin-left: 4px;
}

.el-timeline-item {
  padding-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-stats {
    grid-template-columns: 1fr;
  }
  
  .home-content {
    grid-template-columns: 1fr;
  }
}
</style>