import { Box, CircularProgress, Divider, Drawer, Typography } from "@mui/material";
import OrderDrawerItem from "./OrderDrawerItem";
import { useContext } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";
import { getTotalPrice } from "../../utils/order_utils";
import { MenuContext } from "../../context/MenuContext";

interface OrderDrawerProps {
    isOpen: boolean;
    onClose: () => void;
}

const OrderDrawer = ({ isOpen, onClose }: OrderDrawerProps) => {
    const {order} = useContext(NewOrderContext);
    const {isLoading, menusById} = useContext(MenuContext);

    if (isLoading)
        return (
            <Box display={"flex"} justifyContent={"center"} alignItems={"center"}>
                <CircularProgress size={20}/>
            </Box>
        )

    return (
        <Drawer
            anchor={"bottom"}
            open={isOpen}
            onClose={onClose}
        >
            <Box
                height={'400px'}
                p={4}    
            >
                <Box display={"flex"} textAlign={"center"} justifyItems={"center"} sx={{height: "15%", position: "sticky", top: "0"}}>
                    <Typography variant="h4">Current order</Typography>
                    <Typography variant="h6" mr={1} ml={"auto"}>Total: {getTotalPrice(order, menusById).toFixed(2)}€</Typography>
                    
                </Box>
                <Divider sx={{
                        background: "black",
                    }}/>
                <Box display={"flex"} flexDirection={"column"} sx={{minHeight: "85%"}} p={2} overflow={"auto"} gap={2}>
                    {order.menus.map((createdMenu, index) => (
                        <OrderDrawerItem menu={createdMenu} index={index} key={index}/>
                    ))}
                </Box>
            </Box>
        </Drawer>
    );
}

export default OrderDrawer;