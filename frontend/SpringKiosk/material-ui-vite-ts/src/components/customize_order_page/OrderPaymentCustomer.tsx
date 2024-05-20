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
import { useContext, useState, useEffect } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";
import { PaymentPlace } from "../../types/OrderTypes";

interface OrderPaymentCustomerProps {
  onFormSubmit: (accepted: boolean) => void;
}


const OrderPaymentCustomer = ({ onFormSubmit }: OrderPaymentCustomerProps) => {
  const { paymentPlace, setPaymentPlace, nif, setNif } = useContext(NewOrderContext);
  const [nameOnCard, setNameOnCard] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [errors, setErrors] = useState({
    nameOnCard: "",
    cardNumber: "",
    expirationDate: "",
  });

  useEffect(() => {
    // Perform form validation whenever any relevant state changes
    validateForm();
  }, [nameOnCard, cardNumber, expirationDate]);

  const validateNameOnCard = (value: string) => {
    if (!value) return "Name on card is required";
    if (!/^[\p{L}\p{M}\s]+$/u.test(value)) return "Name on card can only contain letters and spaces";
    return "";
  };

  const validateCardNumber = (value: string) => {
    if (!value) return "Card number is required";
    if (!/^\d{16}$/.test(value)) return "Card number must be 16 digits";
    return "";
  };

  const validateExpirationDate = (value: string) => {
    if (!value) return "Expiration date is required";
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(value)) return "Expiration date must be valid and in MM/YY format";
    return "";
  };

  const validateForm = () => {
    // Perform form validation
    const nameOnCardError = validateNameOnCard(nameOnCard);
    const cardNumberError = validateCardNumber(cardNumber);
    const expirationDateError = validateExpirationDate(expirationDate);

    // Set errors
    setErrors({
      nameOnCard: nameOnCardError,
      cardNumber: cardNumberError,
      expirationDate: expirationDateError,
    });

    // Check if form is valid
    const isFormValid = !nameOnCardError && !cardNumberError && !expirationDateError;

    // Submit form if valid
    if (isFormValid) {
      onFormSubmit(true);
    } else {
      onFormSubmit(false);
    }
  };

  const handleNameOnCardChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNameOnCard(value);
    // setErrors((prevErrors) => ({
    //   ...prevErrors,
    //   nameOnCard: validateNameOnCard(value),
    // }));
  };

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setCardNumber(value);
    // setErrors((prevErrors) => ({
    //   ...prevErrors,
    //   cardNumber: validateCardNumber(value),
    // }));
  };

  const handleExpirationDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setExpirationDate(value);
    // setErrors((prevErrors) => ({
    //   ...prevErrors,
    //   expirationDate: validateExpirationDate(value),
    // }));
  };

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
          id="payment-options"
          onChange={(e) => setPaymentPlace(e.target.value as PaymentPlace)}
        >
          <FormControlLabel value={PaymentPlace.KIOSK} control={<Radio />} label={PaymentPlace.KIOSK} id="kiosk" />
          <FormControlLabel value={PaymentPlace.DESK} control={<Radio />} label={PaymentPlace.DESK} id="desk" />
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
        <TextField
          id="nif-input"
          label="NIF"
          variant="outlined"
          value={nif}
          onChange={(e) => {
            if (e.target.value.length <= 9) setNif(e.target.value);
          }}
        />
        <Typography variant={"h6"}>Payment Details</Typography>
        <TextField
          disabled={paymentPlace === PaymentPlace.DESK}
          id="name-on-card-input"
          label="Name on Card"
          variant="outlined"
          value={nameOnCard}
          onChange={handleNameOnCardChange}
          error={!!errors.nameOnCard}
          helperText={errors.nameOnCard}
        />
        <TextField
          disabled={paymentPlace === PaymentPlace.DESK}
          id="card-number-input"
          label="Card Number"
          variant="outlined"
          value={cardNumber}
          onChange={handleCardNumberChange}
          error={!!errors.cardNumber}
          helperText={errors.cardNumber}
        />
        <TextField
          disabled={paymentPlace === PaymentPlace.DESK}
          id="expiration-date-input"
          label="Expiration Date"
          placeholder={"MM/YY"}
          variant="outlined"
          value={expirationDate}
          onChange={handleExpirationDateChange}
          error={!!errors.expirationDate}
          helperText={errors.expirationDate}
        />
      </Box>
    </Box>
  );
};

export default OrderPaymentCustomer;
