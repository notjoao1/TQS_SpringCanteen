import './DigitalSignage.css';
import React, { useEffect, useState } from 'react';
import { useWebSocket } from "../context/WebsocketContext";
import { CookOrder, OrderStatus, OrderUpdateMessage, WebsocketConnectMessage } from "../types/OrderTypes";
import { IMessage } from "@stomp/stompjs";
import { enqueueSnackbar } from "notistack";

export default function DigitalSignage() {
  const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
  const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);
  const [regularReadyOrders, setRegularReadyOrders] = useState<CookOrder[]>([]);
  const [priorityReadyOrders, setPriorityReadyOrders] = useState<CookOrder[]>([]);
  
  const [preparingOrderIds, setPreparingOrderIds] = useState<string[]>([]);
  const [readyOrderIds, setReadyOrderIds] = useState<string[]>([]);

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
    setRegularReadyOrders(existingOrders.regularReadyOrders);
    setPriorityReadyOrders(existingOrders.priorityReadyOrders);
    
    const allPreparingOrders: CookOrder[] = [...existingOrders.regularPreparingOrders, ...existingOrders.priorityPreparingOrders];
    const allReadyOrders: CookOrder[] = [...existingOrders.regularReadyOrders, ...existingOrders.priorityReadyOrders];

    setPreparingOrderIds(formatOrderIds(orderList(allPreparingOrders.map((order) => order.id))));
    setReadyOrderIds(formatOrderIds(orderList(allReadyOrders.map((order) => order.id))));
  }

  const handleReceiveNewOrderOrOrderUpdate = (message: IMessage) => {
    const receivedMessage: Record<string, any> = JSON.parse(message.body);

    const orderUpdate = receivedMessage as OrderUpdateMessage;
    console.log('Order update received:', orderUpdate);

    // update the queues based on the new status
    if (orderUpdate.newOrderStatus === OrderStatus.PREPARING) {
      // add the new id to the preparing orders
      setPreparingOrderIds((prevState: string[]) => [...prevState, formatOrderId(orderUpdate.orderId)]);
    } else if (orderUpdate.newOrderStatus === OrderStatus.READY) {
      // remove the id from the preparing orders
      const idToRemove = formatOrderId(orderUpdate.orderId);
      setPreparingOrderIds((prevState: string[]) => prevState.filter((orderId) => orderId !== idToRemove));

      // add the id to the ready orders
      setReadyOrderIds((prevState: string[]) => [...prevState, formatOrderId(orderUpdate.orderId)]);
    } else if (orderUpdate.newOrderStatus === OrderStatus.PICKED_UP) {
      // remove the id from the ready orders
      const idToRemove = formatOrderId(orderUpdate.orderId);
      setReadyOrderIds((prevState: string[]) => prevState.filter((orderId) => orderId !== idToRemove));
    }


    const setOldQueue: React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined = getOldQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);
    const setNewQueue: React.Dispatch<React.SetStateAction<CookOrder[]>> | undefined = getNewQueueSetterBasedOnNewStatusAndPriority(orderUpdate.newOrderStatus, orderUpdate.priority);

    let cookOrderToMove: CookOrder | undefined = undefined;
    if (setOldQueue) {
      setOldQueue?.((prevState: CookOrder[]) => {
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
      setNewQueue((prevState: CookOrder[]) => [...prevState, cookOrderToMove!]);
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

  const orderList = (orderIds: number[]) => {
    return orderIds.sort((a, b) => a - b);
  }

  const formatOrderId = (orderId: number): string => {
    return orderId.toString().padStart(3, '0');
  }

  const formatOrderIds = (orders: number[]): string[] => {
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
            {preparingOrderIds.map((orderId, index) => (
              <h2 key={orderId} className="order-child" id={"preparing-" + (index + 1)}>{orderId}</h2>
            ))}
          </div>
        </div>
        <div className="grid-child">
          <div className='order-container cyan'>
            {readyOrderIds.map((orderId, index) => (
              <h2 key={orderId} className="order-child" id={"delivery-" + (index + 1)}>{orderId}</h2>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
