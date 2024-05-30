import { createContext, useEffect, useState, useMemo } from "react";
import { IMenu } from "../types/MenuTypes";
import { fetchAllMenus } from "../api/menu.service";

interface MenuContextType {
    isLoading: boolean;
    menusById: Map<number, IMenu>;
}

const defaultContextState: MenuContextType = {
    isLoading: true,
    menusById: new Map(),
};

export const MenuContext = createContext<MenuContextType>(defaultContextState);

export const MenuContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [menusById, setMenusById] = useState<Map<number, IMenu>>(new Map());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchMenus = async () => {
      try {
        const fetchedMenus = await fetchAllMenus();
        const newMenusById = new Map<number, IMenu>();
        fetchedMenus.forEach(m => newMenusById.set(m.id, m));
        setMenusById(newMenusById);
        setIsLoading(false);
      } catch (error) {
        console.error('Error fetching menus:', error);
      }
    };

    fetchMenus();
  }, []);


  const memoizedValue = useMemo(() => ({ menusById, isLoading }), [menusById, isLoading]);

  return (
    <MenuContext.Provider value={memoizedValue}>
      {children}
    </MenuContext.Provider>
  );
};