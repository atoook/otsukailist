<script>
import ContentArea from '../components/ContentArea.vue';

export default {
  name: 'PrivacyPolicyPage',
  components: {
    ContentArea
  }
};
</script>

<template>
  <ContentArea>
    <article class="space-y-6 text-charcoal-700">
      <header class="space-y-2">
        <p class="text-xs font-semibold text-wood-700">Privacy Policy</p>
        <h1 class="text-2xl font-bold text-charcoal-800">プライバシーポリシー</h1>
        <p class="text-sm leading-relaxed text-charcoal-600">
          OTSUKAI
          リスト（以下「本サービス」といいます。）は、本サービスで取得する情報の取扱いについて、以下のとおり定めます。
        </p>
      </header>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">1. 取得する情報</h2>
        <p class="text-sm leading-relaxed">本サービスでは、以下の情報を取得または保存することがあります。</p>
        <ul class="list-disc space-y-1 pl-5 text-sm leading-relaxed">
          <li>ユーザーが入力したリスト名、メンバー名、アイテム名、数量、状態等の情報</li>
          <li>本サービスの利用に伴い生成されるリストID、メンバーID、更新日時等の情報</li>
          <li>ブラウザのローカルストレージに保存される最近見たリスト、選択中のメンバー等の情報</li>
          <li>アクセス日時、IPアドレス、User-Agent、エラー情報等の技術的なログ情報</li>
        </ul>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">2. 利用目的</h2>
        <p class="text-sm leading-relaxed">取得した情報は、以下の目的で利用します。</p>
        <ul class="list-disc space-y-1 pl-5 text-sm leading-relaxed">
          <li>本サービスの提供、表示、共有、編集機能のため</li>
          <li>本サービスの保守、障害対応、不正利用の防止のため</li>
          <li>本サービスの改善、利用状況の把握のため</li>
          <li>ユーザーからの問い合わせ、フィードバックへの対応のため</li>
        </ul>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">3. 共有URLとローカルストレージ</h2>
        <p class="text-sm leading-relaxed">
          本サービスでは、リストのURLを知っている人が当該リストにアクセスできます。ユーザーは、個人情報、秘密情報、公開を望まない情報を入力しないよう注意してください。
        </p>
        <p class="text-sm leading-relaxed">
          本サービスは、利便性向上のため、最近見たリストや選択中のメンバー等をブラウザのローカルストレージに保存することがあります。これらの情報は、ブラウザの設定や履歴削除機能により削除できます。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">4. 第三者提供</h2>
        <p class="text-sm leading-relaxed">
          運営者は、法令に基づく場合を除き、取得した情報を第三者に提供しません。ただし、本サービスの提供に必要なサーバー、データベース、ホスティング、監視等の外部サービスに情報が保存または処理される場合があります。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">5. 情報の管理と免責</h2>
        <p class="text-sm leading-relaxed">
          運営者は、取得した情報の漏えい、滅失、毀損等を防ぐため、合理的な範囲で必要な措置を講じます。ただし、個人開発の無料サービスである性質上、情報の完全な安全性、保存、復元可能性、継続的な管理を保証するものではありません。
        </p>
        <p class="text-sm leading-relaxed">
          共有URLの管理、第三者によるアクセス、情報の消失・漏えい、外部サービスの障害、その他本サービスに関連して生じたいかなる不利益または損害についても、運営者は法令上認められる範囲で、理由を問わず一切の責任を負わないものとします。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">6. 本ポリシーの変更</h2>
        <p class="text-sm leading-relaxed">
          運営者は、必要に応じて本ポリシーを変更できるものとします。変更後のポリシーは、本サービス上に掲載された時点で効力を生じるものとします。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">7. お問い合わせ</h2>
        <p class="text-sm leading-relaxed">
          本ポリシーに関する問い合わせは、本サービス上のフィードバック導線、または運営者が別途指定する方法によりご連絡ください。
        </p>
      </section>

      <footer class="border-t border-charcoal-200 pt-4">
        <p class="text-xs leading-relaxed text-charcoal-500">制定日：2026年5月8日</p>
      </footer>
    </article>
  </ContentArea>
</template>
