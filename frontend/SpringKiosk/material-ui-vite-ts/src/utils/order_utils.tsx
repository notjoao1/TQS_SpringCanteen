import { ICreateMenu, IMainDish, IMainDishIngredient, IMenu } from "../types/MenuTypes";
import { ICreateOrder } from "../types/OrderTypes"

// calculates total price of an order (excluding +0.30€ from priority queue)
// based on selected main dish, selected drink, and extra ingredients added
export const getTotalPrice = (order: ICreateOrder, menusById: Map<number, IMenu>) => {
    return order.menus.reduce((acc, currMenu) => acc + getTotalMenuPrice(currMenu, menusById.get(currMenu.selectedMenu.id)!), 0)
}

export const getTotalMenuPrice = (menu: ICreateMenu, baseMenu: IMenu) => {
    return menu.selectedDrink.price + getMainDishPrice(menu.selectedMainDish, baseMenu.mainDishOptions.find(md => md.id == menu.selectedMainDish.id)!);
}

// iterate over each customized ingredient, and check if extra ingredients were added based on the base ingredients
// if so, add that to the base main dish price.
export const getMainDishPrice = (customizedMainDish: IMainDish, baseMainDish: IMainDish) => {
    return baseMainDish.price + getPriceOfExtraIngredients(customizedMainDish, baseMainDish);
}

export const getPriceOfExtraIngredients = (customizedMainDish: IMainDish, baseMainDish: IMainDish) => {
    let extraPrice = 0;
    customizedMainDish.mainDishIngredients.forEach((customizedIngredient) => {
        const baseIngredient = baseMainDish.mainDishIngredients.find(i => i.id === customizedIngredient.id)!;
        const extraAddedIngredients = numberOfAddedIngredients(baseIngredient, customizedIngredient);
        if (extraAddedIngredients > 0)
            extraPrice = extraPrice + extraAddedIngredients * baseIngredient.ingredient.price;
    })
    return extraPrice;
}

const numberOfAddedIngredients = (baseIngredient: IMainDishIngredient, customizedIngredient: IMainDishIngredient) => {
    return customizedIngredient.quantity - baseIngredient.quantity;
}