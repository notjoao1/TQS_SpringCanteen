import { Box, Divider, Drawer, Link, Typography } from "@mui/material";
import OrderDrawerItem from "./OrderDrawerItem";
import { mockMenus } from "../../pages/Order";
import { useContext } from "react";
import { NewOrderContext } from "../../context/NewOrderContext";

interface OrderDrawerProps {
    isOpen: boolean;
    onClose: () => void;
}

const OrderDrawer = ({ isOpen, onClose }: OrderDrawerProps) => {
    const {order, setOrder, getOrderTotalCost} = useContext(NewOrderContext);

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
                    <Typography variant="h6" mr={1} ml={"auto"}>Total: {Number(getOrderTotalCost()).toFixed(2)}€</Typography>
                    
                </Box>
                <Divider sx={{
                        background: "black",
                    }}/>
                <Box display={"flex"} flexDirection={"column"} sx={{minHeight: "85%"}} p={2} overflow={"auto"} gap={2}>
                    {order.menus.map((menuInOrder, index) => (
                        <OrderDrawerItem menu={menuInOrder} index={index} key={index}/>
                    ))}
                </Box>
            </Box>
        </Drawer>
    );
}

export default OrderDrawer;