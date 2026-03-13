<script setup lang="ts">
interface Props {
  class?: string
}

defineProps<Props>()
</script>

<template>
  <div
    class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
    :class="$props.class"
  >
    <slot />
  </div>
</template>
