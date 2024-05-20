import {
  Box,
  Button,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Modal,
  Select,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { IDrink, IMainDish, IMenu } from "../../types/MenuTypes";

interface AddToOrderModalProps {
  menu: IMenu | undefined;
  isOpen: boolean;
  onClose: () => void;
  addToOrder: (menu: IMenu, drink: IDrink, mainDish: IMainDish) => void;
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

const AddToOrderModal = ({ isOpen, menu, onClose, addToOrder }: AddToOrderModalProps) => {
  if (!menu) return;
  // yes this is weird, but material-ui Select component works best with strings
  const [selectedMainDishId, setSelectedMainDishId] = useState<string>("");
  const [selectedDrinkId, setSelectedDrinkId] = useState<string>("");

  const handleClose = () => {
    setSelectedMainDishId("");
    setSelectedDrinkId("");
    onClose();
  };

  const handleConfirm = () => {
    if (selectedMainDishId === "") {
        console.log("remember to select main dish")
        return;
    }

    if (selectedDrinkId === "") {
        console.log("remember to select drink")
        return;
    }
    const selectedMainDish = menu.mainDishOptions.find((mainDish) => mainDish.id === parseInt(selectedMainDishId))!
    const selectedDrink = menu.drinkOptions.find((drink) => drink.id === parseInt(selectedDrinkId))!

    addToOrder(menu, selectedDrink, selectedMainDish);
    // clear selects
    setSelectedMainDishId("");
    setSelectedDrinkId("");
  }

  return (
    <Modal
      open={isOpen}
      onClose={() => handleClose()}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box sx={style}>
        <Typography id="modal-modal-title" variant="h5" component="h2">
          Menu - {menu.name}
        </Typography>
        <Divider />
        <Typography pt={2}>Select your main dish and drink!</Typography>
        <Typography id="modal-modal-description" sx={{ mt: 2 }}>
          <Box display={"flex"} flexDirection={"column"} gap={2.5}>
            <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">Main Dish</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="select-main-dish"
                    value={selectedMainDishId}
                    label="Main Dish"
                    onChange={(e) =>
                        setSelectedMainDishId(e.target.value)
                    }
                >
                    {menu.mainDishOptions.map((mainDish, index) => (
                    <MenuItem value={mainDish.id} id={`main-dish-${index + 1}`}>
                        {mainDish.name} - {mainDish.price.toFixed(2)}€
                    </MenuItem>
                    ))}
                </Select>
            </FormControl>
            <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">Drink</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="select-drink"
                    value={selectedDrinkId}
                    label="Drink"
                    onChange={(e) =>
                        setSelectedDrinkId(e.target.value)
                    }
                >
                    {menu.drinkOptions.map((drink, index) => (
                    <MenuItem value={drink.id} id={`drink-${index + 1}`}>
                        {drink.name} - {drink.price.toFixed(2)}€
                    </MenuItem>
                    ))}
                </Select>
            </FormControl>
          </Box>
          <Box pt={2} display={"flex"} justifyContent={"center"} alignItems={"center"} gap={2}>
            <Button color="error" onClick={() => handleClose()} variant="contained" id="cancel-selection">Cancel Selection</Button>
            <Button onClick={() => handleConfirm()} variant="outlined" id="confirm-selection">Confirm selection</Button>
          </Box>
        </Typography>
      </Box>
    </Modal>
  );
};

export default AddToOrderModal;
