export interface IMenu {
    id: number;
    name: string;
    price: number;
    imageLink: string;
    drinkOptions: IDrink[];
    mainDishOptions: IMainDish[];
}

export interface IDrink extends IItem {}

export interface IMainDish extends IItem {
    mainDishIngredients: IMainDishIngredient[];
}
  
export interface IItem {
    id: number;
    name: string;
    price: number;
}

export interface IMainDishIngredient {
    id: number;
    ingredient: IIngredient;
    quantity: number;
}

export interface IIngredient {
    id: number;
    name: string;
    price: number;
    calories: number;
}
