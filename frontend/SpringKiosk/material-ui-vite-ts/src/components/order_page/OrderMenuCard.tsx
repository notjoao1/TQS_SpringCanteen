import {
  Card,
  CardActions,
  CardContent,
  CardHeader,
  CardMedia,
  IconButton,
  Typography,
} from "@mui/material";
import { IMenu } from "../../types/MenuTypes";
import AddIcon from "@mui/icons-material/Add";
import InfoIcon from '@mui/icons-material/Info';

interface OrderMenuCardProps {
  menu: IMenu;
  index: number;
  onClickDetails: (menu: IMenu) => void;
  onClickAddToOrder: (menu: IMenu) => void;
}


const OrderMenuCard = ({
  menu,
  index,
  onClickDetails,
  onClickAddToOrder
}: OrderMenuCardProps) => {
  return (
    <Card
      key={index}
      variant="outlined"
      sx={{ maxWidth: 345 }}
    >
      <CardHeader title={menu.name}/>
      <CardMedia
        component="img"
        height="194"
        image={menu.imageLink}
        alt={menu.name}
      />
      <CardContent>
        <Typography variant="body2" color="text.secondary">
          It's delicious and healthy! Cooked to perfection by the greatest cooks in SpringCanteen kitchen, with decades of 
          experience making amazing food.
        </Typography>
      </CardContent>
      <CardActions disableSpacing>
        <IconButton aria-label="add to order" id={`add-menu-${index + 1}`} onClick={() => onClickAddToOrder(menu)}>
          <AddIcon />
        </IconButton>
        <IconButton
            aria-label="view menu details"
            onClick={() => onClickDetails(menu)}
        >
            <InfoIcon/>
        </IconButton>
      </CardActions>
    </Card>
  );
};

export default OrderMenuCard;
