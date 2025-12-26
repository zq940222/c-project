<template>
  <div class="diff-view">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="左侧文件 (原始版本)" :bordered="false" size="small">
          <template #extra>
            <a-button size="small" @click="loadLeftSample">加载示例</a-button>
          </template>
          <MonacoEditor
            v-model="leftContent"
            language="c"
            height="350px"
          />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="右侧文件 (新版本)" :bordered="false" size="small">
          <template #extra>
            <a-button size="small" @click="loadRightSample">加载示例</a-button>
          </template>
          <MonacoEditor
            v-model="rightContent"
            language="c"
            height="350px"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="比对选项" :bordered="false" style="margin-top: 16px" size="small">
      <a-space>
        <a-checkbox v-model:checked="options.ignoreWhitespace">忽略空白差异</a-checkbox>
        <a-checkbox v-model:checked="options.ignoreComments">忽略注释差异</a-checkbox>
        <a-checkbox v-model:checked="options.ignoreOrder">忽略顺序差异</a-checkbox>
        <a-checkbox v-model:checked="options.ignoreNumberFormat">忽略数值格式差异</a-checkbox>
        <a-checkbox v-model:checked="options.semanticCompare">启用语义比对</a-checkbox>
        <a-button type="primary" @click="executeDiff" :loading="diffing">
          执行比对
        </a-button>
      </a-space>
    </a-card>

    <a-card
      v-if="diffResult"
      title="比对结果"
      :bordered="false"
      style="margin-top: 16px"
    >
      <template #extra>
        <a-space>
          <a-tag color="red">新增: {{ diffResult.summary?.added || 0 }}</a-tag>
          <a-tag color="orange">删除: {{ diffResult.summary?.removed || 0 }}</a-tag>
          <a-tag color="blue">修改: {{ diffResult.summary?.modified || 0 }}</a-tag>
          <a-tag color="green">可忽略: {{ diffResult.summary?.ignorableDiffs || 0 }}</a-tag>
        </a-space>
      </template>

      <a-row :gutter="16">
        <a-col :span="16">
          <a-card title="差异详情" size="small">
            <a-table
              :columns="diffColumns"
              :data-source="diffResult.items"
              :pagination="{ pageSize: 10 }"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'type'">
                  <a-tag :color="getDiffTypeColor(record.type)">
                    {{ getDiffTypeText(record.type) }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'location'">
                  <span v-if="record.leftLocation || record.rightLocation">
                    {{ record.leftLocation?.startLine || '-' }} →
                    {{ record.rightLocation?.startLine || '-' }}
                  </span>
                </template>
                <template v-if="column.key === 'ignorable'">
                  <a-tag v-if="record.ignorable" color="green">
                    {{ record.ignoreReason || '可忽略' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card title="比对摘要" size="small">
            <a-statistic-card-group>
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-statistic
                    title="总差异数"
                    :value="diffResult.summary?.totalDiffs || 0"
                  />
                </a-col>
                <a-col :span="12">
                  <a-statistic
                    title="实质差异"
                    :value="diffResult.summary?.significantDiffs || 0"
                    :value-style="{ color: '#cf1322' }"
                  />
                </a-col>
              </a-row>
            </a-statistic-card-group>

            <a-divider />

            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="新增">
                {{ diffResult.summary?.added || 0 }} 处
              </a-descriptions-item>
              <a-descriptions-item label="删除">
                {{ diffResult.summary?.removed || 0 }} 处
              </a-descriptions-item>
              <a-descriptions-item label="修改">
                {{ diffResult.summary?.modified || 0 }} 处
              </a-descriptions-item>
              <a-descriptions-item label="可忽略">
                {{ diffResult.summary?.ignorableDiffs || 0 }} 处
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="Unified Diff" size="small" style="margin-top: 16px">
        <pre class="diff-output">{{ diffResult.unifiedDiff }}</pre>
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import MonacoEditor from '@/components/MonacoEditor.vue'
import { diffApi } from '@/api'

const leftContent = ref('')
const rightContent = ref('')
const diffResult = ref(null)
const diffing = ref(false)

const options = ref({
  ignoreWhitespace: true,
  ignoreComments: true,
  ignoreOrder: true,
  ignoreNumberFormat: true,
  semanticCompare: true
})

const diffColumns = [
  { title: '类型', key: 'type', width: 100 },
  { title: '位置', key: 'location', width: 100 },
  { title: '左侧内容', dataIndex: 'leftContent', key: 'leftContent', ellipsis: true },
  { title: '右侧内容', dataIndex: 'rightContent', key: 'rightContent', ellipsis: true },
  { title: '可忽略', key: 'ignorable', width: 120 }
]

// 左侧示例
const leftSample = `// 版本1 - 原始数据
struct 猫 {
    char* ID;
    char* 名字;
    char* 品种;
    int 出生日期;
    char* 颜色;
};

猫 园区的猫们[] = {
    {"CAT001", "小黄", "橘猫", 2023, "黄色"},
    {"CAT002", "小黑", "狸花", 2024, "灰色"},
    {"CAT003", "小白", "波斯", 2022, "白色"}
};

int count = 0x01;
`

// 右侧示例（有差异）
const rightSample = `// 版本2 - 修改后数据
struct 猫 {
    char* ID;
    char* 名字;
    char* 品种;
    int 出生日期;
    char* 颜色;
};

猫 园区的猫们[] = {
    {"CAT001", "小黄", "金渐层", 2023, "黄色"},
    {"CAT003", "小白", "波斯", 2022, "白色"},
    {"CAT002", "小黑", "狸花", 2024, "灰色"},
    {"CAT004", "花花", "狸花", 2023, "花色"}
};

int count = 0x1;
`

function loadLeftSample() {
  leftContent.value = leftSample
}

function loadRightSample() {
  rightContent.value = rightSample
}

async function executeDiff() {
  if (!leftContent.value.trim() || !rightContent.value.trim()) {
    message.warning('请输入两个文件的内容')
    return
  }

  diffing.value = true
  try {
    const res = await diffApi.compare(leftContent.value, rightContent.value, options.value)
    if (res.code === 200) {
      diffResult.value = res.data
      message.success('比对完成')
    } else {
      message.error(res.message || '比对失败')
    }
  } catch (error) {
    message.error('比对失败')
  } finally {
    diffing.value = false
  }
}

function getDiffTypeColor(type) {
  switch (type) {
    case 'ADDED': return 'green'
    case 'REMOVED': return 'red'
    case 'MODIFIED': return 'blue'
    case 'FORMAT_ONLY': return 'cyan'
    case 'ORDER_ONLY': return 'purple'
    default: return 'default'
  }
}

function getDiffTypeText(type) {
  switch (type) {
    case 'ADDED': return '新增'
    case 'REMOVED': return '删除'
    case 'MODIFIED': return '修改'
    case 'FORMAT_ONLY': return '格式差异'
    case 'ORDER_ONLY': return '顺序差异'
    default: return type
  }
}
</script>

<style scoped>
.diff-view {
  height: 100%;
}

.diff-output {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow: auto;
}
</style>
