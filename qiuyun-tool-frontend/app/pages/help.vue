<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-6">
          <HelpCircle class="w-8 h-8 text-primary" />
        </div>
        <h1 class="text-3xl sm:text-4xl font-bold mb-4">帮助中心</h1>
        <p class="text-muted-foreground">常见问题解答与使用指南</p>
      </div>

      <div class="mb-8">
        <div class="relative max-w-xl mx-auto">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <Input
            v-model="searchQuery"
            placeholder="搜索常见问题..."
            class="pl-10"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-12">
        <Card
          v-for="category in categories"
          :key="category.id"
          class="cursor-pointer transition-all hover:border-primary/50"
          :class="{ 'border-primary bg-primary/5': selectedCategory === category.id }"
          @click="selectedCategory = selectedCategory === category.id ? null : category.id"
        >
          <CardContent class="p-6 text-center">
            <component :is="category.icon" class="w-8 h-8 mx-auto mb-3 text-primary" />
            <h3 class="font-semibold mb-1">{{ category.name }}</h3>
            <p class="text-sm text-muted-foreground">{{ category.count }} 个问题</p>
          </CardContent>
        </Card>
      </div>

      <div class="space-y-4">
        <Accordion type="single" collapsible class="w-full">
          <AccordionItem
            v-for="faq in filteredFaqs"
            :key="faq.id"
            :value="faq.id"
          >
            <AccordionTrigger class="text-left hover:no-underline">
              <div class="flex items-center gap-3">
                <component :is="faq.icon" class="w-5 h-5 text-primary shrink-0" />
                <span class="font-medium">{{ faq.question }}</span>
              </div>
            </AccordionTrigger>
            <AccordionContent>
              <div class="pl-8 text-muted-foreground leading-relaxed">
                {{ faq.answer }}
              </div>
            </AccordionContent>
          </AccordionItem>
        </Accordion>

        <div v-if="filteredFaqs.length === 0" class="text-center py-12">
          <SearchX class="w-12 h-12 mx-auto text-muted-foreground mb-4" />
          <p class="text-muted-foreground">没有找到相关问题</p>
          <p class="text-sm text-muted-foreground mt-2">请尝试其他关键词或联系客服</p>
        </div>
      </div>

      <div class="mt-16 pt-8 border-t border-border/40">
        <div class="bg-linear-to-r from-primary/5 to-primary/10 rounded-2xl p-8 text-center">
          <MessageCircleQuestion class="w-12 h-12 mx-auto text-primary mb-4" />
          <h2 class="text-xl font-bold mb-2">还有其他问题？</h2>
          <p class="text-muted-foreground mb-6">如果您没有找到需要的答案，欢迎联系我们</p>
          <div class="flex flex-col sm:flex-row justify-center gap-4">
            <NuxtLink to="/contact">
              <Button>
                <MessageCircle class="w-4 h-4 mr-2" />
                联系我们
              </Button>
            </NuxtLink>
            <NuxtLink to="/feedback">
              <Button variant="outline">
                <Send class="w-4 h-4 mr-2" />
                提交反馈
              </Button>
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  HelpCircle,
  Search,
  User,
  CreditCard,
  Wrench,
  Shield,
  FileQuestion,
  SearchX,
  MessageCircleQuestion,
  MessageCircle,
  Send,
  Key,
  RefreshCw,
  Trash2,
  Download,
  Lock,
  Eye,
  Zap,
  Clock,
  AlertCircle
} from 'lucide-vue-next'

useHead({
  title: '帮助中心 - 秋云工具',
  meta: [
    { name: 'description', content: '秋云工具帮助中心，提供常见问题解答和使用指南。' }
  ]
})

const searchQuery = ref('')
const selectedCategory = ref<string | null>(null)

const categories = [
  { id: 'account', name: '账户相关', icon: User, count: 4 },
  { id: 'payment', name: '付费订阅', icon: CreditCard, count: 3 },
  { id: 'tools', name: '工具使用', icon: Wrench, count: 5 },
  { id: 'security', name: '安全隐私', icon: Shield, count: 3 }
]

