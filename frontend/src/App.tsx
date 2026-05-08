const features = [
  {
    title: '文档管理',
    description: '上传 txt 和 md 文件，后端完成解析、切分和本地存储。',
  },
  {
    title: '知识问答',
    description: '基于已入库 chunk 做基础检索，用规则模板生成 MVP 回答。',
  },
  {
    title: '引用追踪',
    description: '回答保留引用来源，问答历史可查看问题、答案和引用片段。',
  },
];

function App() {
  return (
    <main className="min-h-screen overflow-hidden bg-paper text-ink">
      <section className="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col px-6 py-10 sm:px-10 lg:px-12">
        <div className="absolute left-[-8rem] top-[-8rem] h-80 w-80 rounded-full bg-wheat/50 blur-3xl" />
        <div className="absolute bottom-[-10rem] right-[-6rem] h-96 w-96 rounded-full bg-moss/20 blur-3xl" />

        <header className="relative z-10 flex items-center justify-between border-b border-ink/10 pb-6">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.35em] text-clay">KnowBase</p>
            <h1 className="mt-3 font-display text-3xl font-bold tracking-tight sm:text-5xl">
              个人知识库 MVP
            </h1>
          </div>
          <span className="hidden rounded-full border border-moss/30 bg-white/40 px-4 py-2 text-sm text-moss shadow-sm sm:inline-flex">
            Spring Boot + React
          </span>
        </header>

        <div className="relative z-10 grid flex-1 items-center gap-10 py-14 lg:grid-cols-[1.1fr_0.9fr]">
          <section>
            <p className="max-w-2xl text-lg leading-8 text-ink/70">
              KnowBase 用于管理个人文档，并基于检索结果生成带引用的问答。当前前端处于初始化阶段，
              先提供静态布局，后续步骤会接入上传、问答和历史接口。
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <span className="rounded-full bg-ink px-5 py-3 text-sm font-semibold text-paper shadow-lg shadow-ink/10">
                前端框架已就绪
              </span>
              <span className="rounded-full border border-clay/30 bg-clay/10 px-5 py-3 text-sm font-semibold text-clay">
                暂未连接后端 API
              </span>
            </div>
          </section>

          <section className="rounded-[2rem] border border-white/70 bg-white/55 p-5 shadow-2xl shadow-ink/10 backdrop-blur">
            <div className="rounded-[1.5rem] bg-ink p-6 text-paper">
              <p className="text-sm uppercase tracking-[0.3em] text-wheat">MVP Flow</p>
              <ol className="mt-6 space-y-4 text-sm leading-6 text-paper/80">
                <li>1. 上传文档并生成 chunk</li>
                <li>2. 基于关键词检索相关片段</li>
                <li>3. 返回模板回答与引用来源</li>
                <li>4. 保存问答历史用于回看</li>
              </ol>
            </div>
          </section>
        </div>

        <section className="relative z-10 grid gap-4 pb-8 md:grid-cols-3">
          {features.map((feature) => (
            <article
              className="rounded-3xl border border-ink/10 bg-white/55 p-6 shadow-lg shadow-ink/5"
              key={feature.title}
            >
              <h2 className="font-display text-xl font-bold">{feature.title}</h2>
              <p className="mt-3 text-sm leading-6 text-ink/65">{feature.description}</p>
            </article>
          ))}
        </section>
      </section>
    </main>
  );
}

export default App;
