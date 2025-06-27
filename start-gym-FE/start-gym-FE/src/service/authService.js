import axiosInstance from '../utils/axiosInstance';

const AuthService = {
    login: async (username, password) => {
        try {
            const response = await axiosInstance.post('/login', {
                username,
                password
            });
            // Xử lý kết quả đăng nhập ở đây, ví dụ lưu token vào Local Storage.
            const { token } = response.data;
            localStorage.setItem('token', token);
            return response.data;
        } catch (error) {
            throw new Error('Đăng nhập không thành công');
        }
    },

    logout: async () => {
        try {
            // Call logout API to delete refresh token on server
            const token = localStorage.getItem('token');
            if (token) {
                await axiosInstance.post('/public/logout', {}, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
            }
        } catch (error) {
            console.error('Error during logout:', error);
            // Continue with local cleanup even if API fails
        }
        
        // Xử lý đăng xuất, xóa token và refresh token khỏi Local Storage
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('username');
        localStorage.removeItem('isAdmin');
        // Có thể thực hiện các bước khác khi đăng xuất
    },

    isLoggedIn: () => {
        // Kiểm tra xem người dùng có đăng nhập hay không
        return !!localStorage.getItem('token');
    },

    // Các phương thức khác như kiểm tra quyền, lấy thông tin người dùng, v.v.
};

export default AuthService;
