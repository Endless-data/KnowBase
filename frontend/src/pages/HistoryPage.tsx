import { useEffect, useState } from 'react';
import { getHistory, listHistory } from '../api/history';
import HistoryDetail from '../components/HistoryDetail';
import HistoryList from '../components/HistoryList';
import type { HistoryDetail as HistoryDetailType, HistoryListItem } from '../types/api';

function HistoryPage() {
  const [histories, setHistories] = useState<HistoryListItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [detail, setDetail] = useState<HistoryDetailType | null>(null);
  const [isLoadingList, setIsLoadingList] = useState(true);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    void refreshHistory();
  }, []);

  async function refreshHistory() {
    setIsLoadingList(true);
    setErrorMessage('');

    try {
      const nextHistories = await listHistory();
      const firstHistory = nextHistories[0] ?? null;

      setHistories(nextHistories);
      setSelectedId(firstHistory?.id ?? null);
      setDetail(null);

      if (firstHistory) {
        await loadDetail(firstHistory.id);
      }
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoadingList(false);
    }
  }

  async function loadDetail(id: number) {
    setSelectedId(id);
    setIsLoadingDetail(true);
    setErrorMessage('');

    try {
      setDetail(await getHistory(id));
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoadingDetail(false);
    }
  }

  return (
    <main className="min-h-screen overflow-hidden bg-paper text-ink">
      <section className="relative mx-auto min-h-screen w-full max-w-6xl px-6 py-10 sm:px-10 lg:px-12">
        <div className="absolute left-[-8rem] top-[-8rem] h-80 w-80 rounded-full bg-moss/15 blur-3xl" />
        <div className="absolute bottom-[-10rem] right-[-6rem] h-96 w-96 rounded-full bg-wheat/50 blur-3xl" />

        <header className="relative z-10 flex flex-col gap-6 border-b border-ink/10 pb-8 pt-14 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.35em] text-clay">KnowBase</p>
            <h1 className="mt-3 font-display text-4xl font-bold tracking-tight sm:text-6xl">历史记录</h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-ink/65">查看已经保存的问答记录，并回溯每次回答使用过的引用片段。</p>
          </div>
          <button
            className="w-fit rounded-full border border-ink/10 bg-white/60 px-5 py-3 text-sm font-bold text-ink shadow-lg shadow-ink/5 transition hover:border-moss/40 hover:bg-white"
            disabled={isLoadingList || isLoadingDetail}
            onClick={() => void refreshHistory()}
            type="button"
          >
            刷新历史
          </button>
        </header>

        {errorMessage && (
          <div className="relative z-10 mt-6 rounded-2xl border border-clay/30 bg-clay/10 px-5 py-4 text-sm font-semibold text-clay">
            {errorMessage}
          </div>
        )}

        <div className="relative z-10 grid gap-8 py-10 lg:grid-cols-[0.85fr_1.45fr]">
          <HistoryList histories={histories} isLoading={isLoadingList} onSelect={(id) => void loadDetail(id)} selectedId={selectedId} />
          <HistoryDetail detail={detail} isLoading={isLoadingDetail} />
        </div>
      </section>
    </main>
  );
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '历史记录加载失败，请稍后重试';
}

export default HistoryPage;
