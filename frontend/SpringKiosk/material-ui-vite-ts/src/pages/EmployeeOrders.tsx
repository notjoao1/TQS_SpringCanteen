import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { IOrder, OrderStatus } from "../types/OrderTypes";
import { IMenu } from "../types/MenuTypes";
import { useEffect, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import config from "../config";

const EmployeeOrders = () => {
  const [orders, setOrders] = useState<IOrder[]>([]); 

  // Websocket connection
  useEffect(() => { 
    const client = new Client({

      brokerURL: config.ordersWebSocketUrl, 

      onConnect: () => {
        console.log("Connected to WebSocket");

        client.subscribe("/topic/orders", (message: IMessage) => {
          console.log("Received message:", message.body); // debug

          const newOrder: IOrder = JSON.parse(message.body);
          setOrders((prevOrders) => [...prevOrders, newOrder]);
        });
      },
      onDisconnect: () => {
        console.log("Disconnected from WebSocket");
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);


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
            {orders
              .filter((o) => o.order_status === OrderStatus.IDLE)
              .map((order: IOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} />
                </Box>
              ))}
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
            {orders
              .filter((o) => o.order_status === OrderStatus.PREPARING)
              .map((order: IOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} />
                </Box>
              ))}
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
            {orders
              .filter((o) => o.order_status === OrderStatus.READY)
              .map((order: IOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} />
                </Box>
              ))}
            {/* <Box pt={2}>
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
              </Box> */}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default EmployeeOrders;
