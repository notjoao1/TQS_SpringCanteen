import { ICreateOrder } from "../types/OrderTypes"

// calculates total price of an order (excluding +0.30€ from priority queue)
// based on selected main dish, selected drink, and extra ingredients added
//      - if ingredients are removed, they don't affect the price
//      - the minimum price of a mainDish is always the base price of that mainDish. it can only increase
export const getTotalPrice = (order: ICreateOrder) => {
    let totalPrice = 0;

    order.menus.forEach((menu) => {
        // add price of the drink
        totalPrice += menu.selectedDrink.price;
        
        const mainDishPrice = menu.selectedMainDish.mainDishIngredients.reduce(
            (acc, currMainDishIngredient) => acc + currMainDishIngredient.quantity * currMainDishIngredient.ingredient.price, 0
        )

        // add price of the main dish -> never below base price, but can be higher if extra ingredients are added
        totalPrice += mainDishPrice < menu.selectedMainDish.price ? menu.selectedMainDish.price : mainDishPrice;
    })

    return totalPrice;
}