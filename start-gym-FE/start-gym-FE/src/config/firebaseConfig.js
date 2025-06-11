import { initializeApp } from 'firebase/app';
import { getStorage } from 'firebase/storage';

// Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyAw8T4U-bh7hABUtLN3sjnm8rntAh5G9Vs", // Cập nhật API key thực tế
  authDomain: "capstone-project-2909.firebaseapp.com",
  projectId: "capstone-project-2909",
  storageBucket: "capstone-project-2909.appspot.com",
  messagingSenderId: "101205198065409096792", // Từ client_id trong service account
  appId: "1:101205198065409096792:web:abcdef123456789012345678" // Cần lấy giá trị thực từ Firebase console
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Cloud Storage and get a reference to the service
export const storage = getStorage(app);

export default app; 