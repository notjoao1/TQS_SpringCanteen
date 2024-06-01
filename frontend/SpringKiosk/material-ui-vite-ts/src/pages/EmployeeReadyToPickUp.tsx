import { Box, Container, Grid, Typography } from "@mui/material";
import OrderReadyCard from "../components/employee_ready_orders_page/OrderReadyCard";
import { useWebSocket } from "../context/WebsocketContext";
import { useEffect, useState } from "react";
import { IMessage } from "@stomp/stompjs";
import { CookOrder, OrderStatus, OrderUpdateMessage, WebsocketConnectMessage } from "../types/OrderTypes";
import { enqueueSnackbar } from "notistack";

const EmployeeReadyToPickUp = () => {
  const { websocketClient } = useWebSocket();
  const [regularReadyOrders, setRegularReadyOrders] = useState<CookOrder[]>([]);
  const [priorityReadyOrders, setPriorityReadyOrders] = useState<CookOrder[]>([]);
  const [regularIdleOrders, setRegularIdleOrders] = useState<CookOrder[]>([]);
  const [priorityIdleOrders, setPriorityIdleOrders] = useState<CookOrder[]>([]);
  const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
  const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);


  useEffect(() => {
    websocketClient?.subscribe("/user/topic/orders", (message: IMessage) => {
      handleReceiveExistingOrders(message);
    
    });
    
    websocketClient?.subscribe("/topic/orders", (message: IMessage) => {
      handleReceiveNewOrderOrOrderUpdate(message);
    })
  }, [websocketClient])

  const handleReceiveExistingOrders = (message: IMessage) => {
    const existingOrders: WebsocketConnectMessage = JSON.parse(message.body);
    setRegularReadyOrders(existingOrders.regularReadyOrders);
    setPriorityReadyOrders(existingOrders.priorityReadyOrders);
    setRegularIdleOrders(existingOrders.regularIdleOrders);
    setPriorityIdleOrders(existingOrders.priorityIdleOrders);
    setRegularPreparingOrders(existingOrders.regularPreparingOrders);
    setPriorityPreparingOrders(existingOrders.priorityPreparingOrders);
  }

  const handleReceiveNewOrderOrOrderUpdate = (message: IMessage) => {
    const receivedMessage: Record<string, any> = JSON.parse(message.body);
    if (receivedMessage.hasOwnProperty("orderMenus")) { // new order message (new orders are always in IDLE status)
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
      const orderUpdate = receivedMessage as OrderUpdateMessage;
      const setOldQueue = getOldQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);
      const setNewQueue = getNewQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);
      let cookOrderToMove: CookOrder | undefined = undefined;

      // remove this order from old queue
      setOldQueue?.((prevState) => {
        const updatedQueue = prevState.filter((order) => {
          if (order.id === orderUpdate.orderId) {
            cookOrderToMove = order;
            return false;
          }
          return true;
        });
        return updatedQueue;
      })

      // add this order to the new queue
      setNewQueue?.((prevState) => {
        return [
          ...prevState,
          cookOrderToMove!
        ]
      })
      
      enqueueSnackbar<"success">(`Successfully updated ${orderUpdate.priority ? "PRIORITY" : ""} order ${orderUpdate.orderId} to ${orderUpdate.newOrderStatus}`, 
        {variant: "success", autoHideDuration: 5000}
      );
    }
  }

  // returns old queue setter, so that the order can be removed from the queue
  // returns undefined if queue is not relevant to this page (cooks don't need to know when orders are picked up, for example)
  const getOldQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
    if (newStatus === OrderStatus.PREPARING) 
      return priority ? setPriorityIdleOrders : setRegularIdleOrders;
    if (newStatus === OrderStatus.READY)
      return priority ? setPriorityPreparingOrders : setRegularPreparingOrders;
    if (newStatus === OrderStatus.PICKED_UP)
      return priority ? setPriorityReadyOrders : setRegularReadyOrders;
    return undefined;
  }

  // returns new queue, so that the order can be added to the queue
  // returns undefined if new queue is not relevant to this page (cooks don't need to know when orders are picked up, for example)
  const getNewQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
    if (newStatus === OrderStatus.PREPARING)
      return priority ? setPriorityPreparingOrders : setRegularPreparingOrders;
    if (newStatus === OrderStatus.READY)
      return priority ? setPriorityReadyOrders : setRegularReadyOrders;
    return undefined;
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
      <Typography variant="h2" pb={6}>
        Orders that are <span style={{fontWeight: "bold"}}>ready to pick up</span>
      </Typography>
      {regularReadyOrders.length === 0 && priorityReadyOrders.length === 0 ? (
        <Box height={200} display={"flex"} alignItems={"center"} flexDirection={"column"}
        gap={2} justifyContent={"center"}>
          <Typography 
            variant="h3"
            sx={{
              fontWeight: "bold",
              background: 'linear-gradient(to right, darkgreen, lightgreen)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            No orders ready for pick up!
          </Typography>
          <Typography variant="subtitle1">Keep waiting!</Typography>
        </Box>
      ) : (
        <Grid container sx={{ width: "100%" }} spacing={2}>
          {priorityReadyOrders.map((o, index) => (
            <Grid item md={4} key={o.id}>
              <OrderReadyCard index={index} updateStatusMethod={sendOrderStatusUpdate} order={o}/>
            </Grid>
          ))}
          {regularReadyOrders.map((o, index) => (
            <Grid item md={4} key={o.id}>
              <OrderReadyCard index={index} updateStatusMethod={sendOrderStatusUpdate} order={o}/>
            </Grid>
          ))}
        </Grid>
      )}
      
    </Container>
  );
};

export default EmployeeReadyToPickUp;
