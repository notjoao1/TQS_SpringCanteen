import React, { createContext, useContext, useRef, useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { AuthContext } from './AuthContext';
import { refreshToken } from '../api/auth.service';
import { useNavigate } from 'react-router-dom';
import config from '../config';

interface WebSocketContextType {
    websocketClient: Client | undefined;
}

const defaultContextState: WebSocketContextType = {
    websocketClient: undefined,
};
  

const WebSocketContext = createContext<WebSocketContextType>(defaultContextState);

export const WebSocketProvider: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {  

  const { auth, setAuth, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [websocketClient, setWebsocketClient] = useState<Client | undefined>(undefined);

  useEffect(() => {
    const client = new Client({
      brokerURL: config.ordersWebSocketUrl,
      connectHeaders: {
        Authorization: `Bearer ${auth?.token}`,
      },
      reconnectDelay: 5000,
      connectionTimeout: 10000,

      onConnect: (frame) => {
        console.log("Successfully connected to Websocket Server...");
        setWebsocketClient(client);
      },
      onDisconnect: () => {
        console.log("Disconnected from Websocket Server...");
        setWebsocketClient(undefined);
      },
      onStompError: () => {
        console.log("STOMP Error detected... refreshing token")
        if (auth?.refreshToken) {
          refreshToken(auth?.refreshToken)
            .then((refreshResponse) => {
              setAuth((auth) => {
                if (auth)
                  return {
                    ...auth,
                    token: refreshResponse.accessToken,
                  };
              });
            })
            .catch(() => {
              logout();
              navigate("/signin");
            });
        }
      },
    });

    client.activate();

    return () => {
      client.deactivate();
      setWebsocketClient(undefined);
    };
  }, [auth]);

  return (
    <WebSocketContext.Provider value={{
        websocketClient
    }}>
      {children}
    </WebSocketContext.Provider>
  );
};

// hook for easily using websockets
export const useWebSocket = () => {
  return useContext(WebSocketContext);
};
