import React, { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { axiosInstance } from '../utils/axiosInstance';

const AdminRoute = ({ redirectPath = "/", children }) => {
  const { user } = useAuth();
  const [isAdmin, setIsAdmin] = useState(null); // null = đang kiểm tra, true/false = kết quả
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAdminStatus = async () => {
      // Nếu không có user/token, không phải admin
      if (!user || user === 'null' || user === 'undefined') {
        setIsAdmin(false);
        setIsLoading(false);
        return;
      }

      try {
        // Sử dụng axiosInstance thay vì axios trực tiếp để đi qua interceptor
        const response = await axiosInstance.get('/users/info');

        console.log("AdminRoute - User info response:", response.data);
        const isAdminUser = response.data.roles === 'ROLE_ADMIN';
        
        // Cập nhật localStorage
        localStorage.setItem('isAdmin', isAdminUser ? 'true' : 'false');
        setIsAdmin(isAdminUser);
        
        if (!isAdminUser) {
          console.log("AdminRoute - User is not admin, redirecting to home");
        }
      } catch (error) {
        console.error('AdminRoute - Error checking admin status:', error);
        // Nếu có lỗi API (như 401), coi như không phải admin
        setIsAdmin(false);
        localStorage.setItem('isAdmin', 'false');
        
        // Nếu lỗi 401, có thể token đã hết hạn
        if (error?.response?.status === 401) {
          console.log('AdminRoute - Token expired or invalid, clearing localStorage');
          localStorage.removeItem('token');
          localStorage.removeItem('isAdmin');
        }
      } finally {
        setIsLoading(false);
      }
    };

    checkAdminStatus();
  }, [user]);

  // Hiển thị loading trong khi kiểm tra
  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px'
      }}>
        Đang kiểm tra quyền truy cập...
      </div>
    );
  }

  // Nếu không phải admin, chuyển hướng về trang chủ
  if (!isAdmin) {
    console.log("AdminRoute - Access denied, redirecting to:", redirectPath);
    return <Navigate to={redirectPath} replace />;
  }

  // Nếu là admin, cho phép truy cập
  return children || <Outlet />;
};

export default AdminRoute; 