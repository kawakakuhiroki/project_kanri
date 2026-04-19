# プロジェクト管理・工程管理システム

Microsoft Projectの操作感に近いWBS/ガント/工数管理を目指したWebアプリです。

## 技術スタック
- Frontend: HTML / CSS / JavaScript（Vanilla）
- View: JSP
- Backend: Java 17 / Servlet（Jakarta Servlet 5.0）
- JSON: Jackson
- DB: MySQL
- Build: Maven（war）

## セットアップ
1. MySQL に `db/ddl.sql` を実行
2. `src/main/resources/app.properties` のDB接続情報を設定
3. `mvn -q -DskipTests package`
4. `target/project-kanri.war` を Tomcat へデプロイ

## 初期ユーザー
認証を有効化しているため、最初に管理者ユーザーを作成してください。
例: MySQL で以下を実行（`password_hash` は SHA-256 の16進数です）。
```sql
INSERT INTO users (login_id, name, email, password_hash, role, active)
VALUES ('admin', '管理者', 'admin@example.com', SHA2('password', 256), 'ADMIN', 1);
```

## 権限/アクセス制御
- `ADMIN` は全プロジェクトにアクセス可能。
- `MANAGER`/`MEMBER`/`VIEWER` は `project_members` に登録されたプロジェクトのみアクセス可能。
- 新規プロジェクト作成時、作成者は自動で `project_members` に登録されます。
```sql
INSERT INTO project_members (project_id, user_id, role)
VALUES (1, 2, 'MEMBER')
ON DUPLICATE KEY UPDATE role=VALUES(role);
```

## 画面
- `/login`: ログイン
- `/projects`: プロジェクト一覧
- `/project?projectId=1`: 計画編集（WBS/ガント/工数/リソース/ベースライン）
- `/kanban?projectId=1`: かんばん
- `/calendar?projectId=1`: カレンダー
- `/reports?projectId=1`: レポート
- `/finance?projectId=1`: 予実管理
- `/forum?projectId=1`: フォーラム/チャット
- `/docs?projectId=1`: ドキュメント管理
- `/issues?projectId=1`: 不具合管理
- `/workflow?projectId=1`: 承認ワークフロー
- `/wiki?projectId=1`: Wiki
- `/admin/users`: ユーザー管理（ADMINのみ）

## API
- `GET /api/projects`
- `POST /api/projects`
- `GET /api/plan?projectId={id}`
- `PUT /api/plan?projectId={id}`
- `POST /api/plan/baseline?projectId={id}`
- `GET /api/tasks?projectId={id}` / `POST /api/tasks?projectId={id}` / `PUT /api/tasks/{id}`
- `GET /api/finance?projectId={id}` / `PUT /api/finance?projectId={id}`
- `GET /api/reports?projectId={id}` / `GET /api/alerts?projectId={id}`
- `GET /api/forum?projectId={id}` / `POST /api/forum?projectId={id}`
- `GET /api/forum/posts?threadId={id}` / `POST /api/forum/posts?threadId={id}`
- `GET /api/chat?projectId={id}` / `POST /api/chat?projectId={id}`
- `GET /api/docs?projectId={id}` / `POST /api/docs?projectId={id}` / `DELETE /api/docs?id={id}`
- `GET /api/issues?projectId={id}` / `POST /api/issues?projectId={id}` / `PUT /api/issues/{id}` / `DELETE /api/issues/{id}`
- `GET /api/workflow?projectId={id}` / `POST /api/workflow?projectId={id}` / `PUT /api/workflow/{id}`
- `GET /api/wiki?projectId={id}` / `POST /api/wiki?projectId={id}`
- `GET /api/users` / `POST /api/users` / `PUT /api/users/{id}` / `DELETE /api/users/{id}`（ADMINのみ）
