export interface IMenu {
    id: number;
    name: string;
    price: number;
    image: string;
    items: IItem[];
}
  
export interface IItem {
    id: number;
    name: string;
    price: number;
    ingredients: IIngredient[];
}

export interface IIngredient {
    id: number;
    name: string;
    price: number;
    calories: number;
}
