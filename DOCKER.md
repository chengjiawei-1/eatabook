# Docker 部署说明

本项目按前后端分离部署：

- `frontend`: Vue/Vite 构建后由 Nginx 提供静态文件，并反代 `/api`、`/uploads`、`/ws` 到后端。
- `backend`: Spring Boot 17。
- 依赖服务：MySQL、Redis、RabbitMQ、Neo4j、Chroma。

## 1. 准备环境变量

```bash
cp docker.env.example .env
```

然后编辑 `.env`，至少确认：

- `MYSQL_ROOT_PASSWORD`
- `NEO4J_PASSWORD`
- `FILE_BASE_URL`
- `SILICONFLOW_API_KEY`、`DEEPSEEK_API_KEY`、`ARK_API_KEY`、`XFYUN_SPEECH_*`
- `VITE_QWEATHER_API_KEY`
- `QWEATHER_API_HOST`

本地访问可以保留 `FILE_BASE_URL=http://localhost`；服务器部署时改成你的域名，例如 `https://example.com`。

## 2. 准备 MySQL 数据

仓库当前没有 SQL 初始化脚本。第一次启动前，把数据库建表和初始数据 SQL 放到：

```text
deploy/mysql/init/
```

MySQL 容器首次创建数据卷时会自动执行该目录下的 `.sql` 文件。已经启动过的旧数据卷不会重复执行初始化 SQL。

如果你已有本机数据库，可以导出：

```bash
mysqldump -uroot -p lyfw > deploy/mysql/init/001-init.sql
```

## 3. 构建并启动

```bash
docker compose up -d --build
```

启动后访问：

```text
http://localhost
```

查看日志：

```bash
docker compose logs -f backend
docker compose logs -f frontend
```

停止：

```bash
docker compose down
```

连数据一起删除：

```bash
docker compose down -v
```

## 4. 常见调整

- 改端口：编辑 `.env` 的 `FRONTEND_PORT`。
- 生产域名：编辑 `.env` 的 `FILE_BASE_URL=https://你的域名`，并在服务器前面接 HTTPS 反向代理。
- RAG 数据：Chroma 数据保存在 Docker volume `chroma_data`。
- 上传文件：后端上传文件保存在 Docker volume `backend_uploads`。

## 5. 注意事项

- 前端 Dockerfile 使用 `npm run build:client`，因为项目现有 `npm run build` 包含 Windows 专用命令，Linux 容器里不能直接执行。
- 浏览器端 API 默认走相对路径 `/api`，由 Nginx 反代到后端容器。
- 和风天气接口走 `/weather-api`，由 Nginx 反代到 `.env` 里的 `QWEATHER_API_HOST`。
