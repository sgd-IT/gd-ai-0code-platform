<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <header class="chat-header">
      <div class="header-left">
        <a-dropdown>
          <div class="app-name-wrapper">
            <span class="app-name">{{ appInfo?.appName || '加载中...' }}</span>
            <DownOutlined />
          </div>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="goToEdit">
                <EditOutlined />
                编辑应用
              </a-menu-item>
              <a-menu-item @click="goHome">
                <HomeOutlined />
                返回主页
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div class="header-right">
        <a-button
          type="primary"
          :loading="deploying"
          @click="handleDeploy"
        >
          <template #icon><CloudUploadOutlined /></template>
          部署
        </a-button>
      </div>
    </header>

    <!-- 核心内容区域 -->
    <main class="chat-main">
      <!-- 左侧对话区域 -->
      <section class="chat-panel">
        <!-- 消息列表 -->
        <div ref="messagesRef" class="messages-container">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role === 'user' ? 'user-message' : 'assistant-message']"
          >
            <div class="message-avatar">
              <a-avatar v-if="msg.role === 'user'" :src="loginUserStore.loginUser.userAvatar">
                {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <a-avatar v-else class="ai-avatar" :src="aiAvatarImg" />
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div v-if="msg.role === 'assistant'" class="ai-response">
                  <!-- 显示文件列表 -->
                  <div v-if="msg.files && msg.files.length > 0" class="file-list">
                    <div
                      v-for="file in msg.files"
                      :key="file.name"
                      class="file-item"
                    >
                      <FileOutlined />
                      <span class="file-name">{{ file.name }}</span>
                      <span class="file-path">{{ file.path }}</span>
                    </div>
                  </div>
                  <div class="ai-text">{{ msg.content }}</div>
                </div>
                <div v-else>{{ msg.content }}</div>
              </div>
            </div>
          </div>
          <!-- 加载动画 -->
          <div v-if="isGenerating" class="message assistant-message">
            <div class="message-avatar">
              <a-avatar class="ai-avatar" :src="aiAvatarImg" />
            </div>
            <div class="message-content">
              <div class="message-bubble typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <a-textarea
              v-model:value="userInput"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              class="chat-input"
              :disabled="isGenerating"
              @keydown.enter.exact.prevent="handleSend"
            />
            <div class="input-actions">
              <a-button
                type="primary"
                shape="circle"
                :disabled="isGenerating || !userInput.trim()"
                @click="handleSend"
              >
                <SendOutlined />
              </a-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧预览区域 -->
      <section class="preview-panel">
        <div v-if="!previewUrl" class="preview-placeholder">
          <div class="placeholder-content">
            <RocketOutlined class="placeholder-icon" />
            <p>生成完成后将在此显示网页预览</p>
          </div>
        </div>
        <iframe
          v-else
          ref="previewIframe"
          :src="previewUrl"
          class="preview-iframe"
          frameborder="0"
        ></iframe>
      </section>
    </main>

    <!-- 部署成功弹窗 -->
    <a-modal
      v-model:open="deployModalVisible"
      title="部署成功"
      :footer="null"
      centered
    >
      <div class="deploy-success">
        <CheckCircleOutlined class="success-icon" />
        <p>您的应用已成功部署！</p>
        <div class="deploy-url">
          <a-input :value="deployedUrl" readonly>
            <template #addonAfter>
              <a-button type="link" @click="copyUrl">复制</a-button>
            </template>
          </a-input>
        </div>
        <a-button type="primary" :href="deployedUrl" target="_blank">
          访问应用
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  DownOutlined,
  EditOutlined,
  HomeOutlined,
  CloudUploadOutlined,
  FileOutlined,
  SendOutlined,
  RocketOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { API_BASE_URL, APP_PREVIEW_URL } from '@/config'
import aiAvatarImg from '@/assets/aiAvatar.png'
import { deployApp, adminGetAppById, listMyAppVoByPage } from '@/api/appController'

// 消息类型定义
interface FileInfo {
  name: string
  path: string
}

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  files?: FileInfo[]
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appId = computed(() => Number(route.params.id))
const appInfo = ref<API.App | null>(null)

// 对话相关
const messages = ref<ChatMessage[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

// 预览相关
const previewUrl = ref('')

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployedUrl = ref('')

// 基础 URL (从配置读取)

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    // 先尝试用管理员接口
    let appData: API.App | API.AppVO | null = null

    if (loginUserStore.loginUser.userRole === 'admin') {
      const res = await adminGetAppById({ id: appId.value })
      if (res.data.code === 0 && res.data.data) {
        appData = res.data.data
      }
    }

    // 如果管理员接口失败或不是管理员，尝试用普通用户接口
    if (!appData) {
      const res = await listMyAppVoByPage({ pageNum: 1, pageSize: 100 })
      if (res.data.code === 0 && res.data.data?.records) {
        const found = res.data.data.records.find(app => app.id === appId.value)
        if (found) {
          appData = found
        }
      }
    }

    if (appData) {
      appInfo.value = appData
      // 如果有初始提示词，自动发送
      if (appInfo.value.initPrompt && messages.value.length === 0) {
        // 添加用户消息
        messages.value.push({
          role: 'user',
          content: appInfo.value.initPrompt,
          timestamp: Date.now(),
        })
        // 自动发送给 AI
        await sendToAI(appInfo.value.initPrompt)
      }
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 发送消息给 AI (SSE 流式)
const sendToAI = async (msg: string) => {
  isGenerating.value = true

  // 创建 AI 回复消息
  const aiMessage: ChatMessage = {
    role: 'assistant',
    content: '',
    timestamp: Date.now(),
    files: [],
  }
  messages.value.push(aiMessage)
  const aiMessageIndex = messages.value.length - 1

  try {
    const url = `${API_BASE_URL}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(msg)}`
    const eventSource = new EventSource(url, { withCredentials: true })

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.d) {
          messages.value[aiMessageIndex].content += data.d
          scrollToBottom()
        }
      } catch (e) {
        // 忽略解析错误
      }
    }

    eventSource.addEventListener('end', () => {
      eventSource.close()
      isGenerating.value = false
      // 更新预览
      updatePreview()
      scrollToBottom()
    })

    eventSource.onerror = () => {
      eventSource.close()
      isGenerating.value = false
      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = '抱歉，生成过程中出现错误，请重试。'
      }
      scrollToBottom()
    }
  } catch (error) {
    isGenerating.value = false
    messages.value[aiMessageIndex].content = '抱歉，生成过程中出现错误，请重试。'
  }
}

