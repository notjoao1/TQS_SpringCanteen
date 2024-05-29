import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_orders_page/OrderCard";
import { CookOrder, OrderStatus, WebsocketConnectMessage } from "../types/OrderTypes";
import { useContext, useEffect, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import config from "../config";
import { AuthContext } from "../context/AuthContext";
import { refreshToken } from "../api/auth.service";
import { useNavigate } from "react-router-dom";


const EmployeeOrders = () => {
  const navigate = useNavigate();
  const { auth, setAuth, logout } = useContext(AuthContext);
  const [regularIdleOrders, setRegularIdleOrders] = useState<CookOrder[]>([]);
  const [priorityIdleOrders, setPriorityIdleOrders] = useState<CookOrder[]>([]);
  const [regularPreparingOrders, setRegularPreparingOrders] = useState<CookOrder[]>([]);
  const [priorityPreparingOrders, setPriorityPreparingOrders] = useState<CookOrder[]>([]);

  // Websocket connection
  useEffect(() => { 
    const client = new Client({

      brokerURL: config.ordersWebSocketUrl, 
      connectHeaders: {
        Authorization: `Bearer ${auth?.token}`,
      },
      reconnectDelay: 5000,
      connectionTimeout: 10000,

      onConnect: (frame) => {
        console.log("Connected to WebSocket: ", frame);

        // receive existing orders
        client.subscribe("/user/topic/orders", (message: IMessage) => {
          const existingOrders: WebsocketConnectMessage = JSON.parse(message.body);
          setRegularIdleOrders(existingOrders.regularIdleOrders);
          setPriorityIdleOrders(existingOrders.priorityIdleOrders);
          setRegularPreparingOrders(existingOrders.regularPreparingOrders);
          setPriorityPreparingOrders(existingOrders.priorityPreparingOrders);
        });

        // for receiving order status updates
        client.subscribe("/order_updates", (message: IMessage) => {
          console.log("received @ order_updates message ->", message)
        });

        // for receiving new orders -> this topic ALWAYS receives orders in the IDLE state
        client.subscribe("/topic/orders", (message: IMessage) => {
          const newOrder: CookOrder = JSON.parse(message.body);
          if (newOrder.priority) {
            setPriorityIdleOrders((oldState) => {
              return [...oldState, newOrder];  
            })
          } else {
            setRegularIdleOrders((oldState) => {
              return [...oldState, newOrder];
            })
          }
        })
      },
      onDisconnect: () => {
        console.log("Disconnected from WebSocket");
      },
      onStompError: () => {
        if (auth?.refreshToken) {
          refreshToken(auth?.refreshToken).then((refreshResponse) => {
            setAuth((auth) => {
              if (auth)
                return {
                  ...auth,
                  token: refreshResponse.accessToken,
                } 
            })
          }).catch(() => {
            // on error, simply log user out, since refresh token probably expired
            logout();
            navigate("/signin");
          })
        }
      }
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [auth]);


  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h2" pb={2}>
        Current Orders
      </Typography>
      <Grid container sx={{ width: "100%" }}>
        <Grid item xs={12} md={6}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Ready to cook
            </Typography>
            {priorityIdleOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.IDLE}/>
                </Box>
              ))
            }
            {regularIdleOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={false} orderStatus={OrderStatus.IDLE}/>
                </Box>
              ))
            }
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper
            elevation={10}
            sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2, overflowY: "auto"  }}
          >
            <Typography variant="h5" sx={{ fontStyle: "italic" }}>
              Cooking
            </Typography>
            {priorityPreparingOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={true} orderStatus={OrderStatus.PREPARING}/>
                </Box>
              ))
            }
            {regularPreparingOrders
              .map((order: CookOrder) => (
                <Box pt={2} key={order.id}>
                  <OrderCard order={order} isPriority={false} orderStatus={OrderStatus.PREPARING}/>
                </Box>
              ))
            }
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default EmployeeOrders;
