import { request } from "../utils/axiosInstance";

export const userService = {
  // Get user data for a specific customer
  getUserData: async (customerId) => {
    try {
      const response = await request.get(`/track-data-ai/user-data/${customerId}`);
      return response;
    } catch (error) {
      console.error('Error getting user data:', error);
      throw error;
    }
  },
  
  // Get customer profile details
  getCustomerDetails: async () => {
    try {
      const response = await request.get('/customer/detail');
      return response;
    } catch (error) {
      console.error('Error getting customer details:', error);
      throw error;
    }
  },
  
  // Update customer profile
  updateCustomerProfile: async (customerData) => {
    try {
      const response = await request.put('/customer/update', customerData);
      return response;
    } catch (error) {
      console.error('Error updating customer profile:', error);
      throw error;
    }
  },
  
  // Get AI user data for recommendations
  getUserDataAi: async (userDataId) => {
    try {
      if (!userDataId) {
        throw new Error('userDataId is required');
      }
      const response = await request.get(`/track-data-ai/recommendation-details/${userDataId}`);
      return response;
    } catch (error) {
      console.error('Error getting user AI data:', error);
      throw error;
    }
  },
  
  // Get AI recommendation data for a customer
  getCustomerRecommendationData: async (customerId) => {
    try {
      if (!customerId) {
        throw new Error('customerId is required');
      }
      const response = await request.get(`/track-data-ai/customer-recommendations/${customerId}`);
      return response;
    } catch (error) {
      console.error('Error getting customer recommendation data:', error);
      throw error;
    }
  }
}; 