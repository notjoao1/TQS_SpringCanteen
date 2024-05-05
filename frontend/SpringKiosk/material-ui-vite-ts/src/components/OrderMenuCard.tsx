import {
  Card,
  CardActions,
  CardContent,
  CardHeader,
  CardMedia,
  IconButton,
  Typography,
} from "@mui/material";
import { IMenu } from "../types/MenuTypes";
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
      <CardHeader title={menu.name} subheader={menu.price + "€"} />
      <CardMedia
        component="img"
        height="194"
        image={menu.image}
        alt={menu.name}
      />
      <CardContent>
        <Typography variant="body2" color="text.secondary">
          This impressive paella is a perfect party dish and a fun meal to cook
          together with your guests. Add 1 cup of frozen peas along with the
          mussels, if you like.
        </Typography>
      </CardContent>
      <CardActions disableSpacing>
        <IconButton aria-label="add to order" onClick={() => onClickAddToOrder(menu)}>
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
