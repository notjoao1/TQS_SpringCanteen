import { Box, Divider, Drawer, Link, Typography } from "@mui/material";
import OrderDrawerItem from "./OrderDrawerItem";
import { menus } from "../pages/Order";

interface OrderDrawerProps {
    isOpen: boolean;
    onClose: () => void;
}

const OrderDrawer = ({ isOpen, onClose }: OrderDrawerProps) => {
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
                    <Typography variant="h6" mr={1} ml={"auto"}>Total: 40.30€</Typography>
                    <Divider sx={{
                        background: "black",
                    }}/>
                </Box>
                <Box display={"flex"} flexDirection={"column"} sx={{minHeight: "85%"}} p={2} overflow={"auto"} gap={2}>
                    <OrderDrawerItem menu={menus[0]}/>
                    <OrderDrawerItem menu={menus[1]}/>
                </Box>
            </Box>
        </Drawer>
    );
}

export default OrderDrawer;