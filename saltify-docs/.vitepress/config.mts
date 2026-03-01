import { defineConfig } from 'vitepress';

export default defineConfig({
  srcDir: 'content',

  title: 'Saltify 文档',
  description: 'Documentation of Saltify',
  themeConfig: {
    nav: [
      { text: '主页', link: '/' },
      { text: 'Milky', link: 'https://milky.ntqqrev.org/' },
    ],

    sidebar: [
      { text: '快速上手', link: '/quick-tour' },
      {
        text: '开发指南',
        items: [
          { text: '核心配置', link: '/guide/application' },
          { text: '插件开发', link: '/guide/plugin' },
          { text: '指令系统', link: '/guide/command' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/SaltifyDev/saltify' },
      { icon: 'qq', link: 'https://qm.qq.com/q/C04kPQzayk' },
      { icon: 'telegram', link: 'https://t.me/WeavingStar' },
    ],
  },
});
