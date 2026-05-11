import { useState } from 'react';
import DocumentsPage from './pages/DocumentsPage';
import QaPage from './pages/QaPage';

type ActivePage = 'documents' | 'qa';

function App() {
  const [activePage, setActivePage] = useState<ActivePage>('documents');

  return (
    <>
      <nav className="fixed left-1/2 top-4 z-50 flex -translate-x-1/2 rounded-full border border-ink/10 bg-white/75 p-1 shadow-xl shadow-ink/10 backdrop-blur">
        <button
          className={`rounded-full px-5 py-2 text-sm font-bold transition ${
            activePage === 'documents' ? 'bg-ink text-paper' : 'text-ink/65 hover:text-ink'
          }`}
          onClick={() => setActivePage('documents')}
          type="button"
        >
          文档管理
        </button>
        <button
          className={`rounded-full px-5 py-2 text-sm font-bold transition ${
            activePage === 'qa' ? 'bg-ink text-paper' : 'text-ink/65 hover:text-ink'
          }`}
          onClick={() => setActivePage('qa')}
          type="button"
        >
          知识问答
        </button>
      </nav>

      {activePage === 'documents' ? <DocumentsPage /> : <QaPage />}
    </>
  );
}

export default App;
