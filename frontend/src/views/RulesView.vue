<template>
  <div class="rules-view">
    <a-card title="规则管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showCreateModal">
          新建规则
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="rules"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'severity'">
            <a-tag :color="getSeverityColor(record.severity)">
              {{ record.severity }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editRule(record)">
                编辑
              </a-button>
              <a-button
                v-if="record.status !== 'PUBLISHED'"
                type="link"
                size="small"
                @click="publishRule(record)"
              >
                发布
              </a-button>
              <a-popconfirm
                title="确定删除此规则?"
                @confirm="deleteRule(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑规则弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingRule ? '编辑规则' : '新建规则'"
      @ok="saveRule"
      width="600px"
    >
      <a-form :model="ruleForm" layout="vertical">
        <a-form-item label="规则名称" required>
          <a-input v-model:value="ruleForm.name" placeholder="请输入规则名称" />
        </a-form-item>
        <a-form-item label="规则描述">
          <a-textarea
            v-model:value="ruleForm.description"
            placeholder="请输入规则描述"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="目标变量">
          <a-input
            v-model:value="ruleForm.targetVariable"
            placeholder="例如: 园区的猫们"
          />
        </a-form-item>
        <a-form-item label="目标字段">
          <a-input
            v-model:value="ruleForm.targetField"
            placeholder="例如: ID"
          />
        </a-form-item>
        <a-form-item label="规则类型">
          <a-select v-model:value="ruleForm.ruleType" @change="generateDsl">
            <a-select-option value="unique">唯一性检查</a-select-option>
            <a-select-option value="compare">范围检查</a-select-option>
            <a-select-option value="in">枚举检查</a-select-option>
            <a-select-option value="match">正则匹配</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="ruleForm.ruleType === 'compare'" label="比较条件">
          <a-input-group compact>
            <a-select v-model:value="compareOperator" style="width: 80px">
              <a-select-option value=">">></a-select-option>
              <a-select-option value="<"><</a-select-option>
              <a-select-option value=">=">=</a-select-option>
              <a-select-option value="<="><=</a-select-option>
              <a-select-option value="=">=</a-select-option>
            </a-select>
            <a-input
              v-model:value="compareValue"
              style="width: calc(100% - 80px)"
              placeholder="比较值"
              @change="generateDsl"
            />
          </a-input-group>
        </a-form-item>
        <a-form-item v-if="ruleForm.ruleType === 'in'" label="允许值列表">
          <a-input
            v-model:value="inValues"
            placeholder="用逗号分隔，例如: 黄色, 黑色, 白色"
            @change="generateDsl"
          />
        </a-form-item>
        <a-form-item v-if="ruleForm.ruleType === 'match'" label="正则表达式">
          <a-input
            v-model:value="matchPattern"
            placeholder="例如: ^CAT\\d+$"
            @change="generateDsl"
          />
        </a-form-item>
        <a-form-item label="严重程度">
          <a-radio-group v-model:value="ruleForm.severity">
            <a-radio value="ERROR">错误</a-radio>
            <a-radio value="WARNING">警告</a-radio>
            <a-radio value="INFO">提示</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="规则DSL" required>
          <a-textarea
            v-model:value="ruleForm.dslContent"
            placeholder="规则DSL语句"
            :rows="3"
          />
          <div style="margin-top: 8px; color: #666; font-size: 12px">
            语法示例: 变量 字段 不重复 | 变量 字段 > 值 | 变量 字段 IN (值1, 值2)
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { ruleApi } from '@/api'

const loading = ref(false)
const rules = ref([])
const modalVisible = ref(false)
const editingRule = ref(null)

const ruleForm = ref({
  name: '',
  description: '',
  targetVariable: '',
  targetField: '',
  ruleType: 'unique',
  dslContent: '',
  severity: 'ERROR'
})

const compareOperator = ref('>')
const compareValue = ref('')
const inValues = ref('')
const matchPattern = ref('')

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
  { title: '规则名称', dataIndex: 'name', key: 'name' },
  { title: 'DSL内容', dataIndex: 'dslContent', key: 'dslContent', ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '严重程度', key: 'severity', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

onMounted(() => {
  loadRules()
})

// 监听表单变化生成DSL
watch([
  () => ruleForm.value.targetVariable,
  () => ruleForm.value.targetField,
  () => ruleForm.value.ruleType
], () => {
  generateDsl()
})

async function loadRules() {
  loading.value = true
  try {
    const res = await ruleApi.list()
    if (res.code === 200) {
      rules.value = res.data
    }
  } catch (error) {
    message.error('加载规则失败')
  } finally {
    loading.value = false
  }
}

function showCreateModal() {
  editingRule.value = null
  ruleForm.value = {
    name: '',
    description: '',
    targetVariable: '',
    targetField: '',
    ruleType: 'unique',
    dslContent: '',
    severity: 'ERROR'
  }
  compareOperator.value = '>'
  compareValue.value = ''
  inValues.value = ''
  matchPattern.value = ''
  modalVisible.value = true
}

function editRule(record) {
  editingRule.value = record
  ruleForm.value = { ...record }
  modalVisible.value = true
}

function generateDsl() {
  const { targetVariable, targetField, ruleType } = ruleForm.value
  if (!targetVariable || !targetField) return

  let dsl = ''
  switch (ruleType) {
    case 'unique':
      dsl = `${targetVariable} ${targetField} 不重复`
      break
    case 'compare':
      if (compareValue.value) {
        dsl = `${targetVariable} ${targetField} ${compareOperator.value} ${compareValue.value}`
      }
      break
    case 'in':
      if (inValues.value) {
        dsl = `${targetVariable} ${targetField} IN (${inValues.value})`
      }
      break
    case 'match':
      if (matchPattern.value) {
        dsl = `${targetVariable} ${targetField} MATCH ${matchPattern.value}`
      }
      break
  }
  ruleForm.value.dslContent = dsl
}

async function saveRule() {
  if (!ruleForm.value.name || !ruleForm.value.dslContent) {
    message.warning('请填写规则名称和DSL内容')
    return
  }

  try {
    if (editingRule.value) {
      await ruleApi.update(editingRule.value.id, ruleForm.value)
      message.success('规则更新成功')
    } else {
      await ruleApi.create(ruleForm.value)
      message.success('规则创建成功')
    }
    modalVisible.value = false
    loadRules()
  } catch (error) {
    message.error('保存失败')
  }
}

async function publishRule(record) {
  try {
    await ruleApi.publish(record.id)
    message.success('规则发布成功')
    loadRules()
  } catch (error) {
    message.error('发布失败')
  }
}

async function deleteRule(record) {
  try {
    await ruleApi.delete(record.id)
    message.success('规则删除成功')
    loadRules()
  } catch (error) {
    message.error('删除失败')
  }
}

function getStatusColor(status) {
  switch (status) {
    case 'PUBLISHED': return 'green'
    case 'DRAFT': return 'orange'
    case 'DISABLED': return 'default'
    default: return 'default'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'DISABLED': return '已禁用'
    default: return status
  }
}

function getSeverityColor(severity) {
  switch (severity) {
    case 'ERROR': return 'red'
    case 'WARNING': return 'orange'
    case 'INFO': return 'blue'
    default: return 'default'
  }
}
</script>

<style scoped>
.rules-view {
  height: 100%;
}
</style>
