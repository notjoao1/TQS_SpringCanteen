import { Box, Link, Typography } from "@mui/material";
import { IMenu } from "../types/MenuTypes";

interface OrderDrawerItemProps {
  menu: IMenu;
}

const OrderDrawerItem = ({ menu }: OrderDrawerItemProps) => {
  const getTotalCalories = (menu: IMenu): number => {
    return menu.items.reduce(
      (totalCalories, item) =>
        totalCalories +
        item.ingredients.reduce(
          (itemCalories, ingredient) => itemCalories + ingredient.calories,
          0
        ),
      0
    );
  };

  return (
    <Box
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
        <Link component={"button"}>Remove from order</Link>
      </Box>
    </Box>
  );
};

export default OrderDrawerItem;
