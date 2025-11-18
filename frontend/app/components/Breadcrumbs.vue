<script setup lang="ts">
const router = useRouter()
const route = useRoute()

const breadcrumbs = computed(() => {
  const paths = route.path.split('/').filter(p => p)
  const crumbs = [{ name: 'Home', path: '/' }]
  
  paths.forEach((path, index) => {
    const fullPath = '/' + paths.slice(0, index + 1).join('/')
    const name = path.charAt(0).toUpperCase() + path.slice(1)
    crumbs.push({ name, path: fullPath })
  })
  
  return crumbs
})
</script>

<template>
  <div class="flex items-center space-x-2 px-6 py-4 text-sm">
    <template v-for="(crumb, index) in breadcrumbs" :key="crumb.path">
      <button
        v-if="index < breadcrumbs.length - 1"
        @click="router.push(crumb.path)"
        class="text-gray-400 hover:text-gray-200 transition-colors"
      >
        {{ crumb.name }}
      </button>
      <span v-else class="text-gray-100 font-medium">
        {{ crumb.name }}
      </span>
      <Icon
        v-if="index < breadcrumbs.length - 1"
        name="lucide:chevron-right"
        class="w-4 h-4 text-gray-600"
      />
    </template>
  </div>
</template>

