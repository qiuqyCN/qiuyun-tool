<template>
  <div class="space-y-6">
    <div v-for="field in fields" :key="field.name" class="space-y-2">
      <!-- 字段标签 -->
      <div class="flex items-center justify-between">
        <Label :for="field.name" class="text-base font-medium">
          {{ field.label }}
          <span v-if="field.required" class="text-red-500">*</span>
        </Label>
        <Tooltip v-if="field.description" :content="field.description">
          <Icon name="lucide:help-circle" class="w-4 h-4 text-gray-400 cursor-help" />
        </Tooltip>
      </div>

      <!-- 文本输入 -->
      <template v-if="field.type === 'text'">
        <Input
          :id="field.name"
          v-model="formData[field.name]"
          :placeholder="field.placeholder"
          :required="field.required"
        />
      </template>

      <!-- 多行文本 -->
      <template v-else-if="field.type === 'textarea'">
        <Textarea
          :id="field.name"
          v-model="formData[field.name]"
          :placeholder="field.placeholder"
          :rows="field.rows || 4"
          :required="field.required"
        />
      </template>

      <!-- 数字输入 -->
      <template v-else-if="field.type === 'number'">
        <div class="flex items-center gap-2">
          <Input
            :id="field.name"
            v-model.number="formData[field.name]"
            type="number"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :placeholder="field.placeholder"
            :required="field.required"
          />
          <span v-if="field.unit" class="text-sm text-gray-500 whitespace-nowrap">{{ field.unit }}</span>
        </div>
      </template>

      <!-- 滑块 -->
      <template v-else-if="field.type === 'slider'">
        <div class="space-y-2">
          <Slider
            v-model="formData[field.name]"
            :min="field.min || 0"
            :max="field.max || 100"
            :step="field.step || 1"
          />
          <div class="flex justify-between text-sm text-gray-500">
            <span>{{ field.min || 0 }}{{ field.unit }}</span>
            <span class="font-medium text-gray-900">{{ formData[field.name] }}{{ field.unit }}</span>
            <span>{{ field.max || 100 }}{{ field.unit }}</span>
          </div>
        </div>
      </template>

      <!-- 选择器 -->
      <template v-else-if="field.type === 'select'">
        <Select v-model="formData[field.name]">
          <SelectTrigger>
            <SelectValue :placeholder="field.placeholder || '请选择'" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem
              v-for="option in field.options"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </Select>
      </template>

      <!-- 单选框组 -->
      <template v-else-if="field.type === 'radio'">
        <RadioGroup v-model="formData[field.name]" class="flex flex-wrap gap-4">
          <div
            v-for="option in field.options"
            :key="option.value"
            class="flex items-center space-x-2"
          >
            <RadioGroupItem :id="`${field.name}-${option.value}`" :value="option.value" />
            <Label :for="`${field.name}-${option.value}`" class="cursor-pointer">
              {{ option.label }}
            </Label>
          </div>
        </RadioGroup>
      </template>

      <!-- 复选框 -->
      <template v-else-if="field.type === 'checkbox'">
        <div class="flex items-center space-x-2">
          <Checkbox
            :id="field.name"
            v-model:checked="formData[field.name]"
          />
          <Label :for="field.name" class="cursor-pointer text-sm text-gray-600">
            {{ field.checkboxLabel || field.label }}
          </Label>
        </div>
      </template>

      <!-- 复选框组 -->
      <template v-else-if="field.type === 'checkboxGroup'">
        <div class="flex flex-wrap gap-4">
          <div
            v-for="option in field.options"
            :key="option.value"
            class="flex items-center space-x-2"
          >
            <Checkbox
              :id="`${field.name}-${option.value}`"
              :checked="isChecked(field.name, option.value)"
              @update:checked="toggleCheck(field.name, option.value)"
            />
            <Label :for="`${field.name}-${option.value}`" class="cursor-pointer">
              {{ option.label }}
            </Label>
          </div>
        </div>
      </template>

      <!-- 开关 -->
      <template v-else-if="field.type === 'switch'">
        <div class="flex items-center space-x-2">
          <Switch
            :id="field.name"
            v-model:checked="formData[field.name]"
          />
          <Label :for="field.name" class="cursor-pointer text-sm text-gray-600">
            {{ field.switchLabel || (formData[field.name] ? '开启' : '关闭') }}
          </Label>
        </div>
      </template>

      <!-- 颜色选择器 -->
      <template v-else-if="field.type === 'color'">
        <div class="flex items-center gap-3">
          <input
            :id="field.name"
            v-model="formData[field.name]"
            type="color"
            class="w-12 h-10 rounded cursor-pointer border border-gray-300"
          />
          <Input
            v-model="formData[field.name]"
            class="flex-1"
            placeholder="#000000"
          />
        </div>
      </template>

      <!-- 日期选择器 -->
      <template v-else-if="field.type === 'date'">
        <Input
          :id="field.name"
          v-model="formData[field.name]"
          type="date"
          :required="field.required"
        />
      </template>

      <!-- 日期时间选择器 -->
      <template v-else-if="field.type === 'datetime'">
        <Input
          :id="field.name"
          v-model="formData[field.name]"
          type="datetime-local"
          :required="field.required"
        />
      </template>

      <!-- 字段验证错误 -->
      <p v-if="errors[field.name]" class="text-sm text-red-600">{{ errors[field.name] }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'

export interface FieldOption {
  label: string
  value: string | number
}

export interface FormField {
  name: string
  label: string
  type: 'text' | 'textarea' | 'number' | 'slider' | 'select' | 'radio' | 'checkbox' | 'checkboxGroup' | 'switch' | 'color' | 'date' | 'datetime'
  required?: boolean
  placeholder?: string
  description?: string
  defaultValue?: any
  // 文本相关
  rows?: number
  // 数字相关
  min?: number
  max?: number
  step?: number
  unit?: string
  // 选项相关
  options?: FieldOption[]
  // 复选框/开关标签
  checkboxLabel?: string
  switchLabel?: string
}

interface Props {
  fields: FormField[]
  modelValue?: Record<string, any>
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

// 表单数据
const formData = ref<Record<string, any>>({})
const errors = ref<Record<string, string>>({})

// 初始化表单数据
const initFormData = () => {
  const initialData: Record<string, any> = {}
  props.fields.forEach(field => {
    if (props.modelValue && field.name in props.modelValue) {
      initialData[field.name] = props.modelValue[field.name]
    } else if (field.defaultValue !== undefined) {
      initialData[field.name] = field.defaultValue
    } else {
      // 根据类型设置默认值
      switch (field.type) {
        case 'checkbox':
        case 'switch':
          initialData[field.name] = false
          break
        case 'checkboxGroup':
          initialData[field.name] = []
          break
        case 'number':
        case 'slider':
          initialData[field.name] = field.min || 0
          break
        default:
          initialData[field.name] = ''
      }
    }
  })
  formData.value = initialData
}

// 初始化
initFormData()

// 监听字段变化，更新父组件
watch(formData, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

// 监听父组件值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    formData.value = { ...formData.value, ...newValue }
  }
}, { deep: true })

