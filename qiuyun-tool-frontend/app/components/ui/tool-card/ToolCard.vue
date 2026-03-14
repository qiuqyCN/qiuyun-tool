<script setup lang="ts">
interface Props {
  class?: string
}

defineProps<Props>()
</script>

<template>
  <div
    class="bg-card rounded-2xl shadow-sm border border-border p-6"
    :class="$props.class"
  >
    <slot />
  </div>
</template>
