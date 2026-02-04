<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" allow-clear />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input-number v-model:value="searchParams.userId" placeholder="输入用户ID" style="width: 150px" />
      </a-form-item>
      <a-form-item label="优先级">
        <a-select v-model:value="searchParams.priority" placeholder="选择优先级" style="width: 120px" allow-clear>
          <a-select-option :value="99">精选</a-select-option>
          <a-select-option :value="0">普通</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button @click="resetSearch">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- 应用封面 -->
        <template v-if="column.dataIndex === 'cover'">
          <a-image
            v-if="record.cover"
            :src="record.cover"
            :width="80"
            :height="60"
            style="object-fit: cover; border-radius: 4px"
          />
          <span v-else class="no-cover">暂无封面</span>
        </template>

        <!-- 优先级 -->
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">
            <StarFilled /> 精选
          </a-tag>
          <a-tag v-else color="default">普通</a-tag>
        </template>

        <!-- 创建用户 -->
        <template v-else-if="column.dataIndex === 'user'">
          <div v-if="record.user" class="user-info">
            <a-avatar :size="24" :src="record.user.userAvatar">
              {{ record.user.userName?.charAt(0) }}
            </a-avatar>
            <span>{{ record.user.userName || record.user.userAccount }}</span>
          </div>
          <span v-else>用户ID: {{ record.userId }}</span>
        </template>

        <!-- 创建时间 -->
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>

        <!-- 操作列 -->
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goToEdit(record.id)">
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              :disabled="record.priority === 99"
              @click="setFeatured(record)"
            >
              精选
            </a-button>
            <a-popconfirm
              title="确定要删除这个应用吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { StarFilled } from '@ant-design/icons-vue'
import { adminListAppByPage, adminDeleteApp, adminUpdateApp } from '@/api/appController'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    ellipsis: true,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '代码类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '创建用户',
    dataIndex: 'user',
    width: 150,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right' as const,
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.AppAdminQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (t: number) => `共 ${t} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminListAppByPage(searchParams)
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.priority = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 删除应用
const doDelete = async (id: number) => {
  try {
    const res = await adminDeleteApp({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败')
  }
}

// 设为精选
const setFeatured = async (record: API.AppVO) => {
  try {
    const res = await adminUpdateApp({
      id: record.id,
      appName: record.appName,
      cover: record.cover,
      priority: 99,
    })
    if (res.data.code === 0) {
      message.success('设置精选成功')
      fetchData()
    } else {
      message.error('设置失败：' + res.data.message)
    }
  } catch (error) {
    message.error('设置失败')
  }
}

// 跳转到编辑页
const goToEdit = (id: number) => {
  router.push(`/app/edit/${id}`)
}

// 页面加载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#appManagePage {
  padding: 0;
}

.no-cover {
  color: #999;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
