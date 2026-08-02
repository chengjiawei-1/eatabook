FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=
ARG VITE_WS_BASE_URL=
ARG VITE_QWEATHER_API_KEY=

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_WS_BASE_URL=$VITE_WS_BASE_URL
ENV VITE_QWEATHER_API_KEY=$VITE_QWEATHER_API_KEY

RUN npm run build:client

FROM nginx:1.27-alpine

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/static /usr/share/nginx/html
COPY --from=build /app/src/assets/images /usr/share/nginx/html/images
COPY --from=build /app/src/assets/cover /usr/share/nginx/html/cover

EXPOSE 80
