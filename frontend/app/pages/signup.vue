<script setup lang="ts">
import { Page, Block } from 'konsta/vue'

const auth = useAuth()
const router = useRouter()

const email = ref('')
const password = ref('')
const fullName = ref('')
const error = ref('')
const loading = ref(false)

const handleSignup = async () => {
  error.value = ''
  loading.value = true
  try {
    await auth.signup(email.value, password.value, fullName.value)
    router.push('/profile')
  } catch (err: any) {
    error.value = err.data?.message || 'Signup failed. Please try again.'
  } finally {
    loading.value = false
  }
}

definePageMeta({
  layout: false,
})
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen px-6 py-8 flex items-center justify-center">
      <div class="max-w-md w-full">
        <!-- Header -->
        <Block class="text-center mb-8">
          <div class="w-16 h-16 mx-auto mb-4 neomorph flex items-center justify-center">
            <Icon name="lucide:user-plus" class="w-8 h-8 text-blue-400" />
          </div>
          <h1 class="text-3xl font-bold text-gray-100 mb-2">Create Account</h1>
          <p class="text-gray-400">Join us today</p>
        </Block>

        <!-- Error Message -->
        <Block v-if="error" class="mb-4">
          <div class="neomorph-pressed p-4 rounded-xl flex items-center space-x-3">
            <Icon name="lucide:alert-circle" class="w-5 h-5 text-red-600 flex-shrink-0" />
            <p class="text-sm text-red-600">{{ error }}</p>
          </div>
        </Block>

        <!-- Signup Form -->
        <Card>
          <form @submit.prevent="handleSignup" class="p-6 space-y-6">
            <div class="space-y-4">
              <div class="neomorph-pressed rounded-xl p-4">
                <input
                  v-model="fullName"
                  type="text"
                  placeholder="Full Name"
                  required
                  class="w-full bg-transparent outline-none text-gray-200 placeholder-gray-500"
                />
              </div>
              
              <div class="neomorph-pressed rounded-xl p-4">
                <input
                  v-model="email"
                  type="email"
                  placeholder="Email"
                  required
                  class="w-full bg-transparent outline-none text-gray-200 placeholder-gray-500"
                />
              </div>
              
              <div class="neomorph-pressed rounded-xl p-4">
                <input
                  v-model="password"
                  type="password"
                  placeholder="Password"
                  required
                  class="w-full bg-transparent outline-none text-gray-200 placeholder-gray-500"
                />
              </div>
            </div>
            
            <button
              type="submit"
              class="neomorph-btn w-full py-3 px-6 rounded-xl font-medium text-gray-200"
              :disabled="loading"
            >
              {{ loading ? 'Creating account...' : 'Create Account' }}
            </button>

            <div class="text-center">
              <button
                type="button"
                @click="router.push('/login')"
                class="text-sm text-gray-400 hover:text-gray-200"
              >
                Already have an account? <span class="font-semibold">Sign in</span>
              </button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  </Page>
</template>
