<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { 
  Check, 
  Crown, 
  Zap, 
  Star,
  Shield,
  Clock,
  HelpCircle,
  ChevronRight
} from 'lucide-vue-next'
import { membershipPlans } from '@/composables/useTools'

const faqs = [
  {
    question: '会员权益包括哪些内容？',
    answer: 'VIP会员可以使用所有高级工具，享受更大的文件上传限制，去除广告，获得优先客服支持等权益。'
  },
  {
    question: '如何取消自动续费？',
    answer: '您可以在个人中心的"会员中心"页面随时取消自动续费，取消后将在当前周期结束后失效。'
  },
  {
    question: '支持哪些支付方式？',
    answer: '我们支持支付宝、微信支付等多种支付方式，确保您的支付安全便捷。'
  },
  {
    question: '购买后可以退款吗？',
    answer: '购买后7天内，如未使用VIP权益，可申请全额退款。具体请参考我们的退款政策。'
  },
]

const comparisons = [
  { feature: '工具使用', free: '部分免费工具', monthly: '全部工具', quarterly: '全部工具', yearly: '全部工具', permanent: '全部工具' },
  { feature: '单文件大小', free: '1MB', monthly: '10MB', quarterly: '10MB', yearly: '50MB', permanent: '100MB' },
  { feature: '每日使用次数', free: '10次', monthly: '无限', quarterly: '无限', yearly: '无限', permanent: '无限' },
  { feature: '广告显示', free: '有', monthly: '无', quarterly: '无', yearly: '无', permanent: '无' },
  { feature: '云存储空间', free: '-', monthly: '-', quarterly: '-', yearly: '5GB', permanent: '10GB' },
  { feature: '客服支持', free: '普通', monthly: '优先', quarterly: '优先', yearly: '优先', permanent: 'VIP专属' },
  { feature: '新功能体验', free: '-', monthly: '-', quarterly: '✓', yearly: '✓', permanent: '✓' },
]
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Header -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-12 text-center">
        <Badge variant="secondary" class="mb-4">
          <Crown class="w-3 h-3 mr-1" />
          会员套餐
        </Badge>
        <h1 class="text-4xl font-bold text-foreground mb-4">
          选择适合您的方案
        </h1>
        <p class="text-lg text-muted-foreground max-w-2xl mx-auto">
          成为VIP会员，解锁全部高级功能，享受更好的使用体验
        </p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-16">
      <!-- Pricing Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-20">
        <Card 
          v-for="plan in membershipPlans" 
          :key="plan.id"
          :class="[
            'relative flex flex-col',
            plan.popular ? 'border-primary shadow-lg scale-105' : ''
          ]"
        >
          <div v-if="plan.popular" class="absolute -top-3 left-1/2 -translate-x-1/2">
            <Badge class="bg-primary text-primary-foreground">
              <Star class="w-3 h-3 mr-1" />
              最受欢迎
            </Badge>
          </div>
          
          <CardHeader>
            <CardTitle class="text-lg">{{ plan.name }}</CardTitle>
            <CardDescription>
              <div class="flex items-baseline gap-1 mt-2">
                <span class="text-3xl font-bold text-foreground">¥{{ plan.price }}</span>
                <span class="text-muted-foreground">/{{ plan.period }}</span>
              </div>
            </CardDescription>
          </CardHeader>
          
          <CardContent class="flex-1">
            <ul class="space-y-3">
              <li v-for="feature in plan.features" :key="feature" class="flex items-start gap-2 text-sm">
                <Check class="w-4 h-4 text-green-500 mt-0.5 shrink-0" />
                <span class="text-muted-foreground">{{ feature }}</span>
              </li>
            </ul>
          </CardContent>
          
          <CardFooter>
            <Button 
              class="w-full" 
              :variant="plan.popular ? 'default' : 'outline'"
            >
              立即购买
            </Button>
          </CardFooter>
        </Card>
      </div>

      <!-- Comparison Table -->
      <div class="mb-20">
        <h2 class="text-2xl font-bold text-foreground text-center mb-8">功能对比</h2>
        <div class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="border-b border-border">
                <th class="text-left py-4 px-4 font-semibold">功能</th>
                <th class="text-center py-4 px-4 text-muted-foreground">免费版</th>
                <th class="text-center py-4 px-4 text-primary font-semibold">包月</th>
                <th class="text-center py-4 px-4 text-primary font-semibold">包季</th>
                <th class="text-center py-4 px-4 text-primary font-semibold bg-primary/5">包年</th>
                <th class="text-center py-4 px-4 text-primary font-semibold">永久</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="(row, index) in comparisons" 
                :key="row.feature"
                :class="index % 2 === 0 ? 'bg-muted/30' : ''"
              >
                <td class="py-4 px-4 font-medium">{{ row.feature }}</td>
                <td class="text-center py-4 px-4 text-muted-foreground">{{ row.free }}</td>
                <td class="text-center py-4 px-4">{{ row.monthly }}</td>
                <td class="text-center py-4 px-4">{{ row.quarterly }}</td>
                <td class="text-center py-4 px-4 bg-primary/5 font-medium">{{ row.yearly }}</td>
                <td class="text-center py-4 px-4">{{ row.permanent }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- FAQ -->
      <div class="max-w-3xl mx-auto">
        <h2 class="text-2xl font-bold text-foreground text-center mb-8">常见问题</h2>
        <div class="space-y-4">
          <div 
            v-for="faq in faqs" 
            :key="faq.question"
            class="border border-border/40 rounded-lg p-6"
          >
            <h3 class="font-semibold text-foreground mb-2 flex items-center gap-2">
              <HelpCircle class="w-5 h-5 text-primary" />
              {{ faq.question }}
            </h3>
            <p class="text-muted-foreground">{{ faq.answer }}</p>
          </div>
        </div>
      </div>

      <!-- CTA -->
      <div class="mt-20 text-center">
        <div class="bg-linear-to-r from-primary/10 to-primary/5 rounded-2xl p-8 lg:p-12">
          <h2 class="text-2xl font-bold text-foreground mb-4">
            还有疑问？
          </h2>
          <p class="text-muted-foreground mb-6">
            联系我们的客服团队，获取更多信息
          </p>
          <Button variant="outline" as-child>
            <NuxtLink to="/contact">
              联系我们
              <ChevronRight class="w-4 h-4 ml-1" />
            </NuxtLink>
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
