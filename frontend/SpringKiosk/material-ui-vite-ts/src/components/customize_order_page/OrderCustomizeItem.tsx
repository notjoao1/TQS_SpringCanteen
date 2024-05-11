import { Avatar, Box, Button, Link, Typography } from "@mui/material";

interface OrderCustomizeItemProps {
    name: string;
    kcal: number;
    price: number;
    image?: string;
}

const OrderCustomizeItem = ({ name, kcal, price, image }: OrderCustomizeItemProps) => {

  return (
    <Box display={"flex"} sx={{ minHeight: 150 }} py={1} alignItems={"center"}>
      <Box sx={{ width: "20%", height: "100%" }}>
        <Avatar style={{ maxWidth: "100%", maxHeight: "100%", minHeight: "100px", minWidth: "100px" }} src={ image } />
      </Box>
      <Box sx={{ width: "45%", height: "100%" }}>
        <Typography variant="h5">{ name }</Typography>
        <Typography>{ kcal } kcal</Typography>
      </Box>
      <Box component={Button} sx={{ width: "15%", height: "100%", fontStyle: "italic" }}>
        </Box>
      <Box
        sx={{ width: "20%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">
            { price }€
        </Typography>

        <Box component={Button}>Remove</Box>
      </Box>
    </Box>
  );
};

export default OrderCustomizeItem;
