import { useState } from 'react';

interface DocumentUploadProps {
  isUploading: boolean;
  onUpload: (files: File[]) => Promise<void>;
}

function DocumentUpload({ isUploading, onUpload }: DocumentUploadProps) {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (selectedFiles.length === 0) {
      return;
    }
    await onUpload(selectedFiles);
    setSelectedFiles([]);
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
          multiple
          onChange={(event) => setSelectedFiles(Array.from(event.target.files ?? []))}
          type="file"
        />
      </label>

      {selectedFiles.length > 0 && (
        <div className="mt-4 rounded-2xl bg-white/55 p-4 text-sm text-ink/70">
          <p className="font-bold text-ink">已选择 {selectedFiles.length} 个文件</p>
          <ul className="mt-3 space-y-2">
            {selectedFiles.map((file) => (
              <li className="truncate rounded-full bg-paper/80 px-3 py-2" key={`${file.name}-${file.size}-${file.lastModified}`}>
                {file.name}
              </li>
            ))}
          </ul>
        </div>
      )}

      <button
        className="mt-5 w-full rounded-full bg-clay px-5 py-3 text-sm font-bold text-white shadow-lg shadow-clay/20 transition hover:bg-clay/90 disabled:cursor-not-allowed disabled:bg-clay/45"
        disabled={selectedFiles.length === 0 || isUploading}
        type="submit"
      >
        {isUploading ? '上传中...' : `上传 ${selectedFiles.length || ''} 个文档`}
      </button>
    </form>
  );
}

export default DocumentUpload;
