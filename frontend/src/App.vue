<script>
import AppLayout from './components/AppLayout.vue';
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';

export default {
  components: {
    AppLayout,
    AppHeader,
    AppFooter
  }
};
</script>

<template>
  <AppLayout>
    <AppHeader />
    <router-view />
    <AppFooter />
  </AppLayout>
</template>
