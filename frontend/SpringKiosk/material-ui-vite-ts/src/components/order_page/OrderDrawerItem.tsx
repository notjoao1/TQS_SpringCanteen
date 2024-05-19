import { Box, Button, Typography } from "@mui/material";
import { ICreateMenu, IMenu } from "../../types/MenuTypes";
import { useContext } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";
import { getTotalCalories } from "../../utils/menu_utils";
import { getTotalMenuPrice } from "../../utils/order_utils";
import { MenuContext } from "../../context/MenuContext";

interface OrderDrawerItemProps {
  menu: ICreateMenu;
  index: number;
}

const OrderDrawerItem = ({ menu, index }: OrderDrawerItemProps) => {
  const {order, setOrder} = useContext(NewOrderContext);
  const {isLoading, menusById} = useContext(MenuContext);

  if (isLoading)
    return (
      <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
          <CircularProgress size={20}/>
      </Box>
  )

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
      id={`order-drawer-item-${index + 1}`}
    >
      <Box sx={{ width: "20%", height: "100%" }}>
        <img
          style={{ maxWidth: "100%", maxHeight: "100%" }}
          src={menu.selectedMenu.imageLink}
        />
      </Box>
      <Box sx={{ width: "70%", height: "100%" }}>
        <Typography variant="h5">{menu.selectedMenu.name}</Typography>
        <Typography>{getTotalCalories(menu)} kcal</Typography>
      </Box>
      <Box
        sx={{ width: "10%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">{getTotalMenuPrice(menu, menusById.get(menu.selectedMenu.id)!).toFixed(2)}€</Typography>
        <Button onClick={removeMenuFromOrder} id={`remove-menu-${index + 1}`}>
          Remove from order
        </Button>
      </Box>
    </Box>
  );
};

export default OrderDrawerItem;
