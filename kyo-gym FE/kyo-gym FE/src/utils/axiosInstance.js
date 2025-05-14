import axios from 'axios';

export const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1'
});

export const get = async (url, options = {}) => {
    try {
        const response = await axiosInstance.get(url, options);
        return response.data || [];
    } catch (err) {
        console.error(`GET request failed for ${url}:`, err);
        return [];
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

export const remove = async (url, data, options = {}) => {
    try {
        const response = await axiosInstance.delete(url, data, options);
        return response.data;
    } catch (err) {
        console.error(`DELETE request failed for ${url}:`, err);
        throw err;
    }
};

// Mỗi request đều setting sẵn r - bỏ 1 lần ở đây và không dùng axios => dùng request để gọi api
axiosInstance.interceptors.request.use((config) => {
    const token = 'Bearer ' + localStorage.getItem('token');

    if (token && token !== 'Bearer null' && token !== 'Bearer undefined') {
        config.headers.authorization = token;
    }
    return config;
});

axiosInstance.interceptors.response.use(
    (response) => {
        if (response.data && response.data.message === 'Error -> Not authenticated') {
            console.log('Phát hiện lỗi xác thực, chuyển hướng về trang đăng nhập');
            localStorage.removeItem('token');
            window.location.href = '/login';
            return null;
        }
        return response;
    },
    (error) => {
        console.log('API Error:', error);
        
        // Xử lý lỗi xác thực
        if (error?.response?.status === 401) {
            console.log('Mã lỗi 401: Chưa xác thực');
            localStorage.removeItem('token');
            
            if (!window.location.href.includes('login') && !window.location.href.includes('register')) {
                window.location.href = '/login';
            }
            return null;
        }
        
        // Các lỗi khác - báo lỗi nhưng không tự động chuyển hướng
        if (error?.response?.status === 400 || error?.response?.status === 404) {
            console.log(`Lỗi ${error.response.status}: ${error.response.data?.message || 'Unknown error'}`);
        }
        
        return Promise.reject(error);
    }
);

export * as request from './axiosInstance';
