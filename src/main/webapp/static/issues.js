/*
 * [役割] issues: 不具合管理画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const issuesError = document.getElementById('issuesError');
const issueTitle = document.getElementById('issueTitle');
const issueDesc = document.getElementById('issueDesc');
const issuePriority = document.getElementById('issuePriority');
const issueSeverity = document.getElementById('issueSeverity');
const createIssueBtn = document.getElementById('createIssueBtn');
const issuesBody = document.getElementById('issuesBody');

const statusOptions = [
  { value: 'OPEN', label: 'OPEN' },
  { value: 'IN_PROGRESS', label: 'IN_PROGRESS' },
  { value: 'RESOLVED', label: 'RESOLVED' },
  { value: 'CLOSED', label: 'CLOSED' },
];

const severityOptions = [
  { value: 'CRITICAL', label: 'CRITICAL' },
  { value: 'HIGH', label: 'HIGH' },
  { value: 'MEDIUM', label: 'MEDIUM' },
  { value: 'LOW', label: 'LOW' },
];

const priorityOptions = [
  { value: 1, label: '最高' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' },
  { value: 5, label: '最低' },
];

const state = {
  users: [],
  issues: [],
};

createIssueBtn.addEventListener('click', createIssue);

function setError(message) {
  issuesError.textContent = message || '';
}

async function ensureProject() {
  if (projectId) {
    return true;
  }
  try {
    const res = await fetch(`${ctx}/api/projects`);
    if (!res.ok) {
      throw new Error('プロジェクト一覧の取得に失敗しました。');
    }
    const projects = await res.json();
    if (projects.length) {
      window.location.href = `${ctx}/issues?projectId=${projects[0].id}`;
      return false;
    }
    setError('プロジェクトがありません。先に作成してください。');
  } catch (e) {
    setError(e.message);
  }
  return false;
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

async function loadIssues() {
  const res = await fetch(`${ctx}/api/issues?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('不具合一覧の取得に失敗しました。');
  }
  state.issues = (await res.json()) || [];
  renderIssues();
}

function renderIssues() {
  issuesBody.innerHTML = '';
  if (!state.issues.length) {
    issuesBody.innerHTML = '<tr><td colspan="6" class="muted">不具合がありません。</td></tr>';
    return;
  }
  state.issues.forEach((issue) => {
    const tr = document.createElement('tr');
    const statusSelect = buildSelect(statusOptions, issue.status || 'OPEN');
    const prioritySelect = buildSelect(priorityOptions, issue.priority || 3, true);
    const severitySelect = buildSelect(severityOptions, issue.severity || 'MEDIUM');
    const assigneeSelect = buildUserSelect(issue.assigneeId);
    const updateBtn = document.createElement('button');
    updateBtn.textContent = '更新';
    updateBtn.className = 'secondary';
    updateBtn.addEventListener('click', () => updateIssue(issue, {
      status: statusSelect.value,
      priority: Number(prioritySelect.value || 3),
      severity: severitySelect.value,
      assigneeId: assigneeSelect.value ? Number(assigneeSelect.value) : null,
    }));
    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = '削除';
    deleteBtn.className = 'secondary';
    deleteBtn.addEventListener('click', () => deleteIssue(issue.id));

    const titleCell = document.createElement('td');
    titleCell.innerHTML = `${escapeHtml(issue.title)}<div class="muted small">${escapeHtml(issue.description || '')}</div>`;

    const statusCell = document.createElement('td');
    statusCell.appendChild(statusSelect);

    const priorityCell = document.createElement('td');
    priorityCell.appendChild(prioritySelect);

    const severityCell = document.createElement('td');
    severityCell.appendChild(severitySelect);

    const actionCell = document.createElement('td');
    actionCell.appendChild(assigneeSelect);
    actionCell.appendChild(updateBtn);
    actionCell.appendChild(deleteBtn);
    actionCell.classList.add('row');

    tr.innerHTML = `
      <td>${issue.id}</td>
    `;
    tr.appendChild(titleCell);
    tr.appendChild(statusCell);
    tr.appendChild(priorityCell);
    tr.appendChild(severityCell);
    tr.appendChild(actionCell);
    issuesBody.appendChild(tr);
  });
}

function buildSelect(options, value, numeric) {
  const select = document.createElement('select');
  options.forEach((opt) => {
    const option = document.createElement('option');
    option.value = String(opt.value);
    option.textContent = opt.label;
    if (String(opt.value) === String(value)) {
      option.selected = true;
    }
    select.appendChild(option);
  });
  if (numeric) {
    select.value = String(value || 3);
  }
  return select;
}

function buildUserSelect(value) {
  const select = document.createElement('select');
  const empty = document.createElement('option');
  empty.value = '';
  empty.textContent = '未設定';
  select.appendChild(empty);
  state.users.forEach((u) => {
    const opt = document.createElement('option');
    opt.value = String(u.id);
    opt.textContent = u.name || u.loginId || `User#${u.id}`;
    if (value && u.id === value) {
      opt.selected = true;
    }
    select.appendChild(opt);
  });
  return select;
}

async function createIssue() {
  setError('');
  const title = issueTitle.value.trim();
  if (!title) {
    setError('タイトルは必須です。');
    return;
  }
  const payload = {
    title,
    description: issueDesc.value.trim() || null,
    priority: Number(issuePriority.value || 3),
    severity: issueSeverity.value || 'MEDIUM',
  };
  try {
    const res = await fetch(`${ctx}/api/issues?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '登録に失敗しました。');
    }
    issueTitle.value = '';
    issueDesc.value = '';
    issuePriority.value = '3';
    issueSeverity.value = 'MEDIUM';
    await loadIssues();
  } catch (e) {
    setError(e.message);
  }
}

async function updateIssue(issue, updates) {
  setError('');
  const status = updates.status;
  let resolvedAt = issue.resolvedAt;
  if ((status === 'RESOLVED' || status === 'CLOSED') && !resolvedAt) {
    resolvedAt = nowLocal();
  }
  if (status === 'OPEN' || status === 'IN_PROGRESS') {
    resolvedAt = null;
  }
  const payload = {
    ...issue,
    ...updates,
    resolvedAt,
  };
  try {
    const res = await fetch(`${ctx}/api/issues/${issue.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '更新に失敗しました。');
    }
    await loadIssues();
  } catch (e) {
    setError(e.message);
  }
}

async function deleteIssue(id) {
  if (!confirm('削除しますか？')) {
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/issues/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '削除に失敗しました。');
    }
    await loadIssues();
  } catch (e) {
    setError(e.message);
  }
}

function nowLocal() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
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
    await loadIssues();
  } catch (e) {
    setError(e.message);
  }
}

init();
