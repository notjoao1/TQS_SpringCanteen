import * as React from "react";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import Chip from "@mui/material/Chip";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { Check, Close, FormatListNumberedOutlined } from "@mui/icons-material";
import { IDrink, IMainDish, IMenu } from "../types/MenuTypes";
import OrderMenuCard from "../components/order_page/OrderMenuCard";
import { Alert, BottomNavigation, BottomNavigationAction, CircularProgress, Collapse, IconButton, Paper, Snackbar } from "@mui/material";
import MenuDetailsModal from "../components/order_page/MenuDetailsModal";
import OrderDrawer from "../components/order_page/OrderDrawer";
import { useNavigate } from "react-router-dom";
import { NewOrderContext } from "../context/NewOrderContext";
import AddToOrderModal from "../components/order_page/AddToOrderModal";
import { MenuContext } from "../context/MenuContext";


export default function Order() {
  const {isLoading, menusById} = React.useContext(MenuContext);

  const {order, setOrder} = React.useContext(NewOrderContext);

  const navigate = useNavigate();

  const [selectedMenu, setSelectedMenu] = React.useState<IMenu | undefined>(undefined);
  const [isMenuDetailsModalOpen, setIsMenuDetailsModalOpen] = React.useState<boolean>(false);

  const [isAddToOrderModalOpen, setIsAddToOrderModalOpen] = React.useState<boolean>(false);

  const [isDrawerOpen, setIsDrawerOpen] = React.useState<boolean>(false);
  const [isSnackbarOpen, setIsSnackbarOpen] = React.useState<boolean>(false);
  const [isAlertOpen, setIsAlertOpen] = React.useState<boolean>(false);

  if (isLoading)
    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
            <CircularProgress size={100}/>
        </Box>
      </Container>
  )

  const handleOpenModal = (menu: IMenu) => {
    setSelectedMenu(menu);
    setIsMenuDetailsModalOpen(true);
  };

  const handleOpenAddToOrderModal = (menu: IMenu) => {
    setSelectedMenu(menu);
    setIsAddToOrderModalOpen(true);
  }

  const handleAddToOrder = (menu: IMenu, selectedDrink: IDrink, selectedMainDish: IMainDish) => {
    // add menu to order
    setIsAddToOrderModalOpen(false);
    setOrder({
      menus: [...order.menus, {
        selectedMenu: structuredClone(menu),
        selectedDrink: structuredClone(selectedDrink),
        selectedMainDish: structuredClone(selectedMainDish)
      }]
    })
    setIsSnackbarOpen(true);
  }

  // checked before moving on to the customize page. verifies if the order has any menus
  const validateOrder = () => {
    if (order.menus.length === 0) {
      setIsAlertOpen(true);
      return;
    }
    navigate("/order/customize")
  }

  console.log("new state, order: ", order)
  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography component="h2" variant="h4" color="text.primary">
        Add menus to your order
      </Typography>

      <Grid container spacing={6} pt={4}>
        {Array.from(menusById.values()).map((menu, index) => (
          <Grid key={menu.id} item xs={12} md={4}>
            <OrderMenuCard index={index} menu={menu} onClickDetails={handleOpenModal} onClickAddToOrder={handleOpenAddToOrderModal}/>
          </Grid>
        ))}
        <Grid item xs={12} md={6}>
          {/* XS 'Available mockMenus' section */}
          <Grid
            container
            item
            gap={1}
            sx={{ display: { xs: "auto", sm: "none" } }}
          >
            {Array.from(menusById.values()).map((menu, index) => (
              <Chip
                key={menu.id}
                label={menu.name}
                onClick={() => console.log("hi")}
                sx={{
                  borderColor: (theme) => {
                    if (theme.palette.mode === "light") {
                      return selectedMenu === menu ? "primary.light" : "";
                    }
                    return selectedMenu === menu ? "primary.light" : "";
                  },
                  background: (theme) => {
                    if (theme.palette.mode === "light") {
                      return selectedMenu === menu ? "none" : "";
                    }
                    return selectedMenu === menu ? "none" : "";
                  },
                  backgroundColor:
                  selectedMenu === menu ? "primary.main" : "",
                  "& .MuiChip-label": {
                    color: selectedMenu === menu ? "#fff" : "",
                  },
                }}
              />
            ))}
          </Grid>
          {/* END XS 'Available mockMenus' section */}
          {/* XS 'Your Order' section */}
          <Box
            component={Card}
            variant="outlined"
            sx={{
              display: { xs: "auto", sm: "none" },
              mt: 4,
              mb: 4,
            }}
          >
            <Box
              sx={{
                backgroundSize: "cover",
                backgroundPosition: "center",
                minHeight: 280,
              }}
            />
            <Box sx={{ px: 2, pb: 2 }}>
              <Typography
                color="text.primary"
                variant="body2"
                fontWeight="bold"
              >
                {"TEST TODO"}
              </Typography>
            </Box>
          </Box>
          {/* END XS 'Your Order' section */}
          <Box sx={{ px: 1, pb: 2, display: { xs: "auto", sm: "none" } }}>
            <Typography color="text.primary" variant="h5" fontWeight="bold">
              Your order
            </Typography>
          </Box>
          <Box
            component={Card}
            variant="outlined"
            sx={{
              display: { xs: "auto", sm: "none" },
            }}
          >
            <Box
              sx={{
                backgroundSize: "cover",
                backgroundPosition: "center",
                minHeight: 280,
              }}
            />
          </Box>
          {/* END of XS Display mode */}
        </Grid>
      </Grid>
      <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0 }} elevation={5}>
        <BottomNavigation 
          showLabels
          sx={{ px: { xs: 2, md: 20, lg: 40} }}
        >
             <Box
              display={"flex"}
              width={"50%"}
              justifyContent={"flex-start"}
              alignItems={"center"}
            >
              You currently have {order.menus.length} item(s) in your order.
            </Box>
            <BottomNavigationAction id="view-order" label="View your order" icon={<FormatListNumberedOutlined />} onClick={() => setIsDrawerOpen(true)} />
            <BottomNavigationAction id="customize-and-pay" sx={{float: "right"}} label="Customize and pay" icon={<Check />} onClick={validateOrder} />
          </BottomNavigation>
        </Paper>
        <OrderDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)}/>
        <MenuDetailsModal isOpen={isMenuDetailsModalOpen} menu={selectedMenu} onClose={() => setIsMenuDetailsModalOpen(false)} />
        <AddToOrderModal isOpen={isAddToOrderModalOpen} menu={selectedMenu} onClose={() => setIsAddToOrderModalOpen(false)} addToOrder={handleAddToOrder}/>
        <Snackbar
          open={isSnackbarOpen}
          autoHideDuration={4000}
          onClose={() => setIsSnackbarOpen(false)}
          id="snackbar-add-menu-to-order"
          message="Successfully added menu to order."
        />
        {/* Error alert for when the order is empty and the user tries to move on */}
        <Collapse in={isAlertOpen} >
          <Alert sx={{position: "fixed", bottom: 20, right: 20}} variant="filled" severity="error" action={
            <IconButton
              aria-label="close"
              color="inherit"
              size="small"
              onClick={() => {
                setIsAlertOpen(false);
              }}
            >
              <Close fontSize="inherit" />
            </IconButton>
          }
          >
            Error: the order is empty, cannot proceed.
          </Alert>
        </Collapse>
        
    </Container>
  );
}
