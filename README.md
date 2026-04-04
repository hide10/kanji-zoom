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
2. 最新リリースの `kanji-zoom-v*.apk` をダウンロード
3. ダウンロード完了後、ファイルを開く
4. 「提供元不明のアプリのインストールを許可しますか？」と表示されたら許可する
5. インストールをタップ

## 機能

- **漢字入力** - テキストフィールドに漢字を入力
- **拡大表示** - 画面中央に大きく表示
- **ピンチズーム** - ピンチ操作でさらに拡大・縮小
- **書き順アニメ** - 書き順モードで選択した1文字の筆順を順番に表示

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

## GitHub Releases で APK を公開する

GitHub Actions で signed release APK をビルドして、そのまま GitHub Releases に添付できます。

### 1. 署名鍵を作る

```bash
keytool -genkeypair \
  -v \
  -keystore kanji-zoom-release.jks \
  -alias kanji-zoom \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

この `kanji-zoom-release.jks` はアップデート配布に必須なので、必ず安全な場所にバックアップしてください。

### 2. GitHub Secrets を登録する

リポジトリの `Settings > Secrets and variables > Actions` に次を登録します。

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

`ANDROID_KEYSTORE_BASE64` は次で作れます。

```bash
base64 -w 0 kanji-zoom-release.jks
```

macOS の場合:

```bash
base64 < kanji-zoom-release.jks | tr -d '\n'
```

### 3. リリースを作る

タグを push すると `Release APK` workflow が走って、`kanji-zoom-v*.apk` を GitHub Releases に公開します。

```bash
git tag v1.1.6
git push origin v1.1.6
```

手動で出したい場合は `Actions > Release APK > Run workflow` から `release_tag` を指定します。

## ライセンス

アプリ本体のコードは MIT License です。

書き順データは KanjiVG by Ulrich Apel を利用しており、CC BY-SA 3.0 の条件で同梱しています。
- Source: https://kanjivg.tagaini.net/
- License: https://creativecommons.org/licenses/by-sa/3.0/