// 监听字段定义变化
watch(() => props.fields, () => {
  initFormData()
}, { deep: true })

// 复选框组相关方法
const isChecked = (fieldName: string, value: any): boolean => {
  const values = formData.value[fieldName] || []
  return values.includes(value)
}

const toggleCheck = (fieldName: string, value: any) => {
  const values = formData.value[fieldName] || []
  const index = values.indexOf(value)
  if (index > -1) {
    values.splice(index, 1)
  } else {
    values.push(value)
  }
  formData.value[fieldName] = [...values]
}

// 验证表单
const validate = (): boolean => {
  errors.value = {}
  let isValid = true

  props.fields.forEach(field => {
    if (field.required) {
      const value = formData.value[field.name]
      const isEmpty = value === '' || value === null || value === undefined ||
        (Array.isArray(value) && value.length === 0)

      if (isEmpty) {
        errors.value[field.name] = `${field.label}不能为空`
        isValid = false
      }
    }

    // 数字范围验证
    if (field.type === 'number' || field.type === 'slider') {
      const value = formData.value[field.name]
      if (value !== '' && value !== null && value !== undefined) {
        const numValue = Number(value)
        if (field.min !== undefined && numValue < field.min) {
          errors.value[field.name] = `${field.label}不能小于 ${field.min}`
          isValid = false
        }
        if (field.max !== undefined && numValue > field.max) {
          errors.value[field.name] = `${field.label}不能大于 ${field.max}`
          isValid = false
        }
      }
    }
  })

  return isValid
}

// 获取表单数据
const getFormData = (): Record<string, any> => {
  return { ...formData.value }
}

// 重置表单
const reset = () => {
  initFormData()
  errors.value = {}
}

// 暴露方法
defineExpose({
  validate,
  getFormData,
  reset
})
</script>
