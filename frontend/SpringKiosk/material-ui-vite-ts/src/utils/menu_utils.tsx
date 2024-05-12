import { IMenu } from "../types/MenuTypes";

export const getTotalCalories = (menu: IMenu): number => {
  return menu.items.reduce(
    (totalCalories, item) =>
      totalCalories +
      item.ingredients.reduce(
        (itemCalories, ingredient) => itemCalories + ingredient.calories,
        0
      ),
    0
  );
};
