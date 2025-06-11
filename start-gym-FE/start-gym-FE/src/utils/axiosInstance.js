import axios from 'axios';

export const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1'
});

export const get = async (url, options = {}) => {
    try {
        const response = await axiosInstance.get(url, options);
        return response.data;
    } catch (err) {
        console.error(`GET request failed for ${url}:`, err);
        throw err;
    }
};

export const post = async (url, data, options = {}) => {
    try {
        const response = await axiosInstance.post(url, data, options);
        return response.data;
    } catch (err) {
        console.error(`POST request failed for ${url}:`, err);
        throw err;
    }
};

export const put = async (url, data, options = {}) => {
    try {
        const response = await axiosInstance.put(url, data, options);
        return response.data;
    } catch (err) {
        console.error(`PUT request failed for ${url}:`, err);
        throw err;
    }
};

export const patch = async (url, data, options = {}) => {
    try {
        const response = await axiosInstance.patch(url, data, options);
        return response.data;
    } catch (err) {
        console.error(`PATCH request failed for ${url}:`, err);
        throw err;
    }
};

export const remove = async (url, options = {}) => {
    try {
        const response = await axiosInstance.delete(url, options);
        return response.data;
    } catch (err) {
        console.error(`DELETE request failed for ${url}:`, err);
        throw err;
    }
};

// Request interceptor - chỉ log khi debug mode
axiosInstance.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');

    if (token && token !== 'null' && token !== 'undefined') {
        config.headers.Authorization = `Bearer ${token}`;
        // Chỉ log khi cần debug
        if (process.env.NODE_ENV === 'development' && window.localStorage.getItem('debugMode') === 'true') {
            console.log('Setting Authorization header:', `Bearer ${token.substring(0, 20)}...`);
        }
    }
    return config;
});

axiosInstance.interceptors.response.use(
    (response) => {
        if (response.data && response.data.message === 'Error -> Not authenticated') {
            console.log('Phát hiện lỗi xác thực, chuyển hướng về trang đăng nhập');
            localStorage.removeItem('token');
            localStorage.removeItem('isAdmin');
            window.location.href = '/login';
            return null;
        }
        return response;
    },
    (error) => {
        // Chỉ log chi tiết khi debug mode
        const debugMode = process.env.NODE_ENV === 'development' && window.localStorage.getItem('debugMode') === 'true';
        
        if (debugMode) {
            console.log('API Error:', error);
            console.log('Current URL:', window.location.pathname);
            console.log('Error status:', error?.response?.status);
            console.log('Error response data:', error?.response?.data);
        }
        
        // Xử lý lỗi xác thực
        if (error?.response?.status === 401) {
            const currentPath = window.location.pathname;
            
            // Kiểm tra xem có phải trang công khai không
            const isPublicPage = 
                currentPath === '/' ||
                currentPath === '/login' ||
                currentPath === '/register' ||
                currentPath === '/about' ||
                currentPath === '/recipe' ||
                currentPath === '/blog' ||
                currentPath.startsWith('/collections') ||
                currentPath.match(/^\/collections\/\d+\/days\/\d+$/);
            
            // Kiểm tra xem có phải API call từ AuthContext hoặc AdminRoute không
            const isAuthCheck = error.config?.url?.includes('/users/info');
            
            // Chỉ xóa token và redirect nếu không phải trang công khai và không phải auth check
            if (!isPublicPage && !isAuthCheck) {
                if (debugMode) {
                    console.log('Redirecting to login from protected page:', currentPath);
                }
                localStorage.removeItem('token');
                localStorage.removeItem('isAdmin');
                window.location.href = '/login';
            }
            return Promise.reject(error);
        }
        
        // Các lỗi khác - chỉ log khi debug mode
        if (debugMode && (error?.response?.status === 400 || error?.response?.status === 404)) {
            console.log(`Lỗi ${error.response.status}: ${error.response.data?.message || 'Unknown error'}`);
        }
        
        return Promise.reject(error);
    }
);

export * as request from './axiosInstance';
