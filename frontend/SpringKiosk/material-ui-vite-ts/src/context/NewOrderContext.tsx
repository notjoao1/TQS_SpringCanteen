import { createContext, useState } from "react";
import { ICreateOrder, IOrder } from "../types/OrderTypes";

interface NewOrderContextType {
  order: ICreateOrder;
  setOrder: React.Dispatch<React.SetStateAction<ICreateOrder>>;
  getOrderTotalCost: () => number;
}

const defaultContextState: NewOrderContextType = {
  order: {
    menus: []
  },
  setOrder: () => {},
  getOrderTotalCost: () => 1,
};

export const NewOrderContext =
  createContext<NewOrderContextType>(defaultContextState);

export const NewOrderContextProvider: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {
  const [order, setOrder] = useState<ICreateOrder>({
    menus: []
  });

  const getOrderTotalCost = (): number => {
    if (order.menus.length == 0) return 0;
    return order.menus.reduce((acc, currMenu) => acc + currMenu.price, 0)
  }

  return (
    <NewOrderContext.Provider
      value={{
        order,
        setOrder,
        getOrderTotalCost
      }}
    >
      {children}
    </NewOrderContext.Provider>
  );
};
