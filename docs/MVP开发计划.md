# KnowBase MVP 开发计划

## 1. 开发顺序建议

建议先完成后端，再做前端。

原因很直接：KnowBase 的核心难点在后端，包括文档上传、解析、切分、检索、问答、引用和历史记录。前端主要是调用这些接口并展示结果。如果后端接口先稳定下来，前端开发会更清晰，也更容易用 curl 或 Postman 单独验证每个功能。

MVP 推荐顺序：

1. 后端基础项目
2. 后端通用结构
3. 文档上传与管理
4. 文档解析与 chunk 存储
5. 基础检索与问答接口
6. 历史记录与引用
7. 后端接口联调
8. 前端页面初始化
9. 前端接入后端接口
10. 后续替换为真实 Embedding、向量检索和 LLM

## 2. MVP 阶段目标

MVP 不追求一次性完成完整 RAG 系统，而是先做出一个可运行闭环：

用户上传 `.txt` 或 `.md` 文档，后端保存文件和元数据，解析文本，切分为 chunk，用户提问时先用基础检索找到相关 chunk，再返回一个基于检索内容的回答和引用来源，并保存历史记录。最后用前端页面完成上传、问答和历史查看。

MVP 初期可以先用关键词检索或简单文本匹配代替真正的向量检索。这样可以先验证业务流程，后续再替换为 Embedding + 向量库 + LLM。

## 3. 后端优先 MVP 详细计划

### 第 1 步：初始化后端 Spring Boot 项目

要实现什么：

- 在 `backend/` 下创建 Spring Boot Maven 项目。
- Java 版本使用 21。
- 包名使用 `com.zhen.knowbase`。
- 提供 `GET /api/health` 健康检查接口。
- 暂时不连接 MySQL，不创建业务代码。

会修改哪些文件：

- `backend/pom.xml`
- `backend/.gitignore`
- `backend/src/main/java/com/zhen/knowbase/KnowBaseApplication.java`
- `backend/src/main/java/com/zhen/knowbase/controller/HealthController.java`
- `backend/src/test/java/com/zhen/knowbase/KnowBaseApplicationTests.java`

如何运行测试：

```bash
cd backend
mvn test
mvn spring-boot:run
curl http://localhost:8080/api/health
```

期望返回：

```text
KnowBase backend is running
```

建议 commit message：

```text
chore: initialize Spring Boot backend
```

### 第 2 步：补充后端基础配置和统一响应结构

要实现什么：

- 增加基础配置文件。
- 定义统一响应结构，例如 `{ code, message, data }`。
- 定义全局异常处理。
- 暂时仍不连接数据库。

会修改哪些文件：

- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/zhen/knowbase/common/ApiResponse.java`
- `backend/src/main/java/com/zhen/knowbase/common/GlobalExceptionHandler.java`

如何运行测试：

```bash
cd backend
mvn test
mvn spring-boot:run
curl http://localhost:8080/api/health
```

建议 commit message：

```text
chore: add common response and exception handling
```

### 第 3 步：接入 MySQL 和 JPA

要实现什么：

- 添加 Spring Data JPA、MySQL Driver、Validation、Lombok 依赖。
- 配置 MySQL 连接。
- 只完成连接验证，不创建业务表。
- 数据库密码不要写死在代码里，建议通过环境变量传入。

会修改哪些文件：

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`

如何运行测试：

先创建数据库：

```sql
CREATE DATABASE knowbase DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

再运行：

```bash
cd backend
mvn test
mvn spring-boot:run
```

建议 commit message：

```text
chore: configure MySQL and JPA
```

### 第 4 步：实现 Document 实体和文档列表接口

要实现什么：

- 创建 `Document` 实体。
- 创建 `DocumentRepository`。
- 创建文档列表接口 `GET /api/documents`。
- 初期返回数据库中的文档元数据列表。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/entity/Document.java`
- `backend/src/main/java/com/zhen/knowbase/repository/DocumentRepository.java`
- `backend/src/main/java/com/zhen/knowbase/service/DocumentService.java`
- `backend/src/main/java/com/zhen/knowbase/controller/DocumentController.java`
- `backend/src/main/java/com/zhen/knowbase/dto/DocumentResponse.java`

