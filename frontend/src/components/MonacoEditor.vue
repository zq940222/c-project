<template>
  <div ref="editorContainer" class="monaco-container" :style="{ height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as monaco from 'monaco-editor'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'c'
  },
  theme: {
    type: String,
    default: 'vs'
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '400px'
  },
  markers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const editorContainer = ref(null)
let editor = null

onMounted(() => {
  if (editorContainer.value) {
    editor = monaco.editor.create(editorContainer.value, {
      value: props.modelValue,
      language: props.language,
      theme: props.theme,
      readOnly: props.readOnly,
      automaticLayout: true,
      minimap: { enabled: true },
      fontSize: 14,
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      glyphMargin: true,
      folding: true,
      lineDecorationsWidth: 10,
      lineNumbersMinChars: 3
    })

    editor.onDidChangeModelContent(() => {
      const value = editor.getValue()
      emit('update:modelValue', value)
      emit('change', value)
    })

    // 设置初始markers
    updateMarkers()
  }
})

onUnmounted(() => {
  if (editor) {
    editor.dispose()
  }
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue)
  }
})

// 监听markers变化
watch(() => props.markers, () => {
  updateMarkers()
}, { deep: true })

function updateMarkers() {
  if (!editor) return

  const model = editor.getModel()
  if (!model) return

  const monacoMarkers = props.markers.map(m => ({
    severity: m.type === 'ERROR' ? monaco.MarkerSeverity.Error :
              m.type === 'WARNING' ? monaco.MarkerSeverity.Warning :
              monaco.MarkerSeverity.Info,
    message: m.message,
    startLineNumber: m.line || 1,
    startColumn: m.column || 1,
    endLineNumber: m.endLine || m.line || 1,
    endColumn: m.endColumn || 1000
  }))

  monaco.editor.setModelMarkers(model, 'rule-check', monacoMarkers)
}

// 暴露方法
function revealLine(lineNumber) {
  if (editor) {
    editor.revealLineInCenter(lineNumber)
    editor.setPosition({ lineNumber, column: 1 })
  }
}

defineExpose({
  revealLine,
  getEditor: () => editor
})
</script>

<style scoped>
.monaco-container {
  width: 100%;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}
</style>
