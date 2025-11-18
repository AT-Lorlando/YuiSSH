FROM node:20-alpine AS base

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

# Build the application
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Production image
FROM base AS runner
WORKDIR /app

ENV NODE_ENV=production

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 adonisjs

COPY --from=builder --chown=adonisjs:nodejs /app/build ./build
COPY --from=builder --chown=adonisjs:nodejs /app/package*.json ./
COPY --from=deps --chown=adonisjs:nodejs /app/node_modules ./node_modules

USER adonisjs

EXPOSE 3333

CMD ["node", "build/bin/server.js"]

