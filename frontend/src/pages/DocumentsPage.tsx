import { useEffect, useState } from 'react';
import DocumentTable from '../components/DocumentTable';
import DocumentUpload from '../components/DocumentUpload';
import { deleteDocument, listDocuments, uploadDocument } from '../api/documents';
import type { DocumentItem } from '../types/api';

function DocumentsPage() {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    void refreshDocuments();
  }, []);

  async function refreshDocuments() {
    setIsLoading(true);
    setErrorMessage('');
    try {
      setDocuments(await listDocuments());
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  async function handleUpload(file: File) {
    setIsUploading(true);
    setErrorMessage('');
    try {
      await uploadDocument(file);
      await refreshDocuments();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsUploading(false);
    }
  }

  async function handleDelete(document: DocumentItem) {
    if (!window.confirm(`确认删除文档「${document.name}」吗？`)) {
      return;
    }

    setDeletingId(document.id);
    setErrorMessage('');
    try {
      await deleteDocument(document.id);
      await refreshDocuments();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <main className="min-h-screen overflow-hidden bg-paper text-ink">
      <section className="relative mx-auto min-h-screen w-full max-w-6xl px-6 py-10 sm:px-10 lg:px-12">
        <div className="absolute left-[-8rem] top-[-8rem] h-80 w-80 rounded-full bg-wheat/50 blur-3xl" />
        <div className="absolute bottom-[-10rem] right-[-6rem] h-96 w-96 rounded-full bg-moss/20 blur-3xl" />

        <header className="relative z-10 flex flex-col justify-between gap-6 border-b border-ink/10 pb-8 md:flex-row md:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.35em] text-clay">KnowBase</p>
            <h1 className="mt-3 font-display text-4xl font-bold tracking-tight sm:text-6xl">
              文档管理
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-ink/65">
              上传文档、查看处理状态并清理不需要的资料。
            </p>
          </div>
          <button
            className="self-start rounded-full border border-moss/30 bg-white/45 px-5 py-3 text-sm font-bold text-moss shadow-sm transition hover:bg-white/75 md:self-auto"
            onClick={() => void refreshDocuments()}
            type="button"
          >
            刷新列表
          </button>
        </header>

        <div className="relative z-10 grid gap-8 py-10 lg:grid-cols-[0.9fr_1.4fr]">
          <DocumentUpload isUploading={isUploading} onUpload={handleUpload} />

          <section>
            <div className="mb-5 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.25em] text-moss">Documents</p>
                <h2 className="mt-2 font-display text-2xl font-bold">知识库文档</h2>
              </div>
              <span className="rounded-full bg-ink px-4 py-2 text-xs font-bold text-paper">
                {documents.length} 份文档
              </span>
            </div>

            {errorMessage && (
              <div className="mb-5 rounded-2xl border border-clay/30 bg-clay/10 px-5 py-4 text-sm font-semibold text-clay">
                {errorMessage}
              </div>
            )}

            {isLoading ? (
              <div className="rounded-[2rem] border border-ink/10 bg-white/50 p-10 text-center shadow-lg shadow-ink/5">
                <p className="font-display text-2xl font-bold">正在加载文档...</p>
              </div>
            ) : (
              <DocumentTable documents={documents} deletingId={deletingId} onDelete={handleDelete} />
            )}
          </section>
        </div>
      </section>
    </main>
  );
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message;
  }
  return '操作失败，请稍后重试';
}

export default DocumentsPage;
