import { Box, Card, Typography } from "@mui/material";

const OrderPaymentCustomer = () => {
  return (
    <Box
      component={Card}
      variant="outlined"
      sx={{
        mt: 4,
        mb: 4,
      }}
    >
      <Box
        sx={{
          backgroundSize: "cover",
          backgroundPosition: "center",
          minHeight: 500,
        }}
      />
      <Box sx={{ px: 2, pb: 2 }}>
        <Typography color="text.primary" variant="body2" fontWeight="bold">
          Confirm payment
        </Typography>
      </Box>
    </Box>
  );
};

export default OrderPaymentCustomer;
