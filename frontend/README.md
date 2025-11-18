# Mobile Template - Frontend

A mobile-first application template built with Nuxt 4, Capacitor, and Konsta UI, designed to work with the AdonisJS backend.

## 🚀 Features

- **Nuxt 4** - Latest version of Nuxt with enhanced performance
- **Capacitor** - Build native iOS and Android apps from your web code
- **Konsta UI** - Mobile-native UI components with iOS and Material Design themes
- **Tailwind CSS 4** - Modern utility-first CSS framework
- **Pinia** - State management
- **TypeScript** - Full type safety
- **Authentication** - Ready-to-use auth integration with backend

## 📋 Prerequisites

- Node.js >= 20.0.0
- npm or yarn
- For iOS development: macOS with Xcode
- For Android development: Android Studio

## 🛠️ Setup

### Install Dependencies

```bash
npm install
```

### Environment Configuration

Create a `.env` file based on the backend API:

```bash
NUXT_PUBLIC_API_BASE=http://localhost:3333
```

For mobile development with live reload, you'll update the Capacitor config instead (see below).

## 🔧 Development

### Web Development

Start the development server:

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### Mobile Development

#### Initial Setup

1. **Generate static build:**
   ```bash
   npm run generate
   ```

2. **Add native platforms** (first time only):
   ```bash
   npx cap add ios
   npx cap add android
   ```

   This creates `ios/` and `android/` directories with native projects.

3. **Sync web assets to native apps:**
   ```bash
   npm run mobile:sync
   ```

#### Running on Devices

**iOS:**
```bash
npm run mobile:ios
```
This opens Xcode. Select a simulator or connected device and click Run.

**Android:**
```bash
npm run mobile:android
```
This opens Android Studio. Select an emulator or connected device and click Run.

#### Live Reload During Development

For faster development, enable live reload:

1. Find your local IP address:
   - macOS/Linux: `ipconfig getifaddr en0`
   - Windows: `ipconfig`

2. Update `capacitor.config.ts`:
   ```typescript
   server: {
     url: 'http://192.168.x.xx:3000',  // Your local IP
     cleartext: true
   }
   ```

3. Run dev server:
   ```bash
   npm run dev
   ```

4. Copy config changes:
   ```bash
   npx cap copy
   ```

5. Rebuild the app in Xcode/Android Studio

Now changes to your code will hot-reload on the device!

## 📦 Building

### Web Build

```bash
npm run build
```

### Production Build

```bash
npm run preview
```

### Mobile Build

For mobile production builds:

1. Generate static files:
   ```bash
   npm run generate
   ```

2. Sync to native projects:
   ```bash
   npx cap sync
   ```

3. Open native IDE:
   ```bash
   npm run mobile:ios     # or
   npm run mobile:android
   ```

4. In Xcode or Android Studio:
   - Configure signing certificates (iOS) or signing keys (Android)
   - Build for production
   - Archive and upload to App Store / Play Store

## 🎨 UI Components

This template uses **Konsta UI v5** for mobile-native components:

- `Page` - Full page wrapper
- `Navbar` - Top navigation bar
- `Block` - Content blocks
- `List` / `ListItem` / `ListInput` - List views
- `Button` - Action buttons
- And many more!

See [Konsta UI documentation](https://konstaui.com/) for full component reference.

### Theme

The app uses iOS theme by default. To switch to Material Design theme:

In `app/app.vue`:
```vue
<App theme="material">
```

## 📁 Project Structure

```
frontend/
├── app/
│   ├── assets/
│   │   └── css/
│   │       └── tailwind.css        # Tailwind + Konsta imports
│   ├── components/
│   │   └── Navbar.vue              # Mobile navbar component
│   ├── composables/
│   │   ├── useAPI.ts               # API client
│   │   └── useAuth.ts              # Authentication helper
│   ├── layouts/
│   │   └── default.vue             # Default layout
│   ├── pages/
│   │   ├── index.vue               # Home page
│   │   ├── login.vue               # Login page
│   │   ├── signup.vue              # Signup page
│   │   └── profile.vue             # Profile page
│   ├── plugins/
│   │   ├── api.client.ts           # API setup
│   │   └── auth.init.ts            # Auth initialization
│   ├── stores/
│   │   └── auth.ts                 # Pinia auth store
│   └── app.vue                     # Root component
├── public/                         # Static assets
├── ios/                            # iOS native project (after setup)
├── android/                        # Android native project (after setup)
├── capacitor.config.ts             # Capacitor configuration
├── nuxt.config.ts                  # Nuxt configuration
├── package.json                    # Dependencies and scripts
└── SETUP_PLATFORMS.md              # Detailed platform setup guide
```

## 🔌 API Integration

The app is configured to work with the AdonisJS backend:

- **Base URL**: Set via `NUXT_PUBLIC_API_BASE` environment variable
- **Authentication**: Cookie-based sessions with CORS support
- **API Client**: Available via `useAPI()` composable
- **Auth Helper**: Available via `useAuth()` composable

Example API call:
```typescript
const api = useAPI()
const data = await api('/endpoint', { method: 'GET' })
```

Example auth usage:
```typescript
const { user, login, logout, isAuthenticated } = useAuth()
await login('email@example.com', 'password')
```

## 📱 Capacitor Plugins

This template is ready to use any Capacitor plugin. Example:

```bash
npm install @capacitor/camera
```

```typescript
import { Camera } from '@capacitor/camera'

const photo = await Camera.getPhoto({
  quality: 90,
  allowEditing: false,
  resultType: 'uri'
})
```

Popular plugins:
- [@capacitor/camera](https://capacitorjs.com/docs/apis/camera)
- [@capacitor/geolocation](https://capacitorjs.com/docs/apis/geolocation)
- [@capacitor/push-notifications](https://capacitorjs.com/docs/apis/push-notifications)
- [@capacitor/share](https://capacitorjs.com/docs/apis/share)
- [@capacitor/storage](https://capacitorjs.com/docs/apis/storage)

## 🐛 Troubleshooting

### "Module not found" errors
Run `npm install` again to ensure all dependencies are installed.

### Node version errors
Ensure you're using Node.js 20 or higher:
```bash
node --version
```

### Capacitor sync fails
Make sure you've run `npm run generate` first to create the `.output/public` directory.

### iOS build issues
- Update CocoaPods: `sudo gem install cocoapods`
- Clear derived data in Xcode
- Check that Xcode Command Line Tools are installed

### Android build issues
- Ensure Android SDK is installed via Android Studio
- Check ANDROID_HOME environment variable
- Sync Gradle files in Android Studio

### Live reload not working
- Verify your device and computer are on the same network
- Check firewall settings aren't blocking port 3000
- Ensure the IP address in `capacitor.config.ts` is correct
- Try `npx cap copy` after config changes

## 📚 Learn More

- [Nuxt 4 Documentation](https://nuxt.com/docs)
- [Capacitor Documentation](https://capacitorjs.com/docs)
- [Konsta UI Documentation](https://konstaui.com/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)

## 🤝 Backend Integration

This mobile app works with the AdonisJS backend in the `../backend` directory. Make sure the backend is running before testing the app:

```bash
cd ../backend
npm run dev
```

Then start Docker services:
```bash
cd ..
docker-compose up
```

## 📄 License

This template is available for use in your projects.
