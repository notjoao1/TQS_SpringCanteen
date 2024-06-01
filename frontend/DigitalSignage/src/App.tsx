import './App.css'
import * as React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { CssBaseline, PaletteMode } from '@mui/material';
import getLPTheme from './getLPTheme';
import { AuthContextProvider } from './context/AuthContext';
import { SnackbarProvider } from 'notistack';
import TopBar from './components/TopBar';
import SignUp from './pages/SignUp';
import SignIn from './pages/SignIn';
import { WebSocketProvider } from './context/WebsocketContext';
import DigitalSignage from './pages/DigitalSignage';
import { EmployeeRole } from './types/EmployeeTypes';
import PrivateRoute from './components/PrivateRoute';

function App() {
  const [mode, setMode] = React.useState<PaletteMode>('light');
  const [showCustomTheme, setShowCustomTheme] = React.useState(true);
  const LPtheme = createTheme(getLPTheme(mode));
  const defaultTheme = createTheme({ palette: { mode } });

  return (
    <ThemeProvider theme={showCustomTheme ? LPtheme : defaultTheme}>
      <CssBaseline />
        <AuthContextProvider>
          <SnackbarProvider>
            <TopBar mode={mode} />
            <BrowserRouter>
              <Routes>
                  {/* auth pages */}
                  <Route path="/signage/signup" Component={SignUp} />
                  <Route path="/signage/signin" Component={SignIn} />

                   {/* digital signage -> require websockets */}
                   <Route element={
                      <WebSocketProvider>
                        <PrivateRoute />
                      </WebSocketProvider>
                    }>
                      <Route path="/signage/digital-signage" Component={DigitalSignage} />
                    </Route>
              </Routes>
            </BrowserRouter>
        </SnackbarProvider>
      </AuthContextProvider>
    </ThemeProvider>
  )
}

export default App
