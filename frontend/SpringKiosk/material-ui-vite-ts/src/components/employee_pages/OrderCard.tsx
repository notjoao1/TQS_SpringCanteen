import { Paper, Typography } from "@mui/material";

const OrderCard = () => {
    return (
      <Paper square={true} elevation={2} sx={{ bgcolor: "#ffd24b", p: 2 }}>
        <Typography>Order #1</Typography>
        <Typography>Menu: Breakfast Menu</Typography>
        <Typography>
          Status: <span style={{fontWeight: "bold"}}>Cooking</span>
        </Typography>
      </Paper>
    );
}

export default OrderCard;