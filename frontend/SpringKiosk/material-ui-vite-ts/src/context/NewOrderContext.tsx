import { createContext, useState } from "react";
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
  const [order, setOrder] = useState<ICreateOrder>({
    menus: []
  });


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
