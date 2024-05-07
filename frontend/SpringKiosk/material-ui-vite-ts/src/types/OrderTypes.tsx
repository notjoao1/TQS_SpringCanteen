import { IMenu } from "./MenuTypes";

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