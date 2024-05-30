import { Container, Grid, Typography } from "@mui/material";
import OrderReadyCard from "../components/employee_ready_orders_page/OrderReadyCard";

const EmployeeReadyToPickUp = () => {


  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h2" pb={6}>
        Orders that are <span style={{fontWeight: "bold"}}>ready to pick up</span>
      </Typography>
      <Grid container sx={{ width: "100%" }} spacing={2}>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
        <Grid item md={4}>
          <OrderReadyCard />
        </Grid>
      </Grid>
    </Container>
  );
};

export default EmployeeReadyToPickUp;
