import { createContext, useEffect, useState } from "react";
import { IEmployee } from "../types/EmployeeTypes";

interface AuthContextType {
    auth: IEmployee | undefined;
    setAuth: React.Dispatch<React.SetStateAction<IEmployee | undefined>>;
    logout: () => void;
}

const defaultContextState: AuthContextType = {
    auth: undefined,
    setAuth: () => {},
    logout: () => {},
};

export const AuthContext = createContext<AuthContextType>(defaultContextState);

export const AuthContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [auth, setAuth] = useState<IEmployee | undefined>(() => {
    const storedAuth = localStorage.getItem("auth_springcanteen");
    console.log("storedAuth bomboclat", storedAuth)
    return storedAuth ? JSON.parse(storedAuth) : undefined;
  }); 

  const logout = () => {
    localStorage.removeItem("auth_springcanteen");
    setAuth(undefined);
  }

  // whenever auth changes, save in local storage (unsecure but for sake of simplicity lets do it like this)
  useEffect(() => {
    // dont set if undefined
    if (auth)
      localStorage.setItem("auth_springcanteen", JSON.stringify(auth))
  }, [auth])


  return (
    <AuthContext.Provider value={{auth, setAuth, logout}}>
      {children}
    </AuthContext.Provider>
  );
};