import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { IOrder, OrderStatus } from "../types/OrderTypes";
import { IMenu } from "../types/MenuTypes";

export const mockMenus: IMenu[] = [
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


export const mockOrders: IOrder[] = [
  {
      id: 1,
      kiosk_id: 101,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "123456789"
  },
  {
      id: 2,
      kiosk_id: 102,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "987654321"
  },
  {
      id: 3,
      kiosk_id: 103,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "456789123"
  },
  {
      id: 4,
      kiosk_id: 104,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "789123456"
  },
  {
      id: 5,
      kiosk_id: 105,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "321654987"
  },
  {
      id: 6,
      kiosk_id: 106,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0]],
      nif: "654987321"
  },
  {
      id: 7,
      kiosk_id: 107,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "987321654"
  },
  {
      id: 8,
      kiosk_id: 108,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [mockMenus[0]],
      nif: "159263478"
  },
  {
      id: 9,
      kiosk_id: 109,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [mockMenus[0]],
      nif: "852741963"
  },
  {
      id: 10,
      kiosk_id: 110,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [mockMenus[0], mockMenus[1]],
      nif: "369852147"
  }
];



const EmployeeOrders = () => {
  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h2" pb={2}>
        Current Orders
      </Typography>
      <Grid container sx={{ width: "100%" }}>
        <Grid item xs={12} md={4}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Ready to cook
            </Typography>
            {mockOrders.filter((o) => o.order_status === OrderStatus.IDLE).map((order: IOrder) => 
              <Box pt={2}>
                <OrderCard order={order} />
              </Box>
            )}
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Cooking
            </Typography>
            {mockOrders.filter((o) => o.order_status === OrderStatus.PREPARING).map((order: IOrder) => 
              <Box pt={2}>
                <OrderCard order={order} />
              </Box>
            )}
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto" }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Ready to deliver
            </Typography>
            {mockOrders.filter((o) => o.order_status === OrderStatus.READY).map((order: IOrder) => 
              <Box pt={2}>
                <OrderCard order={order} />
              </Box>
            )}
            <Box pt={2}>
                <OrderCard order={mockOrders[0]} />
              </Box>
              <Box pt={2}>
                <OrderCard order={mockOrders[0]} />
              </Box>
              <Box pt={2}>
                <OrderCard order={mockOrders[0]} />
              </Box>
              <Box pt={2}>
                <OrderCard order={mockOrders[0]} />
              </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default EmployeeOrders;
