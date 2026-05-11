import CitationList from './CitationList';
import type { HistoryDetail as HistoryDetailType } from '../types/api';

interface HistoryDetailProps {
  detail: HistoryDetailType | null;
  isLoading: boolean;
}

function HistoryDetail({ detail, isLoading }: HistoryDetailProps) {
  if (isLoading) {
    return (
      <section className="rounded-[2rem] border border-ink/10 bg-white/50 p-10 shadow-lg shadow-ink/5">
        <p className="font-display text-2xl font-bold">正在加载详情</p>
        <p className="mt-3 text-sm leading-6 text-ink/60">正在读取该条问答的回答和引用来源。</p>
      </section>
    );
  }

  if (!detail) {
    return (
      <section className="rounded-[2rem] border border-ink/10 bg-white/50 p-10 shadow-lg shadow-ink/5">
        <p className="font-display text-2xl font-bold">选择一条历史记录</p>
        <p className="mt-3 text-sm leading-6 text-ink/60">点击左侧列表项后，这里会显示完整问答和引用。</p>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="rounded-[2rem] border border-ink/10 bg-white/65 p-6 shadow-xl shadow-ink/10">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-clay">Question</p>
          <span className="rounded-full bg-moss/10 px-4 py-2 text-xs font-bold text-moss">{formatDate(detail.createdAt)}</span>
        </div>
        <h2 className="mt-5 font-display text-3xl font-bold leading-tight">{detail.question}</h2>
      </div>

      <div className="rounded-[2rem] border border-ink/10 bg-white/65 p-6 shadow-xl shadow-ink/10">
        <p className="text-sm font-semibold uppercase tracking-[0.25em] text-moss">Answer</p>
        <p className="mt-5 whitespace-pre-wrap text-base leading-8 text-ink/80">{detail.answer}</p>
      </div>

      <div>
        <div className="mb-4 flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-clay">Citations</p>
            <h2 className="mt-2 font-display text-2xl font-bold">历史引用来源</h2>
          </div>
          <span className="rounded-full bg-ink px-4 py-2 text-xs font-bold text-paper">{detail.citations.length} 条</span>
        </div>
        <CitationList citations={detail.citations} />
      </div>
    </section>
  );
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default HistoryDetail;
