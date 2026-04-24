<template>
  <span class="inline-block align-middle overflow-hidden" aria-hidden="true">
    <img :src="tiredImage" class="h-[3em] w-auto object-contain" />
  </span>
</template>

<script>
import tiredImage from '../../assets/Tired.png';
export default {
  name: 'IconTired',
  data() {
    return { tiredImage };
  }
};
</script>
