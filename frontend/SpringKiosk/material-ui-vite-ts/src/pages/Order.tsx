import * as React from "react";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import Chip from "@mui/material/Chip";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { FormatListNumberedOutlined } from "@mui/icons-material";
import { IMenu } from "../types/MenuTypes";
import OrderMenuCard from "../components/order_page/OrderMenuCard";
import { BottomNavigation, BottomNavigationAction, Paper, Snackbar } from "@mui/material";
import MenuDetailsModal from "../components/order_page/MenuDetailsModal";
import OrderDrawer from "../components/order_page/OrderDrawer";


export const menus: IMenu[] = [
  {
    id: 1,
    name: "Breakfast Menu",
    price: 8.99,
    image: "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fifoodreal.com%2Fwp-content%2Fuploads%2F2018%2F04%2Fhealthy-pancakes-7.jpg&f=1&nofb=1&ipt=b5e84872f7716ef9b58a2414bff1e8192e14c480437f42e19f69c4d8df12e3e9&ipo=images",
    items: [
      {
        id: 101,
        name: "Pancakes",
        price: 5.99,
        ingredients: [
          {
            id: 1001,
            name: "Flour",
            price: 0.5,
            calories: 150,
          },
          {
            id: 1002,
            name: "Eggs",
            price: 1.0,
            calories: 70,
          },
          {
            id: 1003,
            name: "Milk",
            price: 0.75,
            calories: 90,
          },
          {
            id: 1004,
            name: "Butter",
            price: 0.25,
            calories: 50,
          },
        ],
      },
      {
        id: 102,
        name: "Omelette",
        price: 7.49,
        ingredients: [
          {
            id: 1005,
            name: "Eggs",
            price: 1.0,
            calories: 70,
          },
          {
            id: 1006,
            name: "Cheese",
            price: 1.5,
            calories: 120,
          },
          {
            id: 1007,
            name: "Tomatoes",
            price: 0.75,
            calories: 20,
          },
          {
            id: 1008,
            name: "Onions",
            price: 0.5,
            calories: 30,
          },
        ],
      },
    ],
  },
  {
    id: 2,
    name: "Lunch Menu",
    price: 12.99,
    image: "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fsimply-delicious-food.com%2Fwp-content%2Fuploads%2F2015%2F07%2FBacon-and-cheese-burgers-3.jpg&f=1&nofb=1&ipt=c0d8abfa0e74aab9a38695ab7bc7dbc9d8a51d9f90353a28a37a8320986293bf&ipo=images",
    items: [
      {
        id: 103,
        name: "Grilled Chicken Sandwich",
        price: 9.99,
        ingredients: [
          {
            id: 1009,
            name: "Chicken Breast",
            price: 3.0,
            calories: 180,
          },
          {
            id: 1010,
            name: "Lettuce",
            price: 0.5,
            calories: 5,
          },
          {
            id: 1011,
            name: "Tomatoes",
            price: 0.75,
            calories: 20,
          },
          {
            id: 1012,
            name: "Bread",
            price: 1.0,
            calories: 100,
          },
        ],
      },
      {
        id: 104,
        name: "Caesar Salad",
        price: 6.99,
        ingredients: [
          {
            id: 1013,
            name: "Romaine Lettuce",
            price: 1.5,
            calories: 10,
          },
          {
            id: 1014,
            name: "Chicken",
            price: 3.0,
            calories: 180,
          },
          {
            id: 1015,
            name: "Croutons",
            price: 0.75,
            calories: 50,
          },
          {
            id: 1016,
            name: "Caesar Dressing",
            price: 0.5,
            calories: 120,
          },
        ],
      },
    ],
  },
];

