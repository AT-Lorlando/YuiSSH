<script setup lang="ts">
import { Page, Block, List, ListItem } from 'konsta/vue'

const { user, fetchProfile, logout } = useAuth()
const router = useRouter()

const loading = ref(true)

onMounted(async () => {
  if (!user.value) {
    try {
      await fetchProfile()
    } catch (err) {
      router.push('/login')
      return
    }
  }
  loading.value = false
})

const handleLogout = async () => {
  await logout()
  router.push('/login')
}
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen px-6 py-8">
      <!-- Loading -->
      <Block v-if="loading" class="text-center py-16">
        <Icon name="lucide:loader-2" class="w-12 h-12 mx-auto mb-4 animate-spin text-blue-400" />
        <p class="text-gray-400">Loading...</p>
      </Block>
      
      <!-- Profile Content -->
      <div v-else-if="user" class="max-w-md mx-auto w-full space-y-4">
        <!-- User Card -->
        <Card>
          <div class="p-8 text-center">
            <div class="w-20 h-20 mx-auto mb-4 neomorph-pressed flex items-center justify-center">
              <span class="text-3xl font-bold text-blue-400">
                {{ (user.fullName || user.email).charAt(0).toUpperCase() }}
              </span>
            </div>
            <h2 class="text-2xl font-bold mb-1 text-gray-100">{{ user.fullName || 'User' }}</h2>
            <p class="text-sm text-gray-400 mb-6">{{ user.email }}</p>
            <div class="text-xs text-gray-500">ID: #{{ user.id }}</div>
          </div>
        </Card>

        <!-- Actions -->
        <Card>
          <div class="p-4 space-y-3">
            <button class="neomorph-btn w-full py-3 px-6 rounded-xl font-medium text-gray-200 flex items-center justify-between" @click="() => {}">
              <span class="flex items-center">
                <Icon name="lucide:edit" class="w-5 h-5 mr-3" />
                Edit Profile
              </span>
              <Icon name="lucide:chevron-right" class="w-5 h-5" />
            </button>
            
            <button class="neomorph-btn w-full py-3 px-6 rounded-xl font-medium text-gray-200 flex items-center justify-between" @click="() => {}">
              <span class="flex items-center">
                <Icon name="lucide:settings" class="w-5 h-5 mr-3" />
                Settings
              </span>
              <Icon name="lucide:chevron-right" class="w-5 h-5" />
            </button>
          </div>
        </Card>

        <!-- Logout -->
        <Block class="space-y-3 pt-4">
          <button class="neomorph-pressed w-full py-3 px-6 rounded-xl font-medium text-gray-300" @click="router.push('/')">
            Back to Home
          </button>
          
          <button class="neomorph-pressed w-full py-3 px-6 rounded-xl font-medium text-red-400" @click="handleLogout">
            Sign Out
          </button>
        </Block>
      </div>
    </div>
  </Page>
</template>
