<script>
import AppLayout from './components/AppLayout.vue';
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import MaintenancePage from './components/MaintenancePage.vue';
import { isMaintenanceMode } from '@/lib/maintenance';

export default {
  components: {
    AppLayout,
    AppHeader,
    AppFooter,
    MaintenancePage
  },
  data() {
    return {
      maintenance: isMaintenanceMode()
    };
  }
};
</script>

<template>
  <div>
    <MaintenancePage v-if="maintenance" />
    <template v-else>
      <AppLayout>
        <AppHeader />
        <router-view />
        <AppFooter />
      </AppLayout>
    </template>
  </div>
</template>
