import { request } from "../utils/axiosInstance";

export const courseService = {
  // Join a course
  joinCourse: async (courseId) => {
    try {
      const response = await request.post(`/customer-course/join/${courseId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // Get user's courses (both recommended and self-enrolled)
  getMyCourses: async () => {
    try {
      const response = await request.get('/customer-course/my-courses');
      return response;
    } catch (error) {
      throw error;
    }
  },

  // Get recommended courses only
  getRecommendedCourses: async () => {
    try {
      // Try different possible endpoints for getting recommended courses
      let response;
      
      try {
        // First try the most specific endpoint
        response = await request.get('/customer-course/recommended');
        console.log('Endpoint /customer-course/recommended response:', response);
      } catch (error) {
        console.log('Failed to fetch from recommended endpoint, trying alternative');
        
        try {
          // Try alternative endpoint with "my-recommended-courses"
          response = await request.get('/customer-course/my-recommended-courses');
          console.log('Endpoint /customer-course/my-recommended-courses response:', response);
        } catch (secondError) {
          // If both fail, try my-courses and filter client-side
          console.log('Failed alternative endpoint too, will fall back to my-courses');
          
          // Rethrow to handle in component
          throw secondError;
        }
      }
      
      return response;
    } catch (error) {
      console.error('Error fetching recommended courses:', error);
      throw error;
    }
  },

  // Debug endpoint to check user's course enrollments
  debugRecommendations: async () => {
    try {
      const response = await request.get('/customer-course/debug');
      return response;
    } catch (error) {
      throw error;
    }
  },
  
  // Check course completion status for a user
  getCourseCompletion: async (courseId, customerId) => {
    try {
      // Endpoint này sẽ lấy dữ liệu từ bảng customer_day_completion để tính toán % tiến độ
      const response = await request.get(`/customer-progress/course/${courseId}/customer/${customerId}`);
      console.log(`Course completion data for courseId=${courseId}, customerId=${customerId}:`, response);
      
      if (!response) {
        return {
          completedDays: 0,
          totalDays: 0,
          progressPercentage: 0,
          isCompleted: false
        };
      }
      
      return response;
    } catch (error) {
      console.error('Error checking course completion:', error);
      // Trả về dữ liệu mặc định khi có lỗi
      return {
        completedDays: 0,
        totalDays: 0,
        progressPercentage: 0,
        isCompleted: false
      };
    }
  },
  
  // Check if course is fully completed (all days completed including the last day)
  isLastDayCompleted: async (courseId, customerId) => {
    try {
      // First get course details with days
      const courseDetailsResponse = await request.get(`/course-day/courses/${courseId}/with-completion?customerId=${customerId}`);
      
      if (!courseDetailsResponse || !courseDetailsResponse.days || courseDetailsResponse.days.length === 0) {
        console.log('No days found for course');
        return false;
      }
      
      // Sort days by dayId to find the last day
      const sortedDays = [...courseDetailsResponse.days].sort((a, b) => b.dayId - a.dayId);
      const lastDay = sortedDays[0];
      
      // Check if the last day is completed
      return lastDay.isCompleted === true;
    } catch (error) {
      console.error('Error checking if last day is completed:', error);
      return false;
    }
  }
}; 