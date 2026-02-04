<template>
  <div id="homePage">
    <!-- 顶部标题区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="main-title">AI 应用生成平台</h1>
        <p class="sub-title">一句话轻松创建网站应用</p>
      </div>

      <!-- 输入框区域 -->
      <div class="input-section">
        <div class="input-card">
          <a-textarea
            v-model:value="promptInput"
            :auto-size="{ minRows: 2, maxRows: 6 }"
            placeholder="帮我创建个人博客网站"
            class="prompt-input"
            @keydown.enter.ctrl="handleCreateApp"
          />
          <div class="input-actions">
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="creating"
              @click="handleCreateApp"
            >
              <template #icon><SendOutlined /></template>
            </a-button>
          </div>
        </div>

        <!-- 快捷提示词标签 -->
        <div class="quick-tags">
          <a-tag
            v-for="tag in quickTags"
            :key="tag"
            class="quick-tag"
            @click="promptInput = tag"
          >
            {{ tag }}
          </a-tag>
        </div>
      </div>
    </section>

    <!-- 我的作品区域 -->
    <section v-if="loginUserStore.loginUser.id" class="apps-section">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
      </div>
      <div class="apps-grid">
        <AppCard
          v-for="app in myApps"
          :key="app.id"
          :app="app"
          :custom-user-avatar="loginUserStore.loginUser.userAvatar"
          :custom-user-name="loginUserStore.loginUser.userName || '我'"
          @click="goToChat"
        />
      </div>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="myAppsPage"
          :total="myAppsTotal"
          :page-size="myAppsPageSize"
          :show-size-changer="false"
          size="small"
          @change="fetchMyApps"
        />
      </div>
    </section>

    <!-- 精选案例区域 -->
    <section class="apps-section featured-section">
      <div class="section-header">
        <h2 class="section-title">精选案例</h2>
      </div>
      <div class="apps-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :featured="true"
          @click="goToChat"
        />
      </div>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="featuredPage"
          :total="featuredTotal"
          :page-size="featuredPageSize"
          :show-size-changer="false"
          size="small"
          @change="fetchFeaturedApps"
        />
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 提示词输入
const promptInput = ref('')
const creating = ref(false)

// 快捷标签 - 常见网站生成需求
const quickTags = [
  '帮我创建一个简约风格的个人博客网站，要有文章列表、分类标签、关于我页面，主题色用深蓝色，支持深色模式切换，文章卡片要有封面图和摘要展示',
  '设计一个餐饮店铺官网，包含菜单展示、店铺位置地图、在线预约功能、顾客评价区域，整体风格温馨舒适，配色以暖橙色和米白色为主',
  '创建一个摄影师作品集网站，要有瀑布流相册展示、作品分类筛选、联系方式页面，设计要有艺术感，背景用深灰色突出照片色彩',
  '制作一个科技公司官网，包含产品介绍、团队成员、公司新闻、联系我们等模块，风格简洁现代，主色调用科技蓝，要有动效增强科技感',
]

// 我的应用列表
const myApps = ref<API.AppVO[]>([])
const myAppsPage = ref(1)
const myAppsPageSize = 6
const myAppsTotal = ref(0)

// 精选应用列表
const featuredApps = ref<API.AppVO[]>([])
const featuredPage = ref(1)
const featuredPageSize = 6
const featuredTotal = ref(0)

// 创建应用
const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      appName: promptInput.value.slice(0, 20) + (promptInput.value.length > 20 ? '...' : ''),
      initPrompt: promptInput.value,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    message.error('创建失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

// 跳转到对话页
const goToChat = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 获取我的应用列表
const fetchMyApps = async () => {
  if (!loginUserStore.loginUser.id) return

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.value,
      pageSize: myAppsPageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsTotal.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取我的应用失败', error)
  }
}

// 获取精选应用列表
const fetchFeaturedApps = async () => {
  try {
    const res = await listFeaturedAppVoByPage({
      pageNum: featuredPage.value,
      pageSize: featuredPageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredTotal.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取精选应用失败', error)
  }
}

// 页面加载
onMounted(() => {
  fetchMyApps()
  fetchFeaturedApps()
})
</script>

<style scoped>
#homePage {
  min-height: calc(100vh - 120px);
}

/* 顶部 Hero 区域 */
.hero-section {
  padding: 80px 20px 60px;
  text-align: center;
  margin: -24px -24px 40px;
}

.hero-content {
  margin-bottom: 48px;
}

.main-title {
  font-size: 56px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 20px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.sub-title {
  font-size: 20px;
  color: #4a5568;
  margin: 0;
  font-weight: 400;
  letter-spacing: 1px;
}

/* 输入框区域 */
.input-section {
  max-width: 680px;
  margin: 0 auto;
}

.input-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.prompt-input {
  border: none !important;
  resize: none;
  font-size: 16px;
  padding: 0;
  background: transparent !important;
}

.prompt-input:focus {
  box-shadow: none !important;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16px;
}

/* 快捷标签 */
.quick-tags {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 32px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.quick-tag {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(168, 237, 234, 0.4);
  border-radius: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
  font-size: 13px;
  line-height: 1.5;
  color: #4a5568;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.quick-tag:hover {
  border-color: #667eea;
  color: #667eea;
  background: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

/* 应用列表区域 */
.apps-section {
  padding: 0 0 40px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  margin: 0 -12px 24px;
  padding: 32px 24px;
  backdrop-filter: blur(10px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

/* 应用卡片网格 */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
}

/* 精选区域 */
.featured-section {
  background: rgba(255, 255, 255, 0.5);
  margin: 0 -24px;
  padding: 40px 36px;
  border-radius: 24px;
  backdrop-filter: blur(10px);
}
</style>
