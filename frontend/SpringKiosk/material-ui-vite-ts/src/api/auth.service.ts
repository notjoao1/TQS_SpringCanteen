import axios from "axios"
import { IEmployee } from "../types/EmployeeTypes";
import { SignUpRequest } from "../pages/SignUp";
import { SignInRequest } from "../pages/SignIn";

export const signUp = async (signUpData: SignUpRequest): Promise<IEmployee> => {
    const res = await axios.post<IEmployee>("http://localhost:8080/api/auth/signup", signUpData);

    return res.data;
}

export const signIn = async (signInData: SignInRequest): Promise<IEmployee> => {
    const res = await axios.post<IEmployee>("http://localhost:8080/api/auth/signin", signInData);

    return res.data;
}