# KnowBase

KnowBase 是一个个人知识库 MVP，当前后端支持文档上传、解析、文本切分、Embedding 向量化、语义检索、DeepSeek 问答、引用返回、问答历史和文档删除。

## 后端技术栈

- Java 21
- Spring Boot
- Maven
- MySQL 8.4
- Docker Compose

## 前端技术栈

- React
- Vite
- TypeScript
- Axios
- Tailwind CSS

## 启动 MySQL

项目根目录已有 `docker-compose.yml`，只配置了 MySQL：

```bash
docker compose up -d mysql
docker compose ps
```

默认数据库配置：

```text
database: knowbase
username: knowbase
password: knowbase
port: 3306
```

后端支持从项目根目录 `.env` 统一读取本地配置。先复制示例文件：

```bash
cp .env.example .env
```

然后在 `.env` 中填写或调整：

```text
KB_DB_URL=jdbc:mysql://localhost:3306/knowbase?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
KB_DB_USERNAME=knowbase
KB_DB_PASSWORD=knowbase
KB_UPLOAD_DIR=../uploads
KB_CHUNK_SIZE=800
KB_CHUNK_OVERLAP=120
KB_EMBEDDING_PROVIDER=dashscope
KB_EMBEDDING_MODEL=text-embedding-v4
KB_EMBEDDING_DIMENSION=1024
DASHSCOPE_API_KEY=你的阿里云百炼 API Key
KB_VECTOR_STORE_PROVIDER=mysql
KB_LLM_PROVIDER=deepseek
KB_LLM_MODEL=deepseek-v4-flash
KB_LLM_BASE_URL=https://api.deepseek.com
KB_LLM_MAX_TOKENS=1000
DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

`.env` 已被 `.gitignore` 忽略，不要提交真实 API Key。如果只是本地验证 MySQL 和后端启动、暂时不调用真实 Embedding，可以把 `.env` 中的 `KB_EMBEDDING_PROVIDER` 改成 `local`。如果要完整体验 DeepSeek 问答，需要填写 `DEEPSEEK_API_KEY`。

`KB_CHUNK_SIZE` 控制单个文本切片的最大字符数，`KB_CHUNK_OVERLAP` 控制相邻切片保留的重叠字符数。默认分块策略会优先按段落和句子边界切分，超长文本才按固定长度兜底切分。

## 启动后端

```bash
cd backend
mvn test
mvn spring-boot:run
```

服务默认运行在：

```text
http://localhost:8080
```

## 启动前端

首次运行前安装依赖：

```bash
cd frontend
npm install
```

构建检查：

```bash
npm run build
```

启动开发服务器：

```bash
npm run dev
```

前端默认运行在：

```text
http://localhost:5173
```

开发环境通过 Vite 代理访问后端接口，前端请求 `/api` 会转发到：

```text
http://localhost:8080
```

统一 JSON 响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

健康检查接口直接返回文本：

```bash
curl http://localhost:8080/api/health
```

预期：

```text
KnowBase backend is running
```

## 后端 MVP 演示

先准备一个临时 Markdown 文档：

```bash
cat > /tmp/knowbase-demo.md <<'EOF'
KnowBase 是一个个人知识库系统，支持文档管理、检索、问答和引用来源展示。
EOF
```

上传文档：

```bash
curl -F "file=@/tmp/knowbase-demo.md" http://localhost:8080/api/documents/upload
```

响应中的 `documentId` 后面删除文档会用到。

查询文档列表：

```bash
curl http://localhost:8080/api/documents
```

提问：

```bash
curl -X POST http://localhost:8080/api/qa/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"KnowBase"}'
```

查看问答历史列表：

```bash
curl http://localhost:8080/api/history
```

查看问答历史详情，把 `1` 替换为历史列表返回的 `id`：

```bash
curl http://localhost:8080/api/history/1
```

删除文档，把 `1` 替换为上传接口返回的 `documentId`：

```bash
curl -X DELETE http://localhost:8080/api/documents/1
```

## 前后端完整联调

1. 启动 MySQL：

```bash
docker compose up -d mysql
docker compose ps
```

2. 启动后端：

```bash
cd backend
mvn test
mvn spring-boot:run
```

3. 另开终端，启动前端：

```bash
cd frontend
npm run build
npm run dev
```

4. 在浏览器打开：

```text
http://localhost:5173
```

5. 按页面顺序验证 MVP 主流程：

- 在“文档管理”上传 `.txt` 或 `.md` 文档。
- 确认文档列表出现新文档。
- 进入“知识问答”，输入和文档内容相关的问题。
- 确认页面展示回答和引用来源。
- 进入“历史记录”，确认能看到刚才的问题、回答和引用。
- 回到“文档管理”，删除刚才上传的测试文档。

## 常见错误提示

- 上传空文件或缺少 `file` 字段：返回 400。
- 上传非 `.txt` / `.md` 文件：返回 400。
- 上传文件超过 10MB：返回 400。
- 问答请求缺少 `question` 或问题为空：返回 400。
- 查询不存在的历史记录：返回 400。

## 当前限制

- 当前向量存储使用 MySQL JSON 文本保存，并在 Java 中计算余弦相似度，适合课程 MVP 和小规模数据。
- 旧文档如果是在接入向量化或调整分块策略前上传的，需要重新上传后才能使用新的检索和切片效果。
- DeepSeek 和 DashScope API Key 只应保存在本地 `.env` 中，不要提交到 Git。
- 删除文档会删除文档记录、chunk、向量记录和本地上传文件；历史引用保留当时的文本快照。
