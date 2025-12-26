<template>
  <div class="editor-view">
    <a-row :gutter="16">
      <a-col :span="16">
        <a-card title="代码编辑器" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="loadSampleData">加载示例</a-button>
              <a-button type="primary" @click="handleParse" :loading="store.loading">
                解析
              </a-button>
              <a-button type="primary" @click="handleCheck" :loading="store.loading">
                规则检查
              </a-button>
            </a-space>
          </template>
          <MonacoEditor
            ref="editorRef"
            v-model="store.content"
            language="c"
            height="500px"
            :markers="markers"
          />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="解析结果" :bordered="false" style="margin-bottom: 16px;">
          <template v-if="store.parseResult">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="文件ID">
                {{ store.parseResult.fileId }}
              </a-descriptions-item>
              <a-descriptions-item label="结构体">
                {{ store.parseResult.structs?.length || 0 }} 个
              </a-descriptions-item>
              <a-descriptions-item label="变量">
                {{ store.parseResult.variables?.length || 0 }} 个
              </a-descriptions-item>
              <a-descriptions-item label="宏定义">
                {{ store.parseResult.macros?.length || 0 }} 个
              </a-descriptions-item>
            </a-descriptions>

            <a-divider>结构体列表</a-divider>
            <a-collapse v-if="store.parseResult.structs?.length">
              <a-collapse-panel
                v-for="struct in store.parseResult.structs"
                :key="struct.name"
                :header="struct.name"
              >
                <a-table
                  :columns="structFieldColumns"
                  :data-source="struct.fields"
                  :pagination="false"
                  size="small"
                />
              </a-collapse-panel>
            </a-collapse>
            <a-empty v-else description="暂无结构体" />

            <a-divider>变量列表</a-divider>
            <a-list
              v-if="store.parseResult.variables?.length"
              size="small"
              :data-source="store.parseResult.variables"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>{{ item.name }}</template>
                    <template #description>
                      类型: {{ item.structType }} | 数量: {{ item.count }}
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无变量" />
          </template>
          <a-empty v-else description="请先解析代码" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="问题列表" :bordered="false" style="margin-top: 16px;">
      <template #extra>
        <a-space>
          <a-tag color="red">错误: {{ store.summary.errorCount }}</a-tag>
          <a-tag color="orange">警告: {{ store.summary.warningCount }}</a-tag>
          <a-tag color="green">通过: {{ store.summary.passCount }}</a-tag>
        </a-space>
      </template>
      <a-table
        :columns="problemColumns"
        :data-source="store.problems"
        :pagination="{ pageSize: 10 }"
        size="small"
        @row-click="handleProblemClick"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'resultType'">
            <a-tag :color="getResultTypeColor(record.resultType)">
              {{ record.resultType }}
            </a-tag>
          </template>
          <template v-if="column.key === 'location'">
            <span v-if="record.location">
              第 {{ record.location.startLine }} 行
            </span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import MonacoEditor from '@/components/MonacoEditor.vue'
import { useEditorStore } from '@/stores/editor'

const store = useEditorStore()
const editorRef = ref(null)

// 示例代码
const sampleCode = `// C语言数据文件示例
#define WHITE 白色
#define BLACK 黑色

// 定义猫的结构体
struct 猫 {
    char* ID;
    char* 名字;
    char* 品种;
    int 出生日期;
    char* 颜色;
};

// 园区的猫数据
猫 园区的猫们[] = {
    {"CAT001", "小黄", "橘猫", 2023, "黄色"},
    {"CAT002", "小黑", "狸花", 2024, "灰色"},
    {"CAT001", "大黄", "橘猫", 2022, "黄色"},
    {"CAT003", "小白", "波斯", 1985, "白色"},
    {"CAT004", "花花", "狸花", 2023, "花色"},
    {"CAT005", "咪咪", "暹罗", 2024, "紫色"}
};
`

// 表格列定义
const structFieldColumns = [
  { title: '字段名', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '指针', dataIndex: 'isPointer', key: 'isPointer',
    customRender: ({ text }) => text ? '是' : '否' }
]

const problemColumns = [
  { title: '类型', dataIndex: 'resultType', key: 'resultType', width: 80 },
  { title: '规则', dataIndex: 'ruleName', key: 'ruleName', width: 150 },
  { title: '位置', key: 'location', width: 100 },
  { title: '消息', dataIndex: 'message', key: 'message' }
]

// 计算问题markers
const markers = computed(() => {
  return store.problems.map(p => ({
    type: p.resultType,
    message: p.message,
    line: p.location?.startLine || 1
  }))
})

// 加载示例数据
function loadSampleData() {
  store.setContent(sampleCode)
  store.clearResults()
  message.success('已加载示例代码')
}

// 解析代码
async function handleParse() {
  if (!store.content.trim()) {
    message.warning('请先输入代码')
    return
  }
  await store.parseContent()
  message.success('解析完成')
}

// 执行规则检查
async function handleCheck() {
  if (!store.content.trim()) {
    message.warning('请先输入代码')
    return
  }
  await store.executeCheck()
  message.success('检查完成')
}

// 获取结果类型颜色
function getResultTypeColor(type) {
  switch (type) {
    case 'ERROR': return 'red'
    case 'WARNING': return 'orange'
    case 'INFO': return 'blue'
    default: return 'green'
  }
}

// 点击问题定位到代码
function handleProblemClick(record) {
  if (record.location && editorRef.value) {
    editorRef.value.revealLine(record.location.startLine)
  }
}
</script>

<style scoped>
.editor-view {
  height: 100%;
}
</style>
