/*
 * [役割] project_context: プロジェクト選択が必要な画面の案内を共通化する。
 * [入力] projectId / エラー表示先DOM。
 * [出力] 案内メッセージ描画 / 判定結果。
 * [依存] DOM / context-path meta。
 */
window.ProjectContext = (() => {
  const ctx = document.querySelector('meta[name="context-path"]')?.content ?? '';

  function ensure(projectId, errorElement, pageLabel) {
    if (projectId) {
      clear(errorElement);
      return true;
    }
    renderMissingProject(errorElement, pageLabel);
    return false;
  }

  function clear(errorElement) {
    if (!errorElement) {
      return;
    }
    errorElement.innerHTML = '';
  }

  function renderMissingProject(errorElement, pageLabel) {
    if (!errorElement) {
      return;
    }
    const target = pageLabel || 'この画面';
    errorElement.innerHTML = `
      <div class="project-context-notice">
        <div class="project-context-title">${escapeHtml(target)}を表示するには、対象プロジェクトの選択が必要です。</div>
        <div class="project-context-body">プロジェクト一覧から対象プロジェクトを選んで開いてください。</div>
        <div class="project-context-actions">
          <a class="tab active" href="${ctx}/projects">プロジェクト一覧へ</a>
        </div>
      </div>
    `;
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  return { ensure };
})();
