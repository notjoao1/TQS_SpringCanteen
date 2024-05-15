import { ICreateMenu } from "../types/MenuTypes";

export const getTotalCalories = (menu: ICreateMenu): number => {
  const mainDishCalories = menu.selectedMainDish.mainDishIngredients.reduce(
    (totalCalories, mainDishIngredient) => 
      totalCalories + mainDishIngredient.ingredient.calories, 0
  );

  return mainDishCalories;
};
