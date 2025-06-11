import { request } from '../utils/axiosInstance';

// Service cho việc gửi dữ liệu form và nhận đề xuất
const UserFormService = {
  // Gửi dữ liệu form 7 bước và nhận đề xuất khóa học
  submitFormData: async (formData) => {
    try {
      console.log('UserFormService: Submitting form data:', formData);
      
      // Sanitize data to ensure only expected fields are sent to the backend
      const sanitizedData = { ...formData };
      
      // Remove any fields that might cause conflicts with backend DTO
      const fieldsToDelete = ['trainingFrequency', 'desiredFrequency', 'trainingGoal', 
                              'waistSize', 'chestSize', 'bodyImage', 'workEnvironment'];
      
      fieldsToDelete.forEach(field => {
        if (sanitizedData.hasOwnProperty(field)) {
          console.log(`UserFormService: Removing field '${field}' to prevent backend DTO conflicts`);
          delete sanitizedData[field];
        }
      });
      
      console.log('UserFormService: Sanitized data:', sanitizedData);
      
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
        const response = await request.post('/track-data-ai/recommend-course', sanitizedData, { headers });
        
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
  analyzeBodyImage: async (imageFile, height, weight, gender, waistCircumference, chestCircumference, customerId = null) => {
    try {
      const formData = new FormData();
      formData.append('image', imageFile);
      formData.append('height', height);
      formData.append('weight', weight);
      formData.append('gender', gender);
      
      // Add customerId if provided
      if (customerId) {
        formData.append('customerId', customerId);
      }
      
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
        customerId: customerId || 'not provided',
        fileName: imageFile.name, 
        fileSize: imageFile.size 
      });
      
      // Lấy token từ localStorage (hoặc vị trí lưu trữ khác)
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // NOTE: Không set 'Content-Type': 'multipart/form-data' cho FormData
      // Browser sẽ tự động set với boundary chính xác
      
      const response = await request.post('/track-data-ai/analyze-image', formData, { 
        headers,
        timeout: 30000  // 30 seconds timeout for image analysis
      });
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
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // NOTE: Không set 'Content-Type': 'multipart/form-data' cho FormData
      // Browser sẽ tự động set với boundary chính xác

      const response = await request.post('/track-data-ai/upload-csv', formData, { headers });
      return response;
    } catch (error) {
      console.error('Error uploading CSV data:', error);
      throw error;
    }
  },

  // Lấy kết quả phân tích hình ảnh đã lưu cho customer
  getSavedAnalysis: async (customerId) => {
    try {
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      console.log('Getting saved analysis for customer:', customerId);
      const response = await request.get(`/track-data-ai/get-analysis-result/${customerId}`, { headers });
      console.log('Received saved analysis response:', response);
      
      return response;
    } catch (error) {
      console.error('Error getting saved analysis:', error);
      
      // If it's a 404 or customer not found, don't throw error
      if (error.response && (error.response.status === 404 || error.response.status === 400)) {
        return { hasAnalysis: false, message: 'No saved analysis found' };
      }
      
      throw error;
    }
  },

  // Kiểm tra trạng thái phân tích (có thể dùng cho anonymous user)
  checkAnalysisStatus: async (customerId = null, sessionId = null) => {
    try {
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const requestData = {};
      if (customerId) {
        requestData.customerId = customerId;
      }
      if (sessionId) {
        requestData.sessionId = sessionId;
      }

      console.log('Checking analysis status with data:', requestData);
      const response = await request.post('/track-data-ai/check-analysis-status', requestData, { headers });
      console.log('Analysis status response:', response);
      
      return response;
    } catch (error) {
      console.error('Error checking analysis status:', error);
      return { hasAnalysis: false, userType: 'unknown', error: error.message };
    }
  },

  // Method mới cho Step7 với formData và courseId
  getRecommendationStep7DataNew: async (formDataJson, recommendedCourseId) => {
    try {
      console.log('UserFormService: Calling Step7 with formDataJson and courseId:', formDataJson, recommendedCourseId);
      
      // Validate recommendedCourseId and provide detailed error logging
      if (!recommendedCourseId) {
        console.error('UserFormService: Missing recommendedCourseId!');
        console.error('FormData JSON:', formDataJson);
        
        // Try to extract courseId from formData if possible
        try {
          const parsedFormData = JSON.parse(formDataJson);
          if (parsedFormData.recommendedCourseId) {
            recommendedCourseId = parsedFormData.recommendedCourseId;
            console.log('UserFormService: Extracted recommendedCourseId from formData:', recommendedCourseId);
          }
        } catch (parseError) {
          console.error('UserFormService: Failed to parse formDataJson:', parseError);
        }
        
        // If still no courseId, use a default or fallback value
        if (!recommendedCourseId) {
          recommendedCourseId = 1; // Use a default course ID as fallback
          console.warn('UserFormService: Using default courseId=1 as fallback');
        }
      }
      
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
      
      // Ensure consistent property naming between API and frontend
      const formattedResponse = {
        ...response,
        recommendedCourses: response.recommendedCourse || response.recommendedCourses,
        actions: {
          formData: JSON.parse(formDataJson),
          recommendedCourseId: recommendedCourseId
        }
      };
      
      console.log('UserFormService: Formatted response for Step7:', formattedResponse);
      return formattedResponse;
    } catch (error) {
      console.error('Error fetching Step7 data:', error);
      throw error;
    }
  },

  // Method để confirm recommendation
  confirmRecommendation: async (formData, recommendedCourseId) => {
    try {
      console.log('=== ENHANCED UserFormService confirmRecommendation ===');
      console.log('UserFormService: Confirming recommendation with data:');
      console.log('FormData:', JSON.stringify(formData, null, 2));
      console.log('RecommendedCourseId:', recommendedCourseId, 'Type:', typeof recommendedCourseId);
      
      // Enhanced validation with detailed error messages
      if (!formData) {
        console.error('UserFormService: formData is null or undefined');
        throw new Error('Form data is required for confirmation');
      }
      
      if (!recommendedCourseId) {
        console.error('UserFormService: recommendedCourseId is null or undefined');
        throw new Error('Recommended course ID is required for confirmation');
      }
      
      // Validate and convert recommendedCourseId to ensure it's a valid number
      let validatedCourseId = null;
      if (typeof recommendedCourseId === 'number' && !isNaN(recommendedCourseId)) {
        validatedCourseId = recommendedCourseId;
      } else if (typeof recommendedCourseId === 'string' && !isNaN(Number(recommendedCourseId))) {
        validatedCourseId = Number(recommendedCourseId);
      } else if (typeof recommendedCourseId === 'object') {
        // Handle case where it's an object with courseId or id property
        if (recommendedCourseId.courseId && !isNaN(Number(recommendedCourseId.courseId))) {
          validatedCourseId = Number(recommendedCourseId.courseId);
        } else if (recommendedCourseId.id && !isNaN(Number(recommendedCourseId.id))) {
          validatedCourseId = Number(recommendedCourseId.id);
        }
      }
      
      if (!validatedCourseId || isNaN(validatedCourseId)) {
        console.error('UserFormService: Invalid recommendedCourseId format:', recommendedCourseId);
        throw new Error(`Invalid recommended course ID format: ${recommendedCourseId}`);
      }
      
      console.log('UserFormService: Validated course ID:', validatedCourseId);
      
      // Lấy token từ localStorage
      const token = localStorage.getItem('token');
      const headers = {};
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      // Chuẩn bị dữ liệu gửi đi với enhanced validation
      const requestData = {
        formData: formData,
        recommendedCourseId: validatedCourseId
      };
      
      console.log('UserFormService: Final request data:', JSON.stringify(requestData, null, 2));
      
      // Gọi API endpoint xác nhận
      const response = await request.post('/track-data-ai/confirm-recommendation', requestData, { headers });
      console.log('UserFormService: Recommendation confirmation response:', response);
      
      return response;
    } catch (error) {
      console.error('UserFormService: Error confirming recommendation:', error);
      
      // Enhanced error logging
      if (error.response) {
        console.error('UserFormService: Response status:', error.response.status);
        console.error('UserFormService: Response data:', error.response.data);
        console.error('UserFormService: Response headers:', error.response.headers);
      } else if (error.request) {
        console.error('UserFormService: Request made but no response received:', error.request);
      } else {
        console.error('UserFormService: Error in request setup:', error.message);
      }
      
      throw error;
    }
  }
};

export default UserFormService;
