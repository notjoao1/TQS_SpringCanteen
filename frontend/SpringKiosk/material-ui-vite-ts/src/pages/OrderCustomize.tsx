import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Collapse,
  Container,
  Divider,
  Fade,
  FormControlLabel,
  FormGroup,
  Grid,
  Tooltip,
  Typography,
} from "@mui/material";
import OrderPaymentCustomer from "../components/customize_order_page/OrderPaymentCustomer";
import OrderCustomizeMenu from "../components/customize_order_page/OrderCustomizeMenu";
import { useContext, useState } from "react";
import { NewOrderContext } from "../context/NewOrderContext";
import { getTotalPrice } from "../utils/order_utils";
import { MenuContext } from "../context/MenuContext";
import { createOrder } from "../api/order.service";
import { PaymentPlace } from "../types/OrderTypes";
import { useNavigate } from "react-router-dom";

const OrderCustomize = () => {
  const {order, setOrder, isPriority, paymentPlace, setIsPriority, nif} = useContext(NewOrderContext);
  const {isLoading, menusById} = useContext(MenuContext);
  const [hasErrors, setHasErrors] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [isFormAccepted, setIsFormAccepted] = useState(false);
  const navigate = useNavigate();

  if (isLoading)
    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
          <CircularProgress size={100}/>
        </Box>
      </Container>
    )


  if (order.menus.length === 0) {
    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Box display={"flex"} justifyContent={"center"} alignItems={"center"} flexDirection={"column"} gap={4}>
          <Typography variant="h4">
            You have not yet added any meals to your order!
          </Typography>
          <Button onClick={() => navigate("/order")} variant="contained">
            Create your order!
          </Button>
        </Box>
      </Container>
    )
  }

  const handleRemoveMenu = (index: number) => {
    const newMenus = order.menus.filter((menu, i) => i !== index);
    setOrder({...order, menus: newMenus});
  };

  // confirm and place order
  const confirmOrder = async () => {
    // check if nif is exactly 9 numbers
    if (!/^\d{9}$/.test(nif)) {
      setErrorMessage("You must fill NIF with a 9 digit number! Example: 123456789")
      setHasErrors(true);
      setTimeout(() => setHasErrors(false), 5000);
      return;
    }

    if (!isFormAccepted) {
      setErrorMessage("Please fill in the payment form before confirming the order!");
      setHasErrors(true);
      setTimeout(() => setHasErrors(false), 5000);
      return;
    }

    const createOrderResponse = await createOrder(order, paymentPlace === PaymentPlace.KIOSK, isPriority, nif);

    console.log("response of placing order ->", createOrderResponse);
  }

  const handleFormSubmit = (accepted: boolean) => {
    setIsFormAccepted(accepted);
  };

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography component="h2" variant="h4" color="text.primary">
        Customize your order
      </Typography>
      <Grid container spacing={6} py={4}>
        <Grid item xs={12} md={8}>
          {order.menus.map((menu, index) => (
            <div key={index}>
              <OrderCustomizeMenu menu={menu} index={index} handleRemoveMenu={handleRemoveMenu} />
              <Divider />
            </div>
          ))}
          <Box pt={4} display={"flex"} mx={2}>
            <FormGroup>
              <Tooltip title="Donate to the SpringCanteen foundation!">
                <FormControlLabel
                  control={<Checkbox />}
                  label="Priority Queue (+0.30€)"
                  value={isPriority}
                  onChange={(e) =>  setIsPriority(e.target.checked as boolean)}
                />
              </Tooltip>
            </FormGroup>
            <Typography ml={"auto"} mr={0} pt={2} variant="h5">
              Total: <span style={{ fontWeight: "bold" }}>{(getTotalPrice(order, menusById) + (isPriority ? 0.3 : 0.0)).toFixed(2)}€</span>
            </Typography>
          </Box>
        </Grid>
        <Grid item xs={12} md={4}>
          <OrderPaymentCustomer onFormSubmit={handleFormSubmit} />
          <Box component={Button} variant="outlined" onClick={() => confirmOrder()} id="confirm-order-button">
            <Typography color="text.primary" variant="body2" fontWeight="bold">
              Confirm order
            </Typography>
          </Box>
        </Grid>
      </Grid>
      <Fade in={hasErrors} unmountOnExit>
        <Alert severity="error" variant="filled" sx={{position: "fixed", bottom: 10, right: 10, width: '30%'}} id="error-alert">
          <AlertTitle>Error while placing order:</AlertTitle>
          {errorMessage}
        </Alert>
      </Fade>
    </Container>
  );
};

export default OrderCustomize;
