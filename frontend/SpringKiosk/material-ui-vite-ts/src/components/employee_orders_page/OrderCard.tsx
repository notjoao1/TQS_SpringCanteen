import { Box, Button, Paper, Typography, createTheme } from "@mui/material";
import { CookOrder, IOrder, OrderStatus } from "../../types/OrderTypes";
import { ThemeProvider } from "@emotion/react";
import { Check, LocalMall, OutdoorGrill } from "@mui/icons-material";

interface OrderCardProps {
  order: CookOrder;
  isPriority: boolean;
  orderStatus: OrderStatus;
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

const OrderCard = ({ order, isPriority, orderStatus }: OrderCardProps) => {
  return (
    <ThemeProvider theme={customTheme}>
      <Paper square={true} elevation={2} sx={{ bgcolor: "#ffd24b", pb: 2 }}>
        {isPriority && (
          <Box sx={{height: "40%", background: "linear-gradient(to bottom, #fba70a, #542922)"}} display={"flex"} justifyContent={"center"} alignItems={"center"}>
          <Typography color={"white"} variant="h5">Priority Order</Typography>
        </Box>
        )}
        <Box display={"flex"} flexDirection={"column"} sx={{ height: "60%" }} px={2} pt={1}>
          <Typography>Order #{order.id}</Typography>
          <Typography>
            Menus: {order.orderMenus.map((menu) => menu.menu.name).join(", ")}
          </Typography>
          <Typography>
            Status:{" "}
            <span style={{ fontWeight: "bold" }}>{orderStatus}</span>
          </Typography>
        </Box>
        <Box display={"flex"} justifyContent={"flex-end"} pt={1} px={2}>
          <Button variant="contained" endIcon={orderStatus === OrderStatus.IDLE ? <OutdoorGrill/> : (orderStatus === OrderStatus.PREPARING ? <LocalMall/> : <Check />)}>
            {orderStatus === OrderStatus.IDLE ? "Start cooking" : (orderStatus === OrderStatus.PREPARING ? "Ready to pick up" : "Mark as done")}
          </Button>{" "}
        </Box>
      </Paper>
    </ThemeProvider>
  );
};

export default OrderCard;
