<script>
import Layout from './components/Layout.vue';
import Header from './components/Header.vue';
import Footer from './components/Footer.vue';

export default {
  components: {
    Layout,
    Header,
    Footer
  }
};
</script>

<template>
  <Layout>
    <Header />
    <router-view />
    <Footer />
  </Layout>
</template>
