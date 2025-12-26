import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { parseApi, ruleApi } from '@/api'

export const useEditorStore = defineStore('editor', () => {
  // 状态
  const content = ref('')
  const fileName = ref('example.c')
  const parseResult = ref(null)
  const checkResult = ref(null)
  const loading = ref(false)

  // 计算属性
  const problems = computed(() => {
    if (!checkResult.value?.results) return []
    return checkResult.value.results
  })

  const summary = computed(() => {
    return checkResult.value?.summary || { errorCount: 0, warningCount: 0, passCount: 0 }
  })

  // 解析内容
  async function parseContent() {
    if (!content.value.trim()) return

    loading.value = true
    try {
      const res = await parseApi.parseContent(content.value, fileName.value)
      if (res.code === 200) {
        parseResult.value = res.data
      }
    } catch (error) {
      console.error('解析失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 执行规则检查
  async function executeCheck(ruleIds = null) {
    if (!content.value.trim()) return

    loading.value = true
    try {
      const res = await ruleApi.execute(ruleIds, content.value, fileName.value)
      if (res.code === 200) {
        checkResult.value = res.data
      }
    } catch (error) {
      console.error('检查失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 设置内容
  function setContent(newContent) {
    content.value = newContent
  }

  // 清除结果
  function clearResults() {
    parseResult.value = null
    checkResult.value = null
  }

  return {
    content,
    fileName,
    parseResult,
    checkResult,
    loading,
    problems,
    summary,
    parseContent,
    executeCheck,
    setContent,
    clearResults
  }
})
