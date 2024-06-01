import { Button, Card, CardActions, CardContent, Typography } from "@mui/material";
import { CookOrder } from "../../types/OrderTypes";

interface OrderReadyCardProps {
    order: CookOrder;
    updateStatusMethod: (order: CookOrder) => void;
}

const OrderReadyCard = ({order, updateStatusMethod}: OrderReadyCardProps) => {
    return (
        <Card sx={{ minWidth: 275 }}>
            <CardContent>
                <Typography sx={{ fontSize: 30, fontWeight: 'bold' }} color="text.secondary" gutterBottom>        
                    ORDER: {order.id}
                </Typography>
                <Typography variant="subtitle2">
                    <span style={{fontWeight: "bold", fontStyle:"italic"}}>Menus: </span>{order.orderMenus.map(o => o.menu.name).join(" ,")}
                </Typography>
            </CardContent>
            <CardActions sx={{display:"flex"}} disableSpacing>
                <Button size="small" color="primary" onClick={() => updateStatusMethod(order)}>
                    Confirm Pick Up
                </Button>
                {order.priority && (
                    <Typography
                        sx={{
                            paddingRight: 1,
                            marginLeft: "auto",
                            marginRight: 0,
                            fontWeight: "bold",
                            background: 'linear-gradient(to top, darkgreen, green)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                        }}
                    >
                        PRIORITY
                    </Typography>
                )}
                
            </CardActions>
        </Card>
    )
}

export default OrderReadyCard;