<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { testOk } from '@/api/testApi.ts'

testOk({ name: 'test' }).then((res) => {
  console.log(res)
})

</script>

<template>
  <BasicLayout/>
</template>

<style scoped>
</style>
