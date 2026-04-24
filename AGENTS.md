# KnowBase Project Instructions

你正在协助开发 KnowBase：一个基于 Spring Boot + React + MySQL + RAG 的个人知识库系统。

## 项目目标

实现一个面向个人用户的 RAG 知识综合检索库，支持：

1. 文档上传与管理
2. 文档解析
3. 文本切分
4. chunk 存储
5. 向量化与语义检索
6. 基于检索结果的问答
7. 引用来源展示
8. 历史问答记录

参考文档位于 docs/ 目录：

- docs/选题报告.md
- docs/项目需求分析.md
- docs/系统设计说明书.md

## 技术栈

后端：

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- MySQL
- Lombok
- Validation

前端：

- React
- Vite
- TypeScript
- Axios
- Tailwind CSS

## 开发原则

1. 不要一次性生成整个项目。
2. 每次只完成一个小任务。
3. 修改代码前先说明计划。
4. 修改后必须说明改了哪些文件。
5. 不要硬编码 API Key。
6. 不要把密钥写入 Git。
7. 后端接口统一以 /api 开头。
8. 后端采用 Controller / Service / Repository / Entity / DTO 分层。
9. 优先保证项目能运行，再逐步优化。
10. 每个阶段尽量提供测试方式，例如 curl、Postman、浏览器访问方式。

## 输出要求

每次完成任务后，说明：

1. 本次完成了什么
2. 修改了哪些文件
3. 如何运行或测试
4. 下一步建议做什么