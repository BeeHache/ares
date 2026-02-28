# Frontend Dockerfile (SSR with Node.js)
# Stage 1: Build the Angular app
FROM node:25-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci 
COPY . .
RUN npm run build

# Stage 2: Serve with Node.js
FROM node:25-alpine
WORKDIR /app
COPY --from=build /app/dist ./dist
EXPOSE 4000
CMD ["node", "dist/ares-ui/server/server.mjs"]
