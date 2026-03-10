<template>
  <div class="flex gap-1" :class="{ 'cursor-default': readonly }">
    <div
      v-for="star in 5"
      :key="star"
      class="transition-all duration-200"
      :class="[
        sizeClasses[size],
        readonly ? 'cursor-default' : 'cursor-pointer hover:scale-110',
        (star <= Math.floor(displayRating) || (star === Math.ceil(displayRating) && displayRating % 1 !== 0))
          ? 'text-amber-500'
          : 'text-muted'
      ]"
      @click="onStarClick(star)"
      @mouseenter="onStarHover(star)"
      @mouseleave="onStarLeave"
    >
      <svg viewBox="0 0 24 24" fill="currentColor" class="w-full h-full">
        <defs>
          <linearGradient :id="`half-${star}`" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="50%" stop-color="currentColor" />
            <stop offset="50%" stop-color="#e5e7eb" />
          </linearGradient>
        </defs>
        <path
          d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
          :fill="star === Math.ceil(displayRating) && displayRating % 1 !== 0 ? `url(#half-${star})` : 'currentColor'"
        />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  rating: number
  readonly?: boolean
  size?: 'small' | 'medium' | 'large'
}

const props = withDefaults(defineProps<Props>(), {
  rating: 0,
  readonly: false,
  size: 'medium'
})

const emit = defineEmits<{
  change: [rating: number]
}>()

const hoverRating = ref(0)

const sizeClasses = {
  small: 'w-4 h-4',
  medium: 'w-5 h-5',
  large: 'w-6 h-6'
}

const displayRating = computed(() => {
  if (props.readonly) {
    return props.rating
  }
  return hoverRating.value || props.rating
})

const onStarClick = (star: number) => {
  if (props.readonly) return
  emit('change', star)
}

const onStarHover = (star: number) => {
  if (props.readonly) return
  hoverRating.value = star
}

const onStarLeave = () => {
  if (props.readonly) return
  hoverRating.value = 0
}
</script>
