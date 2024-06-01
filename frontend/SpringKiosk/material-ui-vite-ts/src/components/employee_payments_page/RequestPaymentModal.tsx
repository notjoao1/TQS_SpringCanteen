import {
  Box,
  Button,
  CircularProgress,
  Divider,
  Modal,
  Typography,
} from "@mui/material";
import { IOrderResponse } from "../../types/OrderTypes";
import { Check } from "@mui/icons-material";
import { useEffect, useState } from "react";

const style = {
  position: "absolute" as const,
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

interface RequestPaymentModal {
  order: IOrderResponse | undefined;
  isOpen: boolean;
  confirmPayment: (order: IOrderResponse) => void;
}

const RequestPaymentModal = ({isOpen, order, confirmPayment}: RequestPaymentModal) => {
  const [finishedPayment, setFinishedPayment] = useState<boolean>(false);

  useEffect(() => {
    setFinishedPayment(false);
  }, [order])

  useEffect(() => {
    if (!isOpen)
        setFinishedPayment(false);
  }, [isOpen])

  // simulate a payment that takes 3 seconds to be confirmed
  if (!finishedPayment && isOpen)
    setTimeout(() => {
        setFinishedPayment(true);
    }, 3000)

  if (!order) return;

  const handleClose = (event: {}, reason: "backdropClick" | "escapeKeyDown") => {
    if (reason) return; // never close on clicking escape or clicking the backdrop, its an ongoing payment
    confirmPayment(order);
  };

  return (
    <Modal
      open={isOpen}
      onClose={handleClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box sx={style}>
        <Typography id="modal-modal-title" variant="h5" component="h2" pb={1}>
          Request payment for order #{order?.id}
        </Typography>
        <Divider />
        {!finishedPayment ? (
            <Box
            display={"flex"}
            flexDirection={"column"}
            justifyContent={"center"}
            gap={2}
            alignItems={"center"}
          >
            <Typography id="modal-modal-description" sx={{ mt: 2 }}>
              Waiting for payment in Card Reader...
            </Typography>
            <CircularProgress />
          </Box>
        ) : (
          <Box
            display={"flex"}
            flexDirection={"column"}
            justifyContent={"center"}
            gap={2}
            alignItems={"center"}
          >
            <Typography id="modal-modal-description" sx={{ mt: 2 }}>
              Purchase received.
            </Typography>
          </Box>
        )}
        
        <Box display={"flex"} pt={4} justifyContent={"center"} gap={3}>
          {finishedPayment && (
            <Button variant="contained" endIcon={<Check />} onClick={() => confirmPayment(order)}>
                Confirm
            </Button>
          )}
        </Box>
      </Box>
    </Modal>
  );
};

export default RequestPaymentModal;
