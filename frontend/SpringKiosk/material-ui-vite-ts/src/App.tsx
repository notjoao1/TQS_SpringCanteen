import * as React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import Home from './pages/Home';
import TopBar from './components/TopBar';
import getLPTheme from './getLPTheme';
import { CssBaseline, PaletteMode } from '@mui/material';
import Order from './pages/Order';
import OrderCustomize from './pages/OrderCustomize';
import OrderFinish from './pages/OrderFinish';
import SignUp from './pages/SignUp';
import SignIn from './pages/SignIn';

export default function App() {
  const [mode, setMode] = React.useState<PaletteMode>('light');
  const [showCustomTheme, setShowCustomTheme] = React.useState(true);
  const LPtheme = createTheme(getLPTheme(mode));
  const defaultTheme = createTheme({ palette: { mode } });
  
  return (
    <ThemeProvider theme={showCustomTheme ? LPtheme : defaultTheme}>
      <CssBaseline />
      <TopBar mode={mode} />
      <BrowserRouter>
        <Routes>
          <Route path="/" Component={Home} />
          <Route path="/order" Component={Order} />
          <Route path="/order/customize" Component={OrderCustomize} />
          <Route path="/order/finish" Component={OrderFinish} />
          <Route path="/signup" Component={SignUp} />
          <Route path="/signin" Component={SignIn} />
          <Route path="/employee/orders" Component={Home} />
          <Route path="/employee/kitchen" Component={Home} />
          <Route path="/menu" Component={Home} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
