<template>
  <span class="inline-block overflow-hidden w-[1.25em] h-[1.25em] align-middle" aria-hidden="true">
    <img :src="grillImage" class="w-full h-full object-contain scale-[2.5]" />
  </span>
</template>

<script>
import grillImage from '../../assets/Grill.png';
export default {
  name: 'IconFire',
  data() {
    return { grillImage };
  }
};
</script>
