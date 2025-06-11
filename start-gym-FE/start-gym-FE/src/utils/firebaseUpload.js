import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { storage } from '../config/firebaseConfig';

/**
 * Upload file to Firebase Storage
 * @param {File} file - The file to upload
 * @param {string} path - The complete path where file should be stored
 * @returns {Promise<string>} - Download URL of uploaded file
 */
export const uploadFileToFirebase = async (file, path) => {
  try {
    console.log(`Uploading ${file.name} to Firebase Storage at: ${path}`);
    
    // Create reference
    const storageRef = ref(storage, path);
    
    // Upload file
    const snapshot = await uploadBytes(storageRef, file);
    
    // Get download URL
    const downloadURL = await getDownloadURL(snapshot.ref);
    console.log('File uploaded successfully:', downloadURL);
    
    return downloadURL;
  } catch (error) {
    console.error('Error uploading file to Firebase:', error);
    throw new Error(`Upload failed: ${error.message}`);
  }
};

/**
 * Delete file from Firebase Storage by URL
 * @param {string} downloadURL - Download URL of the file to delete
 * @returns {Promise<boolean>} - Whether deletion was successful
 */
export const deleteFileFromFirebaseByURL = async (downloadURL) => {
  try {
    if (!downloadURL || !downloadURL.includes('firebasestorage.googleapis.com')) {
      console.log('URL is not a Firebase Storage URL, skipping deletion');
      return false;
    }
    
    // Extract path from URL
    // URL format: https://firebasestorage.googleapis.com/v0/b/bucket-name/o/path%2Fto%2Ffile.ext?alt=media
    const urlParts = downloadURL.split('/o/');
    if (urlParts.length !== 2) {
      console.error('Invalid Firebase Storage URL format');
      return false;
    }
    
    const pathPart = urlParts[1].split('?')[0];
    const decodedPath = decodeURIComponent(pathPart);
    
    console.log(`Attempting to delete file from Firebase Storage: ${decodedPath}`);
    
    // Create reference and delete
    const fileRef = ref(storage, decodedPath);
    await deleteObject(fileRef);
    
    console.log('File deleted successfully from Firebase Storage');
    return true;
  } catch (error) {
    console.error('Error deleting file from Firebase Storage:', error);
    // Don't throw error, just log it since we want upload to continue even if delete fails
    return false;
  }
};

/**
 * Upload customer avatar to images/Avatar/ with auto-deletion of old avatar
 * @param {File} file - Avatar image file
 * @param {string} username - Username for filename
 * @param {string} oldAvatarUrl - URL of old avatar to delete (optional)
 * @returns {Promise<string>} - Download URL
 */
export const uploadCustomerAvatar = async (file, username, oldAvatarUrl = null) => {
  const fileExtension = file.name.split('.').pop();
  const filename = `avatar_${username}.${fileExtension}`;
  const path = `images/Avatar/${filename}`;
  
  // Delete old avatar if provided
  if (oldAvatarUrl) {
    console.log('Deleting old avatar before uploading new one...');
    await deleteFileFromFirebaseByURL(oldAvatarUrl);
  }
  
  return uploadFileToFirebase(file, path);
};

/**
 * Upload exercise video to videos/Exercise/{bodyPart}/
 * @param {File} file - Video file
 * @param {string} exerciseName - Exercise name for filename
 * @param {string} bodyPart - Body part category (Abs, Back, Biceps, etc.)
 * @returns {Promise<string>} - Download URL
 */
export const uploadExerciseVideo = async (file, exerciseName, bodyPart) => {
  const fileExtension = file.name.split('.').pop();
  
  // Sanitize exercise name for filename
  const sanitizedName = exerciseName
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '');
  
  const filename = `${sanitizedName}.${fileExtension}`;
  const path = `videos/Exercise/${bodyPart}/${filename}`;
  
  return uploadFileToFirebase(file, path);
};

/**
 * Upload course image to images/Course/{courseType}/ with auto-deletion of old image
 * @param {File} file - Image file
 * @param {string} courseName - Course name for filename
 * @param {string} courseType - Course type (1_Strength, 2_Cardiovascular, etc.)
 * @param {string} oldImageUrl - URL of old image to delete (optional)
 * @returns {Promise<string>} - Download URL
 */
export const uploadCourseImage = async (file, courseName, courseType, oldImageUrl = null) => {
  const fileExtension = file.name.split('.').pop();
  
  // Sanitize course name for filename
  const sanitizedName = courseName
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '');
  
  // Format should be course_name.extension (no UUID)
  const filename = `${sanitizedName}.${fileExtension}`;
  const path = `images/Course/${courseType}/${filename}`;
  
  // Delete old image if provided
  if (oldImageUrl) {
    console.log('Deleting old course image before uploading new one...');
    await deleteFileFromFirebaseByURL(oldImageUrl);
  }
  
  return uploadFileToFirebase(file, path);
};

/**
 * Delete file from Firebase Storage
 * @param {string} url - Download URL of file to delete
 * @returns {Promise<void>}
 */
export const deleteFileFromFirebase = async (url) => {
  try {
    // Extract path from URL
    const urlObj = new URL(url);
    const path = decodeURIComponent(urlObj.pathname.split('/o/')[1].split('?')[0]);
    
    const fileRef = ref(storage, path);
    await deleteObject(fileRef);
    console.log('File deleted successfully from Firebase');
  } catch (error) {
    console.error('Error deleting file from Firebase:', error);
    throw new Error(`Delete failed: ${error.message}`);
  }
};

/**
 * Validate file before upload
 * @param {File} file - File to validate
 * @param {string} type - 'image' or 'video'
 * @param {number} maxSizeMB - Maximum file size in MB
 * @returns {boolean} - Whether file is valid
 */
export const validateFile = (file, type = 'image', maxSizeMB = 10) => {
  if (!file) {
    throw new Error('No file provided');
  }

  // Check file size
  const maxSize = maxSizeMB * 1024 * 1024; // Convert to bytes
  if (file.size > maxSize) {
    throw new Error(`File size exceeds ${maxSizeMB}MB limit`);
  }

  // Check file type
  if (type === 'image') {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('Only JPEG, PNG, GIF, and WebP images are allowed');
    }
  } else if (type === 'video') {
    const allowedTypes = ['video/mp4', 'video/webm', 'video/ogg', 'video/avi', 'video/mov'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('Only MP4, WebM, OGG, AVI, and MOV videos are allowed');
    }
  }

  return true;
};

/**
 * Get available body parts for exercise categorization
 */
export const getBodyParts = () => [
  'Abs',
  'Back', 
  'Biceps',
  'Calves',
  'Chest',
  'Deltoid',
  'Lats',
  'Legs',
  'Triceps'
];

/**
 * Get available course types for course categorization
 */
export const getCourseTypes = () => [
  '1_Strength',
  '2_Cardiovascular', 
  '3_Flexibility',
  '4_StrengthEndur',
  '5_MindBody',
  '6_Functional',
  '7_WeightLoss'
];

export default {
  uploadFileToFirebase,
  uploadCustomerAvatar,
  uploadExerciseVideo,
  uploadCourseImage,
  deleteFileFromFirebase,
  deleteFileFromFirebaseByURL,
  validateFile,
  getBodyParts,
  getCourseTypes
}; 