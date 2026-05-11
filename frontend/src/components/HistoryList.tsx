import type { HistoryListItem } from '../types/api';

interface HistoryListProps {
  histories: HistoryListItem[];
  isLoading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
}

function HistoryList({ histories, isLoading, selectedId, onSelect }: HistoryListProps) {
  if (isLoading) {
    return (
      <div className="rounded-[2rem] border border-white/70 bg-white/60 p-6 text-sm font-semibold text-ink/60 shadow-xl shadow-ink/10 backdrop-blur">
        正在加载历史记录...
      </div>
    );
  }

  if (histories.length === 0) {
    return (
      <div className="rounded-[2rem] border border-white/70 bg-white/60 p-6 shadow-xl shadow-ink/10 backdrop-blur">
        <p className="font-display text-2xl font-bold">暂无历史记录</p>
        <p className="mt-3 text-sm leading-6 text-ink/60">完成一次知识问答后，历史记录会显示在这里。</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {histories.map((history) => {
        const isSelected = history.id === selectedId;

        return (
          <button
            className={`w-full rounded-[1.5rem] border p-5 text-left shadow-lg shadow-ink/5 transition ${
              isSelected
                ? 'border-ink bg-ink text-paper'
                : 'border-ink/10 bg-white/60 text-ink hover:border-moss/40 hover:bg-white/80'
            }`}
            key={history.id}
            onClick={() => onSelect(history.id)}
            type="button"
          >
            <div className="flex items-center justify-between gap-3">
              <span className={`text-xs font-bold uppercase tracking-[0.2em] ${isSelected ? 'text-paper/70' : 'text-clay'}`}>
                {formatDate(history.createdAt)}
              </span>
              <span className={`rounded-full px-3 py-1 text-xs font-bold ${isSelected ? 'bg-paper/15 text-paper' : 'bg-moss/10 text-moss'}`}>
                {history.retrievalCount} 引用
              </span>
            </div>
            <p className="mt-4 text-base font-bold leading-6">{history.question}</p>
            <p className={`mt-3 text-sm leading-6 ${isSelected ? 'text-paper/70' : 'text-ink/60'}`}>{shorten(history.answer)}</p>
          </button>
        );
      })}
    </div>
  );
}

function shorten(value: string) {
  return value.length > 78 ? `${value.slice(0, 78)}...` : value;
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default HistoryList;
