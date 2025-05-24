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
      const response = await request.get('/customer-course/recommended');
      return response;
    } catch (error) {
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
  }
}; 