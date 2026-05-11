import { useState } from 'react';
import { askQuestion } from '../api/qa';
import AnswerPanel from '../components/AnswerPanel';
import QuestionInput from '../components/QuestionInput';
import type { AskResponse } from '../types/api';

function QaPage() {
  const [answer, setAnswer] = useState<AskResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [isAsking, setIsAsking] = useState(false);

  async function handleAsk(question: string) {
    setIsAsking(true);
    setErrorMessage('');
    try {
      setAnswer(await askQuestion(question));
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsAsking(false);
    }
  }

  return (
    <main className="min-h-screen overflow-hidden bg-paper text-ink">
      <section className="relative mx-auto min-h-screen w-full max-w-6xl px-6 py-10 sm:px-10 lg:px-12">
        <div className="absolute right-[-8rem] top-[-8rem] h-80 w-80 rounded-full bg-wheat/50 blur-3xl" />
        <div className="absolute bottom-[-10rem] left-[-6rem] h-96 w-96 rounded-full bg-clay/15 blur-3xl" />

        <header className="relative z-10 border-b border-ink/10 pb-8">
          <p className="text-sm font-semibold uppercase tracking-[0.35em] text-clay">KnowBase</p>
          <h1 className="mt-3 font-display text-4xl font-bold tracking-tight sm:text-6xl">
            知识问答
          </h1>
          <p className="mt-4 max-w-2xl text-base leading-7 text-ink/65">
            基于已上传文档进行基础检索，返回模板回答和引用片段。若没有命中内容，会显示暂无相关内容。
          </p>
        </header>

        <div className="relative z-10 grid gap-8 py-10 lg:grid-cols-[0.9fr_1.4fr]">
          <div>
            <QuestionInput isAsking={isAsking} onAsk={handleAsk} />
            {errorMessage && (
              <div className="mt-5 rounded-2xl border border-clay/30 bg-clay/10 px-5 py-4 text-sm font-semibold text-clay">
                {errorMessage}
              </div>
            )}
          </div>
          <AnswerPanel response={answer} />
        </div>
      </section>
    </main>
  );
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message;
  }
  return '提问失败，请稍后重试';
}

export default QaPage;