export default function Order() {
  const [selectedMenu, setSelectedMenu] = React.useState<IMenu | undefined>(undefined);
  const [isModalOpen, setIsModalOpen] = React.useState<boolean>(false);
  const [isDrawerOpen, setIsDrawerOpen] = React.useState<boolean>(false);
  const [isSnackbarOpen, setIsSnackbarOpen] = React.useState<boolean>(false);

  const handleOpenModal = (menu: IMenu) => {
    setSelectedMenu(menu);
    setIsModalOpen(true);
  };

  const handleAddToOrder = (menu: IMenu) => {
    // add to order state (TODO) + open snackbar for feedback
    setIsSnackbarOpen(true);
  }

  //const selectedFeature = items[selectedItemIndex];

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Typography component="h2" variant="h4" color="text.primary">
        Add menus to your order
      </Typography>

      <Grid container spacing={6} pt={4}>
        {menus.map((menu, index) => (
          <Grid item xs={12} md={4}>
            <OrderMenuCard index={index} menu={menu} onClickDetails={handleOpenModal} onClickAddToOrder={handleAddToOrder}/>
          </Grid>
        ))}
        <Grid item xs={12} md={6}>
          {/* XS 'Available menus' section */}
          <Grid
            container
            item
            gap={1}
            sx={{ display: { xs: "auto", sm: "none" } }}
          >
            {menus.map((menu, index) => (
              <Chip
                key={index}
                label={menu.name}
                onClick={() => console.log("hi")}
                sx={{
                  borderColor: (theme) => {
                    if (theme.palette.mode === "light") {
                      return selectedMenu === menu ? "primary.light" : "";
                    }
                    return selectedMenu === menu ? "primary.light" : "";
                  },
                  background: (theme) => {
                    if (theme.palette.mode === "light") {
                      return selectedMenu === menu ? "none" : "";
                    }
                    return selectedMenu === menu ? "none" : "";
                  },
                  backgroundColor:
                  selectedMenu === menu ? "primary.main" : "",
                  "& .MuiChip-label": {
                    color: selectedMenu === menu ? "#fff" : "",
                  },
                }}
              />
            ))}
          </Grid>
          {/* END XS 'Available menus' section */}
          {/* XS 'Your Order' section */}
          <Box
            component={Card}
            variant="outlined"
            sx={{
              display: { xs: "auto", sm: "none" },
              mt: 4,
              mb: 4,
            }}
          >
            <Box
              sx={{
                backgroundSize: "cover",
                backgroundPosition: "center",
                minHeight: 280,
              }}
            />
            <Box sx={{ px: 2, pb: 2 }}>
              <Typography
                color="text.primary"
                variant="body2"
                fontWeight="bold"
              >
                {"TEST TODO"}
              </Typography>
            </Box>
          </Box>
          {/* END XS 'Your Order' section */}
          <Box sx={{ px: 1, pb: 2, display: { xs: "auto", sm: "none" } }}>
            <Typography color="text.primary" variant="h5" fontWeight="bold">
              Your order
            </Typography>
          </Box>
          <Box
            component={Card}
            variant="outlined"
            sx={{
              display: { xs: "auto", sm: "none" },
            }}
          >
            <Box
              sx={{
                backgroundSize: "cover",
                backgroundPosition: "center",
                minHeight: 280,
              }}
            />
          </Box>
          {/* END of XS Display mode */}
        </Grid>
      </Grid>
      <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0 }} elevation={5}>
        <BottomNavigation 
          showLabels
          sx={{ px: { xs: 2, md: 20, lg: 40} }}
        >
             <Box
              display={"flex"}
              width={"100%"}
              justifyContent={"flex-start"}
              alignItems={"center"}
            >
              You currently have two items in your order.
            </Box>
            <BottomNavigationAction sx={{float: "right"}} label="View your order" icon={<FormatListNumberedOutlined />} onClick={() => setIsDrawerOpen(true)} />
          </BottomNavigation>
        </Paper>
        <OrderDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)}/>
        <MenuDetailsModal isOpen={isModalOpen} menu={selectedMenu} onClose={() => setIsModalOpen(false)} />
        <Snackbar
          open={isSnackbarOpen}
          autoHideDuration={4000}
          onClose={() => setIsSnackbarOpen(false)}
          message="Successfully added menu to order."
        />
    </Container>
  );
}
