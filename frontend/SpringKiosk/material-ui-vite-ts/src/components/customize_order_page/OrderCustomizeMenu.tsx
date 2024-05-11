import { Avatar, Box, Button, Link, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

const OrderCustomizeMenu = () => {
  const navigate = useNavigate();
  const navigateToCustomize = () => navigate("/order/customize/menu/1");

  return (
    <Box display={"flex"} sx={{ minHeight: 150 }} py={1} alignItems={"center"}>
      <Box sx={{ width: "20%", height: "100%" }}>
        <Avatar style={{ maxWidth: "100%", maxHeight: "100%", minHeight: "100px", minWidth: "100px" }} src="https://cdn.discordapp.com/attachments/522532170196058124/1235386075158151188/image.png?ex=663822f0&is=6636d170&hm=e2299c151ae46f680149f25a355995b5808420e80856f84099343588a279a6c5&" />
      </Box>
      <Box sx={{ width: "45%", height: "100%" }}>
        <Typography variant="h5">Breakfast Menu</Typography>
        <Typography>20 kcal</Typography>
      </Box>
      <Box component={Button} sx={{ width: "15%", height: "100%", fontStyle: "italic" }} onClick={navigateToCustomize}>
            <Typography variant="subtitle2">customize</Typography>
        </Box>
      <Box
        sx={{ width: "20%", height: "100%" }}
        display={"flex"}
        flexDirection={"column"}
        textAlign={"center"}
      >
        <Typography variant="h6">
            8.30€
        </Typography>
        <Typography variant="overline">
            (+0.40€) {/* extra cost from adding extra ingredients on the order */}
        </Typography>
        <Box component={Button}>Remove</Box>
      </Box>
    </Box>
  );
};

export default OrderCustomizeMenu;
