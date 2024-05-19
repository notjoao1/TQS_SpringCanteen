import { Box, Button, Container, Divider, Paper, Typography } from "@mui/material";
import { useLocation, useNavigate } from "react-router-dom";
import { IOrderResponse } from "../types/OrderTypes";

const OrderFinish = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as { orderResponse: IOrderResponse } | undefined;
  const orderResponse: IOrderResponse | undefined = locationState?.orderResponse;
  
  
  if (!orderResponse)
    return (
      <Container id="features" sx={{ py: { xs: 20, sm: 16 } }}>
        <Box
          display={"flex"}
          justifyContent={"center"}
          flexDirection={"column"}
          alignItems={"center"}
        >
          <Typography>
            You have not yet placed an order
          </Typography>
          <Button variant="contained" onClick={() => navigate("/order")}>
            Create your order!
          </Button>
        </Box>
      </Container>
    )
  return (
    <Container id="features" sx={{ py: { xs: 20, sm: 16 } }}>
      <Box
        display={"flex"}
        justifyContent={"center"}
        flexDirection={"column"}
        alignItems={"center"}
      >
        <Typography sx={{ typography: { sm: "h4", xs: "h3", md: "h2" } }}>
          Your order has been placed!
        </Typography>
        <Box
          display={"flex"}
          minWidth={400}
          maxWidth={400}
          minHeight={400}
          pt={10}
        >
          <Paper sx={{ width: "100%" }} elevation={6}>
            {orderResponse?.priority && (
              <Box sx={{height: "10%", background: "linear-gradient(to bottom, #006400, #228B22)"}} display={"flex"} justifyContent={"center"} alignItems={"center"}>
                <Typography sx={{ color:"white", fontWeight: "bold" }}>
                  Priority Order
                </Typography>
              </Box>
            )}
            
            <Box
              sx={{ height: "30%" }}
              display={"flex"}
              justifyContent={"center"}
              alignItems={"center"}
            >
              <Typography variant="h3" sx={{ fontWeight: "bold" }}>
                ORDER: {orderResponse.id}
              </Typography>
            </Box>
            <Divider />
            <Box
              sx={{ height: "50%" }}
              px={2}
              display={"flex"}
              flexDirection={"column"}
              justifyContent={"center"}
              alignItems={"center"}
              gap={1}
            >
                <Typography variant="body1">
                  <span style={{fontWeight: "bold"}}>NIF: </span>{orderResponse.nif}
                </Typography>
                <Typography variant="body1">
                  <span style={{fontWeight: "bold"}}>Total cost: </span>{orderResponse.price.toFixed(2)}€
                </Typography>
                <Typography variant="body1">
                <span style={{fontWeight: "bold"}}>Menus: </span>{orderResponse.orderMenus.map(m => m.menu.name).join(", ")}
                </Typography>
            </Box>
          </Paper>
        </Box>
        <Box display={"flex"} flexDirection={"column"} alignItems={"center"}>
          {orderResponse.paid ? (
            <Typography sx={{ typography: { sm: "h6", xs: "h5" } }} pt={4}>Pay attention to the digital signage screen for your order number!</Typography>
          ) : (
            <>
              <Typography sx={{ typography: { sm: "h6", xs: "h5" } }} pt={4}>
                You have not yet paid for your order.
              </Typography>
              <Typography variant="body2">Proceed to the desk and pay so we can start cooking your delicious meal!</Typography>
            </>
          )}
        </Box>
      </Box>
    </Container>
  );
};

export default OrderFinish;
