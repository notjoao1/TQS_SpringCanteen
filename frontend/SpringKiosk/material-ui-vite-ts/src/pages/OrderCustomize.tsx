import {
  Box,
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

const OrderCustomize = () => {
  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography component="h2" variant="h4" color="text.primary">
        Customize your order
      </Typography>
      <Grid container spacing={6} py={4}>
        <Grid item xs={12} md={8}>
          <OrderCustomizeMenu />
          <Divider />
          <OrderCustomizeMenu />
          <Divider />
          <OrderCustomizeMenu />
          <Divider />
          <Box pt={4} display={"flex"} mx={2}>
            <FormGroup>
              <Tooltip title="Donate to the SpringCanteen foundation!">
                <FormControlLabel
                  control={<Checkbox defaultChecked />}
                  label="Priority Queue (+0.30€)"
                />
              </Tooltip>
            </FormGroup>
            <Typography ml={"auto"} mr={0} pt={2} variant="h5">
              Total: <span style={{fontWeight: "bold"}}>24.90€</span>
            </Typography>
          </Box>
        </Grid>
        <Grid item xs={12} md={4}>
          <OrderPaymentCustomer />
        </Grid>
      </Grid>
    </Container>
  );
};

export default OrderCustomize;
