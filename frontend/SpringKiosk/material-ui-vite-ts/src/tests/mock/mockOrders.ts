import { IOrder, OrderStatus } from "../../types/OrderTypes";
import mockMenus from "./mockMenus"

const mockOrders: IOrder[] = [
  {
      id: 1,
      kiosk_id: 101,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "123456789"
  },
  {
      id: 2,
      kiosk_id: 102,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "987654321"
  },
  {
      id: 3,
      kiosk_id: 103,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "456789123"
  },
  {
      id: 4,
      kiosk_id: 104,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "789123456"
  },
  {
      id: 5,
      kiosk_id: 105,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "321654987"
  },
  {
      id: 6,
      kiosk_id: 106,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0]],
      nif: "654987321"
  },
  {
      id: 7,
      kiosk_id: 107,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "987321654"
  },
  {
      id: 8,
      kiosk_id: 108,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [mockMenus[0]],
      nif: "159263478"
  },
  {
      id: 9,
      kiosk_id: 109,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "852741963"
  },
  {
      id: 10,
      kiosk_id: 110,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "369852147"
  }
];

export default mockOrders;
