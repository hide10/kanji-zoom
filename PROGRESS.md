# 開発進捗・計画書

> セッション断絶時の引き継ぎ用ドキュメント。
> 各フェーズ完了時にチェックを入れて更新する。

## 現在のステータス

**フェーズ: 2.5 完了 → 課金実装・リリース準備へ**
**最終更新: 2026-02-15**

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
- [ ] #2 書き順表示機能

### フェーズ 3: リリース準備
- [ ] Google Play Developer アカウント登録（$25 一回払い）
- [ ] アプリ署名鍵の生成（upload keystore）
- [ ] リリースビルド作成（AAB 形式）
- [ ] プライバシーポリシー作成（個人情報を扱わない旨）
- [ ] スクリーンショット撮影（スマホ・タブレット各1枚以上）

### フェーズ 4: Google Play 公開
- [ ] Google Play Console でアプリ作成
- [ ] ストア掲載情報入力（タイトル・説明・カテゴリ）
- [ ] コンテンツレーティング質問票の回答
- [ ] AAB アップロード
- [ ] 内部テスト → クローズドテスト → 製品版リリース
- [ ] 審査通過・公開完了

---

## 各フェーズの補足

### Google Play 公開に必要なもの（フェーズ 3-4）
1. **Google Play Developer アカウント** - Google アカウント + $25
2. **署名鍵** - `keytool` で生成、紛失厳禁
3. **AAB ファイル** - `./gradlew bundleRelease` で生成
4. **プライバシーポリシー** - URL として公開する必要あり（GitHub Pages 等）
5. **ストアアセット**
   - アプリアイコン: 512x512 PNG → `ic_launcher-playstore.png` 作成済み
   - スクリーンショット: 最低2枚
   - フィーチャーグラフィック: 1024x500 PNG
6. **コンテンツレーティング** - IARC 質問票に回答

### リリースまでの典型的な所要時間
- 内部テスト: 即時公開
- クローズドテスト: 数時間〜1日
- 製品版: 審査に数日（初回は最大7日）

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
| バージョン | 1.0.0 (versionCode: 1) |

---

## メモ・決定事項
- 2026-02-12: プロジェクト開始。MVP はシンプルに1画面構成とする。
- 2026-02-12: Android プロジェクト構造 + MVP コードを生成。
- 2026-02-15: Android Studio (Windows) でビルド＆エミュレータ動作確認成功。
- 2026-02-15: 機能改善実装完了。複数文字対応、自動拡縮、ピンチズーム、部分拡大、入力表示一体化。
- 2026-02-15: フォント切替廃止 → 明朝体固定。リセットボタン廃止。アイコンを青+「漢」に変更。
- 2026-02-15: GitHub Issues 作成。#1 買い切り課金、#2 書き順表示。
- 注意: 開発環境は WSL2 だが、Android Studio は Windows 側で実行。ファイル編集は `/mnt/c/Users/hide1/projects/kanji-zoom` 経由で行い、WSL 側 `/home/hide10/kanji-zoom` に同期してコミット・プッシュする。
- GitHub リポジトリ: https://github.com/hide10/kanji-zoom (private)
