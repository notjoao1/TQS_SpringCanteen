import './DigitalSignage.css'
import React, { useEffect, useState } from 'react';
import { useWebSocket } from "../context/WebsocketContext";
import { CookOrder, OrderStatus, OrderUpdateMessage, WebsocketConnectMessage } from "../types/OrderTypes";
import { IMessage } from "@stomp/stompjs";
import { enqueueSnackbar } from "notistack";

export default function DigitalSignage() {
    const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
    const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);
    const [preparingOrders, setPreparingOrders] = useState<CookOrder[]>([]);

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
      setRegularPreparingOrders(existingOrders.regularPreparingOrders);
      setPriorityPreparingOrders(existingOrders.priorityPreparingOrders);
      
      const allOrders = [...existingOrders.regularPreparingOrders, ...existingOrders.priorityPreparingOrders];

      // order and format the list of orders
      setPreparingOrders(formatOrderIds(orderList(allOrders)));
    }
  
    const handleReceiveNewOrderOrOrderUpdate = (message: IMessage) => {
      const receivedMessage: Record<string, any> = JSON.parse(message.body);

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
  
    // returns old queue setter, so that the order can be removed from the queue
    // returns undefined if queue is not relevant to this page (cooks don't need to know when orders are picked up, for example)
    const getOldQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
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

    // function to order the list of orders by order id, from lowest to highest
    const orderList = (orders: CookOrder[]) => {
      return orders.sort((a, b) => a.id - b.id);
    }

    // function to format the order id to be displayed on the screen
    // it will display 3 digits, with leading zeros if necessary
    const formatOrderId = (orderId: number) => {
      return orderId.toString().padStart(3, '0');
    }

    // function to format the order ids of the orders to be displayed on the screen
    const formatOrderIds = (orders: CookOrder[]) => {
      return orders.map((order) => formatOrderId(order.id));
    }

    return (
        <>
          <div className='container'>
    
            <div className='grid-container'>
              <div className="grid-child">
                <h1>Preparing...</h1>
              </div>
              <div className="grid-child cyan">
                <h1>Delivery</h1>
              </div>
            </div>
    
            <div className='grid-container orders'>
              <div className="grid-child">
                <div className='order-container'>
                  {/* Preparing Orders */}
                  {preparingOrders.map((orderId) => {
                    return <h2 key={orderId} className="order-child">{orderId}</h2>
                  })}
                </div>
              </div>
              <div className="grid-child">
                <div className='order-container cyan'>
                  <h2 className="order-child">080</h2>
                  <h2 className="order-child">084</h2>
                  <h2 className="order-child">085</h2>
                </div>
              </div>
            </div>
    
          </div>
        </>
      )
}