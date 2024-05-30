import axios from "axios"
import { IEmployee, JwtRefreshResponse } from "../types/EmployeeTypes";
import { SignUpRequest } from "../pages/SignUp";
import { SignInRequest } from "../pages/SignIn";

export const signUp = async (signUpData: SignUpRequest): Promise<IEmployee> => {
    const VITE_HOST = import.meta.env.VITE_HOST as string;
    const res = await axios.post<IEmployee>(`http://${VITE_HOST}/api/auth/signup`, signUpData);

    return res.data;
}

export const signIn = async (signInData: SignInRequest): Promise<IEmployee> => {
    const VITE_HOST = import.meta.env.VITE_HOST as string;
    const res = await axios.post<IEmployee>(`http://${VITE_HOST}/api/auth/signin`, signInData);

    return res.data;
}

export const refreshToken = async (refreshToken: string): Promise<JwtRefreshResponse> => {
    const VITE_HOST = import.meta.env.VITE_HOST as string;
    const res = await axios.post<JwtRefreshResponse>(`http://${VITE_HOST}/api/auth/refreshToken`,
        {
            "refreshToken": refreshToken,
        }
    );

    return res.data;
}