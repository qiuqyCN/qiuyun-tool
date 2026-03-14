<script setup lang="ts">
const route = useRoute()

// 分类页默认显示全部工具，保留当前查询参数
navigateTo({
  path: '/category/all',
  query: route.query
}, { replace: true })
</script>

<template>
  <div />
</template>
