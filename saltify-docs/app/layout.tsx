import { Footer, LastUpdated, Layout, Navbar } from 'nextra-theme-docs';
import { getPageMap } from 'nextra/page-map';
import 'nextra-theme-docs/style.css';
import './styles.css';
import { Head, Search } from 'nextra/components';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Saltify 文档',
  description: '跨平台、可扩展的 QQ Bot 框架 & Milky SDK',
};

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh" suppressHydrationWarning>
    <Head />
    <body>
    <Layout
      navbar={
        <Navbar
          logo={
            <div style={{ fontSize: '1.15rem' }}>
              <b>Saltify</b> 文档
            </div>
          }
          projectLink={'https://github.com/SaltifyDev/saltify'}
        ></Navbar>
      }
      pageMap={await getPageMap()}
      docsRepositoryBase="https://github.com/SaltifyDev/saltify/tree/main/saltify-docs"
      search={
        <Search
          placeholder="搜索内容..."
          emptyResult="没有找到相关内容"
          errorText="加载索引失败"
          loading="加载中..."
        />
      }
      editLink="在 GitHub 上编辑此页"
      feedback={{
        content: '有问题？提交反馈',
        labels: 'documentation',
      }}
      lastUpdated={<LastUpdated locale="zh">最后更新于</LastUpdated>}
      themeSwitch={{
        dark: '暗色',
        light: '亮色',
        system: '跟随系统',
      }}
      toc={{
        title: '目录',
        backToTop: '返回顶部',
      }}
    >
      {children}
      <Footer>
        © {new Date().getFullYear()} SaltifyDev. Licensed under MIT.
      </Footer>
    </Layout>
    </body>
    </html>
  );
}
