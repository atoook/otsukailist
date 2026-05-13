<script>
import ContentArea from '../components/ContentArea.vue';

export default {
  name: 'TermsPage',
  components: {
    ContentArea
  }
};
</script>

<template>
  <ContentArea>
    <article class="space-y-6 text-charcoal-700">
      <header class="space-y-2">
        <p class="text-xs font-semibold text-wood-700">Terms of Service</p>
        <h1 class="text-2xl font-bold text-charcoal-800">利用規約</h1>
        <p class="text-sm leading-relaxed text-charcoal-600">
          この利用規約（以下「本規約」といいます。）は、OTSUKAI
          リスト（以下「本サービス」といいます。）の利用条件を定めるものです。本サービスを利用したユーザーは、本規約に同意したものとみなします。
        </p>
      </header>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第1条（適用）</h2>
        <p class="text-sm leading-relaxed">
          本規約は、本サービスの利用に関するユーザーと運営者との間の一切の関係に適用されます。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第2条（本サービスの内容）</h2>
        <p class="text-sm leading-relaxed">
          本サービスは、買い物リストを作成し、URLを共有して複数人で閲覧・編集できる無料サービスです。ユーザー登録は不要です。リストのURLを知っている人は、当該リストにアクセスできます。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第3条（禁止事項）</h2>
        <p class="text-sm leading-relaxed">ユーザーは、本サービスの利用にあたり、以下の行為をしてはなりません。</p>
        <ul class="list-disc space-y-1 pl-5 text-sm leading-relaxed">
          <li>法令または公序良俗に違反する行為</li>
          <li>他者の権利、利益、プライバシーを侵害する行為</li>
          <li>過度なアクセス、攻撃、解析、その他本サービスの運営を妨害する行為</li>
          <li>第三者になりすます行為</li>
          <li>その他、運営者が不適切と判断する行為</li>
        </ul>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第4条（データと共有URLの管理）</h2>
        <p class="text-sm leading-relaxed">
          ユーザーは、自己の責任で本サービスを利用し、入力した内容および共有URLを管理するものとします。運営者は、ユーザーが入力したリスト名、メンバー名、アイテム情報等の保存、完全性、正確性、復元可能性を保証しません。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第5条（サービスの変更・停止・終了）</h2>
        <p class="text-sm leading-relaxed">
          運営者は、事前の通知なく、本サービスの内容を変更し、または本サービスの全部もしくは一部の提供を停止・中断・終了できるものとします。
        </p>
        <p class="text-sm leading-relaxed">
          本サービスは、本サービスの提供の停止または中断により、ユーザーまたは第三者が被ったいかなる不利益または損害について、法令上認められる範囲で、理由を問わず一切の責任を負わないものとします。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第6条（保証の否認・免責）</h2>
        <p class="text-sm leading-relaxed">
          運営者は、本サービスについて、正確性、有用性、完全性、安全性、特定目的への適合性、継続的な提供、不具合が発生しないこと、データが失われないことを含め、明示または黙示を問わず一切保証しません。
        </p>
        <p class="text-sm leading-relaxed">
          本サービスの利用または利用不能、データの消失、誤表示、第三者によるアクセス、ユーザー間または第三者とのトラブル、その他本サービスに関連して生じたいかなる不利益または損害についても、運営者は法令上認められる範囲で、理由を問わず一切の責任を負わないものとします。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第7条（規約の変更）</h2>
        <p class="text-sm leading-relaxed">
          運営者は、必要に応じて本規約を変更できるものとします。変更後の規約は、本サービス上に掲載された時点で効力を生じるものとします。
        </p>
      </section>

      <section class="space-y-2">
        <h2 class="text-base font-semibold text-charcoal-800">第8条（準拠法）</h2>
        <p class="text-sm leading-relaxed">本規約の解釈には、日本法を準拠法とします。</p>
      </section>

      <footer class="border-t border-charcoal-200 pt-4">
        <p class="text-xs leading-relaxed text-charcoal-500">制定日：2026年5月8日</p>
      </footer>
    </article>
  </ContentArea>
</template>
