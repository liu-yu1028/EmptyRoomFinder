<div align="center">

# 🔍 RoomRush
### 輔仁大學空教室查詢系統
**Fu Jen Catholic University Empty Room Finder**

一款以輔仁大學校園情境出發的 Android App，協助學生快速查詢聖言樓與進修部大樓的可用空教室。

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#)
[![Room](https://img.shields.io/badge/Database-Room%20SQLite-2563EB?style=for-the-badge)](#)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-0F766E?style=for-the-badge)](#)

[![下載 APK](https://img.shields.io/badge/Download-RoomRush%20v1.1.0-2ECC71?style=for-the-badge)](https://github.com/liu-yu1028/EmptyRoomFinder/releases/download/v1.1.0/RoomRush-v1.1.0-debug.apk)

</div>

---

## 📌 專案簡介

RoomRush 是大一下學期小組專題延伸整理而成的 Android 校園工具 App。  
使用者可以依照「大樓、星期、節次、樓層」篩選空教室，快速找到可自習、休息或討論的空間。

這次版本重新調整了整體 UI，讓首頁、查詢頁、詳細頁與管理頁更接近一致的 Material Design 風格，也作為後續鐵人賽「從大一小組專題到個人作品」系列的實作基礎。

---

## ✨ 主要功能

### 👤 使用者模式

| 頁面 | 功能 |
|---|---|
| 首頁 | 選擇查詢大樓：聖言樓 / 進修部大樓 |
| 查詢結果頁 | 依星期、節次、樓層篩選空教室，並以列表顯示 |
| 教室詳細頁 | 顯示教室類型與是否可飲食 |

### 🔐 管理者模式

| 功能 | 說明 |
|---|---|
| 管理者登入 | 進入課表與教室管理功能 |
| 課表管理 | 修改教室各時段使用狀態、刪除教室 |
| 教室詳細設定 | 設定教室類型，例如一般教室、電腦教室、實驗室 |
| 全部教室管理 | 瀏覽所有教室資料並新增教室 |

---

## 🏫 教室類型對照

| 代碼 | 中文名稱 | 可飲食 |
|---|---|---|
| `Normal` | 一般教室 | ✅ 是 |
| `Computer` | 電腦教室 | ❌ 否 |
| `Drawing` | 製圖教室 | ❌ 否 |
| `Lab` | 實驗室 | ❌ 否 |
| `MeetingRoom` | 研討室 | ❌ 否 |

---

## 🧭 使用流程

```text
MainActivity
├── 選擇大樓
├── QueryResultActivity
│   ├── 選擇星期 / 節次 / 樓層
│   ├── 顯示空教室列表
│   └── RoomDetailActivity
│       └── 顯示教室類型與飲食規則
└── LoginActivity
    └── ManagerActivity
        ├── LessonManageActivity
        ├── ClassroomManageActivity
        ├── AllClassroomsActivity
        └── ClassroomScheduleDetailActivity
```

---

## 🧱 技術架構

| 分類 | 技術 |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | XML Layout、Material Components、ConstraintLayout、RecyclerView |
| Architecture | MVVM |
| State | ViewModel、LiveData、Flow |
| Database | Room SQLite |
| Data Source | `assets/SF.csv`、`assets/ES.csv` |
| Build Tool | Gradle Kotlin DSL |

---

## 🔄 資料流概念

```text
SF.csv / ES.csv
  ↓ AppDatabase.parseCsv()
ClassroomSchedule
  ↓ Room Database
ClassroomScheduleDao
  ↓ Repository
ViewModel
  ↓ LiveData
Activity / RecyclerView
```

---

## 📂 專案結構

```text
app/src/main/
├── assets/                         # SF.csv / ES.csv 初始課表資料
├── java/com/example/emptyroomfinder/
│   ├── MainActivity.kt             # 首頁與大樓選擇
│   ├── QueryResultActivity.kt      # 查詢條件與結果列表
│   ├── QueryResultViewModel.kt     # 空教室篩選邏輯
│   ├── RoomDetailActivity.kt       # 教室詳細資料
│   ├── AppDatabase.kt              # Room Database 與 CSV 預載
│   ├── ClassroomSchedule.kt        # Room Entity
│   ├── ClassroomScheduleDao.kt     # DAO 查詢與 CRUD
│   └── ClassroomScheduleRepository.kt
└── res/
    ├── layout/                     # XML UI 畫面
    ├── values/                     # colors / strings / themes
    └── drawable/                   # RoomRush logo 與樣式資源
```

---

## 🚀 安裝與執行

### 使用 Android Studio

1. Clone 此專案：

```bash
git clone https://github.com/liu-yu1028/EmptyRoomFinder.git
```

2. 使用 Android Studio 開啟專案。
3. 等待 Gradle Sync 完成。
4. 選擇 Emulator 或實體 Android 裝置。
5. 點擊 Run 執行 App。

### 使用 Gradle 指令

```bash
./gradlew testDebugUnitTest assembleDebug
```

Debug APK 會輸出在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK 會輸出在：

```text
app/build/outputs/apk/release/
```

---

## 📦 版本紀錄

### v1.1

- 重新調整主要頁面 UI
- 改用更一致的 Material 風格卡片、按鈕、配色與間距
- 新增 RoomRush logo 視覺
- 更新 README，補上功能、架構、資料流與執行方式
- 調整 `versionCode` 為 `2`，`versionName` 為 `1.1`

### v1.0

- 初版空教室查詢功能
- 支援聖言樓與進修部大樓課表資料
- 支援管理者維護教室與課表資料

---

## 📝 學習紀錄 / 鐵人賽方向

這個專案也會作為鐵人賽系列的實作案例：

> 從大一小組專題到個人作品：30 天重讀與整理 Android 空教室 App

系列目標：

1. 重新理解 Android 專案架構
2. 拆解 Activity、ViewModel、Repository、DAO、Room 的角色
3. 整理 GitHub repo，讓課堂專題更像作品集
4. 做低風險的小型優化，而不是一開始就大重構

---

## ⚠️ 備註

- 本專案為課程學習作品，教室資料以專題當時整理的 CSV 為主，僅供學習與展示使用。
- 管理者登入目前仍是學習專題用的簡化設計，不適合直接用於正式產品。
- 若未來要正式部署，需重新設計帳號驗證、資料更新流程與資安機制。
