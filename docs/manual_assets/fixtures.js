
(function() {
  const fixtures = {
  "/api/projects": [
    {
      "id": 1,
      "code": "PJ-001",
      "name": "新商品開発",
      "startDate": "2026-01-10",
      "endDate": "2026-03-31",
      "status": "ACTIVE",
      "progress": 42,
      "taskCount": 12
    }
  ],
  "/api/users": [
    {
      "id": 1,
      "loginId": "admin",
      "name": "管理 太郎",
      "email": "admin@example.com",
      "role": "ADMIN",
      "active": true
    },
    {
      "id": 2,
      "loginId": "pm",
      "name": "山田 花子",
      "email": "pm@example.com",
      "role": "MANAGER",
      "active": true
    },
    {
      "id": 3,
      "loginId": "dev",
      "name": "佐藤 次郎",
      "email": "dev@example.com",
      "role": "MEMBER",
      "active": true
    },
    {
      "id": 4,
      "loginId": "qa",
      "name": "鈴木 三郎",
      "email": "qa@example.com",
      "role": "MEMBER",
      "active": true
    }
  ],
  "/api/plan?projectId=1": {
    "project": {
      "id": 1,
      "name": "新商品開発",
      "code": "PJ-001",
      "startDate": "2026-01-10",
      "endDate": "2026-03-31",
      "status": "ACTIVE",
      "dailyHours": 8,
      "workdays": "1,2,3,4,5",
      "baselineSetAt": "2026-02-01T09:00:00"
    },
    "tasks": [
      {
        "id": 1,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 1,
        "name": "キックオフ",
        "description": "プロジェクト開始",
        "status": "DONE",
        "priority": 2,
        "assignedUserId": 2,
        "taskType": "TASK",
        "startDate": "2026-01-10",
        "endDate": "2026-01-10",
        "dueDate": "2026-01-10",
        "progress": 100,
        "plannedHours": 2,
        "actualHours": 2,
        "milestone": true,
        "baselineStartDate": "2026-01-10",
        "baselineEndDate": "2026-01-10"
      },
      {
        "id": 2,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 2,
        "name": "要件定義",
        "description": "要件ヒアリングと整理",
        "status": "DONE",
        "priority": 2,
        "assignedUserId": 2,
        "taskType": "TASK",
        "startDate": "2026-01-11",
        "endDate": "2026-01-20",
        "dueDate": "2026-01-20",
        "progress": 100,
        "plannedHours": 60,
        "actualHours": 58,
        "milestone": false,
        "baselineStartDate": "2026-01-11",
        "baselineEndDate": "2026-01-20"
      },
      {
        "id": 3,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 3,
        "name": "基本設計",
        "description": "画面・データ設計",
        "status": "IN_PROGRESS",
        "priority": 1,
        "assignedUserId": 3,
        "taskType": "TASK",
        "startDate": "2026-01-21",
        "endDate": "2026-02-05",
        "dueDate": "2026-02-05",
        "progress": 60,
        "plannedHours": 80,
        "actualHours": 40,
        "milestone": false,
        "baselineStartDate": "2026-01-21",
        "baselineEndDate": "2026-02-03"
      },
      {
        "id": 4,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 4,
        "name": "詳細設計レビュー",
        "description": "設計レビュー",
        "status": "REVIEW",
        "priority": 2,
        "assignedUserId": 4,
        "taskType": "TASK",
        "startDate": "2026-02-06",
        "endDate": "2026-02-12",
        "dueDate": "2026-02-12",
        "progress": 30,
        "plannedHours": 24,
        "actualHours": 8,
        "milestone": false,
        "baselineStartDate": "2026-02-06",
        "baselineEndDate": "2026-02-10"
      },
      {
        "id": 5,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 5,
        "name": "実装",
        "description": "機能実装",
        "status": "TODO",
        "priority": 1,
        "assignedUserId": 3,
        "taskType": "TASK",
        "startDate": "2026-02-13",
        "endDate": "2026-03-05",
        "dueDate": "2026-03-05",
        "progress": 10,
        "plannedHours": 160,
        "actualHours": 12,
        "milestone": false,
        "baselineStartDate": "2026-02-13",
        "baselineEndDate": "2026-03-03"
      },
      {
        "id": 6,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 6,
        "name": "テスト",
        "description": "総合テスト",
        "status": "ON_HOLD",
        "priority": 3,
        "assignedUserId": 4,
        "taskType": "TASK",
        "startDate": "2026-03-06",
        "endDate": "2026-03-20",
        "dueDate": "2026-03-20",
        "progress": 0,
        "plannedHours": 80,
        "actualHours": 0,
        "milestone": false,
        "baselineStartDate": "2026-03-06",
        "baselineEndDate": "2026-03-18"
      },
      {
        "id": 7,
        "projectId": 1,
        "parentId": null,
        "sortOrder": 7,
        "name": "リリース",
        "description": "本番リリース",
        "status": "TODO",
        "priority": 2,
        "assignedUserId": 2,
        "taskType": "TASK",
        "startDate": "2026-03-25",
        "endDate": "2026-03-25",
        "dueDate": "2026-03-25",
        "progress": 0,
        "plannedHours": 8,
        "actualHours": 0,
        "milestone": true,
        "baselineStartDate": "2026-03-25",
        "baselineEndDate": "2026-03-25"
      }
    ],
    "dependencies": [
      {
        "predecessorId": 1,
        "successorId": 2,
        "type": "FS",
        "lagDays": 0
      },
      {
        "predecessorId": 2,
        "successorId": 3,
        "type": "FS",
        "lagDays": 0
      },
      {
        "predecessorId": 3,
        "successorId": 4,
        "type": "FS",
        "lagDays": 0
      },
      {
        "predecessorId": 4,
        "successorId": 5,
        "type": "FS",
        "lagDays": 0
      },
      {
        "predecessorId": 5,
        "successorId": 6,
        "type": "FS",
        "lagDays": 0
      },
      {
        "predecessorId": 6,
        "successorId": 7,
        "type": "FS",
        "lagDays": 0
      }
    ],
    "resources": [
      {
        "id": 1,
        "projectId": 1,
        "name": "山田 花子",
        "role": "PM",
        "dailyHours": 8,
        "costRate": 9000
      },
      {
        "id": 2,
        "projectId": 1,
        "name": "佐藤 次郎",
        "role": "開発",
        "dailyHours": 7.5,
        "costRate": 7000
      },
      {
        "id": 3,
        "projectId": 1,
        "name": "鈴木 三郎",
        "role": "QA",
        "dailyHours": 6,
        "costRate": 6500
      }
    ],
    "assignments": [
      {
        "taskId": 3,
        "resourceId": 1,
        "allocationHours": 2
      },
      {
        "taskId": 5,
        "resourceId": 2,
        "allocationHours": 6
      },
      {
        "taskId": 6,
        "resourceId": 3,
        "allocationHours": 4
      }
    ]
  },
  "/api/tasks?projectId=1": [
    {
      "id": 1,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 1,
      "name": "キックオフ",
      "description": "プロジェクト開始",
      "status": "DONE",
      "priority": 2,
      "assignedUserId": 2,
      "taskType": "TASK",
      "startDate": "2026-01-10",
      "endDate": "2026-01-10",
      "dueDate": "2026-01-10",
      "progress": 100,
      "plannedHours": 2,
      "actualHours": 2,
      "milestone": true,
      "baselineStartDate": "2026-01-10",
      "baselineEndDate": "2026-01-10"
    },
    {
      "id": 2,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 2,
      "name": "要件定義",
      "description": "要件ヒアリングと整理",
      "status": "DONE",
      "priority": 2,
      "assignedUserId": 2,
      "taskType": "TASK",
      "startDate": "2026-01-11",
      "endDate": "2026-01-20",
      "dueDate": "2026-01-20",
      "progress": 100,
      "plannedHours": 60,
      "actualHours": 58,
      "milestone": false,
      "baselineStartDate": "2026-01-11",
      "baselineEndDate": "2026-01-20"
    },
    {
      "id": 3,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 3,
      "name": "基本設計",
      "description": "画面・データ設計",
      "status": "IN_PROGRESS",
      "priority": 1,
      "assignedUserId": 3,
      "taskType": "TASK",
      "startDate": "2026-01-21",
      "endDate": "2026-02-05",
      "dueDate": "2026-02-05",
      "progress": 60,
      "plannedHours": 80,
      "actualHours": 40,
      "milestone": false,
      "baselineStartDate": "2026-01-21",
      "baselineEndDate": "2026-02-03"
    },
    {
      "id": 4,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 4,
      "name": "詳細設計レビュー",
      "description": "設計レビュー",
      "status": "REVIEW",
      "priority": 2,
      "assignedUserId": 4,
      "taskType": "TASK",
      "startDate": "2026-02-06",
      "endDate": "2026-02-12",
      "dueDate": "2026-02-12",
      "progress": 30,
      "plannedHours": 24,
      "actualHours": 8,
      "milestone": false,
      "baselineStartDate": "2026-02-06",
      "baselineEndDate": "2026-02-10"
    },
    {
      "id": 5,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 5,
      "name": "実装",
      "description": "機能実装",
      "status": "TODO",
      "priority": 1,
      "assignedUserId": 3,
      "taskType": "TASK",
      "startDate": "2026-02-13",
      "endDate": "2026-03-05",
      "dueDate": "2026-03-05",
      "progress": 10,
      "plannedHours": 160,
      "actualHours": 12,
      "milestone": false,
      "baselineStartDate": "2026-02-13",
      "baselineEndDate": "2026-03-03"
    },
    {
      "id": 6,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 6,
      "name": "テスト",
      "description": "総合テスト",
      "status": "ON_HOLD",
      "priority": 3,
      "assignedUserId": 4,
      "taskType": "TASK",
      "startDate": "2026-03-06",
      "endDate": "2026-03-20",
      "dueDate": "2026-03-20",
      "progress": 0,
      "plannedHours": 80,
      "actualHours": 0,
      "milestone": false,
      "baselineStartDate": "2026-03-06",
      "baselineEndDate": "2026-03-18"
    },
    {
      "id": 7,
      "projectId": 1,
      "parentId": null,
      "sortOrder": 7,
      "name": "リリース",
      "description": "本番リリース",
      "status": "TODO",
      "priority": 2,
      "assignedUserId": 2,
      "taskType": "TASK",
      "startDate": "2026-03-25",
      "endDate": "2026-03-25",
      "dueDate": "2026-03-25",
      "progress": 0,
      "plannedHours": 8,
      "actualHours": 0,
      "milestone": true,
      "baselineStartDate": "2026-03-25",
      "baselineEndDate": "2026-03-25"
    }
  ],
  "/api/reports?projectId=1": {
    "avgProgress": 42,
    "overdueCount": 1,
    "plannedHours": 414,
    "actualHours": 120,
    "budgetRevenue": 5000000,
    "budgetCost": 3200000,
    "revenueActual": 1200000,
    "costActual": 680000,
    "laborCostActual": 420000,
    "profit": 100000,
    "profitRate": 8.3
  },
  "/api/alerts?projectId=1": [
    {
      "message": "期日超過のタスクが1件あります。"
    },
    {
      "message": "実績工数が計画の80%を超えました。"
    }
  ],
  "/api/finance?projectId=1": {
    "project": {
      "budgetRevenue": 5000000,
      "budgetCost": 3200000
    },
    "finance": {
      "revenueActual": 1200000,
      "costActual": 680000,
      "laborCostActual": 420000
    }
  },
  "/api/forum?projectId=1": [
    {
      "id": 1,
      "title": "初回キックオフの議題",
      "createdAt": "2026-01-10T09:10:00",
      "createdBy": 2
    },
    {
      "id": 2,
      "title": "要件の優先順位整理",
      "createdAt": "2026-01-12T13:30:00",
      "createdBy": 3
    }
  ],
  "/api/forum/posts?threadId=1": [
    {
      "id": 1,
      "body": "議題は「目的共有」「役割分担」「スケジュール確認」です。",
      "createdAt": "2026-01-10T09:20:00",
      "createdBy": 2
    },
    {
      "id": 2,
      "body": "資料はドキュメント管理にアップしました。",
      "createdAt": "2026-01-10T09:25:00",
      "createdBy": 3
    }
  ],
  "/api/forum/posts?threadId=2": [
    {
      "id": 3,
      "body": "優先度Aは「ログイン」「ダッシュボード」。",
      "createdAt": "2026-01-12T14:00:00",
      "createdBy": 2
    }
  ],
  "/api/chat?projectId=1": [
    {
      "id": 1,
      "message": "本日の進捗報告お願いします。",
      "createdAt": "2026-01-15T09:00:00",
      "userId": 2
    },
    {
      "id": 2,
      "message": "設計レビュー資料を共有しました。",
      "createdAt": "2026-01-15T10:15:00",
      "userId": 3
    }
  ],
  "/api/docs?projectId=1": [
    {
      "id": 1,
      "filename": "要件定義書_v1.pdf",
      "contentType": "application/pdf",
      "taskId": 2
    },
    {
      "id": 2,
      "filename": "画面設計.xlsx",
      "contentType": "application/vnd.ms-excel",
      "taskId": 3
    },
    {
      "id": 3,
      "filename": "テスト計画.docx",
      "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "taskId": 6
    }
  ],
  "/api/issues?projectId=1": [
    {
      "id": 101,
      "title": "ログイン失敗時のメッセージ誤り",
      "description": "文言が古い",
      "status": "OPEN",
      "priority": 2,
      "severity": "MEDIUM",
      "assigneeId": 3,
      "resolvedAt": null
    },
    {
      "id": 102,
      "title": "カレンダーの土日色が崩れる",
      "description": "Safariのみ",
      "status": "IN_PROGRESS",
      "priority": 3,
      "severity": "LOW",
      "assigneeId": 4,
      "resolvedAt": null
    },
    {
      "id": 103,
      "title": "工数集計が0になる",
      "description": "特定条件で発生",
      "status": "RESOLVED",
      "priority": 1,
      "severity": "HIGH",
      "assigneeId": 2,
      "resolvedAt": "2026-01-20T18:00:00"
    }
  ],
  "/api/workflow?projectId=1": [
    {
      "id": 201,
      "taskId": 3,
      "requesterId": 2,
      "approverId": 1,
      "status": "PENDING",
      "message": "設計完了の承認をお願いします。"
    },
    {
      "id": 202,
      "taskId": 2,
      "requesterId": 2,
      "approverId": 1,
      "status": "APPROVED",
      "message": "要件定義の承認済み。"
    }
  ],
  "/api/wiki?projectId=1": [
    {
      "id": 1,
      "title": "プロジェクト概要",
      "content": "目的・範囲・体制をまとめる",
      "updatedAt": "2026-01-12T11:00:00",
      "version": 3
    },
    {
      "id": 2,
      "title": "用語集",
      "content": "略語と意味",
      "updatedAt": "2026-01-14T16:30:00",
      "version": 2
    }
  ]
};
  window.__fixtures = fixtures;
  window.setupMockFetch = function setupMockFetch() {
    const originalFetch = window.fetch ? window.fetch.bind(window) : null;
    window.fetch = async function(input, init) {
      const url = typeof input === 'string' ? input : (input && input.url) || '';
      let key = '';
      try {
        const u = new URL(url, window.location.href);
        key = u.pathname + u.search;
      } catch (e) {
        key = String(url);
      }
      if (fixtures[key]) {
        return new Response(JSON.stringify(fixtures[key]), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        });
      }
      if (originalFetch) {
        return originalFetch(input, init);
      }
      return new Response(JSON.stringify({ message: 'fixture not found', key }), {
        status: 404,
        headers: { 'Content-Type': 'application/json' },
      });
    };
  };
})();
