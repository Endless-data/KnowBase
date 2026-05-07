# KnowBase

KnowBase 是一个个人知识库 MVP，当前后端支持文档上传、解析、文本切分、关键词检索、规则问答、引用返回、问答历史和文档删除。

## 后端技术栈

- Java 21
- Spring Boot
- Maven
- MySQL 8.4
- Docker Compose

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

后端也支持通过环境变量覆盖数据库和上传目录：

```bash
export KB_DB_URL='jdbc:mysql://localhost:3306/knowbase?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export KB_DB_USERNAME='knowbase'
export KB_DB_PASSWORD='knowbase'
export KB_UPLOAD_DIR='../uploads'
```

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

## 常见错误提示

- 上传空文件或缺少 `file` 字段：返回 400。
- 上传非 `.txt` / `.md` 文件：返回 400。
- 上传文件超过 10MB：返回 400。
- 问答请求缺少 `question` 或问题为空：返回 400。
- 查询不存在的历史记录：返回 400。

## 当前限制

- MVP 阶段暂未接入真实大模型，问答回答由规则模板生成。
- MVP 阶段暂未接入向量库，检索使用关键词匹配。
- 删除文档会删除文档记录、chunk 和本地上传文件；历史引用保留当时的文本快照。
