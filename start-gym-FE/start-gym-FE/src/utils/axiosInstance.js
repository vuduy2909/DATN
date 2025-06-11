import axios from 'axios';

export const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 30000, // Tăng timeout lên 30 giây cho image analysis
    // Don't set Content-Type globally as it will interfere with multipart requests
});

// Simplified image URL helper
export const getImageUrl = (imageUrl) => {
    if (!imageUrl) {
        // Return SVG data URL directly instead of external placeholders
        return 'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22286%22%20height%3D%22180%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20286%20180%22%20preserveAspectRatio%3D%22none%22%3E%3Cdefs%3E%3Cstyle%20type%3D%22text%2Fcss%22%3E%23holder_18c66a8c7b3%20text%20%7B%20fill%3A%23999%3Bfont-weight%3Anormal%3Bfont-family%3AArial%2C%20Helvetica%2C%20Open%20Sans%2C%20sans-serif%2C%20monospace%3Bfont-size%3A14pt%20%7D%20%3C%2Fstyle%3E%3C%2Fdefs%3E%3Cg%20id%3D%22holder_18c66a8c7b3%22%3E%3Crect%20width%3D%22286%22%20height%3D%22180%22%20fill%3D%22%23373940%22%3E%3C%2Frect%3E%3Cg%3E%3Ctext%20x%3D%22107.1875%22%20y%3D%2296.3%22%3EImage%20not%20available%3C%2Ftext%3E%3C%2Fg%3E%3C%2Fg%3E%3C%2Fsvg%3E';
    }
    
    // If the URL is already absolute, return it as is
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
        return imageUrl;
    }
    
    // If it's a data URL, return it as is
    if (imageUrl.startsWith('data:')) {
        return imageUrl;
    }
    
    // Special case for Firebase Storage references
    if (imageUrl.includes('firebasestorage.googleapis.com')) {
        return imageUrl;
    }
    
    // Special case to handle problematic paths that could cause proxy errors
    if (imageUrl.startsWith('/assets/')) {
        // Use our fallback SVG instead of trying to proxy these paths
        return 'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22286%22%20height%3D%22180%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20286%20180%22%20preserveAspectRatio%3D%22none%22%3E%3Cdefs%3E%3Cstyle%20type%3D%22text%2Fcss%22%3E%23holder_18c66a8c7b3%20text%20%7B%20fill%3A%23999%3Bfont-weight%3Anormal%3Bfont-family%3AArial%2C%20Helvetica%2C%20Open%20Sans%2C%20sans-serif%2C%20monospace%3Bfont-size%3A14pt%20%7D%20%3C%2Fstyle%3E%3C%2Fdefs%3E%3Cg%20id%3D%22holder_18c66a8c7b3%22%3E%3Crect%20width%3D%22286%22%20height%3D%22180%22%20fill%3D%22%23373940%22%3E%3C%2Frect%3E%3Cg%3E%3Ctext%20x%3D%22107.1875%22%20y%3D%2296.3%22%3EImage%20not%20available%3C%2Ftext%3E%3C%2Fg%3E%3C%2Fg%3E%3C%2Fsvg%3E';
    }
    
    // Handle server-side image paths
    if (imageUrl.startsWith('/uploads/') || imageUrl.startsWith('uploads/')) {
        const formattedPath = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`;
        return `http://localhost:8080${formattedPath}`;
    }
    
    // Default case - assume it's a relative path to be served from the backend
    return imageUrl;
};

