<script setup lang="ts">
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import { WebLinksAddon } from '@xterm/addon-web-links'
import '@xterm/xterm/css/xterm.css'

const props = defineProps<{
  options?: any
  theme?: any
  fontSize?: number
}>()

const emit = defineEmits<{
  (e: 'data', data: string): void
  (e: 'resize', cols: number, rows: number): void
}>()

const terminalContainer = ref<HTMLElement | null>(null)
let terminal: Terminal | null = null
let fitAddon: FitAddon | null = null

// Initialize terminal
onMounted(() => {
  if (!terminalContainer.value) return

  terminal = new Terminal({
    cursorBlink: true,
    fontSize: props.fontSize || 10,
    fontFamily: 'Menlo, Monaco, "Courier New", monospace',
    theme: props.theme || {
      background: '#000000',
      foreground: '#ffffff',
    },
    ...props.options
  })

  fitAddon = new FitAddon()
  terminal.loadAddon(fitAddon)
  terminal.loadAddon(new WebLinksAddon())

  terminal.open(terminalContainer.value)
  fitAddon.fit()

  // Handle input
  terminal.onData((data) => {
    emit('data', data)
  })

  // Handle resize
  window.addEventListener('resize', handleResize)
  
  // Initial resize
  setTimeout(() => {
    handleResize()
  }, 100)
})

// Watch for prop changes
watch(() => props.theme, (newTheme) => {
  if (terminal && newTheme) {
    terminal.options.theme = newTheme
  }
}, { deep: true })

watch(() => props.fontSize, (newSize) => {
  if (terminal && newSize) {
    terminal.options.fontSize = newSize
    fitAddon?.fit()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  terminal?.dispose()
})

const handleResize = () => {
  if (fitAddon && terminal) {
    fitAddon.fit()
    emit('resize', terminal.cols, terminal.rows)
  }
}

// Expose methods
const write = (data: string) => {
  terminal?.write(data)
}

const clear = () => {
  terminal?.clear()
}

const focus = () => {
  terminal?.focus()
}

defineExpose({
  write,
  clear,
  focus,
  handleResize
})
</script>

<template>
  <div ref="terminalContainer" class="w-full h-full overflow-hidden bg-black" @click="focus"></div>
</template>

<style>
.xterm-viewport {
  overflow-y: auto !important;
}
</style>
 