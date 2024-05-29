import * as React from "react";
import { Link, PaletteMode } from "@mui/material";
import Box from "@mui/material/Box";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Divider from "@mui/material/Divider";
import Typography from "@mui/material/Typography";
import MenuItem from "@mui/material/MenuItem";
import Drawer from "@mui/material/Drawer";
import MenuIcon from "@mui/icons-material/Menu";
import { AuthContext } from "../context/AuthContext";

const logoStyle = {
  width: "140px",
  height: "auto",
  cursor: "pointer",
  padding: "0.5em 1em 0.5em",
};

interface AppAppBarProps {
  mode: PaletteMode;
}

function TopBar({ mode }: AppAppBarProps) {
  const { auth, logout } = React.useContext(AuthContext);
  const [open, setOpen] = React.useState(false);

  const toggleDrawer = (newOpen: boolean) => () => {
    setOpen(newOpen);
  };

  return (
    <div>
      <AppBar
        position="fixed"
        sx={{
          boxShadow: 0,
          bgcolor: "transparent",
          backgroundImage: "none",
          mt: 2,
        }}
      >
        <Container maxWidth="lg">
          <Toolbar
            variant="regular"
            sx={(theme) => ({
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              flexShrink: 0,
              borderRadius: "999px",
              bgcolor:
                theme.palette.mode === "light"
                  ? "rgba(255, 255, 255, 0.4)"
                  : "rgba(0, 0, 0, 0.4)",
              backdropFilter: "blur(24px)",
              maxHeight: 40,
              border: "1px solid",
              borderColor: "divider",
              boxShadow:
                theme.palette.mode === "light"
                  ? `0 0 1px rgba(85, 166, 246, 0.1), 1px 1.5px 2px -1px rgba(85, 166, 246, 0.15), 4px 4px 12px -2.5px rgba(85, 166, 246, 0.15)`
                  : "0 0 1px rgba(2, 31, 59, 0.7), 1px 1.5px 2px -1px rgba(2, 31, 59, 0.65), 4px 4px 12px -2.5px rgba(2, 31, 59, 0.65)",
            })}
          >
            <Box
              sx={{
                flexGrow: 1,
                display: "flex",
                alignItems: "center",
                ml: "-18px",
                px: 0,
              }}
            >
              {/* Logo image */}
              <Link href="/">
                <img
                  src={"/SpringCanteen_Title.png"}
                  style={logoStyle}
                  alt="logo of Spring Canteen"
                />
              </Link>
              <Box sx={{ display: { xs: "none", md: "flex" } }}>
                
                <Link href={auth === undefined ? "/order" : 
                  auth.userRole === "COOK" ? "/employee/orders" : 
                  auth.userRole === "DESK_PAYMENTS" ? "/employee/payments" : 
                  auth.userRole === "DESK_ORDERS" ? "/employee/ready_orders" : "/"}
                >
                  <MenuItem
                    sx={{ py: "6px", px: "12px" }}
                  >
                    <Typography variant="body2" color="text.primary">
                      {auth === undefined ? "Order now" : 
                        auth.userRole === "COOK" ? "Cook orders" : 
                        auth.userRole === "DESK_PAYMENTS" ? "Desk payments" : 
                        auth.userRole === "DESK_ORDERS" ? "Ready orders" : "Home"
                      }
                    </Typography>
                  </MenuItem>
                </Link>
              </Box>
            </Box>
            <Box
              sx={{
                display: { xs: "none", md: "flex" },
                gap: 0.5,
                alignItems: "center",
              }}
            >
              {auth !== undefined ? (
                <Button
                  color="primary"
                  variant="text"
                  size="small"
                  component="a"
                  target="_blank"
                  onClick={() => logout()}
                >
                  Logout
                </Button>
              ) : (
                <>
                  <Button
                    color="primary"
                    variant="text"
                    size="small"
                    component="a"
                    target="_blank"
                  >
                    <Link href={"/signin"}>Sign in</Link>
                  </Button>
                  <Button
                    color="primary"
                    variant="contained"
                    size="small"
                    component="a"
                    target="_blank"
                  >
                    <Link href={"/signup"}>Sign up</Link>
                  </Button>
                </>
              )}
            </Box>
            <Box sx={{ display: { sm: "", md: "none" } }}>
              <Button
                variant="text"
                color="primary"
                aria-label="menu"
                onClick={toggleDrawer(true)}
                sx={{ minWidth: "30px", p: "4px" }}
              >
                <MenuIcon />
              </Button>
              <Drawer anchor="right" open={open} onClose={toggleDrawer(false)}>
                <Box
                  sx={{
                    minWidth: "60dvw",
                    p: 2,
                    backgroundColor: "background.paper",
                    flexGrow: 1,
                  }}
                >
                  <Box
                    sx={{
                      display: "flex",
                      flexDirection: "column",
                      alignItems: "end",
                      flexGrow: 1,
                    }}
                  ></Box>
                  <Link href="/order">
                    <MenuItem
                      // onClick={() => scrollToSection('features')}
                      sx={{ py: "6px", px: "12px" }}
                    >
                      <Typography variant="body2" color="text.primary">
                        Order
                      </Typography>
                    </MenuItem>
                  </Link>
                  <Link href="/restaurants">
                    <MenuItem
                      // onClick={() => scrollToSection('features')}
                      sx={{ py: "6px", px: "12px" }}
                    >
                      <Typography variant="body2" color="text.primary">
                        Restaurants
                      </Typography>
                    </MenuItem>
                  </Link>
                  <Link href="/menu">
                    <MenuItem
                      // onClick={() => scrollToSection('features')}
                      sx={{ py: "6px", px: "12px" }}
                    >
                      <Typography variant="body2" color="text.primary">
                        Menu
                      </Typography>
                    </MenuItem>
                  </Link>
                  <Divider />
                  <MenuItem>
                    <Button
                      color="primary"
                      variant="contained"
                      component="a"
                      href="/material-ui/getting-started/templates/sign-up/"
                      target="_blank"
                      sx={{ width: "100%" }}
                    >
                      Sign up
                    </Button>
                  </MenuItem>
                  <MenuItem>
                    <Button
                      color="primary"
                      variant="outlined"
                      component="a"
                      href="/material-ui/getting-started/templates/sign-in/"
                      target="_blank"
                      sx={{ width: "100%" }}
                    >
                      Sign in
                    </Button>
                  </MenuItem>
                </Box>
              </Drawer>
            </Box>
          </Toolbar>
        </Container>
      </AppBar>
    </div>
  );
}

export default TopBar;
