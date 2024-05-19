import { createContext, useEffect, useState } from "react";
import { ICreateOrder, PaymentPlace } from "../types/OrderTypes";

interface NewOrderContextType {
  order: ICreateOrder;
  setOrder: React.Dispatch<React.SetStateAction<ICreateOrder>>;
  paymentPlace: PaymentPlace;
  setPaymentPlace: React.Dispatch<React.SetStateAction<PaymentPlace>>;
  isPriority: boolean;
  setIsPriority: React.Dispatch<React.SetStateAction<boolean>>;
  nif: string;
  setNif: React.Dispatch<React.SetStateAction<string>>;
}

const defaultContextState: NewOrderContextType = {
  order: {
    menus: []
  },
  setOrder: () => {},
  paymentPlace: PaymentPlace.KIOSK,
  setPaymentPlace: () => {},
  isPriority: false,
  setIsPriority: () => {},
  nif: "",
  setNif: () => {},
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

  const [isPriority, setIsPriority] = useState<boolean>(false);
  const [paymentPlace, setPaymentPlace] = useState<PaymentPlace>(PaymentPlace.KIOSK);
  const [nif, setNif] = useState<string>("");

  useEffect(() => {
    localStorage.setItem("order", JSON.stringify(order));
  }, [order])


  return (
    <NewOrderContext.Provider
      value={{
        order,
        setOrder,
        isPriority,
        setIsPriority,
        paymentPlace,
        setPaymentPlace,
        nif,
        setNif
      }}
    >
      {children}
    </NewOrderContext.Provider>
  );
};
