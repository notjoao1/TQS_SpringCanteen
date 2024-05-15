import {
    Box,
    Button,
    Container,
    Divider,
    Grid,
    Typography,
  } from "@mui/material";
  import OrderCustomizeItem from "../components/customize_order_page/OrderCustomizeItem";
  import { useContext, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { NewOrderContext } from "../context/NewOrderContext";
    
  const Customize = () => {
    const {order, setOrder} = useContext(NewOrderContext);
    const navigate = useNavigate();

    const [items, setItems] = useState([
      { name: 'Pancakes', kcal: 300, price: 5.90, quantity: 1, image: 'https://mojo.generalmills.com/api/public/content/Pw6SBIgi-Ee6pTZBpU1oBg_gmi_hi_res_jpeg.jpeg?v=448d88d0&t=466b54bb264e48b199fc8e83ef1136b4'},
      { name: 'Coffee', kcal: 50, price: 2.50, quantity: 1, image: 'https://vaya.in/recipes/wp-content/uploads/2018/05/Coffee.jpg'},
      { name: 'Banana', kcal: 100, price: 1.50, quantity: 1, image: 'https://www.frutifique.pt/cdn/shop/products/6_218287c7-6440-48ea-b55c-51d67c0e36a5_grande.png?v=1608929179'},
    ]);
    
    const urlParams = useParams();
    // url parameter used to get the menu to show on the page
    const menuId = urlParams.id;

    if (!menuId || isNaN(parseInt(menuId))) {
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

    const selectedMenu = order.menus.find((menu, index) => parseInt(menuId) == index);
    console.log(selectedMenu);
    const price = items.reduce((acc, item) => acc + item.price * item.quantity, 0);

    const [finalPrice, setFinalPrice] = useState(price);

    const handleValueChange = (value: number, name: string) => {
      console.log(value, name);
      const newItems = items.map(item => {
        if (item.name === name) {
          return { ...item, quantity: value };
        }
        return item;
      });
      setItems(newItems);

      const newPrice = newItems.reduce((acc, item) => acc + item.price * item.quantity, 0);
      setFinalPrice(newPrice);
    }

    return (
      <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
        <Typography component="h2" variant="h4" color="text.primary">
          Breakfast Menu
        </Typography>
        <Grid container spacing={6} py={4}>
          <Grid item xs={12} md={12}>

            {items.map((item, index) => (
              <div key={index}>
                <OrderCustomizeItem key={index} name={item.name} kcal={item.kcal} price={item.price} image={item.image} onChange={handleValueChange} />
                <Divider/>
              </div>
            ))  
            }

            <Box pt={4} display={"flex"} mx={2}>
              <Button variant="contained" onClick={() => navigate("/order/customize")}>Go back</Button>
              <Typography ml={"auto"} mr={0} variant="h5">
                Total: <span style={{ fontWeight: "bold" }}>{ finalPrice }€</span>
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Container>
    );
  };
  
export default Customize;