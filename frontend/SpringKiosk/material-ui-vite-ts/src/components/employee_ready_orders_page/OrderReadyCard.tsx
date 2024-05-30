import { Button, Card, CardActions, CardContent, Typography } from "@mui/material";

const OrderReadyCard = () => {
    return (
        <Card sx={{ minWidth: 275 }}>
            <CardContent>
                <Typography sx={{ fontSize: 30 }} color="text.secondary" gutterBottom>
                    ORDER: 10
                </Typography>
            </CardContent>
            <CardActions>
                <Button size="small">Confirm Pick Up</Button>
            </CardActions>
        </Card>
    )
}

export default OrderReadyCard;