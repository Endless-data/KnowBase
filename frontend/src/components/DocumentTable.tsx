import type { DocumentItem } from '../types/api';

interface DocumentTableProps {
  documents: DocumentItem[];
  deletingId: number | null;
  onDelete: (document: DocumentItem) => Promise<void>;
}

const statusStyles: Record<string, string> = {
  INDEXED: 'bg-moss/10 text-moss ring-moss/20',
  FAILED: 'bg-clay/10 text-clay ring-clay/20',
  PARSING: 'bg-wheat/40 text-ink ring-wheat/60',
  UPLOADED: 'bg-ink/5 text-ink/70 ring-ink/10',
};

function DocumentTable({ documents, deletingId, onDelete }: DocumentTableProps) {
  if (documents.length === 0) {
    return (
      <div className="rounded-[2rem] border border-ink/10 bg-white/50 p-10 text-center shadow-lg shadow-ink/5">
        <p className="font-display text-2xl font-bold">还没有文档</p>
        <p className="mt-3 text-sm text-ink/60">先上传一份 Markdown 或文本文件，开始构建知识库。</p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-[2rem] border border-ink/10 bg-white/65 shadow-xl shadow-ink/10">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
          <thead className="bg-ink text-paper">
            <tr>
              <th className="px-5 py-4 font-semibold">文档</th>
              <th className="px-5 py-4 font-semibold">类型</th>
              <th className="px-5 py-4 font-semibold">状态</th>
              <th className="px-5 py-4 font-semibold">创建时间</th>
              <th className="px-5 py-4 text-right font-semibold">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink/10">
            {documents.map((document) => (
              <tr className="bg-white/35" key={document.id}>
                <td className="max-w-xs px-5 py-4 font-semibold text-ink">{document.name}</td>
                <td className="px-5 py-4 text-ink/65">{document.fileType}</td>
                <td className="px-5 py-4">
                  <span
                    className={`inline-flex rounded-full px-3 py-1 text-xs font-bold ring-1 ${
                      statusStyles[document.status] ?? 'bg-ink/5 text-ink/70 ring-ink/10'
                    }`}
                  >
                    {document.status}
                  </span>
                </td>
                <td className="px-5 py-4 text-ink/65">{formatDate(document.createdAt)}</td>
                <td className="px-5 py-4 text-right">
                  <button
                    className="rounded-full border border-clay/30 px-4 py-2 text-xs font-bold text-clay transition hover:bg-clay hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                    disabled={deletingId === document.id}
                    onClick={() => onDelete(document)}
                    type="button"
                  >
                    {deletingId === document.id ? '删除中...' : '删除'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default DocumentTable;
