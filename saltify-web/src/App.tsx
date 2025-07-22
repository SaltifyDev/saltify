import { Avatar, Layout, Menu, theme, type MenuProps } from 'antd';
import logo from './logo.png';
import { Outlet, useNavigate } from 'react-router';
import { HomeOutlined } from '@ant-design/icons';
import type { ReactNode } from 'react';

interface MenuItemData {
  route: string;
  label: string;
  icon: ReactNode;
}

const menuItemData: MenuItemData[] = [
  {
    route: '/',
    label: '主页',
    icon: <HomeOutlined/>,
  },
];

function App() {
  const {
    token: { colorTextLightSolid, colorBgContainer },
  } = theme.useToken();
  const navigate = useNavigate();

  return <Layout style={{ minHeight: '100vh', margin: -8 }}>
    <Layout.Header style={{ display: 'flex', alignItems: 'center', paddingLeft: 16 }}>
      <Avatar src={logo} style={{ marginRight: 8 }} />
      <div style={{ color: colorTextLightSolid }}><h2>Saltify</h2></div>
    </Layout.Header>
    <Layout>
      <Layout.Sider width={'20%'} style={{ background: colorBgContainer }}>
        <Menu
          mode="inline"
          style={{ padding: 8, height: '100%' }}
          items={menuItemData.map<Exclude<MenuProps['items'], undefined>[number]>(item => ({
            key: item.route,
            label: item.label,
            onClick: () => navigate(item.route),
            icon: item.icon,
          }))}
        />
      </Layout.Sider>
      <Layout.Content style={{ paddingLeft: 16, paddingTop: 8 }}>
        <Outlet/>
      </Layout.Content>
    </Layout>
  </Layout>;
}

export default App;