// 更新预览 URL
const updatePreview = () => {
  if (appInfo.value) {
    const codeGenType = appInfo.value.codeGenType || 'multiFile'
    previewUrl.value = `${APP_PREVIEW_URL}/${codeGenType}_${appId.value}/`
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 发送消息
const handleSend = () => {
  const msg = userInput.value.trim()
  if (!msg || isGenerating.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: msg,
    timestamp: Date.now(),
  })
  userInput.value = ''
  scrollToBottom()

  // 发送给 AI
  sendToAI(msg)
}

// 部署应用
const handleDeploy = async () => {
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0 && res.data.data) {
      deployedUrl.value = res.data.data
      deployModalVisible.value = true
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    message.error('部署失败，请稍后重试')
  } finally {
    deploying.value = false
  }
}

// 复制 URL
const copyUrl = () => {
  navigator.clipboard.writeText(deployedUrl.value)
  message.success('已复制到剪贴板')
}

// 跳转到编辑页
const goToEdit = () => {
  router.push(`/app/edit/${appId.value}`)
}

// 返回主页
const goHome = () => {
  router.push('/')
}

// 页面加载
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appChatPage {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  margin: -24px;
  background: #f8f9fa;
}

/* 顶部栏 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: white;
  border-bottom: 1px solid #eee;
}

.header-left {
  display: flex;
  align-items: center;
}

.app-name-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background 0.3s;
}

.app-name-wrapper:hover {
  background: #f5f5f5;
}

.app-name {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
}

/* 核心内容区域 */
.chat-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧对话区域 - 2:3比例中的2 */
.chat-panel {
  flex: 2;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  background: white;
  border-right: 1px solid #eee;
}

/* 消息列表 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message-content {
  max-width: 80%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-bubble {
  background: #1890ff;
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant-message .message-bubble {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}

/* AI 回复样式 */
.ai-response {
  white-space: pre-wrap;
}

.file-list {
  margin-bottom: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 8px;
}

.file-name {
  font-weight: 500;
}

.file-path {
  color: #999;
  font-size: 12px;
}

/* 输入动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 输入框区域 */
.input-area {
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.input-wrapper {
  background: #f5f7f9;
  border-radius: 12px;
  padding: 10px 14px;
}

.chat-input {
  border: none !important;
  background: transparent !important;
  resize: none;
  font-size: 14px;
}

.chat-input:focus {
  box-shadow: none !important;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  gap: 8px;
}

.action-btn {
  color: #666;
  padding: 4px 8px;
}

/* 右侧预览区域 - 2:3比例中的3 */
.preview-panel {
  flex: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a1a2e;
  position: relative;
}

.preview-placeholder {
  text-align: center;
  color: #666;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.placeholder-icon {
  font-size: 64px;
  color: #444;
}

.preview-placeholder p {
  color: #888;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  background: white;
}

/* 部署成功弹窗 */
.deploy-success {
  text-align: center;
  padding: 24px;
}

.success-icon {
  font-size: 64px;
  color: #52c41a;
  margin-bottom: 16px;
}

.deploy-success p {
  font-size: 16px;
  color: #333;
  margin-bottom: 24px;
}

.deploy-url {
  margin-bottom: 24px;
}
</style>
