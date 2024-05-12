import { Box, Button, Typography } from "@mui/material";
import { IMenu } from "../../types/MenuTypes";
import { useContext } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";
import { getTotalCalories } from "../../utils/menu_utils";

interface OrderDrawerItemProps {
  menu: IMenu;
  index: number;
}

const OrderDrawerItem = ({ menu, index }: OrderDrawerItemProps) => {
  const {order, setOrder} = useContext(NewOrderContext);

  const removeMenuFromOrder = () => {
    setOrder((prevOrder) => {
      return {
        ...prevOrder,
        menus: prevOrder.menus.filter((_, idx) => idx != index)
      }
    })
  }

  return (
    <Box
      key={index}
      p={2}
      display={"flex"}
      sx={{ height: "100px" }}
      border={1}
      borderRadius={4}
    >
      <Box sx={{ width: "20%", height: "100%" }}>
        <img
          style={{ maxWidth: "100%", maxHeight: "100%" }}
          src={menu.image}
        />
      </Box>
      <Box sx={{ width: "70%", height: "100%" }}>
        <Typography variant="h5">{menu.name}</Typography>
        <Typography>{getTotalCalories(menu)} kcal</Typography>
      </Box>
      <Box
        sx={{ width: "10%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">{menu.price}€</Typography>
        <Button onClick={removeMenuFromOrder}>Remove from order</Button>
      </Box>
    </Box>
  );
};

export default OrderDrawerItem;
