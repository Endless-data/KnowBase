import { useState } from 'react';

interface DocumentUploadProps {
  isUploading: boolean;
  onUpload: (file: File) => Promise<void>;
}

function DocumentUpload({ isUploading, onUpload }: DocumentUploadProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedFile) {
      return;
    }
    await onUpload(selectedFile);
    setSelectedFile(null);
    event.currentTarget.reset();
  }

  return (
    <form
      className="rounded-[2rem] border border-white/70 bg-white/60 p-6 shadow-xl shadow-ink/10 backdrop-blur"
      onSubmit={handleSubmit}
    >
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.25em] text-clay">Upload</p>
        <h2 className="mt-2 font-display text-2xl font-bold">上传知识文档</h2>
        <p className="mt-3 text-sm leading-6 text-ink/65">
          当前支持 `.txt` 和 `.md` 文件。上传后后端会自动解析、切分并写入知识库。
        </p>
      </div>

      <label className="mt-6 block rounded-2xl border border-dashed border-moss/40 bg-paper/70 p-5 text-sm text-ink/70">
        <span className="font-semibold text-moss">选择文件</span>
        <input
          accept=".txt,.md"
          className="mt-3 block w-full text-sm file:mr-4 file:rounded-full file:border-0 file:bg-ink file:px-4 file:py-2 file:text-sm file:font-semibold file:text-paper hover:file:bg-ink/90"
          disabled={isUploading}
          onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
          type="file"
        />
      </label>

      <button
        className="mt-5 w-full rounded-full bg-clay px-5 py-3 text-sm font-bold text-white shadow-lg shadow-clay/20 transition hover:bg-clay/90 disabled:cursor-not-allowed disabled:bg-clay/45"
        disabled={!selectedFile || isUploading}
        type="submit"
      >
        {isUploading ? '上传中...' : '上传文档'}
      </button>
    </form>
  );
}

export default DocumentUpload;
