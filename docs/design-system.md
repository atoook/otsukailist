# OTSUKAI リスト - デザインシステム

## デザインコンセプト

### 🔥 テーマ：「BBQ & Smoky」

アウトドアキッチンの温かみと力強さを表現したデザインシステム。
木の温もり、スモークの風味、グリルの香ばしさを感じられる UI/UX を目指します。

### 🎨 デザイン哲学

- **温かみ (Warmth)**: 木材の自然な温もりを感じられる
- **力強さ (Strength)**: グリルの火力、スモークの深み
- **自然感 (Natural)**: 素材感を活かしたテクスチャー
- **職人気質 (Crafted)**: 手作りの温もりと丁寧さ

---

## カラーパレット

### 🟤 プライマリカラー（ウッドトーン）

```
CSS変数定義 (@theme):
- --color-wood-50:  #fef7f0   // ライトバーチ
- --color-wood-100: #fde4cc   // ホワイトオーク
- --color-wood-200: #f9c299   // メープル
- --color-wood-300: #f39f66   // チーク
- --color-wood-400: #ed7c33   // レッドシーダー
- --color-wood-500: #d4571e   // ウォールナット（メイン）
- --color-wood-600: #b8471a   // マホガニー
- --color-wood-700: #9c3817   // ダークチーク
- --color-wood-800: #7f2a12   // エボニー
- --color-wood-900: #651e0d   // ダークエボニー

Tailwindクラス使用例:
bg-wood-500, text-wood-50, border-wood-300
hover:bg-wood-600, focus:ring-wood-500
```

### ⚫ セカンダリカラー（チャコール・グリル）

```
CSS変数定義 (@theme):
- --color-charcoal-50:  #f8f9fa
- --color-charcoal-100: #e9ecef
- --color-charcoal-200: #dee2e6
- --color-charcoal-300: #ced4da
- --color-charcoal-400: #9fa6b2
- --color-charcoal-500: #6c757d
- --color-charcoal-600: #495057   // グリルメタル
- --color-charcoal-700: #343a40   // ダークチャコール
- --color-charcoal-800: #212529   // ブラックチャコール
- --color-charcoal-900: #0d1117   // ピットブラック

Tailwindクラス使用例:
bg-charcoal-700, text-charcoal-100, border-charcoal-600
hover:bg-charcoal-800, focus:ring-charcoal-500
```

### 🔥 アクセントカラー（炎・スモーク）

```
CSS変数定義 (@theme):
- --color-ember-400: #ff6b35    // エンバーオレンジ
- --color-ember-500: #ff5722    // ファイアオレンジ
- --color-ember-600: #e64a19    // ディープファイア

- --color-smoke-300: #b0bec5    // ライトスモーク
- --color-smoke-400: #90a4ae    // ミディアムスモーク
- --color-smoke-500: #78909c    // スモークグレー

Tailwindクラス使用例:
アクション・警告: bg-ember-500, text-ember-400, border-ember-600
無効状態・プレースホルダー: bg-smoke-400, text-smoke-500, placeholder-smoke-300
```

---

## タイポグラフィ

### 🏷️ フォント階層

```
CSS変数定義 (@theme):
--font-family-sans: "Inter", "Noto Sans JP", sans-serif
--font-family-serif: "Roboto Slab", "Noto Serif JP", serif

Tailwindクラス使用例:
font-sans, font-serif
font-light (300), font-normal (400), font-medium (500)
font-semibold (600), font-bold (700), font-extrabold (800)
```

### 📏 フォントサイズスケール

```
Tailwind標準クラス (サイズ/行間):
text-xs:    12px / 16px  // キャプション
text-sm:    14px / 20px  // 小テキスト
text-base:  16px / 24px  // 本文
text-lg:    18px / 28px  // 大きめ本文
text-xl:    20px / 28px  // 小見出し
text-2xl:   24px / 32px  // 見出し
text-3xl:   30px / 36px  // 大見出し
text-4xl:   36px / 40px  // タイトル

使用例:
<h1 class="text-3xl font-bold font-serif">メインタイトル</h1>
<p class="text-base font-normal text-charcoal-700">本文テキスト</p>
```

---

## コンポーネントスタイル

### 🧱 カードコンポーネント

```
Background: wood-50 (ライトウッド)
Border: 1px solid wood-200
Border Radius: 12px (手作り感のある丸み)
Shadow: 0 4px 6px -1px smoke-300/10, 0 2px 4px -1px smoke-500/10
```

### 🔘 ボタンデザイン

```
Primary Button:
- Background: wood-500 → wood-600 (hover)
- Text: white
- Border Radius: 8px
- Shadow: 0 2px 4px smoke-400/20
- Transform: scale(1.02) on hover

Secondary Button:
- Background: charcoal-600 → charcoal-700 (hover)
- Text: white
- Border: 1px solid charcoal-500
```

