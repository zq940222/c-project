<template>
  <a-layout class="app-layout">
    <a-layout-header class="app-header">
      <div class="app-logo">C语言数据文件智能处理平台</div>
      <a-menu
        theme="dark"
        mode="horizontal"
        :selectedKeys="[currentRoute]"
        @click="handleMenuClick"
      >
        <a-menu-item key="editor">代码编辑</a-menu-item>
        <a-menu-item key="query">数据查询</a-menu-item>
        <a-menu-item key="rules">规则管理</a-menu-item>
        <a-menu-item key="diff">文件比对</a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-layout-content class="app-content">
      <router-view />
    </a-layout-content>
  </a-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const currentRoute = computed(() => route.name || 'editor')

const handleMenuClick = ({ key }) => {
  router.push({ name: key })
}
</script>

<style scoped>
.app-header :deep(.ant-menu) {
  flex: 1;
  justify-content: flex-end;
  min-width: 0;
}
</style>
