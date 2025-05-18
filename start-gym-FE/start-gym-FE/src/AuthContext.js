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

        // Kiểm tra cờ chặn điều hướng tới trang chủ
        const blockHomeRedirect = sessionStorage.getItem('blockHomeRedirect');
        
        // Check if user is admin
        if (token) {
            // Kiểm tra giá trị isAdmin từ localStorage trước
            const storedIsAdmin = localStorage.getItem('isAdmin') === 'true';
            setIsAdmin(storedIsAdmin);
            
            axios.get('http://localhost:8080/api/v1/users/info', {
                headers: { Authorization: `Bearer ${token}` }
            })
            .then(response => {
                console.log("AuthContext - User info response:", response.data);
                const isAdminUser = response.data.roles === 'ROLE_ADMIN';
                setIsAdmin(isAdminUser);
                localStorage.setItem('isAdmin', isAdminUser ? 'true' : 'false');
                
                // Chỉ điều hướng nếu:
                // 1. User là admin
                // 2. Đang ở trang chủ
                // 3. KHÔNG có cờ blockHomeRedirect
                if (isAdminUser && location.pathname === '/' && !blockHomeRedirect) {
                    console.log("AuthContext - Redirecting admin to /admin");
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
        
        // Xóa cờ blockHomeRedirect sau khi đã xử lý
        if (blockHomeRedirect) {
            setTimeout(() => {
                sessionStorage.removeItem('blockHomeRedirect');
            }, 1000); // Đợi 1 giây rồi mới xóa
        }
    }, [navigate, location.pathname]);

    return (
        <AuthContext.Provider value={{ user, setUser, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
};
