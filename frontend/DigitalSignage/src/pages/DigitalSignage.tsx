import './DigitalSignage.css';
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
    });
  }, [websocketClient]);

  const handleReceiveExistingOrders = (message: IMessage) => {
    const existingOrders: WebsocketConnectMessage = JSON.parse(message.body);
    console.log('Existing orders received:', existingOrders);
    setRegularPreparingOrders(existingOrders.regularPreparingOrders);
    setPriorityPreparingOrders(existingOrders.priorityPreparingOrders);
    
    const allOrders = [...existingOrders.regularPreparingOrders, ...existingOrders.priorityPreparingOrders];

    setPreparingOrders(formatOrderIds(orderList(allOrders.map((order) => order.id))));
  }

  const handleReceiveNewOrderOrOrderUpdate = (message: IMessage) => {
    const receivedMessage: Record<string, any> = JSON.parse(message.body);

    const orderUpdate = receivedMessage as OrderUpdateMessage;
    console.log('Order update received:', orderUpdate);

    // update the queues based on the new status
    if (orderUpdate.newOrderStatus === OrderStatus.PREPARING) {
      // add the new id to the preparing orders
      setPreparingOrders((prevState) => [...prevState, formatOrderId(orderUpdate.orderId)]);
    } else if (orderUpdate.newOrderStatus === OrderStatus.READY) {
      // remove the id from the preparing orders
      const idToRemove = formatOrderId(orderUpdate.orderId);
      setPreparingOrders((prevState) => prevState.filter((orderId) => orderId !== idToRemove));
    }


    const setOldQueue = getOldQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);
    const setNewQueue = getNewQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);

    let cookOrderToMove: CookOrder | undefined = undefined;
    if (setOldQueue) {
      setOldQueue((prevState) => {
        const updatedQueue = prevState.filter((order) => {
          if (order.id === orderUpdate.orderId) {
            cookOrderToMove = order;
            return false;
          }
          return true;
        });
        return updatedQueue;
      });
    }

    if (cookOrderToMove && setNewQueue) {
      setNewQueue((prevState) => [...prevState, cookOrderToMove!]);
    }

    if (cookOrderToMove) {
      enqueueSnackbar<"success">(
        `Successfully updated ${orderUpdate.priority ? "PRIORITY" : ""} order ${orderUpdate.orderId} to ${orderUpdate.newOrderStatus}`,
        { variant: "success", autoHideDuration: 5000 }
      );
    }
  }

  const getOldQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
    if (newStatus === OrderStatus.READY)
      return priority ? setPriorityPreparingOrders : setRegularPreparingOrders;
    return undefined;
  }

  const getNewQueueSetterBasedOnNewStatusAndPriority = (newStatus: OrderStatus, priority: boolean): React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined => {
    if (newStatus === OrderStatus.PREPARING)
      return priority ? setPriorityPreparingOrders : setRegularPreparingOrders;
    return undefined;
  }

  const orderList = (orders: CookOrder[]) => {
    return orders.sort((a, b) => a - b);
  }

  const formatOrderId = (orderId: number) => {
    return orderId.toString().padStart(3, '0');
  }

  const formatOrderIds = (orders: CookOrder[]) => {
    return orders.map((orderId) => formatOrderId(orderId));
  }


  return (
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
            {preparingOrders.map((orderId) => (
              <h2 key={orderId} className="order-child">{orderId}</h2>
            ))}
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
  )
}
