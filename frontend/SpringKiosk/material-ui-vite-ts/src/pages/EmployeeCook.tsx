import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { CookOrder, OrderStatus, OrderUpdateMessage, WebsocketConnectMessage } from "../types/OrderTypes";
import { useEffect, useState } from "react";
import { IMessage } from "@stomp/stompjs";
import { useWebSocket } from "../context/WebsocketContext";


const EmployeeCook = () => {
  const [regularIdleOrders, setRegularIdleOrders] = useState<CookOrder[]>([]);
  const [priorityIdleOrders, setPriorityIdleOrders] = useState<CookOrder[]>([]);
  const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
  const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);

  const { websocketClient } = useWebSocket();

  useEffect(() => {
    websocketClient?.subscribe("/user/topic/orders", (message: IMessage) => {
      console.log("received a message USER -@ /user/topic/orders -> ", message)
      handleReceiveExistingOrders(message);
    });
    
    websocketClient?.subscribe("/topic/orders", (message: IMessage) => {
      console.log("received a message @ /topic/orders -> ", message)
      handleReceiveNewOrderOrOrderUpdate(message);
    })
  }, [websocketClient])

  

  const handleReceiveExistingOrders = (message: IMessage) => {
    const existingOrders: WebsocketConnectMessage = JSON.parse(message.body);
    setRegularIdleOrders(existingOrders.regularIdleOrders);
    setPriorityIdleOrders(existingOrders.priorityIdleOrders);
    setRegularPreparingOrders(existingOrders.regularPreparingOrders);
    setPriorityPreparingOrders(existingOrders.priorityPreparingOrders);
  }

  const handleReceiveNewOrderOrOrderUpdate = (message: IMessage) => {
    const receivedMessage: Record<string, any> = JSON.parse(message.body);
    if (receivedMessage.hasOwnProperty("orderMenus")) { // new order message (new orders are IDLE)
      const newOrder = receivedMessage as CookOrder;
      if (newOrder.priority) {
        setPriorityIdleOrders((oldState) => {
          return [...oldState, newOrder];  
        })
      } else {
        setRegularIdleOrders((oldState) => {
          return [...oldState, newOrder];
        })
      }
    } else if (receivedMessage.hasOwnProperty("newOrderStatus")) { // order update message
      // TODO: continue this
      const orderUpdate = receivedMessage as OrderUpdateMessage;
    }
  }

  const sendOrderStatusUpdate = (order: CookOrder) => {
    const updateBody = {
      orderId: order.id,
    }
    if (websocketClient)
      websocketClient.publish({
          destination: "/app/order_updates", 
          body: JSON.stringify(updateBody),
          headers: {
            "content-type": "application/json"
          }
      })
  }

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h2" pb={2}>
        Current Orders
      </Typography>
      <Grid container sx={{ width: "100%" }}>
        <Grid item xs={12} md={6}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Ready to cook
            </Typography>
            {priorityIdleOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.IDLE} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
            {regularIdleOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={false} orderStatus={OrderStatus.IDLE} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Cooking
            </Typography>
            {priorityPreparingOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.PREPARING} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
            {regularPreparingOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={false} orderStatus={OrderStatus.PREPARING} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default EmployeeCook;
