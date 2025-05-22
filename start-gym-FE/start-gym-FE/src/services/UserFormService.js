import { request } from '../utils/axiosInstance';

// Service cho việc gửi dữ liệu form và nhận đề xuất
const UserFormService = {
  // Gửi dữ liệu form 7 bước và nhận đề xuất khóa học
  submitFormData: async (formData) => {
    try {
      console.log('UserFormService: Preparing to submit form data:', formData);
      
      // Kiểm tra các trường bắt buộc
      const requiredFields = ['age', 'gender', 'customerId'];
      const missingFields = requiredFields.filter(field => !formData[field]);
      
      if (missingFields.length > 0) {
        console.error('UserFormService: Missing required fields:', missingFields);
        throw new Error(`Missing required fields: ${missingFields.join(', ')}`);
      }
      
      // Override window.alert before API call to silence any alerts
      const originalAlert = window.alert;
      window.alert = function(message) {
        console.log('Alert suppressed:', message);
        return;
      };
      
      try {
        // Lấy token từ localStorage (hoặc vị trí lưu trữ khác)
        const token = localStorage.getItem('token');
        const headers = {};
        
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
          console.log('UserFormService: Token found and included in headers');
        } else {
          console.warn('UserFormService: No token found in localStorage');
        }
        
        // Thêm custom header để bỏ qua thông báo từ server
        headers['X-Skip-Alert'] = 'true';
        
        // Sửa endpoint API để trùng với endpoint trong collectionService
        console.log('UserFormService: Sending POST request to /track-data-ai/recommend-course');
        const response = await request.post('/track-data-ai/recommend-course', formData, { headers });
        console.log('UserFormService: Received response:', JSON.stringify(response));
        
        // Restore original alert
        window.alert = originalAlert;
        
        // Xóa bất kỳ popup nào có thể đã xuất hiện
        const closeButtons = document.querySelectorAll('button.MuiButtonBase-root');
        closeButtons.forEach(button => {
          if (button.textContent === 'OK' || button.textContent === 'Close') {
            button.click();
            console.log('UserFormService: Found and clicked a close button');
          }
        });
        
        return response;
      } catch (error) {
        // Restore original alert even on error
        window.alert = originalAlert;
        console.error('API Error:', error);
        throw error;
      }
    } catch (error) {
      console.error('UserFormService: Error submitting form data:', error);
      if (error.response) {
        console.error('UserFormService: Response data:', error.response.data);
        console.error('UserFormService: Response status:', error.response.status);
        console.error('UserFormService: Response headers:', error.response.headers);
        
        // Nếu nhận được phản hồi lỗi nhưng có dữ liệu, vẫn trả về để UI xử lý
        if (error.response.data) {
          return error.response.data;
        }
      }
      throw error;
    }
  },

  // Lấy dữ liệu chi tiết cho bước 7
  getRecommendationStep7Data: async (userDataId) => {
    try {
      console.log('UserFormService: Fetching step 7 data for userDataId:', userDataId);
      
      // Lấy token từ localStorage
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // Gọi API endpoint với userDataId làm query parameter
      const response = await request.get(`/track-data-ai/recommendation-step-7?userDataId=${userDataId}`, { headers });
      console.log('UserFormService: Received step 7 data:', response);
      
      return response;
    } catch (error) {
      console.error('UserFormService: Error fetching step 7 data:', error);
      throw error;
    }
  },

  // Phân tích ảnh cơ thể bằng Claude AI
  analyzeBodyImage: async (imageFile, height, weight, gender, waistCircumference, chestCircumference) => {
    try {
      const formData = new FormData();
      formData.append('image', imageFile);
      formData.append('height', height);
      formData.append('weight', weight);
      formData.append('gender', gender);
      
      // Add waist and chest circumference if provided
      if (waistCircumference) {
        formData.append('waist_circumference', waistCircumference);
      }
      
      if (chestCircumference) {
        formData.append('chest_circumference', chestCircumference);
      }

      console.log('Sending data to analyze image:', { 
        height, 
        weight, 
        gender, 
        waist_circumference: waistCircumference || 'not provided',
        chest_circumference: chestCircumference || 'not provided',
        fileName: imageFile.name, 
        fileSize: imageFile.size 
      });
      
      // Lấy token từ localStorage (hoặc vị trí lưu trữ khác)
      const token = localStorage.getItem('token');
      const headers = {
        'Content-Type': 'multipart/form-data'
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      const response = await request.post('/track-data-ai/analyze-image', formData, { headers });
      console.log('Received response from image analysis:', response);
      
      // Kiểm tra nếu phản hồi là lỗi
      if (response && response.bodyType === 'Unknown') {
        console.warn('Image analysis returned Unknown body type:', response);
      }
      
      return response;
    } catch (error) {
      console.error('Error analyzing body image:', error);
      
      // Xử lý lỗi cụ thể từ API
      if (error.response) {
        // Kiểm tra nếu server trả về lỗi với bodyType
        if (error.response.data && error.response.data.bodyType) {
          return error.response.data; // Trả về dữ liệu lỗi để hiển thị
        }
        throw error; // Nếu không có dữ liệu phù hợp, ném ra lỗi để xử lý ở component
      }
      
      throw error;
    }
  },

  // Upload file CSV chứa dữ liệu theo dõi
  uploadTrackingData: async (csvFile, customerId) => {
    try {
      const formData = new FormData();
      formData.append('file', csvFile);
      formData.append('customerId', customerId);

      // Lấy token từ localStorage (hoặc vị trí lưu trữ khác)
      const token = localStorage.getItem('token');
      const headers = {
        'Content-Type': 'multipart/form-data'
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await request.post('/track-data-ai/upload-csv', formData, { headers });
      return response;
    } catch (error) {
      console.error('Error uploading CSV data:', error);
      throw error;
    }
  }
};

export default UserFormService;
