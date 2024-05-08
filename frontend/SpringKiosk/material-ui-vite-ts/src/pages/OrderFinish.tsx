import { Box, Container, Paper, Typography } from "@mui/material";

const OrderFinish = () => {
  const orderNumber = 30;

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
            <Box
              sx={{ height: "50%" }}
              display={"flex"}
              justifyContent={"center"}
              alignItems={"center"}
            >
              <Typography variant="h3" sx={{ fontWeight: "bold" }}>
                ORDER: {orderNumber}
              </Typography>
            </Box>
            <Box
              sx={{ height: "70%" }}
              px={2}
              display={"flex"}
              flexDirection={"column"}
              gap={1}
            >
                <Typography variant="body1">
                  Name: Pessoa Fixe
                </Typography>
                <Typography variant="body1">
                  Total cost: 20.30€
                </Typography>
            </Box>
          </Paper>
        </Box>
        <Typography sx={{ typography: { sm: "h6", xs: "h5" } }} pt={4}>
          Pay in desk/Pay attention to the digital signage screen for your order number!
        </Typography>
      </Box>
    </Container>
  );
};

export default OrderFinish;
