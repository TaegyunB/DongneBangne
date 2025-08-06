<template>
  <div class="article-item" :class="articleClass">
    <div v-if="imagePosition === 'top'" class="image-wrapper" :class="imageWrapperClass">
      <img :src="imageUrl" :alt="imageAlt" />
      <p class="caption">{{ caption }}</p>
    </div>
    
    <div class="text-wrapper" :class="textWrapperClass">
      <h2>{{ headline }}</h2>
      <p class="content">{{ content }}</p>
    </div>
    
    <div v-if="imagePosition === 'bottom'" class="image-wrapper" :class="imageWrapperClass">
      <img :src="imageUrl" :alt="imageAlt" />
      <p class="caption">{{ caption }}</p>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue'

const props = defineProps({
  headline: String,
  content: String,
  imageUrl: String,
  imageAlt: {
    type: String,
    default: '도전 이미지'
  },
  caption: {
    type: String,
    default: '도전 체험을 하는 모습'
  },
  // 레이아웃 옵션
  layout: {
    type: String,
    default: 'horizontal', // 'horizontal', 'vertical'
    validator: (value) => ['horizontal', 'vertical'].includes(value)
  },
  imagePosition: {
    type: String,
    default: 'right', // 'left', 'right', 'top', 'bottom'
    validator: (value) => ['left', 'right', 'top', 'bottom'].includes(value)
  },
  size: {
    type: String,
    default: 'medium', // 'small', 'medium', 'large'
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

const articleClass = computed(() => {
  const classes = []
  
  if (props.layout === 'horizontal') {
    classes.push('horizontal-layout')
    if (props.imagePosition === 'left') {
      classes.push('reverse')
    }
  } else {
    classes.push('vertical-layout')
  }
  
  classes.push(`size-${props.size}`)
  
  return classes
})

const imageWrapperClass = computed(() => {
  const classes = ['image-container']
  
  if (props.layout === 'horizontal') {
    classes.push('horizontal-image')
  } else {
    classes.push('vertical-image')
  }
  
  classes.push(`${props.size}-image`)
  
  return classes
})

const textWrapperClass = computed(() => {
  const classes = ['text-container']
  
  if (props.layout === 'horizontal') {
    classes.push('horizontal-text')
  } else {
    classes.push('vertical-text')
  }
  
  classes.push(`${props.size}-text`)
  
  return classes
})
</script>

<style scoped>
/* 기본 레이아웃 */
.article-item {
  margin-bottom: 8mm;
}

.horizontal-layout {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8mm;
}

.horizontal-layout.reverse {
  flex-direction: row-reverse;
}

.vertical-layout {
  display: block;
  text-align: center;
}

/* 이미지 스타일 */
.image-container {
  text-align: center;
}

.horizontal-image {
  width: 40%;
}

.vertical-image {
  width: 100%;
  margin-bottom: 3mm;
}

.image-container img {
  width: 100%;
  height: auto;
  object-fit: cover;
  border: 1px solid #ccc;
}

/* 크기별 이미지 높이 */
.large-image img {
  max-height: 80mm;
}

.medium-image img {
  max-height: 60mm;
}

.small-image img {
  max-height: 35mm;
}

/* 텍스트 스타일 */
.horizontal-text {
  width: 55%;
}

.vertical-text {
  width: 100%;
  text-align: left;
}

/* 크기별 텍스트 스타일 */
.size-large .text-container h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6mm;
  line-height: 1.3;
}

.size-large .content {
  font-size: 14px;
  line-height: 1.5;
}

.size-medium .text-container h2 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4mm;
  line-height: 1.3;
}

.size-medium .content {
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 4mm;
}

.size-small .text-container h2 {
  font-size: 14px;
  font-weight: bold;
  margin: 3mm 0 2mm 0;
  line-height: 1.2;
}

.size-small .content {
  font-size: 12px;
  line-height: 1.3;
}

.content {
  word-wrap: break-word;
}

.caption {
  font-size: 10px;
  color: #777;
  margin-top: 3px;
}

.size-small .caption {
  font-size: 9px;
  margin-top: 2px;
}
</style>