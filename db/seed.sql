-- 初期データ投入（idempotent）
USE project_kanri;

-- 管理者ユーザー
INSERT INTO users (login_id, name, email, password_hash, role, active)
SELECT 'admin', '管理者', 'admin@example.com', SHA2('password', 256), 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'admin');

-- 管理者以外のデモユーザー
INSERT INTO users (login_id, name, email, password_hash, role, active)
SELECT 'manager', 'マネージャー', 'manager@example.com', SHA2('password', 256), 'MANAGER', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'manager');

INSERT INTO users (login_id, name, email, password_hash, role, active)
SELECT 'member', 'メンバー', 'member@example.com', SHA2('password', 256), 'MEMBER', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login_id = 'member');

-- 既存プロジェクトに管理者を全紐付け
INSERT INTO project_members (project_id, user_id, role)
SELECT p.id, u.id, 'ADMIN'
FROM projects p
JOIN users u ON u.login_id = 'admin'
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- デモプロジェクト（存在しない場合のみ作成）
INSERT INTO projects (name, code, description, start_date, end_date, status, budget_revenue, budget_cost, workdays, daily_hours)
SELECT 'デモプロジェクト', 'DEMO', '初期確認用のサンプル', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', 1000000, 600000, '1,2,3,4,5', 8.00
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE code = 'DEMO');

-- デモプロジェクトに管理者/マネージャー/メンバーを割当
INSERT INTO project_members (project_id, user_id, role)
SELECT p.id, u.id, u.role
FROM projects p
JOIN users u ON u.login_id IN ('admin','manager','member')
WHERE p.code = 'DEMO'
ON DUPLICATE KEY UPDATE role = VALUES(role);
