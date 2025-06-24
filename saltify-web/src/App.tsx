import { Avatar, Layout, Menu, theme } from 'antd';
import logo from './logo.png';

function App() {
  const {
    token: { colorTextLightSolid, colorBgContainer },
  } = theme.useToken();

  return <Layout style={{ minHeight: '100vh' }}>
    <Layout.Header style={{ display: 'flex', alignItems: 'center', paddingLeft: 16 }}>
      <Avatar src={logo}/>
      <div style={{ color: colorTextLightSolid }}><h2>Saltify</h2></div>
    </Layout.Header>
    <Layout>
      <Layout.Sider width={200} style={{ background: colorBgContainer }}>
        <Menu
          mode="inline"
          style={{ padding: 8, height: '100%' }}
          items={[

          ]}
        />
      </Layout.Sider>
    </Layout>
  </Layout>;
}

export default App;
