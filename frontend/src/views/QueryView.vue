<template>
  <div class="query-view">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="数据源" :bordered="false">
          <template #extra>
            <a-button @click="loadSampleData" size="small">加载示例</a-button>
          </template>
          <MonacoEditor
            v-model="sourceCode"
            language="c"
            height="300px"
            :readOnly="false"
          />
          <a-button
            type="primary"
            style="margin-top: 12px"
            @click="parseSource"
            :loading="parsing"
          >
            解析数据源
          </a-button>
          <span v-if="fileId" style="margin-left: 12px; color: #52c41a">
            已解析 ({{ fileId }})
          </span>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="查询语句" :bordered="false">
          <template #extra>
            <a-dropdown>
              <a-button size="small">示例查询</a-button>
              <template #overlay>
                <a-menu @click="loadSampleQuery">
                  <a-menu-item key="1">SEARCH 园区的猫们</a-menu-item>
                  <a-menu-item key="2">SEARCH 园区的猫们 WHERE 品种 = '狸花'</a-menu-item>
                  <a-menu-item key="3">SEARCH 园区的猫们 WHERE 出生日期 > 2022</a-menu-item>
                  <a-menu-item key="4">SEARCH 园区的猫们 ORDER BY 出生日期 DESC</a-menu-item>
                  <a-menu-item key="5">SEARCH 园区的猫们 WHERE 品种 = '橘猫' ORDER BY 出生日期</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <a-textarea
            v-model:value="queryDsl"
            placeholder="输入查询语句，例如: SEARCH 园区的猫们 WHERE 品种 = '狸花'"
            :rows="4"
          />
          <a-alert
            style="margin-top: 12px"
            type="info"
            show-icon
            message="查询语法"
            description="SEARCH 变量名 [WHERE 条件] [ORDER BY 字段 [ASC|DESC]] [LIMIT 数量]"
          />
          <a-button
            type="primary"
            style="margin-top: 12px"
            @click="executeQuery"
            :loading="querying"
            :disabled="!fileId"
          >
            执行查询
          </a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="查询结果" :bordered="false" style="margin-top: 16px">
      <template #extra>
        <span v-if="queryResult">
          共 {{ queryResult.totalCount }} 条记录，耗时 {{ queryResult.executionTime }}ms
        </span>
      </template>
      <a-table
        v-if="queryResult?.rows?.length"
        :columns="resultColumns"
        :data-source="queryResult.rows"
        :pagination="{ pageSize: 10 }"
        size="small"
        :scroll="{ x: 800 }"
      />
      <a-empty v-else description="暂无查询结果" />
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import MonacoEditor from '@/components/MonacoEditor.vue'
import { parseApi, queryApi } from '@/api'

const sourceCode = ref('')
const queryDsl = ref('')
const fileId = ref('')
const queryResult = ref(null)
const parsing = ref(false)
const querying = ref(false)

// 示例代码
const sampleCode = `// 猫的结构体定义
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
    {"CAT003", "小白", "波斯", 2022, "白色"},
    {"CAT004", "花花", "狸花", 2023, "花色"},
    {"CAT005", "咪咪", "暹罗", 2024, "奶油色"},
    {"CAT006", "大橘", "橘猫", 2021, "黄色"},
    {"CAT007", "小灰", "英短", 2023, "灰色"}
};
`

// 动态生成表格列
const resultColumns = computed(() => {
  if (!queryResult.value?.columns) return []
  return queryResult.value.columns
    .filter(col => !col.startsWith('_'))
    .map(col => ({
      title: col,
      dataIndex: col,
      key: col
    }))
})

// 加载示例数据
function loadSampleData() {
  sourceCode.value = sampleCode
  fileId.value = ''
  queryResult.value = null
}

// 加载示例查询
function loadSampleQuery({ key }) {
  const queries = {
    '1': 'SEARCH 园区的猫们',
    '2': "SEARCH 园区的猫们 WHERE 品种 = '狸花'",
    '3': 'SEARCH 园区的猫们 WHERE 出生日期 > 2022',
    '4': 'SEARCH 园区的猫们 ORDER BY 出生日期 DESC',
    '5': "SEARCH 园区的猫们 WHERE 品种 = '橘猫' ORDER BY 出生日期"
  }
  queryDsl.value = queries[key] || ''
}

// 解析数据源
async function parseSource() {
  if (!sourceCode.value.trim()) {
    message.warning('请先输入数据源代码')
    return
  }

  parsing.value = true
  try {
    const res = await parseApi.parseContent(sourceCode.value, 'query.c')
    if (res.code === 200) {
      fileId.value = res.data.fileId
      message.success('数据源解析成功')
    } else {
      message.error(res.message || '解析失败')
    }
  } catch (error) {
    message.error('解析失败')
  } finally {
    parsing.value = false
  }
}

// 执行查询
async function executeQuery() {
  if (!queryDsl.value.trim()) {
    message.warning('请输入查询语句')
    return
  }

  if (!fileId.value) {
    message.warning('请先解析数据源')
    return
  }

  querying.value = true
  try {
    const res = await queryApi.execute(queryDsl.value, [fileId.value])
    if (res.code === 200) {
      queryResult.value = res.data
      message.success(`查询完成，共 ${res.data.totalCount} 条结果`)
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败')
  } finally {
    querying.value = false
  }
}
</script>

<style scoped>
.query-view {
  height: 100%;
}
</style>
