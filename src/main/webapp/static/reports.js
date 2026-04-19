/*
 * [役割] reports: レポート画面の制御。
 * [入力] APIレスポンス。
 * [出力] DOM更新。
 * [依存] fetch。
 */
const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';
const projectId = window.PROJECT_ID;

const reportCards = document.getElementById('reportCards');
const alertList = document.getElementById('alertList');
const reportsError = document.getElementById('reportsError');

function setError(message) {
  reportsError.textContent = message || '';
}

async function ensureProject() {
  return window.ProjectContext.ensure(projectId, reportsError, 'レポート');
}

async function loadReports() {
  const res = await fetch(`${ctx}/api/reports?projectId=${projectId}`);
  if (!res.ok) {
    throw new Error('レポート取得に失敗しました。');
  }
  return res.json();
}

async function loadAlerts() {
  const res = await fetch(`${ctx}/api/alerts?projectId=${projectId}`);
  if (!res.ok) {
    return [];
  }
  return res.json();
}

function renderReportCards(data) {
  const cards = [
    { label: '平均進捗', value: `${data.avgProgress ?? 0}%` },
    { label: '期日超過', value: `${data.overdueCount ?? 0} 件` },
    { label: '予定工数', value: `${formatNumber(data.plannedHours)} h` },
    { label: '実績工数', value: `${formatNumber(data.actualHours)} h` },
    { label: '売上予算', value: `${formatNumber(data.budgetRevenue)} 円` },
    { label: '原価予算', value: `${formatNumber(data.budgetCost)} 円` },
    { label: '売上実績', value: `${formatNumber(data.revenueActual)} 円` },
    { label: '原価実績', value: `${formatNumber(data.costActual)} 円` },
    { label: '工数原価', value: `${formatNumber(data.laborCostActual)} 円` },
    { label: '利益', value: `${formatNumber(data.profit)} 円` },
    { label: '利益率', value: `${Number(data.profitRate || 0).toFixed(1)}%` },
  ];
  reportCards.innerHTML = '';
  cards.forEach((card) => {
    const div = document.createElement('div');
    div.className = 'report-card';
    div.innerHTML = `
      <div class="report-label">${card.label}</div>
      <div class="report-value">${card.value}</div>
    `;
    reportCards.appendChild(div);
  });
}

function renderAlerts(alerts) {
  alertList.innerHTML = '';
  if (!alerts || !alerts.length) {
    alertList.innerHTML = '<div class="muted">現在アラートはありません。</div>';
    return;
  }
  alerts.forEach((alert) => {
    const div = document.createElement('div');
    div.className = 'alert-item';
    div.textContent = alert.message || 'アラート';
    alertList.appendChild(div);
  });
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString('ja-JP');
}

async function init() {
  if (!(await ensureProject())) {
    return;
  }
  try {
    const data = await loadReports();
    renderReportCards(data);
    const alerts = await loadAlerts();
    renderAlerts(alerts);
  } catch (e) {
    setError(e.message);
  }
}

init();
