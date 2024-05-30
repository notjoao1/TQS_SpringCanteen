import {
  Button,
  Container,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { Payments } from "@mui/icons-material";
import { useState } from "react";
import { IOrder } from "../types/OrderTypes";
import RequestPaymentModal from "../components/employee_payments_page/RequestPaymentModal";


const EmployeePayments = () => {
  const [paymentModalOpen, setPaymentModalOpen] = useState<boolean>(false);
  const [selectedOrder, setSelectedOrder] = useState<IOrder | undefined>(
    undefined
  );

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h3">
        Orders requiring payment confirmation
      </Typography>
      <TableContainer component={Paper} sx={{ mt: 4 }} elevation={4}>
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>Order ID</TableCell>
              <TableCell align="center">NIF</TableCell>
              <TableCell align="center">Cost</TableCell>
              <TableCell align="right">Payment</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {/* {mockOrders
              .filter((o) => !o.isPaid)
              .map((order) => (
                <TableRow
                  key={order.id}
                  sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
                >
                  <TableCell component="th" scope="row">
                    {order.id}
                  </TableCell>
                  <TableCell align="center">{order.nif}</TableCell>
                  <TableCell align="center">10.20€</TableCell>
                  <TableCell align="right">
                    <Button
                      variant="contained"
                      startIcon={<Payments />}
                      onClick={() => {
                        setSelectedOrder(order);
                        setPaymentModalOpen(true);
                      }}
                    >
                      Request Payment
                    </Button>
                  </TableCell>
                </TableRow>
              ))} */}
          </TableBody>
        </Table>
      </TableContainer>
      <RequestPaymentModal
        order={selectedOrder}
        isOpen={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
      />
    </Container>
  );
};

export default EmployeePayments;
