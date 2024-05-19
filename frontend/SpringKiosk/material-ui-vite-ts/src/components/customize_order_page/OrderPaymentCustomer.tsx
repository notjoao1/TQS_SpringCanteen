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
import { useContext, useState } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";
import { PaymentPlace } from "../../types/OrderTypes";



const OrderPaymentCustomer = () => {
  const {paymentPlace, setPaymentPlace} = useContext(NewOrderContext);

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
          value={paymentPlace}
          name="radio-buttons-group"
          onChange={(e) => setPaymentPlace(e.target.value as PaymentPlace)}
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
        <TextField disabled={paymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Name on Card" variant="outlined" />
        <TextField disabled={paymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Card Number" variant="outlined" />
        <TextField disabled={paymentPlace === PaymentPlace.DESK} id="outlined-basic" label="Expiration Date" placeholder={"MM/YY"} variant="outlined" />
      </Box>
    </Box>
  );
};

export default OrderPaymentCustomer;
