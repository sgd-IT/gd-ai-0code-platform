<template>
  <a-modal
    v-model:open="visible"
    :title="app?.appName || '应用详情'"
    :footer="null"
    centered
    width="600px"
    @cancel="handleClose"
  >
    <div v-if="app" class="app-detail">
      <!-- 封面预览 -->
      <div class="detail-cover">
        <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
        <div v-else class="cover-placeholder">
          <AppstoreOutlined />
        </div>
      </div>

      <!-- 应用信息 -->
      <div class="detail-info">
        <div class="info-row">
          <span class="label">应用名称</span>
          <span class="value">{{ app.appName || '未命名应用' }}</span>
        </div>

        <div v-if="app.user" class="info-row">
          <span class="label">创建者</span>
          <div class="value user-info">
            <a-avatar :size="24" :src="app.user.userAvatar">
              {{ app.user.userName?.charAt(0) || 'U' }}
            </a-avatar>
            <span>{{ app.user.userName || '匿名用户' }}</span>
          </div>
        </div>

        <div v-if="app.codeGenType" class="info-row">
          <span class="label">代码类型</span>
          <a-tag class="value">{{ app.codeGenType }}</a-tag>
        </div>

        <div v-if="app.priority === 99" class="info-row">
          <span class="label">应用状态</span>
          <a-tag color="gold" class="value">
            <StarFilled /> 精选
          </a-tag>
        </div>

        <div v-if="app.deployKey" class="info-row">
          <span class="label">部署状态</span>
          <a-tag color="green" class="value">已部署</a-tag>
        </div>

        <div v-if="app.createTime" class="info-row">
          <span class="label">创建时间</span>
          <span class="value">{{ formatDateTime(app.createTime) }}</span>
        </div>

        <div v-if="app.initPrompt" class="info-row description">
          <span class="label">初始提示词</span>
          <p class="value prompt-text">{{ app.initPrompt }}</p>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="detail-actions">
        <a-button type="primary" @click="handleChat">
          <template #icon><MessageOutlined /></template>
          开始对话
        </a-button>
        <a-button v-if="showEdit" @click="handleEdit">
          <template #icon><EditOutlined /></template>
          编辑应用
        </a-button>
        <a-button v-if="app.deployKey" :href="deployUrl" target="_blank">
          <template #icon><LinkOutlined /></template>
          访问应用
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import {
  AppstoreOutlined,
  StarFilled,
  MessageOutlined,
  EditOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue'
import { formatDateTime } from '@/utils/format'
import { APP_DEPLOY_URL } from '@/config'

interface Props {
  /** 控制弹窗显示 */
  open: boolean
  /** 应用数据 */
  app: API.AppVO | null
  /** 是否显示编辑按钮 */
  showEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showEdit: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'chat', appId: number): void
  (e: 'edit', appId: number): void
}>()

// 双向绑定 visible
const visible = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val),
})

// 部署链接
const deployUrl = computed(() => {
  if (props.app?.deployKey) {
    return `${APP_DEPLOY_URL}/${props.app.deployKey}/`
  }
  return ''
})

// 关闭弹窗
const handleClose = () => {
  emit('update:open', false)
}

// 开始对话
const handleChat = () => {
  if (props.app?.id) {
    emit('chat', props.app.id)
  }
}

// 编辑应用
const handleEdit = () => {
  if (props.app?.id) {
    emit('edit', props.app.id)
  }
}
</script>

<style scoped>
.app-detail {
  padding: 8px 0;
}

.detail-cover {
  height: 200px;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 24px;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 64px;
  color: rgba(255, 255, 255, 0.8);
}

.detail-info {
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row.description {
  flex-direction: column;
  align-items: flex-start;
}

.label {
  width: 100px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
}

.value {
  flex: 1;
  color: #1a1a2e;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.prompt-text {
  margin: 8px 0 0;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  width: 100%;
}

.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
