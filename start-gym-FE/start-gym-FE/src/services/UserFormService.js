import { request } from '../utils/axiosInstance';

// Service cho việc gửi dữ liệu form và nhận đề xuất
const UserFormService = {
  // Gửi dữ liệu form 7 bước và nhận đề xuất khóa học
  submitFormData: async (formData) => {
    try {
      console.log('UserFormService: Submitting form data:', formData);
      
      // Lấy token từ localStorage
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // Temporarily replace alert with console.log to suppress popup
      const originalAlert = window.alert;
      
      try {
        window.alert = () => {
          console.log('UserFormService: Suppressed an alert dialog');
        };
        
        // Call the correct AI recommendation endpoint
        const response = await request.post('/track-data-ai/recommend-course', formData, { headers });
        
        // Restore original alert
        window.alert = originalAlert;
        
        console.log('UserFormService: Received recommendation response:', response);
        
        // Try to click any close buttons from unexpected dialogs
        setTimeout(() => {
          const closeButtons = document.querySelectorAll('button[aria-label="close"], .close-button, [onclick*="close"]');
          closeButtons.forEach(button => {
            button.click();
            console.log('UserFormService: Found and clicked a close button');
          });
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
  },

  // Method mới cho Step7 với formData và courseId
  getRecommendationStep7DataNew: async (formDataJson, recommendedCourseId) => {
    try {
      console.log('UserFormService: Calling Step7 with formDataJson and courseId:', formDataJson, recommendedCourseId);
      
      // Lấy token từ localStorage
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // Call the backend endpoint with formDataJson and recommendedCourseId as query parameters
      const response = await request.get('/track-data-ai/recommendation-step-7', {
        params: {
          formDataJson: formDataJson,
          recommendedCourseId: recommendedCourseId
        },
        headers
      });
      
      console.log('UserFormService: Received Step7 data:', response);
      return response;
    } catch (error) {
      console.error('Error fetching Step7 data:', error);
      throw error;
    }
  },

  // Method để confirm recommendation
  confirmRecommendation: async (formData, recommendedCourseId) => {
    try {
      console.log('UserFormService: Confirming recommendation with formData and courseId:', formData, recommendedCourseId);
      
      // Lấy token từ localStorage
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      const requestData = {
        formData: formData,
        recommendedCourseId: recommendedCourseId
      };
      
      const response = await request.post('/track-data-ai/confirm-recommendation', requestData, { headers });
      
      console.log('UserFormService: Received confirmation response:', response);
      return response;
    } catch (error) {
      console.error('Error confirming recommendation:', error);
      throw error;
    }
  }
};

export default UserFormService;
