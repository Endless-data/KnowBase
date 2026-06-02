import CitationList from './CitationList';
import type { AskResponse } from '../types/api';

interface AnswerPanelProps {
  response: AskResponse | null;
  isStreaming?: boolean;
}

function AnswerPanel({ response, isStreaming = false }: AnswerPanelProps) {
  if (!response) {
    return (
      <section className="rounded-[2rem] border border-ink/10 bg-white/50 p-10 shadow-lg shadow-ink/5">
        <p className="font-display text-2xl font-bold">等待问题</p>
        <p className="mt-3 text-sm leading-6 text-ink/60">
          先上传文档，再在左侧输入问题。回答和引用会显示在这里。
        </p>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="rounded-[2rem] border border-ink/10 bg-white/65 p-6 shadow-xl shadow-ink/10">
        <div className="flex items-center justify-between gap-4">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-moss">Answer</p>
          {isStreaming && (
            <span className="rounded-full bg-moss/10 px-3 py-1 text-xs font-bold text-moss">正在生成...</span>
          )}
        </div>
        <p className="mt-5 whitespace-pre-wrap text-base leading-8 text-ink/80">
          {response.answer || (isStreaming ? '正在读取知识库并生成回答...' : '')}
        </p>
      </div>

      <div>
        <div className="mb-4 flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-clay">Citations</p>
            <h2 className="mt-2 font-display text-2xl font-bold">引用来源</h2>
          </div>
          <span className="rounded-full bg-ink px-4 py-2 text-xs font-bold text-paper">
            {response.citations.length} 条
          </span>
        </div>
        <CitationList citations={response.citations} />
      </div>
    </section>
  );
}

export default AnswerPanel;
