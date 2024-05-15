import { Box, Divider, List, ListItem, ListItemIcon, ListItemText, Modal, Typography } from "@mui/material";
import { IMenu } from "../../types/MenuTypes";

interface MenuDetailsModalProps {
  menu: IMenu | undefined;
  isOpen: boolean;
  onClose: () => void;
}

const style = {
  position: "absolute" as "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

const MenuDetailsModal = ({ isOpen, menu, onClose }: MenuDetailsModalProps) => {
  if (!menu) return;
  return (
    <Modal
      open={isOpen}
      onClose={onClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box sx={style}>
        <Typography id="modal-modal-title" variant="h5" component="h2">
          Menu - {menu.name}
        </Typography>
        <Typography id="modal-modal-description" sx={{ mt: 2 }}>
          <Box display={"flex"} flexDirection={"row"} gap={4}>
            <Box>
              <Typography variant="subtitle2">Main Dish Options:</Typography>
              <Divider />
              {menu.mainDishOptions.map((mainDish) => (
                <div key={mainDish.id}>
                  <Typography><span style={{fontWeight: "bold"}}>{mainDish.name}</span> - {mainDish.price.toFixed(2)}€</Typography>
                  <Typography pt={1}>Ingredients:</Typography>
                  <List dense={true}>
                  {mainDish.mainDishIngredients.map((mainDishIngredient) => (
                    <div style={{marginLeft: 5}} key={mainDishIngredient.id}>- {mainDishIngredient.quantity}x {mainDishIngredient.ingredient.name} - {mainDishIngredient.ingredient.calories}kcal</div>
                  ))}
                  </List>
                </div>
              ))}
            </Box>
            <Box>
              <Typography variant="subtitle2">Drink Options:</Typography>
              <Divider />
              {menu.drinkOptions.map((drink) => (
                <Typography key={drink.id}>{drink.name} - {drink.price.toFixed(2)}€</Typography>
              ))}
            </Box>
          </Box>
        </Typography>
      </Box>
    </Modal>
  );
};

export default MenuDetailsModal;
