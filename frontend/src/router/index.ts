import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

// Lazy loading for better performance
const WelcomePage = () => import('../pages/WelcomePage.vue');
const CreateListPage = () => import('../pages/CreateListPage.vue');
const ItemListPage = () => import('../pages/ItemListPage.vue');

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Welcome',
    component: WelcomePage
  },
  {
    path: '/create-list',
    name: 'CreateList',
    component: CreateListPage
  },
  {
    path: '/list/:id',
    name: 'ItemList',
    component: ItemListPage
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
