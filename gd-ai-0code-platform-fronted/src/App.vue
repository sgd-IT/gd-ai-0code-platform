<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { testOk } from '@/api/testApi.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

testOk({ name: 'test' }).then((res) => {
  console.log(res)
})
 const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()
</script>

<template>
  <BasicLayout/>
</template>

<style scoped>
</style>
