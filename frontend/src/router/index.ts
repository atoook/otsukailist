import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

// Lazy loading for better performance
const WelcomePage = () => import('../pages/WelcomePage.vue');
const CreateListPage = () => import('../pages/CreateListPage.vue');
const ShareListPage = () => import('../pages/ShareListPage.vue');
const ItemListPage = () => import('../pages/ItemListPage.vue');
const BBQGenerationPage = () => import('../pages/BBQGenerationPage.vue');
const ListEditPage = () => import('../pages/ListEditPage.vue');
const ItemEditPage = () => import('../pages/ItemEditPage.vue');
const TermsPage = () => import('../pages/TermsPage.vue');
const PrivacyPolicyPage = () => import('../pages/PrivacyPolicyPage.vue');

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
    path: '/lists/:id/templates/bbq',
    name: 'BBQGeneration',
    component: BBQGenerationPage
  },
  {
    path: '/lists/:id/edit',
    name: 'ListEdit',
    component: ListEditPage
  },
  {
    path: '/lists/:id/items/:itemId/edit',
    name: 'ItemEdit',
    component: ItemEditPage
  },
  {
    path: '/terms',
    name: 'Terms',
    component: TermsPage
  },
  {
    path: '/privacy',
    name: 'PrivacyPolicy',
    component: PrivacyPolicyPage
  }
];

export function createHistory() {
  const base = document.querySelector('base')?.getAttribute('href') ?? '/';
  return createWebHistory(base);
}

const router = createRouter({
  history: createHistory(),
  routes
});

export default router;
