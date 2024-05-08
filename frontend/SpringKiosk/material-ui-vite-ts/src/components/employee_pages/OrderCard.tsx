import { Box, Button, Paper, Typography, createTheme } from "@mui/material";
import { IOrder, OrderStatus } from "../../types/OrderTypes";
import { ThemeProvider } from "@emotion/react";
import { Check, LocalMall, OutdoorGrill } from "@mui/icons-material";

interface OrderCardProps {
  order: IOrder;
}

const customTheme = createTheme({
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(90deg, #f6b803, #fbec5f)',
          color: 'black',
        },
      },
    },
  },
});

const OrderCard = ({ order }: OrderCardProps) => {
  return (
    <ThemeProvider theme={customTheme}>
      <Paper square={true} elevation={2} sx={{ bgcolor: "#ffd24b", p: 2 }}>
        <Box display={"flex"} flexDirection={"column"} sx={{ height: "80%" }}>
          <Typography>Order #{order.id}</Typography>
          <Typography>
            Menus: {order.menus.map((menu) => menu.name).join(", ")}
          </Typography>
          <Typography>
            Status:{" "}
            <span style={{ fontWeight: "bold" }}>{order.order_status}</span>
          </Typography>
        </Box>
        <Box display={"flex"} justifyContent={"flex-end"} pt={1}>
          <Button variant="contained" endIcon={order.order_status === OrderStatus.IDLE ? <OutdoorGrill/> : (order.order_status === OrderStatus.PREPARING ? <LocalMall/> : <Check />)}>
            {order.order_status === OrderStatus.IDLE ? "Start cooking" : (order.order_status === OrderStatus.PREPARING ? "Ready to pick up" : "Mark as done")}
          </Button>{" "}
        </Box>
      </Paper>
    </ThemeProvider>
  );
};

export default OrderCard;