// Add request interceptor to add authorization token 
axiosInstance.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// Simplified response interceptor with minimized error logging
axiosInstance.interceptors.response.use(
    response => {
        // Check for is_enable=false in response data - but ONLY for customer endpoints, NOT admin
        const responseData = response.data;
        const isAdminEndpoint = response.config.url && response.config.url.includes('/admin/');
        const isAdminUser = localStorage.getItem('isAdmin') === 'true';
        
        // Skip banned user detection for admin users or admin endpoints
        if (isAdminUser || isAdminEndpoint) {
            return response;
        }
        
        if (responseData && 
            ((responseData.is_enable === false || responseData.isEnable === false) || 
             (responseData.customer && (responseData.customer.is_enable === false || responseData.customer.isEnable === false)))) {
            
            console.log('Banned user detected in API response (non-admin user)');
            localStorage.setItem('userBanned', 'true');
            
            // Store username if available
            if (responseData.username || responseData.userName) {
                localStorage.setItem('username', responseData.username || responseData.userName);
            } else if (responseData.customer && (responseData.customer.username || responseData.customer.userName)) {
                localStorage.setItem('username', responseData.customer.username || responseData.customer.userName);
            }
            
            // Store customer name if available
            if (responseData.customerName) {
                localStorage.setItem('customerName', responseData.customerName);
            } else if (responseData.customer && responseData.customer.customerName) {
                localStorage.setItem('customerName', responseData.customer.customerName);
            }
        }
        
        return response;
    },
    error => {
        // Authentication error handling with throttled logging
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // Limit logging frequency to prevent console spam
            const currentTime = Date.now();
            const lastAuthErrorTime = parseInt(localStorage.getItem('lastAuthErrorTime') || 0);
            
            if (currentTime - lastAuthErrorTime > 5000) {
                console.log('Authentication error detected');
                localStorage.setItem('lastAuthErrorTime', currentTime.toString());
            }
        }
        
        // Handle banned user errors - but ONLY for non-admin users
        const errorResponse = error?.response?.data;
        const isAdminEndpoint = error.config && error.config.url && error.config.url.includes('/admin/');
        const isAdminUser = localStorage.getItem('isAdmin') === 'true';
        
        // Skip banned user detection for admin users or admin endpoints
        if (isAdminUser || isAdminEndpoint) {
            return Promise.reject(error);
        }
        
        if (errorResponse) {
            const errorMessage = errorResponse.error || 
                               errorResponse.message || 
                               (typeof errorResponse === 'string' ? errorResponse : '');
            
            const bannedKeywords = ['banned', 'disabled', 'suspended', 'blocked', 'Customer account is banned'];
            
            // Check for banned status
            const isBanned = bannedKeywords.some(keyword => 
                errorMessage && errorMessage.toLowerCase().includes(keyword.toLowerCase())
            );
            
            // Check if errorResponse has is_enable=false
            const isDisabledField = 
                errorResponse.is_enable === false || 
                errorResponse.isEnable === false || 
                (errorResponse.customer && (
                    errorResponse.customer.is_enable === false || 
                    errorResponse.customer.isEnable === false
                ));
            
            if (isBanned || isDisabledField) {
                console.log('Banned user detected in API response interceptor (non-admin user)');
                localStorage.setItem('userBanned', 'true');
                localStorage.removeItem('token');
                localStorage.removeItem('isAdmin');
                error.isBannedUser = true;
            }
        }
        
        return Promise.reject(error);
    }
);

// Create request object with common HTTP methods
export const request = {
    get: async (url, options = {}) => {
        try {
            const response = await axiosInstance.get(url, options);
            return response.data;
        } catch (err) {
            console.error(`GET request failed for ${url}:`, err);
            throw err;
        }
    },
    
    post: async (url, data, options = {}) => {
        try {
            console.log(`🚀 Making POST request to ${url}`);
            
            // Set Content-Type to application/json only if it's not FormData
            const finalOptions = { ...options };
            if (!(data instanceof FormData) && !finalOptions.headers?.['Content-Type']) {
                finalOptions.headers = { 
                    ...finalOptions.headers, 
                    'Content-Type': 'application/json' 
                };
            }
            
            const response = await axiosInstance.post(url, data, finalOptions);
            console.log(`✅ POST request successful for ${url}:`, response);
            console.log(`📄 Response data:`, response.data);
            return response.data;
        } catch (err) {
            console.error(`❌ POST request failed for ${url}:`, err);
            console.error(`📋 Error details:`, {
                message: err.message,
                response: err.response,
                status: err.response?.status,
                data: err.response?.data
            });
            throw err;
        }
    },
    
    put: async (url, data, options = {}) => {
        try {
            const response = await axiosInstance.put(url, data, options);
            return response.data;
        } catch (err) {
            console.error(`PUT request failed for ${url}:`, err);
            throw err;
        }
    },
    
    patch: async (url, data, options = {}) => {
        try {
            const response = await axiosInstance.patch(url, data, options);
            return response.data;
        } catch (err) {
            console.error(`PATCH request failed for ${url}:`, err);
            throw err;
        }
    },
    
    delete: async (url, options = {}) => {
        try {
            const response = await axiosInstance.delete(url, options);
            return response.data;
        } catch (err) {
            console.error(`DELETE request failed for ${url}:`, err);
            throw err;
        }
    }
};


