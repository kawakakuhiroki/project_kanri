/*
 * [役割] wiki: Wiki画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const wikiError = document.getElementById('wikiError');
const wikiList = document.getElementById('wikiList');
const newWikiBtn = document.getElementById('newWikiBtn');
const wikiTitle = document.getElementById('wikiTitle');
const wikiContent = document.getElementById('wikiContent');
const wikiMeta = document.getElementById('wikiMeta');
const saveWikiBtn = document.getElementById('saveWikiBtn');

const state = {
  pages: [],
  current: null,
};

newWikiBtn.addEventListener('click', () => {
  state.current = null;
  wikiTitle.value = '';
  wikiContent.value = '';
  wikiMeta.textContent = '';
  renderList();
});

saveWikiBtn.addEventListener('click', savePage);

function setError(message) {
  wikiError.textContent = message || '';
}

async function ensureProject() {
  return window.ProjectContext.ensure(projectId, wikiError, 'Wiki');
}

async function loadPages() {
  const res = await fetch(`${ctx}/api/wiki?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('Wiki取得に失敗しました。');
  }
  state.pages = (await res.json()) || [];
}

function renderList() {
  wikiList.innerHTML = '';
  if (!state.pages.length) {
    wikiList.innerHTML = '<div class="muted">ページがありません。</div>';
    return;
  }
  state.pages.forEach((page) => {
    const item = document.createElement('div');
    item.className = 'wiki-item';
    if (state.current && state.current.id === page.id) {
      item.classList.add('active');
    }
    item.innerHTML = `
      <div><strong>${escapeHtml(page.title)}</strong></div>
      <div class="muted small">更新:${formatDateTime(page.updatedAt)} v${page.version}</div>
    `;
    item.addEventListener('click', () => {
      state.current = page;
      wikiTitle.value = page.title || '';
      wikiContent.value = page.content || '';
      wikiMeta.textContent = page.updatedAt ? `最終更新: ${formatDateTime(page.updatedAt)} v${page.version}` : '';
      renderList();
    });
    wikiList.appendChild(item);
  });
}

async function savePage() {
  setError('');
  const title = wikiTitle.value.trim();
  if (!title) {
    setError('タイトルは必須です。');
    return;
  }
  const payload = {
    id: state.current ? state.current.id : null,
    title,
    content: wikiContent.value || '',
    version: state.current ? state.current.version : null,
  };
  try {
    const res = await fetch(`${ctx}/api/wiki?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '保存に失敗しました。');
    }
    const saved = await res.json();
    await loadPages();
    state.current = state.pages.find((p) => p.id === saved.id) || null;
    renderList();
    if (state.current) {
      wikiMeta.textContent = `最終更新: ${formatDateTime(state.current.updatedAt)} v${state.current.version}`;
    }
  } catch (e) {
    setError(e.message);
  }
}

function formatDateTime(value) {
  if (!value) {
    return '';
  }
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return String(value);
  }
  return d.toLocaleString('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
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
    await loadPages();
    if (state.pages.length) {
      state.current = state.pages[0];
      wikiTitle.value = state.current.title || '';
      wikiContent.value = state.current.content || '';
      wikiMeta.textContent = `最終更新: ${formatDateTime(state.current.updatedAt)} v${state.current.version}`;
    }
    renderList();
  } catch (e) {
    setError(e.message);
  }
}

init();
