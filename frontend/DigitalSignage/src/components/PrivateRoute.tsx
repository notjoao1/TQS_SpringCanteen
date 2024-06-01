import { useContext } from 'react';
import { Outlet, Navigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext';
import { EmployeeRole } from '../types/EmployeeTypes';

const PrivateRoute = () => {
    const { auth } = useContext(AuthContext);
    return (
        auth === undefined ? <Navigate to={"/signin"}/> : <Outlet/>
    )
}

export default PrivateRoute;