如何运行测试：

```bash
cd backend
mvn test
mvn spring-boot:run
curl http://localhost:8080/api/documents
```

期望结果：返回空数组或空列表响应，而不是报错。

建议 commit message：

```text
feat: add document list API
```

### 第 5 步：实现文档上传和本地文件保存

要实现什么：

- 实现 `POST /api/documents/upload`。
- 支持上传 `.txt` 和 `.md`。
- 校验文件类型和文件大小。
- 将原始文件保存到本地目录。
- 在数据库保存文档元数据，状态先设为 `UPLOADED`。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/controller/DocumentController.java`
- `backend/src/main/java/com/zhen/knowbase/service/DocumentService.java`
- `backend/src/main/java/com/zhen/knowbase/service/FileStorageService.java`
- `backend/src/main/java/com/zhen/knowbase/dto/DocumentUploadResponse.java`
- `backend/src/main/resources/application.yml`

如何运行测试：

```bash
cd backend
mvn spring-boot:run
curl -F "file=@../README.md" http://localhost:8080/api/documents/upload
curl http://localhost:8080/api/documents
```

建议 commit message：

```text
feat: support document upload
```

### 第 6 步：实现文档解析

要实现什么：

- 读取上传后的 `.txt` 和 `.md` 文件内容。
- 如果内容为空，将文档状态更新为 `FAILED`。
- 如果解析成功，将状态更新为 `PARSING` 或进入下一步处理。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/service/ParserService.java`
- `backend/src/main/java/com/zhen/knowbase/service/DocumentService.java`
- `backend/src/main/java/com/zhen/knowbase/entity/Document.java`

如何运行测试：

```bash
cd backend
mvn test
curl -F "file=@../README.md" http://localhost:8080/api/documents/upload
curl http://localhost:8080/api/documents
```

建议 commit message：

```text
feat: parse uploaded text documents
```

### 第 7 步：实现 Chunk 实体和文本切分

要实现什么：

- 创建 `Chunk` 实体。
- 将解析出的文本按固定长度切分。
- 保存 chunk 到数据库。
- 文档处理成功后状态更新为 `INDEXED`。
- MVP 阶段可以先不做真正向量化，`vectorId` 可以为空。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/entity/Chunk.java`
- `backend/src/main/java/com/zhen/knowbase/repository/ChunkRepository.java`
- `backend/src/main/java/com/zhen/knowbase/service/ChunkService.java`
- `backend/src/main/java/com/zhen/knowbase/service/DocumentService.java`

如何运行测试：

```bash
cd backend
mvn test
curl -F "file=@../README.md" http://localhost:8080/api/documents/upload
```

然后在 MySQL 中查看：

```sql
SELECT * FROM document;
SELECT * FROM chunk;
```

建议 commit message：

```text
feat: split documents into chunks
```

### 第 8 步：实现删除文档接口

要实现什么：

- 实现 `DELETE /api/documents/{id}`。
- 删除文档记录。
- 删除对应 chunk。
- 删除本地上传文件。
- 后续接入向量库后，还要同步删除向量索引。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/controller/DocumentController.java`
- `backend/src/main/java/com/zhen/knowbase/service/DocumentService.java`
- `backend/src/main/java/com/zhen/knowbase/repository/ChunkRepository.java`

如何运行测试：

```bash
curl -X DELETE http://localhost:8080/api/documents/1
curl http://localhost:8080/api/documents
```

建议 commit message：

```text
feat: add document deletion
```

### 第 9 步：实现基础检索服务

要实现什么：

