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
    const [isAuthChecked, setIsAuthChecked] = useState(false);

    const navigate = useNavigate();
    const location = useLocation();

    // Check if user was banned (set by our axios interceptor)
    useEffect(() => {
        const userBanned = localStorage.getItem('userBanned') === 'true';
        const isAdminUser = localStorage.getItem('isAdmin') === 'true';
        
        // Only redirect to contact if user is banned AND not an admin
        if (userBanned && !isAdminUser && location.pathname !== '/contact') {
            console.log('Non-admin user was banned, redirecting to contact page');
            localStorage.removeItem('userBanned'); // Clear the flag
            
            // Get stored information about the banned user
            const username = localStorage.getItem('username') || 'user';
            const customerName = localStorage.getItem('customerName') || '';
            
            // Navigate to contact page
            navigate('/contact', {
                state: {
                    isBanned: true,
                    username: username,
                    customerName: customerName
                }
            });
        } else if (userBanned && isAdminUser) {
            // If admin, just clear the banned flag but don't redirect
            console.log('Admin user detected banned flag, clearing but not redirecting');
            localStorage.removeItem('userBanned');
        }
    }, [location.pathname, navigate]);

    useEffect(() => {
        // Track last auth check time to avoid frequent checks
        const lastAuthCheck = parseInt(localStorage.getItem('lastAuthCheck') || '0');
        const currentTime = Date.now();
        const token = localStorage.getItem('token');
        const MIN_CHECK_INTERVAL = 60000; // 1 minute minimum between checks
        
        // Set initial user state based on token existence
        if (token && token !== 'null' && token !== 'undefined') {
            // Don't set user to token string, wait for API response
            if (!user || typeof user === 'string') {
                setUser({}); // Set empty object until we get real user data
            }
        } else {
            setUser(null);
        }
        
        // Only check auth status if token exists and enough time has passed since last check
        if (token && token !== 'null' && token !== 'undefined' && 
            (currentTime - lastAuthCheck > MIN_CHECK_INTERVAL || !isAuthChecked)) {
            
            // Update last check time
            localStorage.setItem('lastAuthCheck', currentTime.toString());
            
            // Kiểm tra giá trị isAdmin từ localStorage trước
            const storedIsAdmin = localStorage.getItem('isAdmin') === 'true';
            setIsAdmin(storedIsAdmin);
            
            // Sử dụng axiosInstance thay vì axios trực tiếp để đi qua interceptor
            axiosInstance.get('/users/info', {
                validateStatus: function(status) {
                    return status < 500; // Accept 2xx, 3xx, 4xx but not 5xx
                }
            })
            .then(response => {
                // Skip if response is error
                if (response.status >= 400) {
                    console.log(`AuthContext - User info returned status ${response.status}, skipping`);
                    setIsAuthChecked(true);
                    return;
                }
                
                console.log("AuthContext - User info response:", response.data);
                
                // Set user data from API response
                setUser(response.data);
                
                // Check customer is_enable status on each render
                const customerData = response.data;
                const directIsEnable = customerData.is_enable !== false && customerData.isEnable !== false;
                const nestedIsEnable = customerData.customer?.is_enable !== false && 
                                      customerData.customer?.isEnable !== false;
                
                // Get admin status
                const isAdminUser = response.data.roles === 'ROLE_ADMIN';
                
                // If customer is banned (is_enable = false), redirect to contact page
                // BUT ONLY if the user is NOT an admin
                if ((!directIsEnable || !nestedIsEnable) && !isAdminUser) {
                    console.log('AuthContext - Non-admin customer is banned (is_enable = false), redirecting to contact page');
                    localStorage.removeItem('token');
                    localStorage.removeItem('isAdmin');
                    setUser(null);
                    setIsAdmin(false);
                    
                    if (location.pathname !== '/contact') {
                        navigate('/contact', {
                            state: {
                                isBanned: true,
                                username: localStorage.getItem('username') || 'user'
                            }
                        });
                    }
                    return;
                } else if ((!directIsEnable || !nestedIsEnable) && isAdminUser) {
                    console.log('AuthContext - Admin user has banned customer record, but admin access allowed');
                }
                
                // Continue normal flow - check admin status
                setIsAdmin(isAdminUser);
                localStorage.setItem('isAdmin', isAdminUser ? 'true' : 'false');
                setIsAuthChecked(true);
            })
            .catch(error => {
                console.error('AuthContext - Error fetching user info:', error);
                setIsAuthChecked(true);
                
                // The axios interceptor will handle banned users
                
                // Nếu lỗi 401, có thể token đã hết hạn
                if (error?.response?.status === 401) {
                    console.log('AuthContext - Token expired or invalid, clearing localStorage');
                    localStorage.removeItem('token');
                    localStorage.removeItem('isAdmin');
                    setUser(null);
                    setIsAdmin(false);
                }
            });
        } else {
            // If we don't have a token, consider auth check done
            if (!token) {
                setIsAuthChecked(true);
            }
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
    }, [navigate, location.pathname, isAuthChecked]);

    return (
        <AuthContext.Provider value={{ user, setUser, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
};
