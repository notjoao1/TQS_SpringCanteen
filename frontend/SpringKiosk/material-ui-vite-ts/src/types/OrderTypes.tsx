import { ICreateMenu, IMenu } from "./MenuTypes";

export interface IOrder {
    id: number;
    kiosk_id: number;
    order_status: OrderStatus;
    isPaid: boolean;
    menus: IMenu[];
    nif: string;
}


export enum OrderStatus {
    IDLE = "Idle",
    PREPARING = "Preparing",
    READY = "Ready",
    PICKED_UP = "Picked up"
}


// for creating new orders
export interface ICreateOrder {
    menus: ICreateMenu[]
    nif?: string;
}

export enum PaymentPlace {
    KIOSK = "In Kiosk",
    DESK = "In Desk",
}


export interface IOrderResponse {
    id: number;
    paid: boolean;
    priority: boolean;
    nif: number;
    orderMenus: IOrderMenuResponse[];
    price: number;
}


interface IOrderMenuResponse {
    menu: {name: string, price: number};
    customization: string;
}

// only menu data, without customization, see use bellow
interface CookOrderMenuData {
    name: string;
    price: number;
}

// menu data + customization of that particular menu
interface CookOrderMenu {
    menu: CookOrderMenuData;
    customization: string;

}


export interface CookOrder {
    id: number;
    priority: boolean;
    orderMenus: CookOrderMenu[];
}

export interface WebsocketConnectMessage {
  regularIdleOrders: CookOrder[];
  priorityIdleOrders: CookOrder[];
  regularPreparingOrders: CookOrder[];
  priorityPreparingOrders: CookOrder[];
  regularReadyOrders: CookOrder[];
  priorityReadyOrders: CookOrder[];
}

export interface OrderUpdateMessage {
    orderId: number;
    newOrderStatus: OrderStatus;
}