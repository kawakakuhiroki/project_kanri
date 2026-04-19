/*
 * [役割] projects: プロジェクト一覧画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectsBody = document.getElementById('projectsBody');
const newProjectBtn = document.getElementById('newProjectBtn');
const projectForm = document.getElementById('projectForm');
const createProjectBtn = document.getElementById('createProjectBtn');
const cancelProjectBtn = document.getElementById('cancelProjectBtn');
const projectError = document.getElementById('projectError');

const projectName = document.getElementById('projectName');
const projectCode = document.getElementById('projectCode');
const projectStart = document.getElementById('projectStart');
const projectEnd = document.getElementById('projectEnd');
const projectDesc = document.getElementById('projectDesc');

newProjectBtn.addEventListener('click', () => {
  projectForm.classList.toggle('hidden');
  projectError.textContent = '';
});

cancelProjectBtn.addEventListener('click', () => {
  projectForm.classList.add('hidden');
  projectError.textContent = '';
});

createProjectBtn.addEventListener('click', async () => {
  projectError.textContent = '';
  const payload = {
    name: projectName.value.trim(),
    code: projectCode.value.trim() || null,
    startDate: projectStart.value || null,
    endDate: projectEnd.value || null,
    description: projectDesc.value.trim() || null,
    status: 'ACTIVE',
  };
  if (!payload.name) {
    projectError.textContent = 'プロジェクト名は必須です。';
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '作成に失敗しました。');
    }
    const created = await res.json();
    window.location.href = `${ctx}/project?projectId=${created.id}`;
  } catch (e) {
    projectError.textContent = e.message;
  }
});

async function loadProjects() {
  projectsBody.innerHTML = '';
  try {
    const res = await fetch(`${ctx}/api/projects`);
    if (!res.ok) {
      throw new Error('一覧取得に失敗しました。');
    }
    const projects = await res.json();
    if (!projects.length) {
    projectsBody.innerHTML = '<tr><td colspan="9" class="muted">まだプロジェクトがありません。</td></tr>';
    return;
    }
    projects.forEach((p) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${p.id}</td>
        <td>${escapeHtml(p.code || '')}</td>
        <td>${escapeHtml(p.name || '')}</td>
        <td>${p.startDate || ''}</td>
        <td>${p.endDate || ''}</td>
        <td>${escapeHtml(p.status || 'ACTIVE')}</td>
        <td>${p.progress ?? 0}%</td>
        <td>${p.taskCount ?? 0}</td>
        <td>
          <div class="action-row">
            <div class="project-primary-actions">
              <button data-open="${p.id}">計画</button>
              <button class="secondary" data-kanban="${p.id}">かんばん</button>
            </div>
            <div class="project-secondary-actions">
              <select data-screen="${p.id}">
                <option value="">その他の画面を選択</option>
                <option value="calendar">カレンダー</option>
                <option value="reports">レポート</option>
                <option value="finance">予実管理</option>
                <option value="forum">フォーラム</option>
                <option value="docs">ドキュメント</option>
                <option value="issues">不具合</option>
                <option value="workflow">承認</option>
                <option value="wiki">Wiki</option>
              </select>
              <button class="secondary" data-open-screen="${p.id}">開く</button>
            </div>
          </div>
        </td>
      `;
      tr.querySelector('button[data-open]').addEventListener('click', () => openProjectScreen(p.id, 'project'));
      tr.querySelector('button[data-kanban]').addEventListener('click', () => openProjectScreen(p.id, 'kanban'));
      const screenSelect = tr.querySelector('select[data-screen]');
      tr.querySelector('button[data-open-screen]').addEventListener('click', () => {
        if (!screenSelect.value) {
          screenSelect.focus();
          return;
        }
        openProjectScreen(p.id, screenSelect.value);
      });
      projectsBody.appendChild(tr);
    });
  } catch (e) {
    projectsBody.innerHTML = `<tr><td colspan="9" class="inline-error">${escapeHtml(e.message)}</td></tr>`;
  }
}

function openProjectScreen(projectId, screen) {
  const path = screen === 'project' ? 'project' : screen;
  window.location.href = `${ctx}/${path}?projectId=${projectId}`;
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

loadProjects();
