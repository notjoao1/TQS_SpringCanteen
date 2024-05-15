import {
  Box,
  Button,
  Checkbox,
  Container,
  Divider,
  FormControlLabel,
  FormGroup,
  Grid,
  Tooltip,
  Typography,
} from "@mui/material";
import OrderPaymentCustomer from "../components/customize_order_page/OrderPaymentCustomer";
import OrderCustomizeMenu from "../components/customize_order_page/OrderCustomizeMenu";
import { useContext } from "react";
import { NewOrderContext } from "../context/NewOrderContext";
import { getTotalPrice } from "../utils/order_utils";

const OrderCustomize = () => {
  const {order, setOrder} = useContext(NewOrderContext);
  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography component="h2" variant="h4" color="text.primary">
        Customize your order
      </Typography>
      <Grid container spacing={6} py={4}>
        <Grid item xs={12} md={8}>
          {order.menus.map((menu, index) => (
            <div key={index}>
              <OrderCustomizeMenu menu={menu} index={index}/>
              <Divider />
            </div>
          ))}
          <Box pt={4} display={"flex"} mx={2}>
            <FormGroup>
              <Tooltip title="Donate to the SpringCanteen foundation!">
                <FormControlLabel
                  control={<Checkbox />}
                  label="Priority Queue (+0.30€)"
                />
              </Tooltip>
            </FormGroup>
            <Typography ml={"auto"} mr={0} pt={2} variant="h5">
              Total: <span style={{ fontWeight: "bold" }}>{getTotalPrice(order).toFixed(2)}€</span>
            </Typography>
          </Box>
        </Grid>
        <Grid item xs={12} md={4}>
          <OrderPaymentCustomer />
          <Box component={Button} variant="outlined">
            <Typography color="text.primary" variant="body2" fontWeight="bold">
              Confirm order
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
};

export default OrderCustomize;
