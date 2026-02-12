# Kanji Zoom - 漢字拡大アプリ

漢字を大きく表示するシンプルな Android アプリ。
**主目的: Android アプリの公開フローを学ぶ。**

## MVP (Minimum Viable Product)

### 機能
1. **漢字入力** - テキストフィールドに漢字を1文字入力
2. **拡大表示** - 画面中央に大きく表示（フォントサイズ 200sp 以上）
3. **フォント切替** - 明朝体 / ゴシック体の切り替え

### 画面構成（1画面）

```
┌──────────────────────┐
│  漢字ズーム          │
├──────────────────────┤
│                      │
│  ┌────────────────┐  │
│  │ 漢字を入力     │  │
│  └────────────────┘  │
│                      │
│                      │
│        漢              │
│                      │
│                      │
│  [明朝] [ゴシック]   │
│                      │
└──────────────────────┘
```

### 対象外（MVP には含めない）
- 履歴機能
- お気に入り機能
- 筆順表示
- 辞書連携
- ウィジェット

## 技術スタック

| 項目 | 選定 |
|------|------|
| 言語 | Kotlin |
| UI | Jetpack Compose |
| 最小 SDK | API 26 (Android 8.0) |
| ターゲット SDK | API 35 (Android 15) |
| ビルド | Gradle (Kotlin DSL) |
| アーキテクチャ | Single Activity, MVVM |

## セットアップ

```bash
# クローン
git clone <repository-url>
cd kanji-zoom

# Android Studio で開く
# または CLI ビルド
./gradlew assembleDebug
```

## ライセンス

MIT License
