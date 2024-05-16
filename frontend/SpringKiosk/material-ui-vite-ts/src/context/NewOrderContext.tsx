import { createContext, useEffect, useState } from "react";
import { ICreateOrder } from "../types/OrderTypes";

interface NewOrderContextType {
  order: ICreateOrder;
  setOrder: React.Dispatch<React.SetStateAction<ICreateOrder>>;
}

const defaultContextState: NewOrderContextType = {
  order: {
    menus: []
  },
  setOrder: () => {},
};

export const NewOrderContext =
  createContext<NewOrderContextType>(defaultContextState);

export const NewOrderContextProvider: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {
  // try to get order from localStorage. otherwise, create a new order
  const [order, setOrder] = useState<ICreateOrder>(() => {
    const storedOrder = localStorage.getItem("order");
    return storedOrder ? JSON.parse(storedOrder) : { menus: [] };
  });

  useEffect(() => {
    localStorage.setItem("order", JSON.stringify(order));
  }, [order])


  return (
    <NewOrderContext.Provider
      value={{
        order,
        setOrder,
      }}
    >
      {children}
    </NewOrderContext.Provider>
  );
};
