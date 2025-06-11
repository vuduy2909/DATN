// FirebaseStorageService.js
// Service for uploading files to Firebase Storage via backend

import { axiosInstance } from '../utils/axiosInstance';

const FirebaseStorageService = {
  /**
   * Upload course image to Firebase Storage
   * @param {File} imageFile - The image file to upload
   * @param {Object} courseData - The course data containing course type information
   * @returns {Promise<string>} - Promise resolving to the image URL
   */
  uploadCourseImage: async (imageFile, courseData) => {
    if (!imageFile) {
      throw new Error('No image file provided');
    }

    try {
      console.log('Preparing to upload course image to Firebase:', imageFile.name);
      
      // Determine the course type folder based on course type ID
      let courseTypeFolder;
      const courseTypeId = courseData.courseTypeId || courseData.courseType?.courseTypeId;
      
      switch (parseInt(courseTypeId)) {
        case 1:
          courseTypeFolder = '1_Strength';
          break;
        case 2:
          courseTypeFolder = '2_Cardiovascular';
          break;
        case 3:
          courseTypeFolder = '3_Flexibility';
          break;
        case 4:
          courseTypeFolder = '4_StrengthEndur';
          break;
        case 5:
          courseTypeFolder = '5_MindBody';
          break;
        case 6:
          courseTypeFolder = '6_Functional';
          break;
        case 7:
          courseTypeFolder = '7_WeightLoss';
          break;
        default:
          courseTypeFolder = '1_Strength'; // Default fallback
      }
      
      // Get the file extension
      const fileExtension = imageFile.name.split('.').pop();
      
      // Sanitize course name for filename - using course_name format
      const courseName = courseData.courseName || 'course';
      const sanitizedName = courseName
        .toLowerCase()
        .replace(/[^a-z0-9]/g, '_')
        .replace(/_+/g, '_')
        .replace(/^_|_$/g, '');
      
      // Format should be course_name.extension (no random UUID)
      const filename = `${sanitizedName}.${fileExtension}`;
      
      // Create FormData for the upload
      const formData = new FormData();
      formData.append('file', imageFile);
      formData.append('courseType', courseTypeFolder);
      formData.append('courseName', sanitizedName); // Send sanitized course name
      formData.append('filename', filename); // Add the specific filename to use
      
      console.log(`Uploading to course type folder: ${courseTypeFolder} with filename: ${filename}`);
      
      // Call the backend API to handle the Firebase upload
      // Remove the /api/v1 prefix since axiosInstance already has it in baseURL
      const response = await axiosInstance.post('/admin/courses/upload-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      console.log('Image upload successful:', response.data);
      return response.data.imageUrl;
    } catch (error) {
      console.error('Error uploading course image:', error);
      throw error;
    }
  }
};

export default FirebaseStorageService; 