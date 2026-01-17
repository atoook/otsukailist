import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

// Lazy loading for better performance
const WelcomePage = () => import('../pages/WelcomePage.vue');
const CreateListPage = () => import('../pages/CreateListPage.vue');
const ShareListPage = () => import('../pages/ShareListPage.vue');
const ItemListPage = () => import('../pages/ItemListPage.vue');
const ListEditPage = () => import('../pages/ListEditPage.vue');

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
    path: '/share-list/:id',
    name: 'ShareList',
    component: ShareListPage
  },
  {
    path: '/lists/:id',
    name: 'ItemList',
    component: ItemListPage
  },
  {
    path: '/lists/:id/edit',
    name: 'ListEdit',
    component: ListEditPage
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
