import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/editor'
    },
    {
      path: '/editor',
      name: 'editor',
      component: () => import('@/views/EditorView.vue')
    },
    {
      path: '/query',
      name: 'query',
      component: () => import('@/views/QueryView.vue')
    },
    {
      path: '/rules',
      name: 'rules',
      component: () => import('@/views/RulesView.vue')
    },
    {
      path: '/diff',
      name: 'diff',
      component: () => import('@/views/DiffView.vue')
    }
  ]
})

export default router
