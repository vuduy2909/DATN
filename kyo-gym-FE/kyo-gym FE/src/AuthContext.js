import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

// Tạo context để lưu trạng thái xác thực
const AuthContext = createContext();

export const useAuth = () => {
    return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState({});
    const [isAdmin, setIsAdmin] = useState(false);

    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const token = localStorage.getItem('token');
        setUser(token);

        // Check if user is admin
        if (token) {
            axios.get('http://localhost:8080/api/v1/users/info', {
                headers: { Authorization: `Bearer ${token}` }
            })
            .then(response => {
                const isAdminUser = response.data.roles === 'ROLE_ADMIN';
                setIsAdmin(isAdminUser);
                
                // Redirect to admin page if user is admin and not already on admin page
                if (isAdminUser && location.pathname === '/') {
                    navigate('/admin');
                }
            })
            .catch(error => {
                console.error('Error fetching user info:', error);
            });
        }

        const loginRequiredPaths = ['/user', '/collections/1/days/1', '/admin'];

        if (!token && loginRequiredPaths.includes(location.pathname)) {
            navigate('/login');
        }
    }, [navigate, location.pathname]);

    return (
        <AuthContext.Provider value={{ user, setUser, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
};
