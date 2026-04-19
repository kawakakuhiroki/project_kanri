/*
 * [役割] kanban: かんばん画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const statusOptions = [
  { key: 'TODO', label: '未着手' },
  { key: 'IN_PROGRESS', label: '進行中' },
  { key: 'REVIEW', label: 'レビュー' },
  { key: 'ON_HOLD', label: '保留' },
  { key: 'DONE', label: '完了' },
];

const priorityOptions = [
  { value: 1, label: '最高' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' },
  { value: 5, label: '最低' },
];

const state = {
  tasks: [],
  users: [],
  selectedId: null,
};

const kanbanBoard = document.getElementById('kanbanBoard');
const kanbanError = document.getElementById('kanbanError');
const addKanbanTaskBtn = document.getElementById('addKanbanTaskBtn');
const kanbanForm = document.getElementById('kanbanForm');
const kanbanTaskName = document.getElementById('kanbanTaskName');
const kanbanTaskDue = document.getElementById('kanbanTaskDue');
const kanbanTaskPriority = document.getElementById('kanbanTaskPriority');
const kanbanTaskAssignee = document.getElementById('kanbanTaskAssignee');
const createKanbanTaskBtn = document.getElementById('createKanbanTaskBtn');
const cancelKanbanTaskBtn = document.getElementById('cancelKanbanTaskBtn');

const kanbanDetail = document.getElementById('kanbanDetail');
const kanbanDetailEmpty = document.getElementById('kanbanDetailEmpty');
const kanbanDetailForm = document.getElementById('kanbanDetailForm');
const detailName = document.getElementById('detailName');
const detailStatus = document.getElementById('detailStatus');
const detailPriority = document.getElementById('detailPriority');
const detailAssignee = document.getElementById('detailAssignee');
const detailStart = document.getElementById('detailStart');
const detailEnd = document.getElementById('detailEnd');
const detailDue = document.getElementById('detailDue');
const detailProgress = document.getElementById('detailProgress');
const detailDescription = document.getElementById('detailDescription');
const detailPlanned = document.getElementById('detailPlanned');
const detailActual = document.getElementById('detailActual');
const saveKanbanDetailBtn = document.getElementById('saveKanbanDetailBtn');

addKanbanTaskBtn.addEventListener('click', () => {
  kanbanForm.classList.toggle('hidden');
  kanbanError.textContent = '';
});

cancelKanbanTaskBtn.addEventListener('click', () => {
  kanbanForm.classList.add('hidden');
  kanbanError.textContent = '';
});

createKanbanTaskBtn.addEventListener('click', createTask);
saveKanbanDetailBtn.addEventListener('click', saveDetail);

function setError(message) {
  kanbanError.textContent = message;
}

async function ensureProject() {
  return window.ProjectContext.ensure(projectId, kanbanError, 'かんばん');
}

async function loadUsers() {
  try {
    const res = await fetch(`${ctx}/api/users`);
    if (!res.ok) {
      state.users = [];
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
  const tasks = await res.json();
  state.tasks = (tasks || []).map((t) => ({ ...t, status: normalizeStatus(t.status) }));
}

async function init() {
  if (!(await ensureProject())) {
    return;
  }
  try {
    await loadUsers();
    await loadTasks();
    renderAssigneeOptions(kanbanTaskAssignee, true);
    renderAssigneeOptions(detailAssignee, true);
    renderStatusOptions(detailStatus);
    renderPriorityOptions(detailPriority);
    renderBoard();
    syncDetail();
  } catch (e) {
    setError(e.message);
  }
}

function renderStatusOptions(select) {
  select.innerHTML = '';
  statusOptions.forEach((opt) => {
    const option = document.createElement('option');
    option.value = opt.key;
    option.textContent = opt.label;
    select.appendChild(option);
  });
}

function renderPriorityOptions(select) {
  select.innerHTML = '';
  priorityOptions.forEach((opt) => {
    const option = document.createElement('option');
    option.value = String(opt.value);
    option.textContent = opt.label;
    select.appendChild(option);
  });
}

function renderAssigneeOptions(select, includeBlank) {
  select.innerHTML = '';
  if (includeBlank) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = '未設定';
    select.appendChild(option);
  }
  state.users.forEach((u) => {
    const option = document.createElement('option');
    option.value = String(u.id);
    option.textContent = u.name || u.loginId || `User#${u.id}`;
    select.appendChild(option);
  });
}

function leafTasks(tasks) {
  const parents = new Set();
  tasks.forEach((t) => {
    if (t.parentId) {
      parents.add(t.parentId);
    }
  });
  return tasks.filter((t) => !parents.has(t.id) && t.taskType !== 'SUMMARY');
}

function normalizeStatus(status) {
  if (!status) {
    return 'TODO';
  }
  if (status === 'DOING') {
    return 'IN_PROGRESS';
  }
  return status;
}

function renderBoard() {
  kanbanBoard.innerHTML = '';
  const tasks = leafTasks(state.tasks);
  statusOptions.forEach((status) => {
    const column = document.createElement('div');
    column.className = 'kanban-column';
    column.dataset.status = status.key;
    column.innerHTML = `
      <div class="kanban-column-header">
        <span>${status.label}</span>
        <span class="badge">${tasks.filter((t) => normalizeStatus(t.status) === status.key).length}</span>
      </div>
      <div class="kanban-column-body"></div>
    `;
    const body = column.querySelector('.kanban-column-body');
    body.addEventListener('dragover', (ev) => {
      ev.preventDefault();
    });
    body.addEventListener('drop', (ev) => {
      ev.preventDefault();
      const id = Number(ev.dataTransfer.getData('text/plain'));
      const task = state.tasks.find((t) => t.id === id);
      if (task && (task.status || 'TODO') !== status.key) {
        updateTask(task, { status: status.key });
      }
    });

    tasks
      .filter((t) => normalizeStatus(t.status) === status.key)
      .sort((a, b) => (a.priority || 3) - (b.priority || 3))
      .forEach((task) => {
        body.appendChild(createCard(task));
      });

    kanbanBoard.appendChild(column);
  });
}

function createCard(task) {
  const card = document.createElement('div');
  card.className = 'kanban-card';
  card.draggable = true;
  card.dataset.id = task.id;
  const assignee = findUserName(task.assignedUserId);
  const due = task.dueDate || task.endDate || '';
  const priority = priorityOptions.find((p) => p.value === Number(task.priority || 3));
  card.innerHTML = `
    <div class="title">${escapeHtml(task.name || '')}</div>
    <div class="meta">
      ${assignee ? `<span>担当:${escapeHtml(assignee)}</span>` : '<span>担当:未設定</span>'}
      ${due ? `<span>期日:${due}</span>` : '<span>期日:-</span>'}
      <span>優先:${priority ? priority.label : '中'}</span>
      <span>進捗:${task.progress ?? 0}%</span>
    </div>
  `;
  card.addEventListener('click', () => {
    state.selectedId = task.id;
    syncDetail();
  });
  card.addEventListener('dragstart', (ev) => {
    ev.dataTransfer.setData('text/plain', String(task.id));
    card.classList.add('dragging');
  });
  card.addEventListener('dragend', () => {
    card.classList.remove('dragging');
  });
  return card;
}

function syncDetail() {
  const task = state.tasks.find((t) => t.id === state.selectedId);
  if (!task) {
    kanbanDetailEmpty.classList.remove('hidden');
    kanbanDetailForm.classList.add('hidden');
    return;
  }
  kanbanDetailEmpty.classList.add('hidden');
  kanbanDetailForm.classList.remove('hidden');
  detailName.value = task.name || '';
  detailStatus.value = normalizeStatus(task.status);
  detailPriority.value = String(task.priority || 3);
  detailAssignee.value = task.assignedUserId ? String(task.assignedUserId) : '';
  detailStart.value = task.startDate || '';
  detailEnd.value = task.endDate || '';
  detailDue.value = task.dueDate || '';
  detailProgress.value = task.progress ?? 0;
  detailDescription.value = task.description || '';
  detailPlanned.value = task.plannedHours ?? 0;
  detailActual.value = task.actualHours ?? 0;
}

async function createTask() {
  const name = kanbanTaskName.value.trim();
  if (!name) {
    setError('タスク名は必須です。');
    return;
  }
  const payload = {
    name,
    status: 'TODO',
    priority: Number(kanbanTaskPriority.value || 3),
    assignedUserId: kanbanTaskAssignee.value ? Number(kanbanTaskAssignee.value) : null,
    dueDate: kanbanTaskDue.value || null,
    progress: 0,
    taskType: 'TASK',
  };
  try {
    const res = await fetch(`${ctx}/api/tasks?projectId=${projectId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || 'タスク作成に失敗しました。');
    }
    const created = await res.json();
    state.tasks.push(created);
    kanbanTaskName.value = '';
    kanbanTaskDue.value = '';
    kanbanForm.classList.add('hidden');
    renderBoard();
  } catch (e) {
    setError(e.message);
  }
}

async function saveDetail() {
  const task = state.tasks.find((t) => t.id === state.selectedId);
  if (!task) {
    return;
  }
  const updated = {
    ...task,
    name: detailName.value.trim(),
    status: detailStatus.value,
    priority: Number(detailPriority.value || 3),
    assignedUserId: detailAssignee.value ? Number(detailAssignee.value) : null,
    startDate: detailStart.value || null,
    endDate: detailEnd.value || null,
    dueDate: detailDue.value || null,
    progress: clamp(Number(detailProgress.value || 0), 0, 100),
    description: detailDescription.value.trim() || null,
    plannedHours: Number(detailPlanned.value || 0),
    actualHours: Number(detailActual.value || 0),
  };
  if (!updated.name) {
    setError('タスク名は必須です。');
    return;
  }
  await updateTask(task, updated, true);
}

async function updateTask(task, updates, keepSelection) {
  const payload = { ...task, ...updates };
  try {
    const res = await fetch(`${ctx}/api/tasks/${task.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '更新に失敗しました。');
    }
    const idx = state.tasks.findIndex((t) => t.id === task.id);
    if (idx !== -1) {
      state.tasks[idx] = payload;
    }
    renderBoard();
    if (keepSelection) {
      syncDetail();
    }
  } catch (e) {
    setError(e.message);
  }
}

function findUserName(id) {
  if (!id) {
    return '';
  }
  const user = state.users.find((u) => u.id === id);
  return user?.name || user?.loginId || `User#${id}`;
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch (e) {
    return null;
  }
}

init();
