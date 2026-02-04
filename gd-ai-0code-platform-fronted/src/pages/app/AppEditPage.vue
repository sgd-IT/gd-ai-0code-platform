<template>
  <div id="appEditPage">
    <div class="page-header">
      <a-button type="text" @click="goBack">
        <template #icon><ArrowLeftOutlined /></template>
        返回
      </a-button>
      <h2 class="page-title">编辑应用</h2>
    </div>

    <a-spin :spinning="loading">
      <div class="form-container">
        <a-form
          :model="formState"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 16 }"
          @finish="handleSubmit"
        >
          <!-- 应用名称 - 所有用户可编辑 -->
          <a-form-item
            label="应用名称"
            name="appName"
            :rules="[{ required: true, message: '请输入应用名称' }]"
          >
            <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
          </a-form-item>

          <!-- 管理员专属字段 -->
          <template v-if="isAdmin">
            <a-form-item label="应用封面" name="cover">
              <a-input v-model:value="formState.cover" placeholder="请输入封面图片URL" />
              <div v-if="formState.cover" class="cover-preview">
                <a-image :src="formState.cover" :width="200" />
              </div>
            </a-form-item>

            <a-form-item label="优先级" name="priority">
              <a-radio-group v-model:value="formState.priority">
                <a-radio :value="0">普通</a-radio>
                <a-radio :value="99">精选</a-radio>
              </a-radio-group>
            </a-form-item>
          </template>

          <!-- 只读信息展示 -->
          <a-divider>应用信息</a-divider>

          <a-form-item label="应用ID">
            <span class="info-text">{{ appInfo?.id }}</span>
          </a-form-item>

          <a-form-item label="代码类型">
            <a-tag>{{ appInfo?.codeGenType || '未知' }}</a-tag>
          </a-form-item>

          <a-form-item label="创建时间">
            <span class="info-text">{{ formatDateTime(appInfo?.createTime) }}</span>
          </a-form-item>

          <a-form-item label="更新时间">
            <span class="info-text">{{ formatDateTime(appInfo?.updateTime) }}</span>
          </a-form-item>

          <a-form-item v-if="appInfo?.deployKey" label="部署状态">
            <a-tag color="green">已部署</a-tag>
            <a-button
              type="link"
              size="small"
              :href="deployUrl"
              target="_blank"
            >
              访问
            </a-button>
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
            <a-space>
              <a-button type="primary" html-type="submit" :loading="submitting">
                保存修改
              </a-button>
              <a-button @click="goBack">取消</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatDateTime } from '@/utils/format'
import { adminGetAppById, updateApp, adminUpdateApp, listMyAppVoByPage } from '@/api/appController'
import { APP_DEPLOY_URL } from '@/config'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用ID
const appId = computed(() => Number(route.params.id))

// 是否是管理员
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 应用信息
const appInfo = ref<API.App | null>(null)

// 表单状态
const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 部署链接
const deployUrl = computed(() => {
  if (appInfo.value?.deployKey) {
    return `${APP_DEPLOY_URL}/${appInfo.value.deployKey}/`
  }
  return ''
})


// 获取应用信息
const fetchAppInfo = async () => {
  loading.value = true
  try {
    let appData: API.App | API.AppVO | null = null

    // 管理员使用管理员接口
    if (isAdmin.value) {
      const res = await adminGetAppById({ id: appId.value })
      if (res.data.code === 0 && res.data.data) {
        appData = res.data.data
      }
    } else {
      // 普通用户使用普通接口
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

      // 检查权限：普通用户只能编辑自己的应用
      if (!isAdmin.value && appInfo.value.userId !== loginUserStore.loginUser.id) {
        message.error('无权编辑此应用')
        router.push('/')
        return
      }

      // 填充表单
      formState.appName = appInfo.value.appName || ''
      formState.cover = appInfo.value.cover || ''
      formState.priority = appInfo.value.priority || 0
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以更新更多字段
      res = await adminUpdateApp({
        id: appId.value,
        appName: formState.appName,
        cover: formState.cover,
        priority: formState.priority,
      })
    } else {
      // 普通用户只能更新应用名称
      res = await updateApp({
        id: appId.value,
        appName: formState.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('保存成功')
      router.back()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appEditPage {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
}

.form-container {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.cover-preview {
  margin-top: 12px;
}

.info-text {
  color: #666;
}
</style>
