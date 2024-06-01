import { Alert, AlertTitle, Avatar, Box, Button, Container, CssBaseline, Fade, FormControl, FormControlLabel, FormLabel, Grid, IconButton, Radio, RadioGroup, TextField, Typography } from "@mui/material";
import Copyright from "../components/Copyright";
import { Close, LockOutlined } from "@mui/icons-material";
import { Link, useNavigate } from "react-router-dom";
import { EmployeeRole, IEmployee } from "../types/EmployeeTypes";
import { useContext, useEffect, useState } from "react";
import { signUp } from "../api/auth.service";
import { AuthContext } from "../context/AuthContext";

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
  role: EmployeeRole;
}

const SignUp = () => {
  const {auth, setAuth} = useContext(AuthContext);

  const navigate = useNavigate();
  const [selectedRole, setSelectedRole] = useState<string>('');
  const [error, setError] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>("");

  const validateEmail = (email: string): boolean => {
    // something@something.something
    const regex = new RegExp('.*@.*\..+');
    return regex.test(email);
  }

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    // check if all values were input
    if (!Object.values(EmployeeRole).includes(selectedRole as EmployeeRole) || !validateEmail(data.get("email") as string)
        || (data.get("password") as string).length == 0 || (data.get("username") as string).length == 0) {
      setError(true);
      setErrorMessage("Invalid form data! Make sure you fill required fields and email has correct format.");
      return;
    }
    setErrorMessage('');
    setError(false);
    const sendSignUpRequest = async () => {
      try {
        const userData: IEmployee = await signUp({
          username: data.get("username") as string,
          email: data.get("email") as string,
          password: data.get("password") as string,
          role: selectedRole as EmployeeRole
        });
        setAuth(userData);
        navigate("/signage");
      } catch (error) {
        setError(true);
        setErrorMessage("Invalid credentials.");
      }
    };

    sendSignUpRequest();
  };

  useEffect(() => {
    if (auth !== undefined)
      navigate("/signage");
  }, [])

  return (
    <Container id="features" sx={{ py: { xs: 8, sm: 16 } }}>
      <CssBaseline />
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
          Sign up
        </Typography>
        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
          <FormControl error={error}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  error={error}
                  id="username"
                  label="Username"
                  name="username"
                  autoComplete="username"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  error={error}
                  id="email"
                  label="Email Address"
                  name="email"
                  autoComplete="email"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  name="password"
                  error={error}
                  label="Password"
                  type="password"
                  id="password"
                  autoComplete="new-password"
                />
              </Grid>
              <Grid item xs={12}>
                  <FormLabel id="demo-radio-buttons-group-label" required>Role</FormLabel>
                  <RadioGroup
                    aria-labelledby="demo-radio-buttons-group-label"
                    defaultValue="female"
                    name="role"
                    value={selectedRole}
                    onChange={(e) => setSelectedRole(e.target.value)}
                    row
                  >
                    <FormControlLabel value={EmployeeRole.COOK} control={<Radio />} label="Cook" />
                    <FormControlLabel value={EmployeeRole.DESK_PAYMENTS} control={<Radio />} label="Desk Payments" />
                    <FormControlLabel value={EmployeeRole.DESK_ORDERS} control={<Radio />} label="Desk Orders" />
                  </RadioGroup>
              </Grid>
            </Grid>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              Sign Up
            </Button>
            <Grid container justifyContent="flex-end">
              <Grid item>
                <Link to={"/signage/signin"}>
                  Already have an account? Sign in
                </Link>
              </Grid>
            </Grid>
          </FormControl>
        </Box>
      </Box>
      <Box sx={{ mt: 5 }}>
        <Copyright />
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
          <AlertTitle>Error while signing up:</AlertTitle>
            {errorMessage}
        </Alert>
      </Fade>
    </Container>
  );
}

export default SignUp;
