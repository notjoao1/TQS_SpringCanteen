import {
  Box,
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
import { useContext, useEffect, useState } from "react";
import { IOrderResponse } from "../types/OrderTypes";
import RequestPaymentModal from "../components/employee_payments_page/RequestPaymentModal";
import { AuthContext } from "../context/AuthContext";
import { confirmPaymentOrder, getNotPaidOrders } from "../api/order.service";
import { useNavigate } from "react-router-dom";
import { refreshToken } from "../api/auth.service";
import { enqueueSnackbar } from "notistack";


const EmployeePayments = () => {
  const navigate = useNavigate();
  const { auth, setAuth, logout } = useContext(AuthContext);
  const [notPaidOrders, setNotPaidOrders] = useState<IOrderResponse[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<IOrderResponse | undefined>(
    undefined
  );
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);

  useEffect(() => {
    if (!auth) {
      logout();
      navigate("/signin");
      return;
    }

    const fetchNotPaidOrders = () => {
      getNotPaidOrders(auth.token ?? "").then((responseOrders) => {
        setNotPaidOrders(responseOrders);
      }).catch(() => {
        // try to refresh token, and if it doesn't work, just redirect to sign in
        refreshTokenOrRedirectToLogin();
      });
    };

    fetchNotPaidOrders();

    const intervalId = setInterval(fetchNotPaidOrders, 5000);

    return () => clearInterval(intervalId);
  }, [auth]);

  const refreshTokenOrRedirectToLogin = () => {
    refreshToken(auth?.refreshToken ?? "")
    .then((refreshResponse) => {
      setAuth((auth) => {
        if (auth)
          return {
            ...auth,
            token: refreshResponse.accessToken,
          };
      });
    })
    .catch(() => {
      logout();
      navigate("/signin");
    });
  }

  const confirmPayment = (order: IOrderResponse) => {
    confirmPaymentOrder(auth?.token ?? "", order.id).then((res) => {
      // 204 -> no content -> all good
      if (res.status === 204) {
        setNotPaidOrders(notPaidOrders.filter(o => o.id !== order.id));
        enqueueSnackbar<"success">(`Order ${order.id} was successfully paid for!`, 
          {variant: "success", autoHideDuration: 5000}
        );
      } else {
        enqueueSnackbar<"error">(`Order ${order.id} could not be paid...`, 
          {variant: "error", autoHideDuration: 5000}
        );
        refreshTokenOrRedirectToLogin();
      }
    }).catch(_ => {
      enqueueSnackbar<"error">(`Order ${order.id} could not be paid...`, 
        {variant: "error", autoHideDuration: 5000}
      );
      refreshTokenOrRedirectToLogin();
    })
  }

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography variant="h3">
        Orders requiring payment confirmation
      </Typography>
      <TableContainer component={Paper} sx={{ mt: 4 }} elevation={4}>
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead sx={{background: "linear-gradient(.25turn, #097c09, 10%, #002e00)"}}>
            <TableRow>
              <TableCell sx={{color: "white"}}>Order ID</TableCell>
              <TableCell sx={{color: "white"}} align="center">NIF</TableCell>
              <TableCell sx={{color: "white"}} align="center">Cost</TableCell>
              <TableCell sx={{color: "white"}} align="right">Payment</TableCell>
            </TableRow>
          </TableHead>
          <TableBody sx={{backgroundColor: "#f0fef0"}}>
            {notPaidOrders
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
              ))}
          </TableBody>
        </Table>
      </TableContainer>
      {notPaidOrders.length === 0 && (
        <Box display={"flex"} pt={4} alignItems={"center"} justifyContent={"center"}>
          <Typography variant="h4">
            No orders to be paid!
          </Typography>
        </Box>
      )}
      <RequestPaymentModal
        order={selectedOrder}
        isOpen={paymentModalOpen}
        confirmPayment={(order: IOrderResponse) => {
          confirmPayment(order);
          setPaymentModalOpen(false);
        }}
      />
    </Container>
  );
};

export default EmployeePayments;
