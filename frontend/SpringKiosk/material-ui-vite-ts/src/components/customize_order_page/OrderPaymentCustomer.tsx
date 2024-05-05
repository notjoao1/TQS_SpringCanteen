import {
  Box,
  FormControl,
  FormControlLabel,
  FormLabel,
  Paper,
  Radio,
  RadioGroup,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";

enum PaymentPlace {
  KIOSK = "In Kiosk",
  DESK = "In Desk",
}

const OrderPaymentCustomer = () => {
  const [selectedPaymentPlace, setSelectedPaymentPlace] = useState<PaymentPlace>(PaymentPlace.KIOSK);

  return (
    <Box
      component={Paper}
      variant="outlined"
      sx={{
        mt: 4,
        mb: 4,
        boxShadow: "1px 1px 10px grey",
      }}
      px={4}
      py={2}
    >
      <FormControl>
        <FormLabel id="demo-radio-buttons-group-label">Where will you pay?</FormLabel>
        <RadioGroup
          aria-labelledby="demo-radio-buttons-group-label"
          defaultValue={PaymentPlace.KIOSK}
          name="radio-buttons-group"
          onChange={(e) => setSelectedPaymentPlace(e.target.value as PaymentPlace)}
        >
          <FormControlLabel value={PaymentPlace.KIOSK} control={<Radio />} label={PaymentPlace.KIOSK} />
          <FormControlLabel value={PaymentPlace.DESK} control={<Radio />} label={PaymentPlace.DESK} />
        </RadioGroup>
      </FormControl>
      <Box
        pt={2}
        component={"form"}
        display={"flex"}
        flexDirection={"column"}
        gap={4}
        alignItems={"flex-start"}
        sx={{
          minHeight: 400,
        }}
      >
        <Typography variant={"h6"}>Payment Details</Typography>
        <TextField disabled={selectedPaymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Name on Card" variant="outlined" />
        <TextField disabled={selectedPaymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Card Number" variant="outlined" />
        <TextField disabled={selectedPaymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Expiration Date" placeholder={"MM/YY"} variant="outlined" />
      </Box>
    </Box>
  );
};

export default OrderPaymentCustomer;
