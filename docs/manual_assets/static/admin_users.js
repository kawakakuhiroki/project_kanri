/*
 * [役割] admin_users: ユーザー管理画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';

const adminUsersError = document.getElementById('adminUsersError');
const newUserLoginId = document.getElementById('newUserLoginId');
const newUserName = document.getElementById('newUserName');
const newUserEmail = document.getElementById('newUserEmail');
const newUserRole = document.getElementById('newUserRole');
const newUserPassword = document.getElementById('newUserPassword');
const newUserActive = document.getElementById('newUserActive');
const createUserBtn = document.getElementById('createUserBtn');
const adminUsersBody = document.getElementById('adminUsersBody');

createUserBtn.addEventListener('click', createUser);

function setError(message) {
  adminUsersError.textContent = message || '';
}

async function loadUsers() {
  const res = await fetch(`${ctx}/api/users`);
  if (!res.ok) {
    throw new Error('ユーザー一覧取得に失敗しました。');
  }
  const users = await res.json();
  renderUsers(users || []);
}

function renderUsers(users) {
  adminUsersBody.innerHTML = '';
  if (!users.length) {
    adminUsersBody.innerHTML = '<tr><td colspan="8" class="muted">ユーザーがいません。</td></tr>';
    return;
  }
  users.forEach((user) => {
    const tr = document.createElement('tr');

    const loginInput = document.createElement('input');
    loginInput.value = user.loginId || '';
    const nameInput = document.createElement('input');
    nameInput.value = user.name || '';
    const emailInput = document.createElement('input');
    emailInput.value = user.email || '';

    const roleSelect = document.createElement('select');
    ['ADMIN', 'MANAGER', 'MEMBER', 'VIEWER'].forEach((role) => {
      const opt = document.createElement('option');
      opt.value = role;
      opt.textContent = role;
      if (role === user.role) {
        opt.selected = true;
      }
      roleSelect.appendChild(opt);
    });

    const activeSelect = document.createElement('select');
    const activeOpt = document.createElement('option');
    activeOpt.value = 'true';
    activeOpt.textContent = '有効';
    const inactiveOpt = document.createElement('option');
    inactiveOpt.value = 'false';
    inactiveOpt.textContent = '無効';
    activeSelect.appendChild(activeOpt);
    activeSelect.appendChild(inactiveOpt);
    activeSelect.value = user.active ? 'true' : 'false';

    const passwordInput = document.createElement('input');
    passwordInput.type = 'password';
    passwordInput.placeholder = '変更する場合のみ入力';

    const updateBtn = document.createElement('button');
    updateBtn.className = 'secondary';
    updateBtn.textContent = '更新';
    updateBtn.addEventListener('click', () => updateUser(user.id, {
      loginId: loginInput.value.trim(),
      name: nameInput.value.trim(),
      email: emailInput.value.trim() || null,
      role: roleSelect.value,
      active: activeSelect.value === 'true',
      password: passwordInput.value,
    }));

    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'secondary';
    deleteBtn.textContent = '削除';
    deleteBtn.addEventListener('click', () => deleteUser(user.id));

    tr.innerHTML = `
      <td>${user.id}</td>
    `;
    const loginCell = document.createElement('td');
    loginCell.appendChild(loginInput);
    const nameCell = document.createElement('td');
    nameCell.appendChild(nameInput);
    const emailCell = document.createElement('td');
    emailCell.appendChild(emailInput);
    const roleCell = document.createElement('td');
    roleCell.appendChild(roleSelect);
    const activeCell = document.createElement('td');
    activeCell.appendChild(activeSelect);
    const passCell = document.createElement('td');
    passCell.appendChild(passwordInput);
    const actionCell = document.createElement('td');
    actionCell.appendChild(updateBtn);
    actionCell.appendChild(deleteBtn);
    actionCell.classList.add('row');

    tr.appendChild(loginCell);
    tr.appendChild(nameCell);
    tr.appendChild(emailCell);
    tr.appendChild(roleCell);
    tr.appendChild(activeCell);
    tr.appendChild(passCell);
    tr.appendChild(actionCell);
    adminUsersBody.appendChild(tr);
  });
}

async function createUser() {
  setError('');
  const payload = {
    loginId: newUserLoginId.value.trim(),
    name: newUserName.value.trim(),
    email: newUserEmail.value.trim() || null,
    role: newUserRole.value || 'MEMBER',
    password: newUserPassword.value,
    active: newUserActive.value === 'true',
  };
  if (!payload.loginId || !payload.name || !payload.password) {
    setError('ログインID・氏名・パスワードは必須です。');
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '作成に失敗しました。');
    }
    newUserLoginId.value = '';
    newUserName.value = '';
    newUserEmail.value = '';
    newUserPassword.value = '';
    newUserRole.value = 'MEMBER';
    newUserActive.value = 'true';
    await loadUsers();
  } catch (e) {
    setError(e.message);
  }
}

async function updateUser(id, payload) {
  setError('');
  if (!payload.loginId || !payload.name) {
    setError('ログインIDと氏名は必須です。');
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '更新に失敗しました。');
    }
    await loadUsers();
  } catch (e) {
    setError(e.message);
  }
}

async function deleteUser(id) {
  if (!confirm('削除しますか？')) {
    return;
  }
  try {
    const res = await fetch(`${ctx}/api/users/${id}`, {
      method: 'DELETE',
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '削除に失敗しました。');
    }
    await loadUsers();
  } catch (e) {
    setError(e.message);
  }
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch (e) {
    return null;
  }
}

loadUsers().catch((e) => setError(e.message));
