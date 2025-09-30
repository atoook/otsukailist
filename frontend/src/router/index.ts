import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

// Lazy loading for better performance
const Welcome = () => import('../pages/Welcome.vue');
const CreateList = () => import('../pages/CreateList.vue');
const ItemList = () => import('../pages/ItemList.vue');

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Welcome',
    component: Welcome
  },
  {
    path: '/create-list',
    name: 'CreateList',
    component: CreateList
  },
  {
    path: '/list/:id',
    name: 'ItemList',
    component: ItemList
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
