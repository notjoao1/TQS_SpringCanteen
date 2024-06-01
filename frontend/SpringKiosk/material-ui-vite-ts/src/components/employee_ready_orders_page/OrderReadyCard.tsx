import { Button, Card, CardActions, CardContent, Paper, Typography } from "@mui/material";
import { CookOrder } from "../../types/OrderTypes";

interface OrderReadyCardProps {
    order: CookOrder;
}

const OrderReadyCard = ({order}: OrderReadyCardProps) => {
    return (
        <Card sx={{ minWidth: 275 }}>
            <CardContent>
                <Paper sx={{ padding: 1, backgroundColor: 'darkgreen', color: 'white', borderRadius: 1, textAlign: 'center', marginBottom: 2 }}>
                    PRIORITY
                </Paper>
                <Typography sx={{ fontSize: 30, fontWeight: 'bold' }} color="text.secondary" gutterBottom>        
                    ORDER: {order.id}
                </Typography>
                <Typography variant="subtitle2">
                    <span style={{fontWeight: "bold", fontStyle:"italic"}}>Menus: </span>{order.orderMenus.map(o => o.menu.name).join(" ,")}
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small" color="primary">
                    Confirm Pick Up
                </Button>
            </CardActions>
        </Card>
    )
}

export default OrderReadyCard;