### 📝 入力フィールド

```
Input Field:
- Background: wood-50
- Border: 2px solid wood-200
- Focus Border: wood-500
- Border Radius: 6px
- Text: charcoal-800
- Placeholder: charcoal-400
```

---

## アイコン・イラストレーション

### 🎨 アイコンスタイル

- **スタイル**: Outline + Filled のハイブリッド
- **ストローク**: 2px（しっかりとした線）
- **角の丸み**: rounded corners（温かみ）
- **カラー**: wood-600, charcoal-600 をメイン

### 🖼️ イラストレーション要素

```
BBQ関連アイコン:
- 🔥 炎（ember-500）
- 🍖 グリル（charcoal-700）
- 🌿 ハーブ（緑系アクセント）
- 🪵 薪（wood-600）

テクスチャーパターン:
- 木目模様（ヘッダー背景）
- スモークパーティクル（loading状態）
- チャコールテクスチャー（アクセント）
```

---

## レイアウト原則

### 📐 スペーシング

```
Tailwind標準スケール (基本単位: 4px):
- 1: 4px   // p-1, m-1, gap-1
- 2: 8px   // p-2, m-2, gap-2
- 3: 12px  // p-3, m-3, gap-3
- 4: 16px  // p-4, m-4, gap-4 (標準間隔)
- 6: 24px  // p-6, m-6, gap-6 (大きい間隔)
- 8: 32px  // p-8, m-8, gap-8 (セクション間)
- 12: 48px // p-12, m-12, gap-12 (大セクション間)
- 16: 64px // p-16, m-16, gap-16 (ページ間)

使用例:
<div class="p-6 mb-8">コンテンツ</div>
<div class="flex gap-4">アイテム間隔</div>
```

### 🏢 グリッドシステム & レスポンシブデザイン

```
Tailwind標準ブレークポイント:
- sm: 640px  (small devices - メインターゲット: スマホ)
- md: 768px  (medium devices - タブレット)
- lg: 1024px (large devices)
- xl: 1280px (extra large devices)

Container設定:
max-width: sm (640px) - スマホファースト設計

Tailwindクラス実装:
<!-- コンテナ（スマホファースト） -->
<div class="max-w-sm mx-auto px-4">
  <!-- メインコンテンツ -->
</div>

<!-- レスポンシブ例 -->
<div class="p-4 sm:p-6 md:p-8">レスポンシブパディング</div>
<div class="text-sm sm:text-base md:text-lg">レスポンシブテキスト</div>
```

---

## アニメーション・マイクロインタラクション

### ⚡ トランジション & アニメーション

```
Tailwind標準クラス:
Duration:
- Fast: 150ms    // duration-150 (ホバー、クリック)
- Normal: 300ms  // duration-300 (ページ遷移、モーダル)
- Slow: 500ms    // duration-500 (ローディング)

Easing:
- ease-out: ボタンホバー
- ease-in-out: ページ遷移
- transition-all: 全プロパティのアニメーション

使用例:
<!-- ボタンホバー -->
<button class="transition-all duration-200 hover:scale-105 hover:shadow-lg">
<!-- フェードイン -->
<div class="transition-opacity duration-300 opacity-0 hover:opacity-100">
<!-- スライド -->
<div class="transform transition-transform duration-300 translate-x-full">
```

Easing:

- ease-out: ボタンホバー
- ease-in-out: ページ遷移
- spring: モーダル表示

```

### 🎭 特殊効果

```

Smoke Effect:

- パーティクルアニメーション（ローディング）
- フェードイン/アウト

Fire Effect:

- ゆらめきアニメーション（アクション完了）
- Color shifting（ember-400 → ember-600）

```

---

## 実装優先度

### 🚀 Phase 1: 基礎カラーパレット

- [ ] Tailwind config でカスタムカラー定義
- [ ] 基本コンポーネントの色変更
- [ ] タイポグラフィの適用

### 🔥 Phase 2: テクスチャー・アクセント

- [ ] 木目テクスチャーの背景
- [ ] スモークエフェクトの追加
- [ ] アイコンのカスタマイズ

### ✨ Phase 3: インタラクション

- [ ] マイクロアニメーション
- [ ] ローディングエフェクト
- [ ] 特殊効果の実装

---

## 参考イメージ

### 🎨 カラーインスピレーション

- 焚き火の炎の色合い
- 燻製された木材の色味
- チャコールグリルのメタリック感
- ウッドチップの自然な茶色

### 🏞️ テクスチャーインスピレーション

- オーク材の木目
- チャコールの粗い質感
- スモークの柔らかな煙
- 鋳鉄グリルの重厚感
```
