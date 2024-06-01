import { Outlet } from 'react-router-dom';
import { MenuContextProvider } from '../context/MenuContext';

const MenuContextLayout = () => {
  return (
    <MenuContextProvider>
      <Outlet />
    </MenuContextProvider>
  );
};

export default MenuContextLayout;