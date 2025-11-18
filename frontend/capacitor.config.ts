import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.example.mobiletemplate',
  appName: 'MobileTemplate',
  webDir: '.output/public',
  bundledWebRuntime: false,
  server: {
    // Uncomment and update with your local IP for live reload during development
    // url: 'http://192.168.x.xx:3000',
    // cleartext: true
  }
}

export default config

