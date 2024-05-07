import { Box, Container, Grid, Paper, Typography } from "@mui/material";
import OrderCard from "../components/employee_pages/OrderCard";

const EmployeeOrders = () => {
    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Typography variant="h2" pb={2}>Current Orders</Typography>
        <Grid container sx={{ width: "100%" }}>
          <Grid item xs={12} md={4}>
            <Paper
              elevation={10}
              sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2 }}
            >
              <Typography variant="h5" sx={{ fontStyle: "italic" }}>
                Ready to cook
              </Typography>
              <Box pt={2}>
                <OrderCard />
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper
              elevation={10}
              sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2 }}
            >
              <Typography variant="h5" sx={{ fontStyle: "italic" }}>
                Cooking
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper
              elevation={10}
              sx={{ maxHeight: "600px", minHeight: "600px", m: 2, p: 2 }}
            >
              <Typography variant="h5" sx={{ fontStyle: "italic" }}>
                Ready to deliver
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    );
}

export default EmployeeOrders;