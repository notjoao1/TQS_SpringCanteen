import { useContext } from 'react';
import { Outlet, Navigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext';

const PrivateRoute = () => {
    const { auth } = useContext(AuthContext);
    return (
        auth === undefined ? <Navigate to={"/signage/signin"}/> : <Outlet/>
    )
}

export default PrivateRoute;