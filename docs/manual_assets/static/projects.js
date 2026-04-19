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
            <button data-open="${p.id}">計画</button>
            <button class="secondary" data-kanban="${p.id}">かんばん</button>
            <button class="secondary" data-calendar="${p.id}">カレンダー</button>
            <button class="secondary" data-reports="${p.id}">レポート</button>
            <button class="secondary" data-finance="${p.id}">予実</button>
            <button class="secondary" data-forum="${p.id}">フォーラム</button>
            <button class="secondary" data-docs="${p.id}">ドキュメント</button>
            <button class="secondary" data-issues="${p.id}">不具合</button>
            <button class="secondary" data-workflow="${p.id}">承認</button>
            <button class="secondary" data-wiki="${p.id}">Wiki</button>
          </div>
        </td>
      `;
      tr.querySelector('button[data-open]').addEventListener('click', () => {
        window.location.href = `${ctx}/project?projectId=${p.id}`;
      });
      tr.querySelector('button[data-kanban]').addEventListener('click', () => {
        window.location.href = `${ctx}/kanban?projectId=${p.id}`;
      });
      tr.querySelector('button[data-calendar]').addEventListener('click', () => {
        window.location.href = `${ctx}/calendar?projectId=${p.id}`;
      });
      tr.querySelector('button[data-reports]').addEventListener('click', () => {
        window.location.href = `${ctx}/reports?projectId=${p.id}`;
      });
      tr.querySelector('button[data-finance]').addEventListener('click', () => {
        window.location.href = `${ctx}/finance?projectId=${p.id}`;
      });
      tr.querySelector('button[data-forum]').addEventListener('click', () => {
        window.location.href = `${ctx}/forum?projectId=${p.id}`;
      });
      tr.querySelector('button[data-docs]').addEventListener('click', () => {
        window.location.href = `${ctx}/docs?projectId=${p.id}`;
      });
      tr.querySelector('button[data-issues]').addEventListener('click', () => {
        window.location.href = `${ctx}/issues?projectId=${p.id}`;
      });
      tr.querySelector('button[data-workflow]').addEventListener('click', () => {
        window.location.href = `${ctx}/workflow?projectId=${p.id}`;
      });
      tr.querySelector('button[data-wiki]').addEventListener('click', () => {
        window.location.href = `${ctx}/wiki?projectId=${p.id}`;
      });
      projectsBody.appendChild(tr);
    });
  } catch (e) {
    projectsBody.innerHTML = `<tr><td colspan="9" class="inline-error">${escapeHtml(e.message)}</td></tr>`;
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

loadProjects();
