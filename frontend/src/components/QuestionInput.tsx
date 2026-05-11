import { useState } from 'react';

interface QuestionInputProps {
  isAsking: boolean;
  onAsk: (question: string) => Promise<void>;
}

function QuestionInput({ isAsking, onAsk }: QuestionInputProps) {
  const [question, setQuestion] = useState('');

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) {
      return;
    }
    await onAsk(trimmedQuestion);
  }

  return (
    <form
      className="rounded-[2rem] border border-white/70 bg-white/60 p-6 shadow-xl shadow-ink/10 backdrop-blur"
      onSubmit={handleSubmit}
    >
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.25em] text-clay">Ask</p>
        <h2 className="mt-2 font-display text-2xl font-bold">向知识库提问</h2>
        <p className="mt-3 text-sm leading-6 text-ink/65">
          输入自然语言问题。MVP 阶段会基于已上传文档的 chunk 生成规则回答。
        </p>
      </div>

      <textarea
        className="mt-6 min-h-40 w-full resize-y rounded-3xl border border-ink/10 bg-paper/80 px-5 py-4 text-base leading-7 outline-none ring-0 transition placeholder:text-ink/35 focus:border-moss/50 focus:bg-white/80"
        disabled={isAsking}
        onChange={(event) => setQuestion(event.target.value)}
        placeholder="例如：KnowBase 是什么？"
        value={question}
      />

      <button
        className="mt-5 w-full rounded-full bg-ink px-5 py-3 text-sm font-bold text-paper shadow-lg shadow-ink/10 transition hover:bg-ink/90 disabled:cursor-not-allowed disabled:bg-ink/45"
        disabled={!question.trim() || isAsking}
        type="submit"
      >
        {isAsking ? '思考中...' : '提交问题'}
      </button>
    </form>
  );
}

export default QuestionInput;