- 创建 `RetrievalService`。
- 根据用户问题检索相关 chunk。
- MVP 阶段先用关键词包含、简单计分或数据库 LIKE 查询。
- 返回 Top-K chunk。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/service/RetrievalService.java`
- `backend/src/main/java/com/zhen/knowbase/repository/ChunkRepository.java`
- `backend/src/main/java/com/zhen/knowbase/dto/RetrievedChunk.java`

如何运行测试：

可以先通过单元测试验证检索逻辑：

```bash
cd backend
mvn test
```

后续也可以通过问答接口间接验证。

建议 commit message：

```text
feat: add basic chunk retrieval
```

### 第 10 步：实现问答接口

要实现什么：

- 实现 `POST /api/qa/ask`。
- 接收用户问题。
- 调用基础检索服务找到相关 chunk。
- 如果没有检索结果，返回“知识库中暂无相关内容”。
- 如果有检索结果，先用规则模板生成回答，例如“根据知识库内容，相关信息如下...”。
- 返回回答和引用来源。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/controller/QaController.java`
- `backend/src/main/java/com/zhen/knowbase/service/QaService.java`
- `backend/src/main/java/com/zhen/knowbase/dto/AskRequest.java`
- `backend/src/main/java/com/zhen/knowbase/dto/AskResponse.java`
- `backend/src/main/java/com/zhen/knowbase/dto/CitationResponse.java`

如何运行测试：

```bash
curl -X POST http://localhost:8080/api/qa/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"KnowBase 是什么"}'
```

建议 commit message：

```text
feat: add MVP QA endpoint
```

### 第 11 步：实现 ChatRecord 和 Citation 持久化

要实现什么：