const faqs = [
  {
    id: '1',
    category: 'account',
    icon: Key,
    question: '如何注册秋云工具账户？',
    answer: '点击网站右上角的"注册"按钮，输入您的邮箱地址、设置密码，然后点击"注册"即可。您也可以使用第三方账号（如GitHub、Google）快速登录。'
  },
  {
    id: '2',
    category: 'account',
    icon: RefreshCw,
    question: '忘记密码怎么办？',
    answer: '在登录页面点击"忘记密码"链接，输入您的注册邮箱，我们会发送密码重置链接到您的邮箱。点击链接即可设置新密码。'
  },
  {
    id: '3',
    category: 'account',
    icon: Trash2,
    question: '如何注销账户？',
    answer: '如需注销账户，请登录后进入"个人中心"-"账户设置"-"注销账户"。请注意，注销后您的所有数据将被永久删除，无法恢复。'
  },
  {
    id: '4',
    category: 'account',
    icon: Eye,
    question: '如何修改个人信息？',
    answer: '登录后点击右上角头像，选择"个人中心"，在"基本信息"页面可以修改您的昵称、头像、邮箱等信息。'
  },
  {
    id: '5',
    category: 'payment',
    icon: CreditCard,
    question: '支持哪些支付方式？',
    answer: '我们支持支付宝、微信支付、银行卡等多种支付方式。所有支付均通过安全的第三方支付平台处理，保障您的资金安全。'
  },
  {
    id: '6',
    category: 'payment',
    icon: Zap,
    question: '如何升级到专业版？',
    answer: '点击网站顶部的"定价"页面，选择适合您的套餐，点击"立即升级"按钮，按照提示完成支付即可立即享受专业版功能。'
  },
  {
    id: '7',
    category: 'payment',
    icon: RefreshCw,
    question: '如何取消订阅？',
    answer: '登录后进入"个人中心"-"订阅管理"，点击"取消订阅"即可。取消后，您仍可使用专业版功能直到当前订阅周期结束。'
  },
  {
    id: '8',
    category: 'tools',
    icon: FileQuestion,
    question: '工具使用有次数限制吗？',
    answer: '免费版用户有每日使用次数限制，专业版用户享有无限次使用权限。具体限制请查看"定价"页面的详细对比。'
  },
  {
    id: '9',
    category: 'tools',
    icon: Download,
    question: '如何保存工具处理结果？',
    answer: '大多数工具都提供"复制"和"下载"功能。点击相应按钮即可将结果保存到剪贴板或下载为文件。专业版用户还可以保存历史记录。'
  },
  {
    id: '10',
    category: 'tools',
    icon: Clock,
    question: '处理大量数据时超时怎么办？',
    answer: '如果遇到超时，建议将数据分批处理。对于超大文件，专业版用户享有更长的处理时间和更大的文件大小限制。'
  },
  {
    id: '11',
    category: 'tools',
    icon: Wrench,
    question: '如何收藏常用工具？',
    answer: '登录后，在工具页面点击"收藏"按钮即可将工具添加到您的收藏列表。收藏的工具可以在"我的收藏"中快速访问。'
  },
  {
    id: '12',
    category: 'tools',
    icon: AlertCircle,
    question: '工具处理出错怎么办？',
    answer: '请检查输入数据格式是否正确。如果问题持续存在，请通过"问题反馈"页面提交错误信息，我们会尽快修复。'
  },
  {
    id: '13',
    category: 'security',
    icon: Lock,
    question: '我的数据安全吗？',
    answer: '我们非常重视数据安全。所有数据传输均采用SSL加密，我们不会存储您上传的敏感数据，处理完成后会立即删除。详细安全政策请查看"隐私政策"页面。'
  },
  {
    id: '14',
    category: 'security',
    icon: Shield,
    question: '如何保护我的账户安全？',
    answer: '建议您：1）使用强密码并定期更换；2）开启两步验证；3）不要在公共设备上保存登录状态；4）定期检查账户登录历史。'
  },
  {
    id: '15',
    category: 'security',
    icon: Eye,
    question: '你们会收集哪些个人信息？',
    answer: '我们仅收集提供服务所必需的信息，包括邮箱、使用记录等。我们不会出售您的个人信息给第三方。详情请查看"隐私政策"。'
  }
]

const filteredFaqs = computed(() => {
  let result = faqs

  if (selectedCategory.value) {
    result = result.filter(faq => faq.category === selectedCategory.value)
  }

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(faq =>
      faq.question.toLowerCase().includes(query) ||
      faq.answer.toLowerCase().includes(query)
    )
  }

  return result
})
</script>