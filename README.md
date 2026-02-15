# Kanji Zoom - 漢字拡大アプリ

漢字を大きく表示するシンプルな Android アプリ。

## ダウンロード

[Releases ページ](https://github.com/hide10/kanji-zoom/releases) から最新の APK をダウンロードできます。

### APK インストール時の注意

> **APK ファイルは Google Play を経由しないため、以下の点にご注意ください。**
>
> - APK は Google Play の審査を受けていません。インストールは自己責任でお願いします。
> - このアプリはオープンソースです。ソースコードをすべて公開しているので、安全性はご自身で確認いただけます。
> - **提供元不明のアプリ** を信頼できない場合は、インストールしないでください。

### インストール方法

1. Android 端末で [Releases ページ](https://github.com/hide10/kanji-zoom/releases) にアクセス
2. 最新リリースの `app-debug.apk` をダウンロード
3. ダウンロード完了後、ファイルを開く
4. 「提供元不明のアプリのインストールを許可しますか？」と表示されたら許可する
5. インストールをタップ

## 機能

- **漢字入力** - テキストフィールドに漢字を入力
- **拡大表示** - 画面中央に大きく表示
- **ピンチズーム** - ピンチ操作でさらに拡大・縮小

## 技術スタック

| 項目 | 選定 |
|------|------|
| 言語 | Kotlin |
| UI | Jetpack Compose |
| 最小 SDK | API 26 (Android 8.0) |
| ターゲット SDK | API 35 (Android 15) |
| ビルド | Gradle (Kotlin DSL) |

## ビルド

```bash
git clone https://github.com/hide10/kanji-zoom.git
cd kanji-zoom
./gradlew assembleDebug
```

## ライセンス

MIT License