- 创建 `ChatRecord` 实体。
- 创建 `Citation` 实体。
- 每次问答后保存问题、回答、检索数量。
- 保存回答引用了哪些 chunk。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/entity/ChatRecord.java`
- `backend/src/main/java/com/zhen/knowbase/entity/Citation.java`
- `backend/src/main/java/com/zhen/knowbase/repository/ChatRecordRepository.java`
- `backend/src/main/java/com/zhen/knowbase/repository/CitationRepository.java`
- `backend/src/main/java/com/zhen/knowbase/service/QaService.java`

如何运行测试：

```bash
curl -X POST http://localhost:8080/api/qa/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"KnowBase 是什么"}'
```

然后在 MySQL 中查看：

```sql
SELECT * FROM chat_record;
SELECT * FROM citation;
```

建议 commit message：

```text
feat: persist QA history and citations
```

### 第 12 步：实现历史记录接口

要实现什么：

- 实现 `GET /api/history`。
- 实现 `GET /api/history/{id}`。
- 历史列表按时间倒序返回。
- 历史详情包含问题、回答和引用。

会修改哪些文件：

- `backend/src/main/java/com/zhen/knowbase/controller/HistoryController.java`
- `backend/src/main/java/com/zhen/knowbase/service/HistoryService.java`
- `backend/src/main/java/com/zhen/knowbase/dto/HistoryListResponse.java`
- `backend/src/main/java/com/zhen/knowbase/dto/HistoryDetailResponse.java`

如何运行测试：

```bash
curl http://localhost:8080/api/history
curl http://localhost:8080/api/history/1
```

建议 commit message：

```text
feat: add QA history APIs
```

### 第 13 步：后端接口整理和 README 补充

要实现什么：

- 整理接口返回格式。
- 补充必要的错误提示。
- 在 README 中写清楚后端如何启动、如何配置 MySQL、如何用 curl 测试。
- 确保后端 MVP 能独立演示。

会修改哪些文件：

- `README.md`
- 可能修改 `backend/src/main/java/com/zhen/knowbase/common/*`
- 可能修改各 Controller 和 Service 中的错误处理

如何运行测试：

```bash
cd backend
mvn test
mvn spring-boot:run
```

按 README 中的 curl 命令完整跑一遍：

1. 健康检查
2. 上传文档
3. 查询文档
4. 提问
5. 查看历史
6. 删除文档

建议 commit message：

```text
docs: add backend MVP runbook
```

## 4. 前端 MVP 详细计划

### 第 14 步：初始化前端项目

要实现什么：

- 在 `frontend/` 下创建 React + Vite + TypeScript 项目。
- 接入 Tailwind CSS。
- 创建基础页面布局。
- 暂时不接后端接口。

会修改哪些文件：

- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/src/main.tsx`
- `frontend/src/App.tsx`
- `frontend/src/index.css`
- `frontend/tailwind.config.*`

如何运行测试：

```bash
cd frontend
npm install
npm run dev
```

建议 commit message：

```text
chore: initialize React frontend
```

### 第 15 步：实现前端 API 客户端

要实现什么：

- 封装 Axios。
- 配置后端基础地址。
- 定义文档、问答、历史相关 TypeScript 类型。

会修改哪些文件：

- `frontend/src/api/client.ts`
- `frontend/src/api/documents.ts`
- `frontend/src/api/qa.ts`
- `frontend/src/api/history.ts`
- `frontend/src/types/api.ts`

如何运行测试：

```bash
cd frontend
npm run build
```

建议 commit message：

```text
feat: add frontend API client
```

### 第 16 步：实现文档管理页面

要实现什么：

- 上传文件。
- 展示文档列表。
- 展示文档状态。
- 删除文档。

会修改哪些文件：

- `frontend/src/pages/DocumentsPage.tsx`
- `frontend/src/components/DocumentUpload.tsx`
- `frontend/src/components/DocumentTable.tsx`
- `frontend/src/App.tsx`

如何运行测试：

先启动后端：

```bash
cd backend
mvn spring-boot:run
```

再启动前端：

```bash
cd frontend
npm run dev
```

浏览器测试上传、列表刷新、删除。

建议 commit message：

```text
feat: add document management UI
```

### 第 17 步：实现问答页面

要实现什么：

- 输入自然语言问题。
- 调用 `/api/qa/ask`。
- 展示回答。
- 展示引用来源。
- 处理无相关内容和接口错误。

会修改哪些文件：

- `frontend/src/pages/QaPage.tsx`
- `frontend/src/components/QuestionInput.tsx`
- `frontend/src/components/AnswerPanel.tsx`
- `frontend/src/components/CitationList.tsx`
- `frontend/src/App.tsx`

如何运行测试：

浏览器中先上传一份文档，然后输入问题，确认能展示回答和引用。

建议 commit message：

```text
feat: add QA UI with citations
```

### 第 18 步：实现历史记录页面

要实现什么：

- 展示历史问答列表。
- 查看某条历史详情。
- 展示历史回答和引用来源。

会修改哪些文件：

- `frontend/src/pages/HistoryPage.tsx`
- `frontend/src/components/HistoryList.tsx`
- `frontend/src/components/HistoryDetail.tsx`
- `frontend/src/App.tsx`

如何运行测试：

浏览器中完成一次提问，然后进入历史页面查看记录。

建议 commit message：

```text
feat: add QA history UI
```

### 第 19 步：前后端完整联调

要实现什么：

- 从前端完整跑通上传、查看文档、提问、引用展示、历史查看、删除文档。
- 修复跨域、接口字段不一致、错误提示不清晰等问题。
- 补充 README 的前端运行说明。

会修改哪些文件：

- `README.md`
- 可能修改 `frontend/src/api/*`
- 可能修改后端 DTO 或 Controller 返回字段

如何运行测试：

```bash
cd backend
mvn test
mvn spring-boot:run
```

另开终端：

```bash
cd frontend
npm run build
npm run dev
```

建议 commit message：

```text
test: verify MVP end-to-end flow
```

## 5. RAG 能力增强计划

这部分不建议放进最初 MVP，建议在 MVP 跑通后再做。

### 第 20 步：抽象 EmbeddingService 和 VectorStoreService

要实现什么：

- 增加 Embedding 服务接口。
- 增加向量存储服务接口。
- 先保留 mock 或本地实现。
- 不直接把 API Key 写进代码。

建议 commit message：

```text
feat: introduce embedding and vector store abstractions
```

### 第 21 步：接入真实 Embedding 和向量检索

要实现什么：

- 文档 chunk 入库后生成向量。
- 查询时对问题生成向量。
- 用向量相似度检索 Top-K chunk。
- 替换第 9 步的关键词检索。

建议 commit message：

```text
feat: add semantic chunk retrieval
```

### 第 22 步：接入真实 LLM

要实现什么：

- 增加 `LlmService`。
- 基于问题和检索上下文构造 Prompt。
- 调用大模型生成回答。
- 没有检索结果时不要强行生成答案。

建议 commit message：

```text
feat: add LLM-based RAG answers
```

## 6. 什么时候需要新开一个对话

一般不需要频繁新开对话。一个阶段内连续做同一个目标，留在同一个对话更好，因为上下文完整。

建议新开对话的情况：

- 当前对话已经很长，模型开始遗漏前面约束或反复问已经回答过的问题。
- 你要切换到完全不同的任务，例如从“实现文档上传”切到“写论文答辩 PPT”。
- 当前任务已经完成并提交，准备开始下一个独立阶段。
- 你想让我重新基于最新代码做一次干净分析，避免受旧讨论影响。
- 出现多次误解后，你希望用更短、更明确的新上下文重启。

不建议新开对话的情况：

- 同一个 bug 还没解决。
- 同一个功能还在连续实现。
- 刚刚让我读完项目文档或代码，马上又要我继续基于这些上下文工作。

如果新开对话，建议第一条消息这样写：

```text
这是 KnowBase 项目，技术栈是 Spring Boot + React + MySQL。请先阅读 AGENTS.md 和 docs/MVP开发计划.md。当前要做第 X 步：……
不要改无关文件。修改前先说明计划。
```

## 7. 什么时候需要新建 Git 分支

Git 分支的作用是隔离风险。只要一个任务可能改动多、耗时长、或者不确定能不能一次做好，就应该新建分支。

建议新建分支的情况：

- 开始一个新功能，例如文档上传、文本切分、问答接口、历史记录、前端页面。
- 要做较大改动，例如调整数据库表结构、重构 DTO、替换检索实现。
- 要尝试不确定方案，例如接入某个向量库或 LLM SDK。
- 当前主分支是稳定版本，你不想让半成品影响它。
- 你准备让我连续改多个文件。

可以不新建分支的情况：

- 改 README 或 docs。
- 修一个很小的拼写、注释或配置问题。
- 做一次非常明确的小改动，并且可以快速测试。

推荐分支命名：

```text
feat/backend-health
feat/document-upload
feat/chunk-splitting
feat/qa-api
feat/history-api
feat/frontend-documents
fix/upload-validation
docs/mvp-plan
```

常用命令：

```bash
git checkout -b feat/document-upload
git status
git add .
git commit -m "feat: support document upload"
git checkout main
git merge feat/document-upload
```

如果你是新手，建议每一个 MVP 步骤都单独一个 commit；较大的步骤可以单独一个分支。这样出错时容易回退，也方便让 Codex 或你自己 review。

## 8. 推荐的协作方式

每次只让我做一个小步骤，例如：

```text
现在执行 docs/MVP开发计划.md 的第 4 步：实现 Document 实体和文档列表接口。
只做这一小步。修改前先说明计划。修改后告诉我改了哪些文件、如何测试。
```

如果你不确定下一步该做什么，可以问：

```text
请根据当前代码和 docs/MVP开发计划.md，告诉我下一步最小可执行任务是什么。不要写代码。
```

如果你想让我检查代码质量，可以说：

```text
请 review 当前分支相对 main 的改动，重点找 bug、风险和缺少的测试。不要直接修改代码。
```
