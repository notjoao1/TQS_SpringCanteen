const VITE_HOST = import.meta.env.VITE_HOST as string ?? "localhost:8080";

const config = {
    
    ordersWebSocketUrl: `ws://${VITE_HOST}/websocket`,
};
  
export default config;
  
