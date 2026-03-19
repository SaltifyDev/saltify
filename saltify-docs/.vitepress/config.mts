import { defineConfig } from 'vitepress';

export default defineConfig({
  srcDir: 'content',

  title: 'Saltify 文档',
  description: 'Documentation of Saltify',
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: '/saltify-150.png' }],
  ],
  themeConfig: {
    logo: '/saltify-150.png',
    nav: [
      { text: '主页', link: '/' },
      { text: 'Milky', link: 'https://milky.ntqqrev.org/' },
    ],

    sidebar: [
      { text: '快速开始', link: '/quick-tour' },
      {
        text: 'saltify-core 文档',
        items: [
          { text: '应用配置', link: '/guide/application' },
          { text: '插件开发', link: '/guide/plugin' },
          { text: '指令开发', link: '/guide/command' },
          { text: '日志实现', link: '/guide/logging' }
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
