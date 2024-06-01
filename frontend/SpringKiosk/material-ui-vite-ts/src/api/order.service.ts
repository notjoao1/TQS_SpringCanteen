import axios from "axios";
import { ICreateOrder, IOrderResponse } from "../types/OrderTypes";

interface OrderCreateObject {
    kioskId: number;
    isPaid: boolean;
    isPriority: boolean;
    nif: string;
    orderMenus: CustomizedMenu[]
}

interface CustomizedMenu {
    menuId: number;
    customization: CustomizedDrinkAndMainDish;
}

interface CustomizedDrinkAndMainDish {
    customizedDrink: CustomizedDrink;
    customizedMainDish: CustomizedMainDish;
}

interface CustomizedDrink {
    itemId: number;
}

interface CustomizedMainDish {
    itemId: number;
    customizedIngredients: CustomizedIngredient[];
}

interface CustomizedIngredient {
    ingredientId: number;
    quantity: number;
}

export const createOrder = async (order: ICreateOrder, isPaid: boolean, isPriority: boolean, nif: string): Promise<IOrderResponse> => {
    const postObject = createOrderCreateObject(order, isPaid, isPriority, nif);
    const VITE_HOST = import.meta.env.VITE_HOST as string ?? "localhost:8080";
    const res = await axios.post<IOrderResponse>(`http://${VITE_HOST}/api/orders`, postObject);

    return res.data;
}




const createOrderCreateObject = (order: ICreateOrder, isPaid: boolean, isPriority: boolean, nif: string): OrderCreateObject => {
    const postObject: OrderCreateObject = {
        kioskId: 1,
        isPaid,
        isPriority,
        nif,
        orderMenus: []
    }
    order.menus.forEach((orderMenu) => {
        postObject.orderMenus.push({
            menuId: orderMenu.selectedMenu.id,
            customization: {
                customizedDrink: {
                    itemId: orderMenu.selectedDrink.id,
                },
                customizedMainDish: {
                    itemId: orderMenu.selectedMainDish.id,
                    customizedIngredients: orderMenu.selectedMainDish.mainDishIngredients.map((mainDishIngredient) => {
                        return {
                            ingredientId: mainDishIngredient.ingredient.id,
                            quantity: mainDishIngredient.quantity,
                        }
                    })
                }
            }
        })
    })

    return postObject;
}

