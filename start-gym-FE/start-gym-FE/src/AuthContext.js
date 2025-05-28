import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { axiosInstance } from './utils/axiosInstance';

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
        if (token && token !== 'null' && token !== 'undefined') {
            // Kiểm tra giá trị isAdmin từ localStorage trước
            const storedIsAdmin = localStorage.getItem('isAdmin') === 'true';
            setIsAdmin(storedIsAdmin);
            
            // Sử dụng axiosInstance thay vì axios trực tiếp để đi qua interceptor
            axiosInstance.get('/users/info')
            .then(response => {
                console.log("AuthContext - User info response:", response.data);
                const isAdminUser = response.data.roles === 'ROLE_ADMIN';
                setIsAdmin(isAdminUser);
                localStorage.setItem('isAdmin', isAdminUser ? 'true' : 'false');
            })
            .catch(error => {
                console.error('AuthContext - Error fetching user info:', error);
                
                // Nếu lỗi 401, có thể token đã hết hạn
                if (error?.response?.status === 401) {
                    console.log('AuthContext - Token expired or invalid, clearing localStorage');
                    localStorage.removeItem('token');
                    localStorage.removeItem('isAdmin');
                    setUser(null);
                    setIsAdmin(false);
                }
            });
        }

        // Chỉ yêu cầu đăng nhập cho các trang cá nhân và admin
        const loginRequiredPaths = ['/user', '/profile', '/account', '/admin'];
        const currentPath = location.pathname;

        // Kiểm tra xem có phải trang yêu cầu đăng nhập không
        const requiresLogin = loginRequiredPaths.some(path => currentPath.startsWith(path));

        if (!token && requiresLogin) {
            console.log('Redirecting to login from protected page:', currentPath);
            navigate('/login');
        }
    }, [navigate, location.pathname]);

    return (
        <AuthContext.Provider value={{ user, setUser, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
};
