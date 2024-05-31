import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { CookOrder, OrderStatus, OrderUpdateMessage, WebsocketConnectMessage } from "../types/OrderTypes";
import { useEffect, useState } from "react";
import { IMessage } from "@stomp/stompjs";
import { useWebSocket } from "../context/WebsocketContext";
import { enqueueSnackbar } from "notistack";


const EmployeeCook = () => {
  const [regularIdleOrders, setRegularIdleOrders] = useState<CookOrder[]>([]);
  const [priorityIdleOrders, setPriorityIdleOrders] = useState<CookOrder[]>([]);
  const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
  const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);

  const { websocketClient } = useWebSocket();

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
      const setOldQueue = getOldQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.isPriority);
      const setNewQueue = getNewQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.isPriority);
      let cookOrderToMove: CookOrder | undefined = undefined;
      
      // remove this order from old queue
      setOldQueue?.((prevState) => {
        const updatedQueue = prevState.filter((order) => {
          if (order.id === orderUpdate.orderId)
            cookOrderToMove = order;
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
      
      enqueueSnackbar<"success">(`Successfully updated ${orderUpdate.isPriority ? "PRIORITY" : ""} order ${orderUpdate.orderId} to ${orderUpdate.newOrderStatus}`, 
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
    return undefined;
  }

  // returns new queue, so that the order can be added to the queue
  // returns undefined if new queue is not relevant to this page (cooks don't need to know when orders are picked up, for example)
  const getNewQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
    if (newStatus === OrderStatus.PREPARING)
      return priority ? setPriorityPreparingOrders : setRegularPreparingOrders;
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
              .map((order: CookOrder, index: number) => (
                <Box pt={2} key={order.id} id={`priority-idle-order-${index + 1}`}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.IDLE} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
            {regularIdleOrders
              .map((order: CookOrder, index: number) => (
                <Box pt={2} key={order.id} id={`regular-idle-order-${index + 1}`}>
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
              .map((order: CookOrder, index: number) => (
                <Box pt={2} key={order.id} id={`priority-preparing-order-${index + 1}`}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.PREPARING} updateStatusMethod={sendOrderStatusUpdate}/>
                </Box>
              ))
            }
            {regularPreparingOrders
              .map((order: CookOrder, index: number) => (
                <Box pt={2} key={order.id} id={`regular-preparing-order-${index + 1}`}>
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
