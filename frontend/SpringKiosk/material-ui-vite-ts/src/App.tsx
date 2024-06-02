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
import EmployeeCook from './pages/EmployeeCook';
import EmployeePayments from './pages/EmployeePayments';
import Customize from './pages/Customize';
import { NewOrderContextProvider } from './context/NewOrderContext';
import EmployeeReadyToPickUp from './pages/EmployeeReadyToPickUp';
import { AuthContextProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import { EmployeeRole } from './types/EmployeeTypes';
import MenuContextLayout from './components/MenuContextLayout';
import { WebSocketProvider } from './context/WebsocketContext';
import { SnackbarProvider } from 'notistack';

export default function App() {
  const [mode] = React.useState<PaletteMode>('light');
  const [showCustomTheme] = React.useState(true);
  const LPtheme = createTheme(getLPTheme(mode));
  const defaultTheme = createTheme({ palette: { mode } });
  
  return (
    <ThemeProvider theme={showCustomTheme ? LPtheme : defaultTheme}>
      <CssBaseline />
      <AuthContextProvider>
        <SnackbarProvider>
          <TopBar mode={mode} />
          <BrowserRouter>
              <NewOrderContextProvider>
                  <Routes>
                    {/* order pages that require menu data */}
                    <Route element={<MenuContextLayout/>}>
                      <Route path="/" Component={Home} />
                      <Route path="/order" Component={Order} />
                      <Route path="/order/customize" Component={OrderCustomize} />
                      <Route path="/order/customize/menu/:id" Component={Customize} />
                      <Route path="/order/finish" Component={OrderFinish} />
                    </Route>
                    
                    {/* auth pages */}
                    <Route path="/signup" Component={SignUp} />
                    <Route path="/signin" Component={SignIn} />

                    {/* employee pages -> require websockets */}
                    <Route element={
                      <WebSocketProvider>
                        <PrivateRoute requiredRole={EmployeeRole.COOK}/>
                      </WebSocketProvider>
                    }>
                      <Route path="/employee/orders" Component={EmployeeCook} />
                    </Route>

                    <Route element={
                        <PrivateRoute requiredRole={EmployeeRole.DESK_PAYMENTS}/>
                    }>
                      <Route path="/employee/payments" Component={EmployeePayments}/>
                    </Route>

                    <Route element={
                      <WebSocketProvider>
                        <PrivateRoute requiredRole={EmployeeRole.DESK_ORDERS}/>
                      </WebSocketProvider>
                    }>
                      <Route path="/employee/ready_orders" Component={EmployeeReadyToPickUp}/>
                    </Route>
                  </Routes>
              </NewOrderContextProvider>
          </BrowserRouter>
        </SnackbarProvider>
      </AuthContextProvider>
    </ThemeProvider>
  );
}
