# 開発進捗・計画書

> セッション断絶時の引き継ぎ用ドキュメント。
> 各フェーズ完了時にチェックを入れて更新する。

## 現在のステータス

**フェーズ: 3 進行中 → GitHub Releases 公開準備**
**最終更新: 2026-04-04**

---

## フェーズ一覧

### フェーズ 0: プロジェクト準備 ✅
- [x] Git リポジトリ作成
- [x] README.md 作成（MVP 定義）
- [x] PROGRESS.md 作成（本ファイル）
- [x] .gitignore 作成
- [x] Android プロジェクト生成（Jetpack Compose テンプレート）
- [x] 初回ビルド確認（Android Studio で開いて確認）

### フェーズ 1: MVP 実装 ✅
- [x] 漢字入力用 BasicTextField（表示と入力を一体化、Word風 UX）
- [x] 自動拡縮表示（BoxWithConstraints + calculateOptimalLayout、横一列）
- [x] フォント: 明朝体（Serif）固定（筆画がよく分かるフォント）
- [x] ViewModel 作成（入力文字管理）- StateFlow + MVVM
- [x] エミュレータで動作確認

### フェーズ 2: 品質・仕上げ ✅
- [x] アプリアイコン（青背景 #1565C0 + 白太字「漢」、全密度 + Play Store 512px）
- [x] ピンチ操作による拡大縮小（transformable + graphicsLayer, 1x〜10x）
- [x] 漢字の部分拡大表示（ズーム中にドラッグで移動）
- [x] バージョン情報ダイアログ（「i」ボタン → アプリ情報・操作方法表示）
- [ ] ダークモード確認
- [ ] 画面回転対応の確認

### フェーズ 2.5: 追加機能（GitHub Issues で管理）
- [ ] #1 買い切り課金（機能制限なし使用期限付きトライアル → 買い切りで無期限）
- [x] #2 書き順表示機能

### フェーズ 3: リリース準備
- [ ] アプリ署名鍵の生成（GitHub Releases 用 keystore）
- [x] GitHub Releases 自動公開 workflow 作成
- [ ] GitHub Actions Secrets 登録
- [ ] 初回タグ公開 (`v*`)
- [ ] プライバシーポリシー作成（個人情報を扱わない旨）
- [ ] スクリーンショット撮影（README / 紹介ページ用）

### フェーズ 4: GitHub Releases 公開
- [ ] リリースノート確認
- [ ] APK ダウンロード確認
- [ ] 実機インストール確認
- [ ] リポジトリ公開可否の判断

---

## 各フェーズの補足

### GitHub Releases 公開に必要なもの（フェーズ 3-4）
1. **署名鍵** - `keytool` で生成、紛失厳禁
2. **GitHub Actions Secrets** - keystore とパスワード類を登録
3. **Release APK** - `assembleRelease` で生成
4. **タグ運用** - `v1.1.6` のように tag を切って公開
5. **公開導線** - README か紹介ページから Releases へ誘導

### リリースまでの典型的な所要時間
- GitHub Releases: tag push 後に数分
- README 反映: push 後すぐ

---

## 技術構成

| 項目 | 内容 |
|------|------|
| 言語 | Kotlin 2.1.0 |
| UI | Jetpack Compose (BOM 2024.12.01) |
| アーキテクチャ | Single Activity + MVVM (StateFlow) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| フォント | Serif 固定（Noto Serif CJK） |
| パッケージ | com.hide10.kanjizoom |
| バージョン | 1.1.6 (versionCode: 8) |

---

## メモ・決定事項
- 2026-02-12: プロジェクト開始。MVP はシンプルに1画面構成とする。
- 2026-02-12: Android プロジェクト構造 + MVP コードを生成。
- 2026-02-15: Android Studio (Windows) でビルド＆エミュレータ動作確認成功。
- 2026-02-15: 機能改善実装完了。複数文字対応、自動拡縮、ピンチズーム、部分拡大、入力表示一体化。
- 2026-02-15: フォント切替廃止 → 明朝体固定。リセットボタン廃止。アイコンを青+「漢」に変更。
- 2026-02-15: GitHub Issues 作成。#1 買い切り課金、#2 書き順表示。
- 2026-04-03: 書き順モードを実装。KanjiVG ベースの常用漢字 SVG を assets に同梱し、先頭1文字の筆順アニメを追加。
- 2026-04-04: GitHub Releases 配布に切り替え。signed release APK を GitHub Actions で公開する workflow を追加。
- 注意: 開発環境は WSL2 だが、Android Studio は Windows 側で実行。ファイル編集は `/mnt/c/Users/hide1/projects/kanji-zoom` 経由で行い、WSL 側 `/home/hide10/kanji-zoom` に同期してコミット・プッシュする。
- GitHub リポジトリ: https://github.com/hide10/kanji-zoom (private)
