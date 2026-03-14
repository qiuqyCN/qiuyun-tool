<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'outline'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  type?: 'button' | 'submit'
}

withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  type: 'button'
})

const emit = defineEmits<{
  (e: 'click'): void
}>()
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    @click="emit('click')"
    class="inline-flex items-center justify-center font-medium rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
    :class="{
      // Variants
      'bg-linear-to-r from-rose-500 to-pink-600 text-white hover:from-rose-600 hover:to-pink-700': variant === 'primary',
      'bg-gray-100 text-gray-700 hover:bg-gray-200': variant === 'secondary',
      'border-2 border-rose-500 text-rose-600 hover:bg-rose-50': variant === 'outline',
      // Sizes
      'px-4 py-2 text-sm': size === 'sm',
      'px-6 py-3 text-base': size === 'md',
      'px-8 py-4 text-lg': size === 'lg'
    }"
  >
    <slot />
  </button>
</template>
