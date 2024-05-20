import { IMenu } from "../../types/MenuTypes";

const mockMenus: IMenu[] = [
    {
      id: 1,
      name: "Breakfast Menu",
      price: 8.99,
      image: "https://cdn.pixabay.com/photo/2017/05/07/08/56/pancakes-2291908_960_720.jpg",
      items: [
        {
          id: 101,
          name: "Pancakes",
          price: 5.99,
          ingredients: [
            {
              id: 1001,
              name: "Flour",
              price: 0.5,
              calories: 150,
            },
            {
              id: 1002,
              name: "Eggs",
              price: 1.0,
              calories: 70,
            },
            {
              id: 1003,
              name: "Milk",
              price: 0.75,
              calories: 90,
            },
            {
              id: 1004,
              name: "Butter",
              price: 0.25,
              calories: 50,
            },
          ],
        },
        {
          id: 102,
          name: "Omelette",
          price: 7.49,
          ingredients: [
            {
              id: 1005,
              name: "Eggs",
              price: 1.0,
              calories: 70,
            },
            {
              id: 1006,
              name: "Cheese",
              price: 1.5,
              calories: 120,
            },
            {
              id: 1007,
              name: "Tomatoes",
              price: 0.75,
              calories: 20,
            },
            {
              id: 1008,
              name: "Onions",
              price: 0.5,
              calories: 30,
            },
          ],
        },
      ],
    },
    {
      id: 2,
      name: "Lunch Menu",
      price: 12.99,
      image: "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fsimply-delicious-food.com%2Fwp-content%2Fuploads%2F2015%2F07%2FBacon-and-cheese-burgers-3.jpg&f=1&nofb=1&ipt=c0d8abfa0e74aab9a38695ab7bc7dbc9d8a51d9f90353a28a37a8320986293bf&ipo=images",
      items: [
        {
          id: 103,
          name: "Grilled Chicken Sandwich",
          price: 9.99,
          ingredients: [
            {
              id: 1009,
              name: "Chicken Breast",
              price: 3.0,
              calories: 180,
            },
            {
              id: 1010,
              name: "Lettuce",
              price: 0.5,
              calories: 5,
            },
            {
              id: 1011,
              name: "Tomatoes",
              price: 0.75,
              calories: 20,
            },
            {
              id: 1012,
              name: "Bread",
              price: 1.0,
              calories: 100,
            },
          ],
        },
        {
          id: 104,
          name: "Caesar Salad",
          price: 6.99,
          ingredients: [
            {
              id: 1013,
              name: "Romaine Lettuce",
              price: 1.5,
              calories: 10,
            },
            {
              id: 1014,
              name: "Chicken",
              price: 3.0,
              calories: 180,
            },
            {
              id: 1015,
              name: "Croutons",
              price: 0.75,
              calories: 50,
            },
            {
              id: 1016,
              name: "Caesar Dressing",
              price: 0.5,
              calories: 120,
            },
          ],
        },
      ],
    },
  ];

export default mockMenus;