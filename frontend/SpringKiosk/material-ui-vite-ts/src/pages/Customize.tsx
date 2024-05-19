import {
    Box,
    Button,
    CircularProgress,
    Container,
    Divider,
    Grid,
    Typography,
  } from "@mui/material";
  import OrderCustomizeItem from "../components/customize_order_page/OrderCustomizeItem";
  import { useContext } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { NewOrderContext } from "../context/NewOrderContext";
import { getMainDishPrice } from "../utils/order_utils";
import { MenuContext } from "../context/MenuContext";
    
  const Customize = () => {
    const {order, setOrder} = useContext(NewOrderContext);
    const {isLoading, menusById} = useContext(MenuContext);

    const navigate = useNavigate();
    
    const urlParams = useParams();
    // url parameter used to get the menu to show on the page
    const menuIdString = urlParams.id;

    if (!menuIdString || isNaN(parseInt(menuIdString))) {
      return (
        <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
          <Box display={"flex"} justifyContent={"center"} alignItems={"center"} flexDirection={"column"} gap={2}>
            <Typography variant="h2">
              Invalid menu.
            </Typography>
            <Button variant="contained" onClick={() => navigate("/order/customize")}>Go back to your order</Button>
          </Box>
        </Container>
        
      )
    }

    if (isLoading) {
      return (
        <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
          <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
            <CircularProgress size={100}/>
          </Box>
        </Container>
      )
    }
    
    const menuId = parseInt(menuIdString);

    const menuToCustomize = order.menus.find((menu, index) => menuId === index);
    if (!menuToCustomize) {
      return (
        <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
          <Box display={"flex"} justifyContent={"center"} alignItems={"center"} flexDirection={"column"} gap={2}>
            <Typography variant="h2">
              Invalid menu.
            </Typography>
            <Button variant="contained" onClick={() => navigate("/order/customize")}>Go back to your order</Button>
          </Box>
        </Container>
        
      )
    }

    const price = getMainDishPrice(menuToCustomize.selectedMainDish, menusById.get(menuToCustomize.selectedMenu.id)!.mainDishOptions.find(m => m.id === menuToCustomize.selectedMainDish.id)!);

    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Typography component="h2" variant="h4" color="text.primary">
          {menuToCustomize.selectedMenu.name}
        </Typography>
        <Typography variant="subtitle1">
          Customize the ingredients included in this dish. Keep in mind, adding more ingredients increases the cost!
          But removing ingredients doesn't change the price. The base price is {" "}
          <span style={{fontWeight: "bold"}}>
            {menuToCustomize.selectedMainDish.price.toFixed(2)}€.
          </span>
        </Typography>
        <Grid container spacing={6} py={4}>
          <Grid item xs={12} md={12}>

            {menuToCustomize.selectedMainDish.mainDishIngredients.map((mainDishIngredient, index) => (
              <div key={index}>
                <OrderCustomizeItem key={index} menuIndex={menuId} ingredientIndex={index} mainDishIngredient={mainDishIngredient} />
                <Divider/>
              </div>
            ))  
            }

            <Box pt={4} display={"flex"} mx={2}>
              <Button variant="contained" onClick={() => navigate("/order/customize")}>Go back</Button>
              <Typography ml={"auto"} mr={0} variant="h5" id="total-price">
                Total: <span style={{ fontWeight: "bold" }}>{price.toFixed(2)}€</span>
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Container>
    );
  };
  
export default Customize;