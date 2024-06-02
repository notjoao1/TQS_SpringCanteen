export enum EmployeeRole {
    COOK = "COOK",
    DESK_PAYMENTS = "DESK_PAYMENTS",
    DESK_ORDERS = "DESK_ORDERS"
}


export interface IEmployee {
    id: number;
    username: string;
    email: string;
    token: string;
    refreshToken: string;
    userRole: EmployeeRole;
}

export interface JwtRefreshResponse {
    accessToken: string;
}