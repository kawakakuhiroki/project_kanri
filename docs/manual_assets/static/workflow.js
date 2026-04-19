/*
 * [役割] workflow: 承認ワークフロー画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const workflowError = document.getElementById('workflowError');
const workflowTask = document.getElementById('workflowTask');
const workflowApprover = document.getElementById('workflowApprover');
const workflowMessage = document.getElementById('workflowMessage');
const createWorkflowBtn = document.getElementById('createWorkflowBtn');
const workflowBody = document.getElementById('workflowBody');

const state = {
  tasks: [],
  users: [],
  workflows: [],
};

createWorkflowBtn.addEventListener('click', createWorkflow);

function setError(message) {
  workflowError.textContent = message || '';
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
      window.location.href = `${ctx}/workflow?projectId=${projects[0].id}`;
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

async function loadTasks() {
  const res = await fetch(`${ctx}/api/tasks?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('タスク取得に失敗しました。');
  }
  const tasks = (await res.json()) || [];
  const parents = new Set(tasks.filter((t) => t.parentId).map((t) => t.parentId));
  state.tasks = tasks.filter((t) => !parents.has(t.id) && t.taskType !== 'SUMMARY');
}

async function loadWorkflows() {
  const res = await fetch(`${ctx}/api/workflow?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('ワークフロー取得に失敗しました。');
  }
  state.workflows = (await res.json()) || [];
}

function renderSelects() {
  workflowTask.innerHTML = '';
  state.tasks.forEach((t) => {
    const opt = document.createElement('option');
    opt.value = String(t.id);
    opt.textContent = t.name;
    workflowTask.appendChild(opt);
  });
  workflowApprover.innerHTML = '';
  state.users.forEach((u) => {
    const opt = document.createElement('option');
    opt.value = String(u.id);
    opt.textContent = u.name || u.loginId || `User#${u.id}`;
    workflowApprover.appendChild(opt);
  });
}

function renderWorkflows() {
  workflowBody.innerHTML = '';
  if (!state.workflows.length) {
    workflowBody.innerHTML = '<tr><td colspan="7" class="muted">申請がありません。</td></tr>';
    return;
  }
  state.workflows.forEach((wf) => {
    const tr = document.createElement('tr');
    const statusSelect = document.createElement('select');
    ['PENDING', 'APPROVED', 'REJECTED'].forEach((s) => {
      const opt = document.createElement('option');
      opt.value = s;
      opt.textContent = s;
      if (s === wf.status) {
        opt.selected = true;
      }
      statusSelect.appendChild(opt);
    });
    const msgInput = document.createElement('input');
    msgInput.type = 'text';
    msgInput.value = wf.message || '';
    const updateBtn = document.createElement('button');
    updateBtn.className = 'secondary';
    updateBtn.textContent = '更新';
    updateBtn.addEventListener('click', () => updateWorkflow(wf, {
      status: statusSelect.value,
      message: msgInput.value,
    }));

    tr.innerHTML = `
      <td>${wf.id}</td>
      <td>${escapeHtml(findTaskName(wf.taskId))}</td>
      <td>${escapeHtml(findUserName(wf.requesterId))}</td>
      <td>${escapeHtml(findUserName(wf.approverId))}</td>
    `;
    const statusCell = document.createElement('td');
    statusCell.appendChild(statusSelect);
    const messageCell = document.createElement('td');
    messageCell.appendChild(msgInput);
    const actionCell = document.createElement('td');
    actionCell.appendChild(updateBtn);
    tr.appendChild(statusCell);
    tr.appendChild(messageCell);
    tr.appendChild(actionCell);
    workflowBody.appendChild(tr);
  });
}

async function createWorkflow() {
  setError('');
  if (!workflowTask.value) {
    setError('タスクを選択してください。');
    return;
  }
  if (!workflowApprover.value) {
    setError('承認者を選択してください。');
    return;
  }
  const payload = {
    taskId: Number(workflowTask.value),
    approverId: Number(workflowApprover.value),
    status: 'PENDING',
    message: workflowMessage.value.trim() || null,
  };
  try {
    const res = await fetch(`${ctx}/api/workflow?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '申請に失敗しました。');
    }
    workflowMessage.value = '';
    await loadWorkflows();
    renderWorkflows();
  } catch (e) {
    setError(e.message);
  }
}

async function updateWorkflow(workflow, updates) {
  setError('');
  const payload = { ...workflow, ...updates };
  try {
    const res = await fetch(`${ctx}/api/workflow/${workflow.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '更新に失敗しました。');
    }
    await loadWorkflows();
    renderWorkflows();
  } catch (e) {
    setError(e.message);
  }
}

function findUserName(id) {
  if (!id) {
    return '未設定';
  }
  const user = state.users.find((u) => u.id === id);
  return user?.name || user?.loginId || `User#${id}`;
}

function findTaskName(id) {
  const task = state.tasks.find((t) => t.id === id);
  return task?.name || `#${id}`;
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
    await loadTasks();
    await loadWorkflows();
    renderSelects();
    renderWorkflows();
  } catch (e) {
    setError(e.message);
  }
}

init();
