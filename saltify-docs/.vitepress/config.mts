import { defineConfig } from 'vitepress';

// https://vitepress.dev/reference/site-config
export default defineConfig({
  srcDir: 'content',

  title: 'Saltify 文档',
  description: 'Documentation of Saltify',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '主页', link: '/' },
      { text: 'Milky', link: 'https://milky.ntqqrev.org/' },
    ],

    sidebar: [
      { text: '快速上手', link: '/quick-tour' },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/SaltifyDev/saltify' },
      { icon: 'qq', link: 'https://qm.qq.com/q/C04kPQzayk' },
      { icon: 'telegram', link: 'https://t.me/WeavingStar' },
    ],
  },
});
