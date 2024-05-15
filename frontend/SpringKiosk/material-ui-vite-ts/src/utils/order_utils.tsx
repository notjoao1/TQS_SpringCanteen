import { ICreateMenu, IMainDish } from "../types/MenuTypes";
import { ICreateOrder } from "../types/OrderTypes"

// calculates total price of an order (excluding +0.30€ from priority queue)
// based on selected main dish, selected drink, and extra ingredients added
export const getTotalPrice = (order: ICreateOrder) => {
    return order.menus.reduce((acc, currMenu) => acc + getTotalMenuPrice(currMenu), 0)
}

export const getTotalMenuPrice = (menu: ICreateMenu) => {
    return menu.selectedDrink.price + getMainDishPrice(menu.selectedMainDish);
}

// gets total price of mainDish based on the ingredients, with the following requirements:
//   - if ingredients are removed, they don't affect the price
//   - the minimum price of a mainDish is always the base price of that mainDish. it can only increase
export const getMainDishPrice = (mainDish: IMainDish) => {
    const priceWithIngredients = mainDish.mainDishIngredients.reduce(
        (acc, currMainDishIngredient) => acc + currMainDishIngredient.quantity * currMainDishIngredient.ingredient.price, 0
    );

    return priceWithIngredients < mainDish.price ? mainDish.price : priceWithIngredients;
}