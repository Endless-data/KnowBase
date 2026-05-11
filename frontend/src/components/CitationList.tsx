import type { Citation } from '../types/api';

interface CitationListProps {
  citations: Citation[];
}

function CitationList({ citations }: CitationListProps) {
  if (citations.length === 0) {
    return (
      <div className="rounded-3xl border border-ink/10 bg-white/45 p-5 text-sm text-ink/60">
        暂无引用来源
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {citations.map((citation) => (
        <article
          className="rounded-3xl border border-ink/10 bg-white/55 p-5 shadow-lg shadow-ink/5"
          key={`${citation.documentId}-${citation.chunkId}-${citation.chunkIndex}`}
        >
          <div className="flex flex-wrap items-center gap-3">
            <span className="rounded-full bg-moss/10 px-3 py-1 text-xs font-bold text-moss">
              chunk #{citation.chunkIndex}
            </span>
            <span className="text-sm font-semibold text-ink">{citation.documentName}</span>
          </div>
          <p className="mt-4 whitespace-pre-wrap text-sm leading-7 text-ink/70">{citation.content}</p>
        </article>
      ))}
    </div>
  );
}

export default CitationList;
