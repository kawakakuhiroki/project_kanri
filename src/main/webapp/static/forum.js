/*
 * [役割] forum: フォーラム/チャット画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const forumError = document.getElementById('forumError');
const threadList = document.getElementById('threadList');
const threadTitleInput = document.getElementById('threadTitle');
const newThreadBtn = document.getElementById('newThreadBtn');
const createThreadBtn = document.getElementById('createThreadBtn');
const threadForm = document.getElementById('threadForm');
const postList = document.getElementById('postList');
const postBody = document.getElementById('postBody');
const postBtn = document.getElementById('postBtn');
const threadTitleLabel = document.getElementById('threadTitleLabel');
const chatList = document.getElementById('chatList');
const chatInput = document.getElementById('chatInput');
const chatSendBtn = document.getElementById('chatSendBtn');

const state = {
  users: [],
  threads: [],
  posts: [],
  chat: [],
  selectedThreadId: null,
};

newThreadBtn.addEventListener('click', () => {
  threadForm.classList.toggle('hidden');
});
createThreadBtn.addEventListener('click', createThread);
postBtn.addEventListener('click', createPost);
chatSendBtn.addEventListener('click', sendChat);

function setError(message) {
  forumError.textContent = message || '';
}

async function ensureProject() {
  return window.ProjectContext.ensure(projectId, forumError, 'フォーラム');
}

async function loadUsers() {
  try {
    const res = await fetch(`${ctx}/api/users`);
    if (!res.ok) {
      return;
    }
    const users = await res.json();
    state.users = (users || []).filter((u) => u.active !== false);
  } catch (e) {
    state.users = [];
  }
}

async function loadThreads() {
  const res = await fetch(`${ctx}/api/forum?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('スレッド取得に失敗しました。');
  }
  state.threads = (await res.json()) || [];
}

async function loadPosts(threadId) {
  const res = await fetch(`${ctx}/api/forum/posts?threadId=${threadId}`);
  if (!res.ok) {
    throw new Error('投稿取得に失敗しました。');
  }
  state.posts = (await res.json()) || [];
  renderPosts();
}

async function loadChat() {
  const res = await fetch(`${ctx}/api/chat?projectId=${projectId}`);
  if (!res.ok) {
    return;
  }
  const list = (await res.json()) || [];
  state.chat = list.reverse();
  renderChat();
}

function renderThreads() {
  threadList.innerHTML = '';
  if (!state.threads.length) {
    threadList.innerHTML = '<div class="muted">スレッドがありません。</div>';
    return;
  }
  state.threads.forEach((thread) => {
    const item = document.createElement('div');
    item.className = 'thread-item';
    if (thread.id === state.selectedThreadId) {
      item.classList.add('active');
    }
    item.innerHTML = `
      <div><strong>${escapeHtml(thread.title)}</strong></div>
      <div class="muted small">${formatDateTime(thread.createdAt)} ${escapeHtml(findUserName(thread.createdBy))}</div>
    `;
    item.addEventListener('click', () => {
      state.selectedThreadId = thread.id;
      threadTitleLabel.textContent = `投稿: ${thread.title}`;
      renderThreads();
      loadPosts(thread.id).catch((e) => setError(e.message));
    });
    threadList.appendChild(item);
  });
}

function renderPosts() {
  postList.innerHTML = '';
  if (!state.posts.length) {
    postList.innerHTML = '<div class="muted">投稿がありません。</div>';
    return;
  }
  state.posts.forEach((post) => {
    const item = document.createElement('div');
    item.className = 'post-item';
    item.innerHTML = `
      <div>${escapeHtml(post.body)}</div>
      <div class="muted small">${formatDateTime(post.createdAt)} ${escapeHtml(findUserName(post.createdBy))}</div>
    `;
    postList.appendChild(item);
  });
  postList.scrollTop = postList.scrollHeight;
}

function renderChat() {
  chatList.innerHTML = '';
  if (!state.chat.length) {
    chatList.innerHTML = '<div class="muted">メッセージがありません。</div>';
    return;
  }
  state.chat.forEach((msg) => {
    const div = document.createElement('div');
    div.className = 'chat-item';
    div.innerHTML = `
      <div>${escapeHtml(msg.message)}</div>
      <div class="muted small">${formatDateTime(msg.createdAt)} ${escapeHtml(findUserName(msg.userId))}</div>
    `;
    chatList.appendChild(div);
  });
  chatList.scrollTop = chatList.scrollHeight;
}

async function createThread() {
  const title = threadTitleInput.value.trim();
  if (!title) {
    setError('スレッドタイトルを入力してください。');
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/forum?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title }),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || 'スレッド作成に失敗しました。');
    }
    const created = await res.json();
    state.threads.unshift(created);
    threadTitleInput.value = '';
    threadForm.classList.add('hidden');
    state.selectedThreadId = created.id;
    renderThreads();
    loadPosts(created.id);
  } catch (e) {
    setError(e.message);
  }
}

async function createPost() {
  if (!state.selectedThreadId) {
    setError('スレッドを選択してください。');
    return;
  }
  const body = postBody.value.trim();
  if (!body) {
    setError('投稿内容を入力してください。');
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/forum/posts?threadId=${state.selectedThreadId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ body }),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '投稿に失敗しました。');
    }
    postBody.value = '';
    await loadPosts(state.selectedThreadId);
  } catch (e) {
    setError(e.message);
  }
}

async function sendChat() {
  const message = chatInput.value.trim();
  if (!message) {
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/chat?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '送信に失敗しました。');
    }
    chatInput.value = '';
    await loadChat();
  } catch (e) {
    setError(e.message);
  }
}

function findUserName(id) {
  if (!id) {
    return 'system';
  }
  const user = state.users.find((u) => u.id === id);
  return user?.name || user?.loginId || `User#${id}`;
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
    await loadUsers();
    await loadThreads();
    if (state.threads.length) {
      state.selectedThreadId = state.threads[0].id;
      threadTitleLabel.textContent = `投稿: ${state.threads[0].title}`;
      await loadPosts(state.selectedThreadId);
    }
    renderThreads();
    await loadChat();
    setInterval(loadChat, 10000);
  } catch (e) {
    setError(e.message);
  }
}

init();
