/*
 * [役割] calendar: カレンダー画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const calendarGrid = document.getElementById('calendarGrid');
const calendarTitle = document.getElementById('calendarTitle');
const calendarError = document.getElementById('calendarError');
const prevMonthBtn = document.getElementById('prevMonthBtn');
const nextMonthBtn = document.getElementById('nextMonthBtn');

const state = {
  tasks: [],
  current: new Date(),
};

prevMonthBtn.addEventListener('click', () => {
  state.current = new Date(state.current.getFullYear(), state.current.getMonth() - 1, 1);
  renderCalendar();
});
nextMonthBtn.addEventListener('click', () => {
  state.current = new Date(state.current.getFullYear(), state.current.getMonth() + 1, 1);
  renderCalendar();
});

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
      window.location.href = `${ctx}/calendar?projectId=${projects[0].id}`;
      return false;
    }
    calendarError.textContent = 'プロジェクトがありません。先に作成してください。';
  } catch (e) {
    calendarError.textContent = e.message;
  }
  return false;
}

async function loadTasks() {
  const res = await fetch(`${ctx}/api/tasks?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('タスク取得に失敗しました。');
  }
  state.tasks = (await res.json()) || [];
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

function buildEventMap(tasks) {
  const map = new Map();
  tasks.forEach((task) => {
    const due = task.dueDate || task.endDate;
    if (due) {
      addEvent(map, due, task, '期日');
    }
    if (task.startDate) {
      addEvent(map, task.startDate, task, '開始');
    }
  });
  return map;
}

function addEvent(map, date, task, label) {
  if (!map.has(date)) {
    map.set(date, []);
  }
  map.get(date).push({ task, label });
}

function renderCalendar() {
  const year = state.current.getFullYear();
  const month = state.current.getMonth();
  calendarTitle.textContent = `${year}年${month + 1}月`;
  calendarGrid.innerHTML = '';

  const dayNames = ['日', '月', '火', '水', '木', '金', '土'];
  dayNames.forEach((name) => {
    const head = document.createElement('div');
    head.className = 'calendar-cell calendar-head';
    head.innerHTML = `<div class=\"calendar-date\">${name}</div>`;
    calendarGrid.appendChild(head);
  });

  const tasks = leafTasks(state.tasks);
  const eventMap = buildEventMap(tasks);

  const firstDay = new Date(year, month, 1);
  const start = new Date(year, month, 1 - firstDay.getDay());
  for (let i = 0; i < 42; i += 1) {
    const day = new Date(start);
    day.setDate(start.getDate() + i);
    const dateKey = formatDate(day);
    const cell = document.createElement('div');
    cell.className = 'calendar-cell';
    if (day.getMonth() !== month) {
      cell.classList.add('outside');
    }
    if (isToday(day)) {
      cell.style.borderColor = '#0b5ad9';
      cell.style.boxShadow = '0 0 0 2px rgba(11,90,217,0.1) inset';
    }
    cell.innerHTML = `
      <div class="calendar-date">${day.getDate()}</div>
      <div class="calendar-items"></div>
    `;
    const items = cell.querySelector('.calendar-items');
    const events = eventMap.get(dateKey) || [];
    events.slice(0, 3).forEach((ev) => {
      const item = document.createElement('div');
      item.className = 'calendar-item';
      if (ev.task.status && ev.task.status.toUpperCase() === 'DONE') {
        item.classList.add('done');
      }
      item.textContent = `${ev.label}:${ev.task.name}`;
      items.appendChild(item);
    });
    if (events.length > 3) {
      const more = document.createElement('div');
      more.className = 'calendar-item';
      more.textContent = `他${events.length - 3}件`;
      items.appendChild(more);
    }
    calendarGrid.appendChild(cell);
  }
}

function formatDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function isToday(d) {
  const now = new Date();
  return (
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  );
}

async function init() {
  if (!(await ensureProject())) {
    return;
  }
  try {
    await loadTasks();
    renderCalendar();
  } catch (e) {
    calendarError.textContent = e.message;
  }
}

init();
