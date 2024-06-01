import { Close, LockOutlined } from "@mui/icons-material";
import { Alert, AlertTitle, Avatar, Box, Button, Container, Fade, Grid, IconButton, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import Copyright from "../components/Copyright";
import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../context/AuthContext";
import { signIn } from "../api/auth.service";
import { IEmployee } from "../types/EmployeeTypes";

export interface SignInRequest {
  email: string;
  password: string;
}

const SignIn = () => {
  const {auth, setAuth} = useContext(AuthContext);

  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [error, setError] = useState<boolean>(false);
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    // check if all values were input
    if ((data.get("email") as string).length == 0 || (data.get("password") as string).length == 0) {
      setError(true);
      setErrorMessage("Invalid form data! Make sure you fill required fields!");
      return;
    }
    setErrorMessage('');
    setError(false);
    const sendSignInRequest = async () => {
      try {
        const userData: IEmployee = await signIn({
          email: data.get("email") as string,
          password: data.get("password") as string,
        });
        setAuth(userData);
        navigate("/signage");
      } catch (error) {
        setError(true);
        setErrorMessage("Invalid credentials.");
      }
    };

    sendSignInRequest();

  };

  useEffect(() => {
    if (auth !== undefined)
      navigate("/signage")
  }, [])

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: "secondary.main" }}>
          <LockOutlined />
        </Avatar>
        <Typography component="h1" variant="h5">
          Sign in
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Email Address"
            name="email"
            autoComplete="email"
            autoFocus
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
          />
          <Button
            id="signin-button"
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
          >
            Sign In
          </Button>
          <Grid container>
            <Grid item>
              <Link to={"/signage/signup"}>{"Don't have an account? Sign Up"}</Link>
            </Grid>
          </Grid>
        </Box>
      </Box>
      <Box sx={{mt: 8, mb: 4}}>
        <Copyright /* sx={{ mt: 8, mb: 4 }} */ />
      </Box>
      <Fade in={error} unmountOnExit>
        <Alert 
          severity="error" 
          variant="filled" 
          sx={{position: "fixed", bottom: 10, right: 10, width: '30%'}} 
          id="error-alert"
          action={
            <IconButton
              aria-label="close"
              color="inherit"
              size="small"
              onClick={() => {
                setError(false);
              }}
            >
              <Close fontSize="inherit" />
            </IconButton>
          }
        >
          <AlertTitle>Error while signing in:</AlertTitle>
            {errorMessage}
        </Alert>
      </Fade>
    </Container>
  );
}

export default SignIn;
