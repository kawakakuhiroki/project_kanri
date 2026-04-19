/*
 * [役割] docs: ドキュメント管理画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const docsError = document.getElementById('docsError');
const docFile = document.getElementById('docFile');
const docTaskId = document.getElementById('docTaskId');
const uploadDocBtn = document.getElementById('uploadDocBtn');
const docsBody = document.getElementById('docsBody');

uploadDocBtn.addEventListener('click', uploadDoc);

function setError(message) {
  docsError.textContent = message || '';
}

async function ensureProject() {
  return window.ProjectContext.ensure(projectId, docsError, 'ドキュメント管理');
}

async function loadDocs() {
  const res = await fetch(`${ctx}/api/docs?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('ドキュメント取得に失敗しました。');
  }
  const list = await res.json();
  docsBody.innerHTML = '';
  if (!list.length) {
    docsBody.innerHTML = '<tr><td colspan="5" class="muted">ドキュメントがありません。</td></tr>';
    return;
  }
  list.forEach((doc) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${doc.id}</td>
      <td>${escapeHtml(doc.filename)}</td>
      <td>${escapeHtml(doc.contentType || '')}</td>
      <td>${doc.taskId ?? ''}</td>
      <td>
        <a class="tab" href="${ctx}/api/docs?id=${doc.id}">ダウンロード</a>
        <button class="secondary" data-delete>削除</button>
      </td>
    `;
    tr.querySelector('button[data-delete]').addEventListener('click', () => deleteDoc(doc.id));
    docsBody.appendChild(tr);
  });
}

async function uploadDoc() {
  setError('');
  const file = docFile.files?.[0];
  if (!file) {
    setError('ファイルを選択してください。');
    return;
  }
  const form = new FormData();
  form.append('file', file);
  const taskId = docTaskId.value.trim();
  if (taskId && !/^\d+$/.test(taskId)) {
    setError('タスクIDは数値で入力してください。');
    return;
  }
  const query = taskId ? `?projectId=${projectId}&taskId=${encodeURIComponent(taskId)}` : `?projectId=${projectId}`;
  try {
    const res = await fetch(`${ctx}/api/docs${query}`, {
      method: 'POST',
      body: form,
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || 'アップロードに失敗しました。');
    }
    docFile.value = '';
    docTaskId.value = '';
    await loadDocs();
  } catch (e) {
    setError(e.message);
  }
}

async function deleteDoc(id) {
  if (!confirm('削除しますか？')) {
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/docs?id=${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '削除に失敗しました。');
    }
    await loadDocs();
  } catch (e) {
    setError(e.message);
  }
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch (e) {
    return null;
  }
}

async function init() {
  if (!(await ensureProject())) {
    return;
  }
  try {
    await loadDocs();
  } catch (e) {
    setError(e.message);
  }
}

init();
