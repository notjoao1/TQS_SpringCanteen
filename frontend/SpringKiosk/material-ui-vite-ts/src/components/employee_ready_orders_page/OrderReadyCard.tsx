import { Button, Card, CardActions, CardContent, Typography } from "@mui/material";
import { CookOrder } from "../../types/OrderTypes";

interface OrderReadyCardProps {
    order: CookOrder;
}

const OrderReadyCard = ({order}: OrderReadyCardProps) => {
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
            <CardActions>
                <Button size="small" color="primary">
                    Confirm Pick Up
                </Button>
                {order.priority && (
                    <Typography ml={"auto"} mr={0} pr={1}
                        sx={{
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