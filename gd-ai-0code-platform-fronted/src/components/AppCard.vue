<template>
  <div class="app-card" @click="handleClick">
    <div class="app-cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="cover-placeholder" :class="{ 'featured-placeholder': featured }">
        <StarOutlined v-if="featured" />
        <AppstoreOutlined v-else />
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :size="40" :src="userAvatar">
          {{ userInitial }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-name">{{ app.appName || '未命名应用' }}</h3>
        <span class="author-name">{{ userName }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { AppstoreOutlined, StarOutlined } from '@ant-design/icons-vue'

interface Props {
  /** 应用数据 */
  app: API.AppVO
  /** 是否是精选卡片样式 */
  featured?: boolean
  /** 自定义用户头像（可选，默认从 app.user 获取） */
  customUserAvatar?: string
  /** 自定义用户名（可选，默认从 app.user 获取） */
  customUserName?: string
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
  customUserAvatar: '',
  customUserName: '',
})

const emit = defineEmits<{
  (e: 'click', appId: number | undefined): void
}>()

// 计算用户头像
const userAvatar = computed(() => {
  return props.customUserAvatar || props.app.user?.userAvatar || ''
})

// 计算用户名首字母
const userInitial = computed(() => {
  const name = props.customUserName || props.app.user?.userName || ''
  return name.charAt(0) || 'U'
})

// 计算用户名
const userName = computed(() => {
  if (props.customUserName) return props.customUserName
  return props.app.user?.userName || '匿名用户'
})

// 点击处理
const handleClick = () => {
  emit('click', props.app.id)
}
</script>

<style scoped>
.app-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.app-cover {
  height: 160px;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.8);
}

.featured-placeholder {
  color: rgba(255, 255, 255, 0.9);
}

.app-info {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-name {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-name {
  font-size: 13px;
  color: #999;
}
</style>
