import { ICreateMenu } from "../types/MenuTypes";

// returns total calories of the menu
// based on the main dish. must consider added and removed ingredients
export const getTotalCalories = (menu: ICreateMenu): number => {
  const mainDishCalories = menu.selectedMainDish.mainDishIngredients.reduce(
    (totalCalories, mainDishIngredient) => 
      totalCalories + mainDishIngredient.ingredient.calories * mainDishIngredient.quantity, 0
  );

  return mainDishCalories;
};
