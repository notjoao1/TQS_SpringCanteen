import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { IOrder, OrderStatus } from "../types/OrderTypes";
import { menus } from "./Order";

export const mockOrders: IOrder[] = [
  {
      id: 1,
      kiosk_id: 101,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [menus[0]],
      nif: "123456789"
  },
  {
      id: 2,
      kiosk_id: 102,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [menus[0], menus[1]],
      nif: "987654321"
  },
  {
      id: 3,
      kiosk_id: 103,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [menus[0]],
      nif: "456789123"
  },
  {
      id: 4,
      kiosk_id: 104,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [menus[0], menus[1]],
      nif: "789123456"
  },
  {
      id: 5,
      kiosk_id: 105,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [menus[0], menus[1]],
      nif: "321654987"
  },
  {
      id: 6,
      kiosk_id: 106,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [menus[0]],
      nif: "654987321"
  },
  {
      id: 7,
      kiosk_id: 107,
      order_status: OrderStatus.READY,
      isPaid: true,
      menus: [menus[0], menus[1]],
      nif: "987321654"
  },
  {
      id: 8,
      kiosk_id: 108,
      order_status: OrderStatus.PICKED_UP,
      isPaid: false,
      menus: [menus[0]],
      nif: "159263478"
  },
  {
      id: 9,
      kiosk_id: 109,
      order_status: OrderStatus.IDLE,
      isPaid: true,
      menus: [menus[0]],
      nif: "852741963"
  },
  {
      id: 10,
      kiosk_id: 110,
      order_status: OrderStatus.PREPARING,
      isPaid: false,
      menus: [menus[0], menus[1]],
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
