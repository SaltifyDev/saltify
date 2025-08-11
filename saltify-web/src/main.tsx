import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router';
import { Provider } from './components/ui/provider.tsx';
import App from './App.tsx';
import Home from './pages/Home.tsx';
import Plugins from './pages/Plugins.tsx';
import Adapters from './pages/Adapters.tsx';
import Settings from './pages/Settings.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider>
      <BrowserRouter>
        <Routes>
          <Route element={<App />}>
            <Route index element={<Home />} />
            <Route path={'/plugins'} element={<Plugins />} />
            <Route path={'/adapters'} element={<Adapters />} />
            <Route path={'/settings'} element={<Settings />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
  </StrictMode>
);
