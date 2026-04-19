/*
 * [役割] finance: 予実管理画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const budgetRevenue = document.getElementById('budgetRevenue');
const budgetCost = document.getElementById('budgetCost');
const revenueActual = document.getElementById('revenueActual');
const costActual = document.getElementById('costActual');
const laborCostActual = document.getElementById('laborCostActual');
const saveFinanceBtn = document.getElementById('saveFinanceBtn');
const financeError = document.getElementById('financeError');
const financeSummary = document.getElementById('financeSummary');

saveFinanceBtn.addEventListener('click', saveFinance);

function bucketInputs() {
  return [budgetRevenue, budgetCost, revenueActual, costActual, laborCostActual].filter(Boolean);
}

function setError(message) {
  financeError.textContent = message || '';
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
      window.location.href = `${ctx}/finance?projectId=${projects[0].id}`;
      return false;
    }
    setError('プロジェクトがありません。先に作成してください。');
  } catch (e) {
    setError(e.message);
  }
  return false;
}

async function loadFinance() {
  const res = await fetch(`${ctx}/api/finance?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('予実情報の取得に失敗しました。');
  }
  const data = await res.json();
  const project = data.project || {};
  const finance = data.finance || {};
  budgetRevenue.value = project.budgetRevenue ?? 0;
  budgetCost.value = project.budgetCost ?? 0;
  revenueActual.value = finance.revenueActual ?? 0;
  costActual.value = finance.costActual ?? 0;
  laborCostActual.value = finance.laborCostActual ?? 0;
  renderSummary();
}

function renderSummary() {
  const revenueBudgetVal = toNumber(budgetRevenue.value);
  const costBudgetVal = toNumber(budgetCost.value);
  const revenueActualVal = toNumber(revenueActual.value);
  const costActualVal = toNumber(costActual.value);
  const laborActualVal = toNumber(laborCostActual.value);
  const totalCost = costActualVal + laborActualVal;
  const profit = revenueActualVal - totalCost;
  const profitRate = revenueActualVal === 0 ? 0 : (profit / revenueActualVal) * 100;

  financeSummary.innerHTML = `
    <div class="summary-card">
      <div class="muted small">粗利</div>
      <div class="h2">${formatNumber(profit)} 円</div>
    </div>
    <div class="summary-card">
      <div class="muted small">粗利率</div>
      <div class="h2">${profitRate.toFixed(1)}%</div>
    </div>
    <div class="summary-card">
      <div class="muted small">予算差異</div>
      <div class="h2">${formatNumber(revenueBudgetVal - revenueActualVal)} 円</div>
    </div>
    <div class="summary-card">
      <div class="muted small">原価差異</div>
      <div class="h2">${formatNumber(costBudgetVal - totalCost)} 円</div>
    </div>
  `;
}

async function saveFinance() {
  setError('');
  const payload = {
    budgetRevenue: toNumber(budgetRevenue.value),
    budgetCost: toNumber(budgetCost.value),
    revenueActual: toNumber(revenueActual.value),
    costActual: toNumber(costActual.value),
    laborCostActual: toNumber(laborCostActual.value),
  };
  try {
    const res = await fetch(`${ctx}/api/finance?projectId=${projectId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await safeJson(res);
      throw new Error(err?.message || '保存に失敗しました。');
    }
    renderSummary();
  } catch (e) {
    setError(e.message);
  }
}

function toNumber(value) {
  const n = Number(value || 0);
  return Number.isNaN(n) ? 0 : n;
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString('ja-JP');
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
    await loadFinance();
    bucketInputs().forEach((input) => {
      input.addEventListener('input', renderSummary);
    });
  } catch (e) {
    setError(e.message);
  }
}

init();
