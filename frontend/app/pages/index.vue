<script setup lang="ts">
import { Page, Block, Button } from 'konsta/vue'

const { user } = useAuth()
const router = useRouter()
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen px-6 py-8 flex flex-col">
      <!-- Hero Section -->
      <Block class="text-center mb-8">
        <div class="w-20 h-20 mx-auto mb-6 neomorph flex items-center justify-center">
          <Icon name="lucide:smartphone" class="w-10 h-10 text-blue-400" />
        </div>
        <h1 class="text-4xl font-bold mb-3 text-gray-100">
          Mobile Template
        </h1>
        <p class="text-gray-400 text-lg">
          Modern • Minimal • Neumorphic
        </p>
      </Block>

      <!-- User Card -->
      <div v-if="user" class="max-w-md mx-auto w-full space-y-4">
        <Card>
          <div class="p-8 text-center">
            <div class="w-16 h-16 mx-auto mb-4 neomorph-pressed flex items-center justify-center">
              <span class="text-2xl font-bold text-blue-400">
                {{ (user.fullName || user.email).charAt(0).toUpperCase() }}
              </span>
            </div>
            <h3 class="text-xl font-semibold mb-1 text-gray-100">{{ user.fullName || 'Welcome' }}</h3>
            <p class="text-sm text-gray-400 mb-6">{{ user.email }}</p>
            <button class="neomorph-btn w-full py-3 px-6 rounded-xl font-medium text-gray-200" @click="router.push('/profile')">
              View Profile
            </button>
          </div>
        </Card>
      </div>

      <!-- Guest CTA -->
      <div v-else class="max-w-md mx-auto w-full space-y-4">
        <Card>
          <div class="p-8 text-center space-y-6">
            <div>
              <h3 class="text-2xl font-bold mb-2 text-gray-100">Get Started</h3>
              <p class="text-gray-400">
                Create an account to continue
              </p>
            </div>
            
            <div class="space-y-3">
              <button class="neomorph-btn w-full py-3 px-6 rounded-xl font-medium text-gray-200" @click="router.push('/signup')">
                Create Account
              </button>
              <button class="neomorph-pressed w-full py-3 px-6 rounded-xl font-medium text-gray-300" @click="router.push('/login')">
                Sign In
              </button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  </Page>
</template>
