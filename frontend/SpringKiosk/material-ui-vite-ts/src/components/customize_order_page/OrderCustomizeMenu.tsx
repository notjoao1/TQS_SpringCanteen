import { Avatar, Box, Button, CircularProgress, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { ICreateMenu } from "../../types/MenuTypes";
import { getTotalCalories } from "../../utils/menu_utils";
import { getPriceOfExtraIngredients, getTotalMenuPrice } from "../../utils/order_utils";
import { MenuContext } from "../../context/MenuContext";
import { useContext } from "react";

interface OrderCustomizeMenuProps {
  menu: ICreateMenu,
  index: number,
  handleRemoveMenu: (index: number) => void
}

const OrderCustomizeMenu = ({ menu, index, handleRemoveMenu }: OrderCustomizeMenuProps) => {
  const navigate = useNavigate();
  const navigateToCustomize = () => navigate(`/order/customize/menu/${index}`);
  const {isLoading, menusById} = useContext(MenuContext);

  if (isLoading) {
    return (
        <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
          <CircularProgress size={20}/>
        </Box>
    )
  }


  return (
    <Box display={"flex"} sx={{ minHeight: 150 }} py={1} alignItems={"center"}>
      <Box sx={{ width: "20%", height: "100%" }}>
        <Avatar style={{ maxWidth: "100%", maxHeight: "100%", minHeight: "100px", minWidth: "100px" }} src={menu.selectedMenu.imageLink} />
      </Box>
      <Box sx={{ width: "45%", height: "100%" }}>
        <Typography variant="h5">{menu.selectedMenu.name}</Typography>
        <Typography>{getTotalCalories(menu)} kcal</Typography>
      </Box>
      <Box component={Button} sx={{ width: "15%", height: "100%", fontStyle: "italic" }} onClick={navigateToCustomize} id={`customize-menu-${index + 1}`}>
            <Typography variant="subtitle2">customize</Typography>
        </Box>
      <Box
        sx={{ width: "20%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">
            {getTotalMenuPrice(menu, menusById.get(menu.selectedMenu.id)!).toFixed(2)}€
        </Typography>
        <Typography variant="overline">
            (+{getPriceOfExtraIngredients(menu.selectedMainDish, menusById.get(menu.selectedMenu.id)?.mainDishOptions.find(m => m.id === menu.selectedMainDish.id)!).toFixed(2)}€)
        </Typography>
        <Box id={`remove-menu-customize-${index + 1}`} component={Button} onClick={() => handleRemoveMenu(index)}>
          Remove
        </Box>
      </Box>
    </Box>
  );
};

export default OrderCustomizeMenu;