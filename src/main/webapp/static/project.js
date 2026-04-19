/*
 * [役割] project: プロジェクト詳細画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const state = {
  project: {},
  tasks: [],
  dependencies: [],
  resources: [],
  assignments: [],
  users: [],
  selectedId: null,
  zoomIndex: 1,
  tempId: -1,
  resourceTempId: -1,
  wbsById: new Map(),
  idByWbs: new Map(),
};

const zoomLevels = [
  { label: '日', dayWidth: 30 },
  { label: '週', dayWidth: 18 },
  { label: '月', dayWidth: 10 },
];

const wbsBody = document.getElementById('wbsBody');
const ganttHeader = document.getElementById('ganttHeader');
const ganttBody = document.getElementById('ganttBody');
const ganttLines = document.getElementById('ganttLines');
const ganttToday = document.getElementById('ganttToday');
const planError = document.getElementById('planError');
const planSummary = document.getElementById('planSummary');
const workdaysControl = document.getElementById('workdaysControl');
const dailyHoursInput = document.getElementById('dailyHours');
const baselineInfo = document.getElementById('baselineInfo');

const resourcesBody = document.getElementById('resourcesBody');
const addResourceBtn = document.getElementById('addResourceBtn');
const saveResourcesBtn = document.getElementById('saveResourcesBtn');
const resourceError = document.getElementById('resourceError');
const assignmentTaskLabel = document.getElementById('assignmentTaskLabel');
const assignmentList = document.getElementById('assignmentList');

const planProjectName = document.getElementById('planProjectName');
const planProjectCode = document.getElementById('planProjectCode');
const planProjectStart = document.getElementById('planProjectStart');
const planProjectEnd = document.getElementById('planProjectEnd');
const planProjectStatus = document.getElementById('planProjectStatus');

const savePlanBtn = document.getElementById('savePlanBtn');
const backToListBtn = document.getElementById('backToListBtn');
const addTaskBtn = document.getElementById('addTaskBtn');
const indentTaskBtn = document.getElementById('indentTaskBtn');
const outdentTaskBtn = document.getElementById('outdentTaskBtn');
const deleteTaskBtn = document.getElementById('deleteTaskBtn');
const setBaselineBtn = document.getElementById('setBaselineBtn');
const autoSchedule = document.getElementById('autoSchedule');
const zoomLabel = document.getElementById('zoomLabel');

const toast = createToast();

savePlanBtn.addEventListener('click', savePlan);
backToListBtn.addEventListener('click', () => {
  window.location.href = `${ctx}/projects`;
});
addTaskBtn.addEventListener('click', addTask);
indentTaskBtn.addEventListener('click', indentTask);
outdentTaskBtn.addEventListener('click', outdentTask);
deleteTaskBtn.addEventListener('click', deleteTask);
autoSchedule.addEventListener('change', render);
setBaselineBtn.addEventListener('click', setBaseline);
addResourceBtn.addEventListener('click', addResource);
saveResourcesBtn.addEventListener('click', savePlan);
workdaysControl.querySelectorAll('input[type=\"checkbox\"]').forEach((cb) => {
  cb.addEventListener('change', () => {
    updateWorkdaysFromUI();
    render();
  });
});
dailyHoursInput.addEventListener('change', () => {
  updateProjectFromInputs();
  render();
});

document.querySelectorAll('.zoom button').forEach((btn) => {
  btn.addEventListener('click', () => {
    const dir = Number(btn.dataset.zoom || 0);
    state.zoomIndex = Math.min(
      zoomLevels.length - 1,
      Math.max(0, state.zoomIndex + dir)
    );
    zoomLabel.textContent = zoomLevels[state.zoomIndex].label;
    render();
  });
});

let syncingScroll = false;
const wbsPanel = document.querySelector('.wbs-panel');
wbsPanel.addEventListener('scroll', () => syncScroll(wbsPanel, ganttBody));
ganttBody.addEventListener('scroll', () => syncScroll(ganttBody, wbsPanel));

function syncScroll(source, target) {
  if (syncingScroll) {
    return;
  }
  syncingScroll = true;
  target.scrollTop = source.scrollTop;
  syncingScroll = false;
}

async function loadPlan() {
  if (!projectId) {
    planError.textContent = 'projectId が指定されていません。';
    return;
  }
  planError.textContent = '';
  resourceError.textContent = '';
  const prevSelected = state.selectedId;
  try {
    await loadUsers();
    const res = await fetch(`${ctx}/api/plan?projectId=${projectId}`);
    if (!res.ok) {
      throw new Error('計画の取得に失敗しました。');
    }
    const plan = await res.json();
    state.project = plan.project || {};
    state.tasks = plan.tasks || [];
    state.dependencies = plan.dependencies || [];
    state.resources = plan.resources || [];
    state.assignments = plan.assignments || [];
    state.tempId = -1;
    if (state.tasks.length) {
      const minId = Math.min(...state.tasks.map((t) => t.id || 0), 0);
      state.tempId = Math.min(-1, minId - 1);
    }
    state.resourceTempId = -1;
    if (state.resources.length) {
      const minResId = Math.min(...state.resources.map((r) => r.id || 0), 0);
      state.resourceTempId = Math.min(-1, minResId - 1);
    }
    state.selectedId = state.tasks.some((t) => t.id === prevSelected) ? prevSelected : null;
    bindProjectFields();
    bindCalendarFields();
    renderResources();
    renderAssignmentsPanel();
    render();
  } catch (e) {
    planError.textContent = e.message;
  }
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

function bindProjectFields() {
  planProjectName.value = state.project.name || '';
  planProjectCode.value = state.project.code || '';
  planProjectStart.value = state.project.startDate || '';
  planProjectEnd.value = state.project.endDate || '';
  planProjectStatus.value = state.project.status || 'ACTIVE';
}

function bindCalendarFields() {
  const workdays = parseWorkdays(state.project.workdays);
  workdaysControl.querySelectorAll('input[type=\"checkbox\"]').forEach((cb) => {
    cb.checked = workdays.includes(Number(cb.value));
  });
  dailyHoursInput.value = state.project.dailyHours ?? 8;
  if (state.project.baselineSetAt) {
    baselineInfo.textContent = formatDateTime(state.project.baselineSetAt);
  } else {
    baselineInfo.textContent = '未設定';
  }
}

function updateProjectFromInputs() {
  state.project.name = planProjectName.value.trim();
  state.project.code = planProjectCode.value.trim() || null;
  state.project.startDate = planProjectStart.value || null;
  state.project.endDate = planProjectEnd.value || null;
  state.project.status = planProjectStatus.value || 'ACTIVE';
  state.project.dailyHours = Number(dailyHoursInput.value || 8);
  updateWorkdaysFromUI();
}

function render() {
  const flatTasks = buildFlatTasks();
  buildWbsMaps(flatTasks);
  if (autoSchedule.checked) {
    applyDependencies(flatTasks);
  }
  renderWbs(flatTasks);
  renderGantt(flatTasks);
  updateSummary(flatTasks);
  renderAssignmentsPanel();
}

function buildWbsMaps(flatTasks) {
  state.wbsById = new Map();
  state.idByWbs = new Map();
  flatTasks.forEach((t) => {
    if (t._wbs) {
      state.wbsById.set(t.id, t._wbs);
      state.idByWbs.set(t._wbs, t.id);
    }
  });
}

function buildFlatTasks() {
  const taskMap = new Map();
  state.tasks.forEach((t) => {
    taskMap.set(t.id, t);
  });
  const children = new Map();
  state.tasks.forEach((t) => {
    const key = t.parentId ?? 0;
    if (!children.has(key)) {
      children.set(key, []);
    }
    children.get(key).push(t);
  });
  children.forEach((list) => {
    list.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
  });
  const result = [];
  function visit(parentId, prefix) {
    const list = children.get(parentId) || [];
    list.forEach((t, idx) => {
      const wbs = [...prefix, idx + 1];
      t._wbs = wbs.join('.');
      t._level = prefix.length;
      result.push(t);
      visit(t.id, wbs);
    });
  }
  visit(0, []);
  return result;
}

function renderWbs(flatTasks) {
  wbsBody.innerHTML = '';
  if (!flatTasks.length) {
    const tr = document.createElement('tr');
    tr.innerHTML = '<td colspan="9" class="muted">タスクがありません。</td>';
    wbsBody.appendChild(tr);
    return;
  }
  flatTasks.forEach((task, index) => {
    const tr = document.createElement('tr');
    tr.className = 'wbs-row' + (task.id === state.selectedId ? ' selected' : '');
    tr.dataset.id = task.id;
    tr.draggable = true;
    tr.addEventListener('click', () => {
      state.selectedId = task.id;
      render();
    });
    tr.addEventListener('dragstart', (e) => {
      e.dataTransfer.setData('text/plain', task.id);
    });
    tr.addEventListener('dragover', (e) => {
      e.preventDefault();
    });
    tr.addEventListener('drop', (e) => {
      e.preventDefault();
      const sourceId = Number(e.dataTransfer.getData('text/plain'));
      reorderTasks(sourceId, task.id);
    });

    const duration = calcDuration(task);
    const predecessors = getPredecessors(task.id)
      .map((id) => state.wbsById.get(id) || id)
      .join(', ');
    const assignees = getAssignedResourceNames(task.id).join(', ');
    const statusSelect = buildStatusSelect(task);
    const prioritySelect = buildPrioritySelect(task);
    const assigneeSelect = buildAssigneeSelect(task);

    tr.innerHTML = `
      <td>${task._wbs || ''}</td>
      <td>
        <div class="task-name">
          <span class="drag-handle"></span>
          ${'<span class="task-indent"></span>'.repeat(task._level || 0)}
          <input type="text" value="${escapeHtml(task.name || '')}" data-field="name" />
        </div>
      </td>
      <td>${statusSelect}</td>
      <td>${prioritySelect}</td>
      <td><input type="date" value="${task.startDate || ''}" data-field="startDate" /></td>
      <td><input type="date" value="${task.endDate || ''}" data-field="endDate" /></td>
      <td><input type="date" value="${task.dueDate || ''}" data-field="dueDate" /></td>
      <td class="muted">${duration}</td>
      <td><input type="number" min="0" max="100" value="${task.progress ?? 0}" data-field="progress" /></td>
      <td><input type="number" min="0" step="0.5" value="${task.plannedHours ?? 0}" data-field="plannedHours" /></td>
      <td><input type="number" min="0" step="0.5" value="${task.actualHours ?? 0}" data-field="actualHours" /></td>
      <td>${assigneeSelect}</td>
      <td class="muted">${escapeHtml(assignees)}</td>
      <td><input type="text" value="${escapeHtml(predecessors)}" data-field="predecessors" placeholder="例: 1,3" /></td>
      <td style="text-align: center;"><input type="checkbox" data-field="milestone" ${
        task.milestone ? 'checked' : ''
      } /></td>
    `;

    tr.querySelectorAll('input').forEach((input) => {
      input.addEventListener('change', () => {
        const field = input.dataset.field;
        const value = input.type === 'checkbox' ? input.checked : input.value;
        handleTaskInput(task, field, value);
      });
    });
    tr.querySelectorAll('select').forEach((select) => {
      select.addEventListener('change', () => {
        handleTaskInput(task, select.dataset.field, select.value);
      });
    });

    wbsBody.appendChild(tr);
  });
}

function buildStatusSelect(task) {
  const statuses = [
    { value: 'TODO', label: '未着手' },
    { value: 'IN_PROGRESS', label: '進行中' },
    { value: 'REVIEW', label: 'レビュー' },
    { value: 'DONE', label: '完了' },
    { value: 'ON_HOLD', label: '保留' },
  ];
  const current = task.status === 'DOING' ? 'IN_PROGRESS' : (task.status || 'TODO');
  const options = statuses
    .map(
      (s) => `<option value="${s.value}" ${s.value === current ? 'selected' : ''}>${s.label}</option>`
    )
    .join('');
  return `<select data-field="status">${options}</select>`;
}

function buildPrioritySelect(task) {
  const priorities = [
    { value: 1, label: '最高' },
    { value: 2, label: '高' },
    { value: 3, label: '中' },
    { value: 4, label: '低' },
    { value: 5, label: '最低' },
  ];
  const current = task.priority ?? 3;
  const options = priorities
    .map(
      (p) => `<option value="${p.value}" ${p.value === Number(current) ? 'selected' : ''}>${p.label}</option>`
    )
    .join('');
  return `<select data-field="priority">${options}</select>`;
}

function buildAssigneeSelect(task) {
  const current = task.assignedUserId || '';
  const options = [
    '<option value="">未割当</option>',
    ...state.users.map(
      (u) =>
        `<option value="${u.id}" ${String(u.id) === String(current) ? 'selected' : ''}>${escapeHtml(
          u.name || u.loginId || ''
        )}</option>`
    ),
  ].join('');
  return `<select data-field="assignedUserId">${options}</select>`;
}

function handleTaskInput(task, field, value) {
  if (field === 'name') {
    task.name = value.trim() || '名称未設定';
  }
  if (field === 'startDate') {
    const date = value ? new Date(value) : null;
    if (date) {
      const duration = calcDuration(task);
      const start = normalizeWorkday(date, 1);
      task.startDate = formatDate(start);
      task.endDate = formatDate(addWorkdays(start, Math.max(0, duration - 1)));
    } else {
      task.startDate = null;
    }
  }
  if (field === 'endDate') {
    const date = value ? new Date(value) : null;
    if (date) {
      const end = normalizeWorkday(date, -1);
      task.endDate = formatDate(end);
      if (task.startDate && parseDate(task.startDate) > end) {
        task.startDate = formatDate(end);
      }
    } else {
      task.endDate = null;
    }
  }
  if (field === 'progress') {
    const v = Number(value);
    task.progress = Number.isFinite(v) ? Math.min(100, Math.max(0, v)) : 0;
  }
  if (field === 'status') {
    task.status = value;
  }
  if (field === 'priority') {
    const v = Number(value);
    task.priority = Number.isFinite(v) ? v : 3;
  }
  if (field === 'assignedUserId') {
    const v = Number(value);
    task.assignedUserId = Number.isFinite(v) && v > 0 ? v : null;
  }
  if (field === 'plannedHours') {
    task.plannedHours = Number(value || 0);
  }
  if (field === 'actualHours') {
    task.actualHours = Number(value || 0);
  }
  if (field === 'predecessors') {
    updateDependencies(task.id, value);
  }
  if (field === 'milestone') {
    task.milestone = Boolean(value);
  }
  if (field === 'dueDate') {
    task.dueDate = value || null;
  }
  render();
}

function renderGantt(flatTasks) {
  ganttHeader.innerHTML = '';
  ganttBody.querySelectorAll('.gantt-row').forEach((el) => el.remove());
  const rowHeight = 36;

  const timeline = computeTimeline(flatTasks);
  const dayWidth = zoomLevels[state.zoomIndex].dayWidth;
  zoomLabel.textContent = zoomLevels[state.zoomIndex].label;

  const totalDays = diffDays(timeline.start, timeline.end) + 1;
  ganttHeader.style.gridTemplateColumns = `repeat(${totalDays}, ${dayWidth}px)`;
  ganttHeader.style.width = `${totalDays * dayWidth}px`;

  const headerDates = buildDates(timeline.start, totalDays);
  headerDates.forEach((d) => {
    const cell = document.createElement('div');
    cell.textContent = formatHeaderDate(d, state.zoomIndex);
    if (!isWorkday(d)) {
      cell.classList.add('non-workday');
    }
    ganttHeader.appendChild(cell);
  });

  ganttBody.style.width = `${totalDays * dayWidth}px`;
  ganttLines.setAttribute('width', `${totalDays * dayWidth}`);
  ganttLines.setAttribute('height', `${flatTasks.length * rowHeight}`);
  ganttLines.innerHTML = '';
  renderGanttShades(headerDates, dayWidth, flatTasks.length * rowHeight);

  const today = new Date();
  const todayOffset = diffDays(timeline.start, today);
  if (todayOffset >= 0 && todayOffset <= totalDays) {
    ganttToday.style.display = 'block';
    ganttToday.style.left = `${todayOffset * dayWidth}px`;
  } else {
    ganttToday.style.display = 'none';
  }

  const barPositions = new Map();

  flatTasks.forEach((task, index) => {
    const row = document.createElement('div');
    row.className = 'gantt-row';
    row.style.height = `${rowHeight}px`;

    const start = task.startDate ? parseDate(task.startDate) : timeline.start;
    const end = task.endDate ? parseDate(task.endDate) : start;
    const offset = diffDays(timeline.start, start);
    const duration = Math.max(1, diffDays(start, end) + 1);

    if (task.baselineStartDate && task.baselineEndDate) {
      const bStart = parseDate(task.baselineStartDate);
      const bEnd = parseDate(task.baselineEndDate);
      const bOffset = diffDays(timeline.start, bStart);
      const bDuration = Math.max(1, diffDays(bStart, bEnd) + 1);
      const baseline = document.createElement('div');
      baseline.className = 'gantt-baseline';
      baseline.style.left = `${bOffset * dayWidth}px`;
      baseline.style.width = `${bDuration * dayWidth}px`;
      row.appendChild(baseline);
    }

    const bar = document.createElement('div');
    bar.className = 'gantt-bar' + (task.id === state.selectedId ? ' selected' : '');
    if (task.milestone) {
      bar.classList.add('milestone');
    }
    bar.style.left = `${offset * dayWidth}px`;
    bar.style.width = `${duration * dayWidth}px`;

    const progress = document.createElement('div');
    progress.className = 'progress';
    progress.style.width = `${task.progress ?? 0}%`;
    bar.appendChild(progress);

    const resize = document.createElement('div');
    resize.className = 'gantt-resize';
    bar.appendChild(resize);

    bar.addEventListener('mousedown', (e) => {
      if (e.target === resize) {
        startResize(e, task, timeline, dayWidth);
      } else {
        startDrag(e, task, timeline, dayWidth);
      }
    });
    bar.addEventListener('click', (e) => {
      e.stopPropagation();
      state.selectedId = task.id;
      render();
    });

    row.appendChild(bar);
    ganttBody.appendChild(row);

    barPositions.set(task.id, {
      x: offset * dayWidth,
      y: index * rowHeight + 8,
      width: duration * dayWidth,
      height: 18,
    });
  });

  drawDependencies(barPositions);
}

function updateSummary(flatTasks) {
  const totalPlanned = flatTasks.reduce((sum, t) => sum + Number(t.plannedHours || 0), 0);
  const totalActual = flatTasks.reduce((sum, t) => sum + Number(t.actualHours || 0), 0);
  const avgProgress =
    flatTasks.length > 0
      ? Math.round(
          flatTasks.reduce((sum, t) => sum + Number(t.progress || 0), 0) / flatTasks.length
        )
      : 0;
  planSummary.innerHTML = `
    <div>合計タスク数: <strong>${flatTasks.length}</strong></div>
    <div>平均進捗: <strong>${avgProgress}%</strong></div>
    <div>予定工数: <strong>${totalPlanned.toFixed(1)}h</strong></div>
    <div>実績工数: <strong>${totalActual.toFixed(1)}h</strong></div>
  `;
}

function renderResources() {
  resourcesBody.innerHTML = '';
  if (!state.resources.length) {
    resourcesBody.innerHTML =
      '<tr><td colspan="5" class="muted">リソースが登録されていません。</td></tr>';
    return;
  }
  state.resources.forEach((r) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><input type="text" value="${escapeHtml(r.name || '')}" data-field="name" /></td>
      <td><input type="text" value="${escapeHtml(r.role || '')}" data-field="role" /></td>
      <td><input type="number" min="0" step="0.5" value="${r.dailyHours ?? 8}" data-field="dailyHours" /></td>
      <td><input type="number" min="0" step="1" value="${r.costRate ?? 0}" data-field="costRate" /></td>
      <td><button class="secondary" data-remove>削除</button></td>
    `;
    tr.querySelectorAll('input').forEach((input) => {
      input.addEventListener('change', () => {
        const field = input.dataset.field;
        const value = input.value;
        if (field === 'name') {
          r.name = value.trim();
        }
        if (field === 'role') {
          r.role = value.trim();
        }
        if (field === 'dailyHours') {
          r.dailyHours = Number(value || 0);
        }
        if (field === 'costRate') {
          r.costRate = Number(value || 0);
        }
        render();
      });
    });
    tr.querySelector('button[data-remove]').addEventListener('click', () => {
      state.resources = state.resources.filter((item) => item.id !== r.id);
      state.assignments = state.assignments.filter((a) => a.resourceId !== r.id);
      renderResources();
      render();
    });
    resourcesBody.appendChild(tr);
  });
}

function addResource() {
  resourceError.textContent = '';
  const r = {
    id: state.resourceTempId--,
    projectId: Number(projectId),
    name: '新規リソース',
    role: '',
    dailyHours: 8,
    costRate: 0,
  };
  state.resources.push(r);
  renderResources();
}

function renderAssignmentsPanel() {
  assignmentList.innerHTML = '';
  const task = state.tasks.find((t) => t.id === state.selectedId);
  if (!task) {
    assignmentTaskLabel.textContent = '未選択';
    assignmentList.innerHTML = '<div class="muted">タスクを選択してください。</div>';
    return;
  }
  assignmentTaskLabel.textContent = `${task._wbs || ''} ${task.name || ''}`.trim();
  if (!state.resources.length) {
    assignmentList.innerHTML = '<div class="muted">リソースを登録してください。</div>';
    return;
  }
  state.resources.forEach((r) => {
    const assigned = getAssignment(task.id, r.id);
    const hours = assigned ? Number(assigned.allocationHours || 0) : 0;
    const row = document.createElement('div');
    row.className = 'assignment-row';
    row.innerHTML = `
      <div>
        <div><strong>${escapeHtml(r.name || '')}</strong></div>
        <div class="muted small">${escapeHtml(r.role || '')}</div>
      </div>
      <div class="muted">${Number(r.dailyHours ?? 8).toFixed(1)}h/日</div>
      <div><input type="number" min="0" step="0.5" value="${hours}" data-resource="${r.id}" /></div>
    `;
    row.querySelector('input').addEventListener('change', (e) => {
      const v = Number(e.target.value || 0);
      setAssignment(task.id, r.id, v);
      render();
    });
    assignmentList.appendChild(row);
  });
}

function getAssignment(taskId, resourceId) {
  return state.assignments.find(
    (a) => a.taskId === taskId && a.resourceId === resourceId
  );
}

function setAssignment(taskId, resourceId, hours) {
  state.assignments = state.assignments.filter(
    (a) => !(a.taskId === taskId && a.resourceId === resourceId)
  );
  if (hours > 0) {
    state.assignments.push({
      taskId,
      resourceId,
      allocationHours: hours,
    });
  }
}

function getAssignedResourceNames(taskId) {
  return state.assignments
    .filter((a) => a.taskId === taskId)
    .map((a) => state.resources.find((r) => r.id === a.resourceId))
    .filter(Boolean)
    .map((r) => r.name || '');
}

function startDrag(e, task, timeline, dayWidth) {
  e.preventDefault();
  const start = task.startDate ? parseDate(task.startDate) : timeline.start;
  const end = task.endDate ? parseDate(task.endDate) : start;
  const duration = Math.max(1, calcDuration(task));
  const originX = e.clientX;
  const originStart = start;

  function onMove(ev) {
    const dx = ev.clientX - originX;
    const days = Math.round(dx / dayWidth);
    const nextStart = normalizeWorkday(addDays(originStart, days), 1);
    task.startDate = formatDate(nextStart);
    task.endDate = formatDate(addWorkdays(nextStart, duration - 1));
    render();
  }

  function onUp() {
    document.removeEventListener('mousemove', onMove);
    document.removeEventListener('mouseup', onUp);
  }

  document.addEventListener('mousemove', onMove);
  document.addEventListener('mouseup', onUp);
}

function startResize(e, task, timeline, dayWidth) {
  e.preventDefault();
  const start = task.startDate ? parseDate(task.startDate) : timeline.start;
  const end = task.endDate ? parseDate(task.endDate) : start;
  const originX = e.clientX;
  const originEnd = end;

  function onMove(ev) {
    const dx = ev.clientX - originX;
    const days = Math.round(dx / dayWidth);
    const nextEnd = normalizeWorkday(addDays(originEnd, days), -1);
    task.startDate = formatDate(start);
    task.endDate = formatDate(nextEnd < start ? start : nextEnd);
    render();
  }

  function onUp() {
    document.removeEventListener('mousemove', onMove);
    document.removeEventListener('mouseup', onUp);
  }

  document.addEventListener('mousemove', onMove);
  document.addEventListener('mouseup', onUp);
}

function addTask() {
  let parentId = null;
  let sortOrder = 0;
  if (state.selectedId != null) {
    const selected = state.tasks.find((t) => t.id === state.selectedId);
    parentId = selected?.parentId ?? null;
    const siblings = state.tasks.filter((t) => (t.parentId ?? null) === parentId);
    sortOrder = Math.max(0, ...siblings.map((t) => t.sortOrder || 0)) + 1;
  } else {
    const siblings = state.tasks.filter((t) => t.parentId == null);
    sortOrder = Math.max(0, ...siblings.map((t) => t.sortOrder || 0)) + 1;
  }
  const baseDate = planProjectStart.value
    ? normalizeWorkday(parseDate(planProjectStart.value), 1)
    : normalizeWorkday(new Date(), 1);
  const newTask = {
    id: state.tempId--,
    projectId: Number(projectId),
    parentId,
    sortOrder,
    name: '新規タスク',
    description: '',
    status: 'TODO',
    priority: 3,
    assignedUserId: null,
    taskType: 'TASK',
    startDate: formatDate(baseDate),
    endDate: formatDate(baseDate),
    dueDate: formatDate(baseDate),
    progress: 0,
    plannedHours: 0,
    actualHours: 0,
    milestone: false,
  };
  state.tasks.push(newTask);
  state.selectedId = newTask.id;
  render();
}

function indentTask() {
  const flat = buildFlatTasks();
  const idx = flat.findIndex((t) => t.id === state.selectedId);
  if (idx <= 0) {
    return;
  }
  const target = flat[idx];
  const parent = flat[idx - 1];
  target.parentId = parent.id;
  target.sortOrder = nextSortOrder(parent.id);
  normalizeSortOrder(parent.id);
  render();
}

function outdentTask() {
  const target = state.tasks.find((t) => t.id === state.selectedId);
  if (!target || target.parentId == null) {
    return;
  }
  const parent = state.tasks.find((t) => t.id === target.parentId);
  target.parentId = parent ? parent.parentId : null;
  target.sortOrder = nextSortOrder(target.parentId ?? null);
  normalizeSortOrder(target.parentId ?? null);
  render();
}

function deleteTask() {
  if (state.selectedId == null) {
    return;
  }
  const toDelete = new Set();
  collectDescendants(state.selectedId, toDelete);
  state.tasks = state.tasks.filter((t) => !toDelete.has(t.id));
  state.dependencies = state.dependencies.filter(
    (d) => !toDelete.has(d.predecessorId) && !toDelete.has(d.successorId)
  );
  state.assignments = state.assignments.filter((a) => !toDelete.has(a.taskId));
  state.selectedId = null;
  render();
}

function collectDescendants(id, set) {
  set.add(id);
  state.tasks
    .filter((t) => t.parentId === id)
    .forEach((child) => collectDescendants(child.id, set));
}

function reorderTasks(sourceId, targetId) {
  if (sourceId === targetId) {
    return;
  }
  const source = state.tasks.find((t) => t.id === sourceId);
  const target = state.tasks.find((t) => t.id === targetId);
  if (!source || !target) {
    return;
  }
  source.parentId = target.parentId ?? null;
  const siblings = state.tasks
    .filter((t) => (t.parentId ?? null) === (target.parentId ?? null) && t.id !== source.id)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
  const index = siblings.findIndex((t) => t.id === target.id);
  siblings.splice(index >= 0 ? index : siblings.length, 0, source);
  siblings.forEach((t, idx) => {
    t.sortOrder = idx + 1;
  });
  render();
}

function normalizeSortOrder(parentId) {
  const siblings = state.tasks
    .filter((t) => (t.parentId ?? null) === (parentId ?? null))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
  siblings.forEach((t, idx) => {
    t.sortOrder = idx + 1;
  });
}

function nextSortOrder(parentId) {
  const siblings = state.tasks.filter((t) => (t.parentId ?? null) === (parentId ?? null));
  return Math.max(0, ...siblings.map((t) => t.sortOrder || 0)) + 1;
}

function updateDependencies(taskId, value) {
  const list = value
    .split(',')
    .map((v) => v.trim())
    .filter((v) => v.length > 0)
    .map((token) => {
      if (state.idByWbs.has(token)) {
        return state.idByWbs.get(token);
      }
      const num = Number(token);
      return Number.isFinite(num) ? num : null;
    })
    .filter((v) => Number.isFinite(v));
  state.dependencies = state.dependencies.filter((d) => d.successorId !== taskId);
  list.forEach((pred) => {
    state.dependencies.push({
      predecessorId: pred,
      successorId: taskId,
      type: 'FS',
      lagDays: 0,
    });
  });
}

function getPredecessors(taskId) {
  return state.dependencies
    .filter((d) => d.successorId === taskId)
    .map((d) => d.predecessorId);
}

function applyDependencies(flatTasks) {
  const taskMap = new Map();
  flatTasks.forEach((t) => taskMap.set(t.id, t));
  let changed = true;
  let guard = 0;
  while (changed && guard < 10) {
    changed = false;
    guard += 1;
    state.dependencies.forEach((d) => {
      const pred = taskMap.get(d.predecessorId);
      const succ = taskMap.get(d.successorId);
      if (!pred || !succ || !pred.endDate) {
        return;
      }
      const predEnd = parseDate(pred.endDate);
      const lag = d.lagDays || 0;
      const earliest = addWorkdays(predEnd, 1 + lag);
      if (!succ.startDate || parseDate(succ.startDate) < earliest) {
        const duration = Math.max(1, calcDuration(succ));
        succ.startDate = formatDate(earliest);
        succ.endDate = formatDate(addWorkdays(earliest, duration - 1));
        changed = true;
      }
    });
  }
}

function computeTimeline(flatTasks) {
  const dates = flatTasks
    .flatMap((t) => [t.startDate, t.endDate])
    .filter(Boolean)
    .map((d) => parseDate(d));
  let start = dates.length
    ? new Date(Math.min(...dates))
    : state.project.startDate
    ? parseDate(state.project.startDate)
    : new Date();
  let end = dates.length
    ? new Date(Math.max(...dates))
    : state.project.endDate
    ? parseDate(state.project.endDate)
    : addDays(start, 14);
  start = addDays(start, -3);
  end = addDays(end, 7);
  return { start, end };
}

function buildDates(start, totalDays) {
  const list = [];
  for (let i = 0; i < totalDays; i += 1) {
    list.push(addDays(start, i));
  }
  return list;
}

function drawDependencies(barPositions) {
  ganttLines.innerHTML = '';
  state.dependencies.forEach((d) => {
    const from = barPositions.get(d.predecessorId);
    const to = barPositions.get(d.successorId);
    if (!from || !to) {
      return;
    }
    const x1 = from.x + from.width;
    const y1 = from.y + from.height / 2;
    const x2 = to.x;
    const y2 = to.y + to.height / 2;
    const midX = x1 + 8;
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute(
      'd',
      `M ${x1} ${y1} L ${midX} ${y1} L ${midX} ${y2} L ${x2 - 4} ${y2} L ${x2} ${y2}`
    );
    path.setAttribute('fill', 'none');
    path.setAttribute('stroke', '#6f86b6');
    path.setAttribute('stroke-width', '1.4');
    ganttLines.appendChild(path);
  });
}

function renderGanttShades(dates, dayWidth, height) {
  let shade = ganttBody.querySelector('.gantt-shade');
  if (!shade) {
    shade = document.createElement('div');
    shade.className = 'gantt-shade';
    ganttBody.insertBefore(shade, ganttBody.firstChild);
  }
  shade.innerHTML = '';
  shade.style.width = `${dates.length * dayWidth}px`;
  shade.style.height = `${height}px`;
  dates.forEach((d, idx) => {
    if (isWorkday(d)) {
      return;
    }
    const cell = document.createElement('div');
    cell.className = 'gantt-shade-day';
    cell.style.left = `${idx * dayWidth}px`;
    cell.style.width = `${dayWidth}px`;
    shade.appendChild(cell);
  });
}

function calcDuration(task) {
  if (!task.startDate || !task.endDate) {
    return 1;
  }
  const start = parseDate(task.startDate);
  const end = parseDate(task.endDate);
  return Math.max(1, Math.abs(diffWorkdays(start, end)));
}

function parseWorkdays(value) {
  if (!value) {
    return [1, 2, 3, 4, 5];
  }
  return value
    .split(',')
    .map((v) => Number(v))
    .filter((v) => Number.isFinite(v));
}

function updateWorkdaysFromUI() {
  const days = Array.from(workdaysControl.querySelectorAll('input[type=\"checkbox\"]'))
    .filter((cb) => cb.checked)
    .map((cb) => Number(cb.value))
    .filter((v) => Number.isFinite(v));
  if (!days.length) {
    const defaultDays = [1, 2, 3, 4, 5];
    state.project.workdays = defaultDays.join(',');
    workdaysControl.querySelectorAll('input[type=\"checkbox\"]').forEach((cb) => {
      cb.checked = defaultDays.includes(Number(cb.value));
    });
    return;
  }
  state.project.workdays = days.join(',');
}

function isWorkday(date) {
  const days = parseWorkdays(state.project.workdays);
  if (!days.length) {
    return true;
  }
  return days.includes(date.getDay());
}

function normalizeWorkday(date, direction) {
  let d = new Date(date);
  d.setHours(0, 0, 0, 0);
  if (parseWorkdays(state.project.workdays).length === 0) {
    return d;
  }
  const dir = direction == null ? 1 : direction >= 0 ? 1 : -1;
  while (!isWorkday(d)) {
    d = addDays(d, dir);
  }
  return d;
}

function addWorkdays(date, days) {
  let d = new Date(date);
  d.setHours(0, 0, 0, 0);
  if (days === 0) {
    return d;
  }
  const dir = days >= 0 ? 1 : -1;
  let remaining = Math.abs(days);
  while (remaining > 0) {
    d = addDays(d, dir);
    if (isWorkday(d)) {
      remaining -= 1;
    }
  }
  return d;
}

function diffWorkdays(start, end) {
  let s = new Date(start);
  let e = new Date(end);
  s.setHours(0, 0, 0, 0);
  e.setHours(0, 0, 0, 0);
  if (s.getTime() === e.getTime()) {
    return isWorkday(s) ? 1 : 0;
  }
  const dir = s < e ? 1 : -1;
  let count = 0;
  while ((dir > 0 && s <= e) || (dir < 0 && s >= e)) {
    if (isWorkday(s)) {
      count += dir;
    }
    s = addDays(s, dir);
  }
  return count;
}

function parseDate(value) {
  const d = new Date(value);
  d.setHours(0, 0, 0, 0);
  return d;
}

function formatDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function formatHeaderDate(date, zoomIndex) {
  if (zoomIndex === 2) {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
  if (zoomIndex === 1) {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

function diffDays(a, b) {
  const ms = b.getTime() - a.getTime();
  return Math.floor(ms / (1000 * 60 * 60 * 24));
}

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function formatDateTime(value) {
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return String(value);
  }
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}/${m}/${day} ${h}:${min}`;
}

async function setBaseline() {
  planError.textContent = '';
  try {
    const res = await fetch(`${ctx}/api/plan/baseline?projectId=${projectId}`, {
      method: 'POST',
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || 'ベースライン保存に失敗しました。');
    }
    showToast('ベースラインを保存しました');
    await loadPlan();
  } catch (e) {
    planError.textContent = e.message;
  }
}

async function savePlan() {
  planError.textContent = '';
  resourceError.textContent = '';
  updateProjectFromInputs();
  if (!state.project.name) {
    planError.textContent = 'プロジェクト名は必須です。';
    return;
  }
  const invalidResource = state.resources.find((r) => !r.name || !r.name.trim());
  if (invalidResource) {
    resourceError.textContent = 'リソース名は必須です。';
    return;
  }
  const payload = {
    project: state.project,
    tasks: state.tasks.map((t) => ({
      id: t.id,
      projectId: t.projectId,
      parentId: t.parentId,
      sortOrder: t.sortOrder,
      name: t.name,
      description: t.description,
      status: t.status,
      priority: t.priority,
      assignedUserId: t.assignedUserId,
      taskType: t.taskType,
      startDate: t.startDate,
      endDate: t.endDate,
      dueDate: t.dueDate,
      progress: t.progress,
      plannedHours: t.plannedHours,
      actualHours: t.actualHours,
      milestone: t.milestone,
    })),
    dependencies: state.dependencies.map((d) => ({
      id: d.id,
      projectId: d.projectId,
      predecessorId: d.predecessorId,
      successorId: d.successorId,
      type: d.type,
      lagDays: d.lagDays,
    })),
    resources: state.resources.map((r) => ({
      id: r.id,
      projectId: Number(projectId),
      name: r.name,
      role: r.role,
      dailyHours: r.dailyHours,
      costRate: r.costRate,
    })),
    assignments: state.assignments.map((a) => ({
      id: a.id,
      taskId: a.taskId,
      resourceId: a.resourceId,
      allocationHours: a.allocationHours,
    })),
  };
  try {
    const res = await fetch(`${ctx}/api/plan?projectId=${projectId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '保存に失敗しました。');
    }
    showToast('保存しました');
    await loadPlan();
  } catch (e) {
    planError.textContent = e.message;
  }
}

function createToast() {
  const el = document.createElement('div');
  el.className = 'toast';
  document.body.appendChild(el);
  return el;
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2000);
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch (e) {
    return null;
  }
}

loadPlan();
