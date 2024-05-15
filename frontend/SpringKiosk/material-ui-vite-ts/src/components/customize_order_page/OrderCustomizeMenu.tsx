import { Avatar, Box, Button, Link, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { IMenu } from "../../types/MenuTypes";
import { getTotalCalories } from "../../utils/menu_utils";

interface OrderCustomizeMenuProps {
  menu: IMenu
}

const OrderCustomizeMenu = () => {
  const navigate = useNavigate();
  const navigateToCustomize = () => navigate("/order/customize/menu/1");

  return (
    <Box display={"flex"} sx={{ minHeight: 150 }} py={1} alignItems={"center"}>
      <Box sx={{ width: "20%", height: "100%" }}>
        <Avatar style={{ maxWidth: "100%", maxHeight: "100%", minHeight: "100px", minWidth: "100px" }} src={menu.image} />
      </Box>
      <Box sx={{ width: "45%", height: "100%" }}>
        <Typography variant="h5">{menu.name}</Typography>
        <Typography>{getTotalCalories(menu)} kcal</Typography>
      </Box>
      <Box component={Button} sx={{ width: "15%", height: "100%", fontStyle: "italic" }} onClick={navigateToCustomize}>
            <Typography variant="subtitle2">customize</Typography>
        </Box>
      <Box
        sx={{ width: "20%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">
            {menu.price}€
        </Typography>
        <Typography variant="overline">
            (+0.40€) {/* extra cost from adding extra ingredients on the order */}
        </Typography>
        <Box component={Button}>Remove</Box>
      </Box>
    </Box>
  );
};

export default OrderCustomizeMenu;
