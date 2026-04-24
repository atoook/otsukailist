<template>
  <span class="inline-block align-middle overflow-hidden" aria-hidden="true">
    <img :src="bbqImage" class="h-[3em] w-auto object-contain -my-[20%]" />
  </span>
</template>

<script>
import bbqImage from '../../assets/BBQ_Grill.png';
export default {
  name: 'IconBbq',
  data() {
    return { bbqImage };
  }
};
</script>
