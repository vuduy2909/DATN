import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Button, 
  CircularProgress, 
  Typography, 
  Paper, 
  Divider,
  Alert,
  Tooltip,
  IconButton,
  TextField,
  Grid,
  createTheme,
  ThemeProvider,
  InputAdornment
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import UserFormService from '../services/UserFormService';
import { useAuth } from '../AuthContext';

// Enhanced styles with more specific formatting:
const theme = createTheme({
  components: {
    MuiCssBaseline: {
      styleOverrides: `
        /* General text styling */
        .analysis-content {
          font-size: 0.95rem;
          line-height: 1.25;
        }
        
        /* Headings */
        .analysis-content h3 {
          font-size: 1.3rem;
          font-weight: bold;
          margin-top: 14px;
          margin-bottom: 6px;
          color: #1e4a76;
          border-bottom: 1px solid #ddd;
          padding-bottom: 4px;
        }
        
        /* Paragraphs */
        .analysis-content p, 
        .analysis-content div {
          font-size: 0.95rem;
          margin-top: 4px;
          margin-bottom: 6px;
          line-height: 1.25;
        }
        
        /* Lists */
        .analysis-content ul {
          padding-left: 18px;
          margin-top: 4px;
          margin-bottom: 8px;
        }
        
        .analysis-content li {
          font-size: 0.95rem;
          margin-bottom: 4px;
          line-height: 1.25;
        }
        
        /* Emphasized text */
        .analysis-content strong {
          font-weight: bold;
          color: #2a5885;
          font-size: 0.95rem;
        }
        
        /* Section titles - Styled như card headers */
        .analysis-content .section-title {
          font-weight: bold;
          font-size: 1.1rem;
          color: #ffffff;
          display: block;
          margin-top: 20px;
          margin-bottom: 8px;
          background: linear-gradient(135deg, #1e4a76 0%, #2a5885 100%);
          padding: 12px 16px;
          border-radius: 8px;
          border-left: 4px solid #FF5722;
          box-shadow: 0 2px 8px rgba(30, 74, 118, 0.2);
          text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
        }
        
        /* Section content - Styled như card body */
        .analysis-content .section-content {
          background-color: #fafbfc;
          padding: 16px;
          border-radius: 8px;
          margin-bottom: 20px;
          border: 1px solid #e1e5e9;
          line-height: 1.6;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
          position: relative;
        }
        
        /* Add subtle left border for section content */
        .analysis-content .section-content::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background: #FF5722;
          border-radius: 0 0 0 8px;
        }
        
        /* Section breaks for clear separation */
        .analysis-content .section-break {
          height: 24px;
        }
        
        /* Container formatting */
        .analysis-content .recommendation-container {
          display: flex;
          flex-direction: column;
          gap: 0px;
          padding: 16px;
          background-color: #f8f9fa;
          border-radius: 12px;
          border: 1px solid #e9ecef;
          min-height: 400px; /* Minimum height to ensure content is visible */
        }
        
        /* Exercise details */
        .analysis-content .exercise-detail {
          margin-left: 10px;
          display: block;
        }
        
        /* Spacing after br tags */
        .analysis-content br {
          display: block;
          content: "";
          margin-top: 4px;
        }
        
        /* Enhanced styling for strong elements within sections */
        .analysis-content .section-content strong {
          color: #1e4a76;
          font-weight: 600;
          background-color: rgba(30, 74, 118, 0.1);
          padding: 2px 4px;
          border-radius: 4px;
        }
        
        /* Ensure all 4 sections are visible */
        .analysis-content .recommendation-container {
          min-height: 400px; /* Minimum height to ensure content is visible */
        }
        
        /* Force section visibility */
        .analysis-content .section-title,
        .analysis-content .section-content {
          display: block !important;
          visibility: visible !important;
        }
        
        /* Debug styling to help identify missing sections */
        .analysis-content .recommendation-container::after {
          content: "";
          display: block;
          clear: both;
          height: 10px;
        }
      `,
    },
  },
});

const BodyImageAnalyzer = ({ formData, onAnalysisComplete, user: userProp }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState('');
  const [waistCircumference, setWaistCircumference] = useState('');
  const [chestCircumference, setChestCircumference] = useState('');
  const [waistError, setWaistError] = useState('');
  const [chestError, setChestError] = useState('');
  const { user: authUser } = useAuth();
  
  // Use passed user prop or fallback to auth user
  const user = userProp || authUser;
  
  // Debug logging for user data
  useEffect(() => {
    console.log('BodyImageAnalyzer: User data loaded:', {
      hasUser: !!user,
      customerId: user?.customerId,
      username: user?.username
    });
  }, [user]);

  // Initialize measurements from formData on first mount
  useEffect(() => {
    console.log('🚀 Initial mount - setting measurements from formData:', {
      waistSize: formData.waistSize,
      chestSize: formData.chestSize
    });
    
    if (formData.waistSize) {
      setWaistCircumference(formData.waistSize);
    }
    if (formData.chestSize) {
      setChestCircumference(formData.chestSize);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Empty dependency array - only run on mount

  // Tạo key riêng cho từng user dựa trên customerId
  const getStorageKey = () => {
    const userId = user?.customerId || user?.id || 'anonymous';
    return `bodyImageAnalysisData_${userId}`;
  };

  // IndexedDB utilities for storing images efficiently
  const openImageDB = () => {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('BodyImageAnalyzer', 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result);
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains('images')) {
          const store = db.createObjectStore('images', { keyPath: 'userId' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }
      };
    });
  };

  const saveImageToDB = async (file, analysisResult = null) => {
    try {
      const db = await openImageDB();
      const transaction = db.transaction(['images'], 'readwrite');
      const store = transaction.objectStore('images');
      
      const imageData = {
        userId: user?.customerId || user?.id || 'anonymous',
        file: file,
        fileName: file.name,
        fileSize: file.size,
        fileType: file.type,
        analysisResult: analysisResult,
        timestamp: Date.now()
      };
      
      await store.put(imageData);
      console.log('💾 Saved image to IndexedDB:', file.name, 'Size:', (file.size / 1024).toFixed(1) + 'KB');
      return true;
    } catch (error) {
      console.error('❌ Failed to save image to IndexedDB:', error);
      return false;
    }
  };

  const loadImageFromDB = async () => {
    try {
      const db = await openImageDB();
      const transaction = db.transaction(['images'], 'readonly');
      const store = transaction.objectStore('images');
      const userId = user?.customerId || user?.id || 'anonymous';
      const request = store.get(userId);
      
      return new Promise((resolve, reject) => {
        request.onsuccess = () => {
          const result = request.result;
          if (result && result.file) {
            console.log('✅ Loaded image from IndexedDB:', result.fileName, 'Size:', (result.fileSize / 1024).toFixed(1) + 'KB');
            resolve(result);
          } else {
            console.log('📷 No image found in IndexedDB for user:', userId);
            resolve(null);
          }
        };
        request.onerror = () => reject(request.error);
      });
    } catch (error) {
      console.error('❌ Failed to load image from IndexedDB:', error);
      return null;
    }
  };

  const clearImageFromDB = async () => {
    try {
      const db = await openImageDB();
      const transaction = db.transaction(['images'], 'readwrite');
      const store = transaction.objectStore('images');
      const userId = user?.customerId || user?.id || 'anonymous';
      await store.delete(userId);
      console.log('🗑️ Cleared image from IndexedDB for user:', userId);
      return true;
    } catch (error) {
      console.error('❌ Failed to clear image from IndexedDB:', error);
      return false;
    }
  };

  // Initialize measurements from formData when component mounts or formData changes
  // BUT only if local state is empty to avoid overriding user input
  useEffect(() => {
    console.log('🔄 FormData sync check:', {
      formDataWaist: formData.waistSize,
      localWaist: waistCircumference,
      formDataChest: formData.chestSize,
      localChest: chestCircumference
    });
    
    // Only sync from formData if local state is empty (initial load)
    if (formData.waistSize && !waistCircumference) {
      console.log('🔄 Syncing waist from formData:', formData.waistSize);
      setWaistCircumference(formData.waistSize);
    }
    if (formData.chestSize && !chestCircumference) {
      console.log('🔄 Syncing chest from formData:', formData.chestSize);
      setChestCircumference(formData.chestSize);
    }
  }, [formData.waistSize, formData.chestSize]);

  // Cleanup timeouts on unmount
  useEffect(() => {
    return () => {
      if (window.waistUpdateTimeout) {
        clearTimeout(window.waistUpdateTimeout);
      }
      if (window.chestUpdateTimeout) {
        clearTimeout(window.chestUpdateTimeout);
      }
      
      // Cleanup object URLs to prevent memory leaks
      if (previewUrl && previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
        console.log('🧹 Cleaned up object URL on unmount');
      }
    };
  }, [previewUrl]);

  // Validation functions
  const validateWaist = (value) => {
    const numValue = parseFloat(value);
    if (!value || value.trim() === '') {
      return 'Vui lòng nhập vòng eo';
    }
    if (isNaN(numValue)) {
      return 'Vòng eo phải là số';
    }
    if (numValue < 50 || numValue > 140) {
      return 'Vòng eo phải từ 50 -140 cm';
    }
    return '';
  };

  const validateChest = (value) => {
    const numValue = parseFloat(value);
    if (!value || value.trim() === '') {
      return 'Vui lòng nhập vòng ngực';
    }
    if (isNaN(numValue)) {
      return 'Vòng ngực phải là số';
    }
    if (numValue < 60 || numValue > 140) {
      return 'Vòng ngực phải từ 60 - 140 cm';
    }
    return '';
  };

  // Update formData only when valid and after user stops typing
  const updateFormData = (fieldName, value) => {
    if (onAnalysisComplete) {
      const changeEvent = {
        target: {
          name: fieldName,
          value: value,
        },
      };
      onAnalysisComplete(formData.bodyType, changeEvent);
    }
  };

  // Xử lý lỗi phân tích hình ảnh
  const handleAnalysisError = (error) => {
    console.error('Image analysis error:', error);
    let errorMessage = 'Có lỗi xảy ra trong quá trình phân tích hình ảnh';
    
    if (error.response) {
      // Error response from server
      if (error.response.status === 503) {
        errorMessage = 'Hệ thống AI đang quá tải. Vui lòng thử lại sau 1-2 phút.';
      } else if (error.response.status === 400) {
        errorMessage = error.response.data.analysis || 'Dữ liệu đầu vào không hợp lệ';
      } else if (error.response.status === 401) {
        errorMessage = 'Lỗi xác thực. Vui lòng đăng nhập lại.';
      } else if (error.response.data && error.response.data.analysis) {
        errorMessage = error.response.data.analysis;
      }
    } else if (error.request) {
      // Network error
      errorMessage = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.';
    }
    
    setError(errorMessage);
  };

  // Load kết quả phân tích đã lưu từ server
  const loadSavedAnalysis = async () => {
    if (!user?.id) {
      console.log('No user ID available to load saved analysis');
      return;
    }
    
    try {
      const response = await UserFormService.getSavedAnalysis(user.id);
      
      if (response && response.hasAnalysis && response.analysisData) {
        console.log('✅ Loaded saved analysis from server:', response.analysisData);
        
        const analysisData = response.analysisData;
        
        // Restore analysis result only if not already loaded from localStorage
        const savedResult = {
          bodyType: analysisData.bodyType,
          analysis: analysisData.analysis,
          recommendations: analysisData.recommendations
        };
        
        if (!analysisResult) {
        setAnalysisResult(savedResult);
        }
        
        // Restore image if available and not already loaded
        if (analysisData.imageBase64 && !previewUrl) {
          setPreviewUrl(analysisData.imageBase64);
        }
        
        // Update form data if measurements are available AND not already set locally
        if (analysisData.waist_circumference && !waistCircumference && onAnalysisComplete) {
          setWaistCircumference(analysisData.waist_circumference);
          const waistEvent = {
            target: { name: 'waistSize', value: analysisData.waist_circumference }
          };
          onAnalysisComplete(savedResult.bodyType, waistEvent);
          console.log('✅ Restored waist from server:', analysisData.waist_circumference);
        }
        
        if (analysisData.chest_circumference && !chestCircumference && onAnalysisComplete) {
          setChestCircumference(analysisData.chest_circumference);
          const chestEvent = {
            target: { name: 'chestSize', value: analysisData.chest_circumference }
          };
          onAnalysisComplete(savedResult.bodyType, chestEvent);
          console.log('✅ Restored chest from server:', analysisData.chest_circumference);
        }
        
        // Save to localStorage as well for backup
        saveAnalysisData(savedResult, analysisData.imageBase64);
        
      } else {
        console.log('No saved analysis found on server');
      }
    } catch (error) {
      console.error('Error loading saved analysis from server:', error);
      // Fallback to localStorage if server fails
      console.log('Falling back to localStorage...');
    }
  };

  // Load saved analysis data khi component mount (ưu tiên localStorage, backup server)
  useEffect(() => {
    const loadAnalysisData = async () => {
      try {
        // Load từ localStorage trước (nhanh hơn)
        const storageKey = getStorageKey();
        const savedAnalysisData = localStorage.getItem(storageKey);
        
        if (savedAnalysisData) {
          const parsedData = JSON.parse(savedAnalysisData);
          console.log('💾 Loading from localStorage for user:', user?.id);
          
          if (parsedData.analysisResult) {
            setAnalysisResult(parsedData.analysisResult);
            console.log('✅ Restored analysis result from localStorage:', parsedData.analysisResult);
          }
          
          // Load ảnh từ localStorage nếu có (fallback)
          if (parsedData.previewUrl) {
            setPreviewUrl(parsedData.previewUrl);
            console.log('✅ Restored image preview from localStorage:', parsedData.previewUrl.substring(0, 50) + '...');
          }
          
          // ⚠️ KHÔNG restore measurements từ localStorage để tránh override giá trị user đang nhập
          // Chỉ restore nếu chưa có giá trị nào được nhập
          if (parsedData.waistCircumference && !waistCircumference) {
            setWaistCircumference(parsedData.waistCircumference);
            console.log('✅ Restored waist measurement from localStorage:', parsedData.waistCircumference);
          }
          
          if (parsedData.chestCircumference && !chestCircumference) {
            setChestCircumference(parsedData.chestCircumference);
            console.log('✅ Restored chest measurement from localStorage:', parsedData.chestCircumference);
          }
        }
        
        // Load ảnh từ IndexedDB (ưu tiên)
        try {
          const imageData = await loadImageFromDB();
          if (imageData && imageData.file) {
            // Tạo blob URL từ file trong IndexedDB
            const blobUrl = URL.createObjectURL(imageData.file);
            setPreviewUrl(blobUrl);
            setSelectedFile(imageData.file);
            
            // Restore analysis result nếu có
            if (imageData.analysisResult && !analysisResult) {
              setAnalysisResult(imageData.analysisResult);
            }
            
            console.log('✅ Restored image and file from IndexedDB');
          }
        } catch (dbError) {
          console.warn('Could not load image from IndexedDB:', dbError);
        }
        
        // Sau đó thử sync từ server (nhưng không override localStorage nếu đã có)
        try {
          await loadSavedAnalysis();
        } catch (serverError) {
          console.warn('Could not load from server, using localStorage data:', serverError);
        }
        
      } catch (error) {
        console.error('Error loading analysis data:', error);
      }
    };
    
    if (user?.id) {
      loadAnalysisData();
    }
  }, [user?.id]);

  // Save analysis data to localStorage whenever it changes
  const saveAnalysisData = (newAnalysisResult = null, newPreviewUrl = null, fileName = null) => {
    try {
      const storageKey = getStorageKey();
      
      // Chỉ lưu blob URL (nhẹ) hoặc object URL, không lưu base64
      let urlToSave = null;
      if (newPreviewUrl && (newPreviewUrl.startsWith('blob:') || newPreviewUrl.startsWith('data:image/'))) {
        // Nếu là blob URL thì lưu, nếu là base64 quá lớn thì không lưu
        if (newPreviewUrl.startsWith('blob:') || newPreviewUrl.length < 50000) { // < 50KB
          urlToSave = newPreviewUrl;
        }
      } else if (previewUrl && (previewUrl.startsWith('blob:') || previewUrl.length < 50000)) {
        urlToSave = previewUrl;
      }
      
      const dataToSave = {
        analysisResult: newAnalysisResult || analysisResult,
        previewUrl: urlToSave, // Lưu blob URL hoặc base64 nhỏ
        fileName: fileName,
        timestamp: Date.now(),
        hasImage: !!(newPreviewUrl || previewUrl || selectedFile), // Flag để biết có ảnh
        // Lưu measurements để restore sau này (nhưng không override khi user đang nhập)
        waistCircumference: waistCircumference,
        chestCircumference: chestCircumference
      };
      
      localStorage.setItem(storageKey, JSON.stringify(dataToSave));
      console.log('💾 Saved analysis data to localStorage for user:', user?.id, 'hasImage:', dataToSave.hasImage, 'savedUrl:', !!urlToSave);
    } catch (error) {
      console.error('Error saving analysis data:', error);
    }
  };

  // Clear analysis data
  const clearAnalysisData = async () => {
    try {
      const storageKey = getStorageKey();
      localStorage.removeItem(storageKey);
      
      // Clear from IndexedDB
      await clearImageFromDB();
      
      // Also clear from server if possible
      if (user?.id) {
        try {
          await UserFormService.clearSavedAnalysis(user.id);
          console.log('🗑️ Cleared analysis data from server');
        } catch (serverError) {
          console.warn('Could not clear server data, but localStorage cleared:', serverError);
        }
      }
      
      // Clean up preview URL to prevent memory leaks
      if (previewUrl && previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
      }
      
      setAnalysisResult(null);
      setPreviewUrl('');
      setSelectedFile(null);
      setError('');
      
      // Reset file input element to allow re-uploading same image
      const fileInput = document.querySelector('#body-image-input');
      if (fileInput) {
        fileInput.value = '';
      }
      
      console.log('🗑️ Cleared analysis data from all sources and reset file input for user:', user?.id);
    } catch (error) {
      console.error('Error clearing analysis data:', error);
    }
  };

  // Xử lý khi người dùng chọn file ảnh
  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (file) {
      // Clear previous preview URL to prevent memory leaks
      if (previewUrl && previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
      }
      
      setSelectedFile(file);
      setError('');
      // Clear previous analysis when new image is selected
      setAnalysisResult(null);
      
      // Tạo URL preview ảnh - sử dụng object URL thay vì base64 để tránh lỗi
      try {
        const newPreviewUrl = URL.createObjectURL(file);
        setPreviewUrl(newPreviewUrl);
        
        // Save file to IndexedDB (ưu tiên) và localStorage (backup)
        await saveImageToDB(file);
        saveAnalysisData(null, newPreviewUrl, file.name);
        
        console.log('📷 New image loaded and saved:', file.name, 'Size:', (file.size / 1024).toFixed(1) + 'KB');
      } catch (error) {
        console.error('🚨 Failed to create preview URL:', error);
        setError('Không thể tạo preview cho ảnh đã chọn');
      }
    } else {
      // If no file selected, clear preview
      setPreviewUrl('');
      setSelectedFile(null);
    }
  };

  // Cập nhật waist_circumference vào formData
  const handleWaistChange = (event) => {
    const value = event.target.value;
    
    console.log('🔢 Waist input changed:', value);
    
    // Immediately update local state to prevent focus loss
    setWaistCircumference(value);
    
    // Clear any existing error when user starts typing
    if (waistError) {
      setWaistError('');
    }
    
    // Debounce validation and formData update
    clearTimeout(window.waistUpdateTimeout);
    window.waistUpdateTimeout = setTimeout(() => {
      const error = validateWaist(value);
      setWaistError(error);
      
      // Always update formData with the current value, even if there's an error
      // This prevents the value from being lost
      updateFormData('waistSize', value);
      
      console.log('🔢 Waist validation result:', { value, error, updated: true });
    }, 500); // Reduced to 500ms for better responsiveness
  };

  // Cập nhật chest_circumference vào formData
  const handleChestChange = (event) => {
    const value = event.target.value;
    
    console.log('🔢 Chest input changed:', value);
    
    // Immediately update local state to prevent focus loss
    setChestCircumference(value);
    
    // Clear any existing error when user starts typing
    if (chestError) {
      setChestError('');
    }
    
    // Debounce validation and formData update
    clearTimeout(window.chestUpdateTimeout);
    window.chestUpdateTimeout = setTimeout(() => {
      const error = validateChest(value);
      setChestError(error);
      
      // Always update formData with the current value, even if there's an error
      // This prevents the value from being lost
      updateFormData('chestSize', value);
      
      console.log('🔢 Chest validation result:', { value, error, updated: true });
    }, 500); // Reduced to 500ms for better responsiveness
  };

  // Helper function to update formData - keeping for backward compatibility
  const handleChange = (name, value) => {
    const fieldMap = {
      'waist_circumference': 'waistSize',
      'chest_circumference': 'chestSize'
    };
    
    const fieldName = fieldMap[name] || name;
    const changeEvent = {
      target: {
        name: fieldName,
        value: value,
      },
    };
    onAnalysisComplete(formData.bodyType, changeEvent);
  };

  // Gửi ảnh để phân tích
  const analyzeImage = async () => {
    if (!selectedFile) {
      setError('Vui lòng chọn file ảnh để phân tích');
      return;
    }
    
    // Check if user data is available
    if (!user) {
      setError('Đang tải thông tin người dùng. Vui lòng đợi và thử lại.');
      return;
    }

    // Validate measurements before analysis
    const waistValidationError = validateWaist(waistCircumference);
    const chestValidationError = validateChest(chestCircumference);
    
    if (waistValidationError) {
      setWaistError(waistValidationError);
      setError('Vui lòng nhập vòng eo hợp lệ trước khi phân tích');
      return;
    }
    
    if (chestValidationError) {
      setChestError(chestValidationError);
      setError('Vui lòng nhập vòng ngực hợp lệ trước khi phân tích');
      return;
    }

    try {
      setLoading(true);
      setError('');

      // Get customerId for API call - improved resolution logic
      let customerId = null;
      
      // Try multiple ways to get customerId based on AuthContext structure
      if (user?.customerId) {
        customerId = user.customerId;
      } else if (user?.customer?.customerId) {
        customerId = user.customer.customerId;
      } else if (user?.id) {
        customerId = user.id;
      } else if (user?.customer_id) {
        customerId = user.customer_id;
      } else if (user?.customer?.customer_id) {
        customerId = user.customer.customer_id;
      } else if (user?.userId) {
        customerId = user.userId;
      }
      
      console.log('🔍 Debug user object for customerId:', {
        user: user,
        userType: typeof user,
        userKeys: user ? Object.keys(user) : 'no user',
        customerId: user?.customerId,
        id: user?.id,
        customer_id: user?.customer_id,
        customer: user?.customer,
        customerCustomerId: user?.customer?.customerId,
        customerCustomer_id: user?.customer?.customer_id,
        userId: user?.userId,
        resolvedCustomerId: customerId
      });
      
      if (!customerId) {
        console.error('❌ No customerId found in user object:', user);
        console.error('Available user properties:', user ? Object.keys(user) : 'user is null/undefined');
        
        // Check if user is still loading
        if (!user || Object.keys(user).length === 0) {
          setError('Đang tải thông tin người dùng. Vui lòng thử lại sau vài giây.');
        } else {
          setError('Không thể xác định thông tin người dùng. Vui lòng đăng nhập lại.');
        }
        setLoading(false);
        return;
      }
      
      console.log('Using customerId for analysis:', customerId);

      // Lấy thông tin từ formData
      const { height, weight, gender } = formData;
      if (!height || !weight || !gender) {
        throw new Error('Vui lòng điền đầy đủ thông tin chiều cao, cân nặng và giới tính trước khi phân tích ảnh');
      }

      // Kiểm tra nếu file quá lớn - giảm xuống 2MB để tránh lỗi 431
      if (selectedFile.size > 2 * 1024 * 1024) { // 2MB
        setError('Kích thước ảnh quá lớn. Vui lòng chọn ảnh dưới 2MB để tránh lỗi kết nối.');
        setLoading(false);
        return;
      }

      // Gọi API phân tích ảnh
      console.log('Analyzing image with data:', { 
        height, 
        weight, 
        gender, 
        waist_circumference: waistCircumference,
        chest_circumference: chestCircumference
      });
      
      console.log('Sending image analysis request with customerId:', customerId);
      
      const result = await UserFormService.analyzeBodyImage(
        selectedFile, 
        height, 
        weight, 
        gender,
        waistCircumference,
        chestCircumference,
        customerId // Use resolved customerId
      );
      console.log('✅ Analysis completed, bodyType:', result?.bodyType);
      
      setAnalysisResult(result);
      
      // Ensure previewUrl is maintained after analysis
      let currentPreviewUrl = previewUrl;
      if (!currentPreviewUrl && selectedFile) {
        try {
          currentPreviewUrl = URL.createObjectURL(selectedFile);
          setPreviewUrl(currentPreviewUrl);
          console.log('Created new preview URL after analysis:', currentPreviewUrl);
        } catch (error) {
          console.error('Failed to create preview URL after analysis:', error);
        }
      }
      
      // Save analysis result to IndexedDB (ưu tiên) và localStorage (backup)
      console.log('🖼️ Saving analysis result, selectedFile:', selectedFile?.name);
      if (selectedFile) {
        await saveImageToDB(selectedFile, result);
      }
      saveAnalysisData(result, previewUrl, selectedFile?.name);
      
      // Đảm bảo ảnh vẫn hiển thị sau khi phân tích
      console.log('🖼️ Current previewUrl after analysis:', !!previewUrl);
      console.log('🖼️ Current selectedFile after analysis:', !!selectedFile);
      
      // Cập nhật body type trong form dựa trên kết quả phân tích
      if (result && result.bodyType && result.bodyType !== 'Unknown' && onAnalysisComplete) {
        // Chuẩn bị analysisData để truyền về Step6 - không gửi base64 để tránh lỗi 431
        const analysisDataToSend = {
          imageBase64: null, // Không gửi base64 để tránh request quá lớn
          analysis: result.analysis,
          recommendations: result.recommendations,
          bodyType: result.bodyType,
          timestamp: Date.now()
        };
        
        // Call onAnalysisComplete với đầy đủ thông tin  
        onAnalysisComplete(result.bodyType, null, analysisDataToSend);
        
        // Update the measurements as well
        if (waistCircumference) {
          const waistChangeEvent = {
            target: {
              name: 'waistSize',
              value: waistCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, waistChangeEvent, analysisDataToSend);
        }
        
        if (chestCircumference) {
          const chestChangeEvent = {
            target: {
              name: 'chestSize',
              value: chestCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, chestChangeEvent, analysisDataToSend);
        }
      }
    } catch (err) {
      console.error('Failed to analyze image:', err.message);
      
      if (err.response) {
        // Lỗi HTTP có phản hồi từ máy chủ
        console.log('Response status:', err.response.status);
        
        if (err.response.status === 431) {
          setError('Kích thước yêu cầu quá lớn. Vui lòng chọn ảnh nhỏ hơn (dưới 1MB) và thử lại.');
        } else if (err.response.status === 401) {
          setError('Lỗi xác thực: Bạn cần đăng nhập để sử dụng tính năng này.');
        } else if (err.response.status === 403) {
          setError('Bạn không có quyền truy cập tính năng này.');
        } else if (err.response.status === 400) {
          // Lỗi Bad Request - thường là lỗi về chính sách Claude hoặc định dạng ảnh
          if (err.response.data && err.response.data.analysis && 
              (err.response.data.analysis.includes('khuôn mặt') || 
               err.response.data.analysis.includes('không thể phân tích'))) {
            setError('Ảnh có thể chứa khuôn mặt. Vui lòng chọn ảnh chỉ hiển thị cơ thể (không có mặt) hoặc chọn loại hình thể thủ công.');
          } else {
            setError(err.response.data?.analysis || 'Định dạng ảnh không hợp lệ hoặc không thể phân tích.');
          }
        } else if (err.response.status === 500) {
          setError('Lỗi máy chủ: Không thể kết nối tới dịch vụ phân tích ảnh. Vui lòng thử lại sau.');
        } else if (err.response.status === 503) {
          setError('Dịch vụ phân tích ảnh tạm thời không khả dụng. Vui lòng thử lại sau hoặc chọn loại hình thể thủ công.');
        } else if (err.response.data && err.response.data.analysis) {
          setError(`${err.response.data.analysis}`);
        } else {
          setError(`Lỗi không xác định (${err.response.status}): Vui lòng thử lại sau.`);
        }
      } else {
        // Sử dụng handleAnalysisError để xử lý tất cả loại lỗi khác
        handleAnalysisError(err);
      }
    } finally {
      setLoading(false);
    }
  };

  // Format recommendations to have clear numbered sections with proper paragraph breaks
  const formatRecommendations = (htmlContent) => {
    if (!htmlContent) {
      console.log('🎯 formatRecommendations: No content provided');
      return '';
    }
    
    console.log('🎯 formatRecommendations input:', htmlContent);
    console.log('🎯 formatRecommendations input type:', typeof htmlContent);
    console.log('🎯 formatRecommendations input length:', htmlContent.length);
    
    // Không filter ra content nào cả, backend đã xử lý tốt rồi
    console.log('✅ Processing recommendation content for display');
    
    // Clean up any extra spacing or unnecessary line breaks
    let cleanedContent = htmlContent
      .replace(/(\r\n|\n|\r)/gm, " ") // Replace line breaks with spaces
      .replace(/\s+/g, " ") // Replace multiple spaces with single space
      .trim();
    
    // Regular expressions to match various forms of section titles - cập nhật để match chính xác
    const sectionPatterns = [
      // KHÓA TẬP LUYỆN patterns
      { 
        pattern: /(?:1\.?\s*)?(?:KHÓA\s*TẬP\s*LUYỆN|Khóa\s*học\s*phù\s*hợp):?/gi, 
        replacement: '<div class="section-break"></div><div class="section-title">1. KHÓA TẬP LUYỆN:</div><div class="section-content">' 
      },
      // BÀI TẬP patterns - mở rộng để match nhiều variant
      { 
        pattern: /(?:2\.?\s*)?(?:(?:5\s*)?BÀI\s*TẬP\s*(?:CHÍNH|CỤ\s*THỂ|CƠ\s*BẢN|CHỦ\s*YẾU)|5\s*bài\s*tập\s*cụ\s*thể):?/gi, 
        replacement: '</div><div class="section-break"></div><div class="section-title">2. 5 BÀI TẬP CHỦ YẾU:</div><div class="section-content">' 
      },
      // CHẾ ĐỘ DINH DƯỠNG patterns
      { 
        pattern: /(?:3\.?\s*)?(?:CHẾ\s*ĐỘ\s*DINH\s*DƯỠNG|Chế\s*độ\s*dinh\s*dưỡng):?/gi, 
        replacement: '</div><div class="section-break"></div><div class="section-title">3. CHẾ ĐỘ DINH DƯỠNG:</div><div class="section-content">' 
      },
      // TẦN SUẤT VÀ LỊCH TẬP patterns - simplified và multiple variants
      { 
        pattern: /(?:4\.?\s*)?(?:TẦN\s*SUẤT.*?LỊCH\s*TẬP|LỊCH\s*TẬP.*?TẦN\s*SUẤT|TẦN\s*SUẤT.*?tập|LỊCH\s*TẬP):?/gi, 
        replacement: '</div><div class="section-break"></div><div class="section-title">4. TẦN SUẤT VÀ LỊCH TẬP:</div><div class="section-content">' 
      }
    ];
    
    // Check how many sections we can find
    let sectionsFound = 0;
    sectionPatterns.forEach(({ pattern, replacement }, index) => {
      if (cleanedContent.match(pattern)) {
        sectionsFound++;
        console.log(`✅ Found section ${index + 1}: ${pattern}`);
      } else {
        console.log(`❌ Missing section ${index + 1}: ${pattern}`);
      }
    });
    
    console.log(`📊 Total sections found: ${sectionsFound}/4`);
    
    // Xử lý đặc biệt - nếu không tìm thấy pattern tiêu chuẩn, hiển thị raw content
    if (!cleanedContent.match(/(?:1\.?\s*)?(?:KHÓA\s*TẬP\s*LUYỆN|Khóa\s*học)/gi)) {
      console.log('🔄 No structured sections found, displaying raw content');
      // Không parse nếu không có cấu trúc rõ ràng, tránh cắt sai text
      console.log('📝 Raw content will be displayed as-is');
      
      // Chỉ format cơ bản cho raw content
      let rawFormatted = cleanedContent
        // Format exercise names
        .replace(/(Squat|Deadlift|Bench\s*Press|Pull-ups?|Push-ups?|Plank|Cable\s*Row):?\s*/gi, '<strong>$1:</strong> ')
        .replace(/(\d+\s*hiệp\s*x\s*\d+(?:-\d+)?\s*(?:lần|giây))/gi, '<strong>$1</strong>')
        // Format nutrition percentages  
        .replace(/(Protein|Carbs|Chất\s*béo):?\s*(\d+%)/gi, '<strong>$1: $2</strong>')
        .replace(/(\d+g\/kg)/gi, '<strong>$1</strong>')
        // Add line breaks for readability
        .replace(/\.\s+/g, '.<br/><br/>');
      
      console.log('🎨 Final raw formatted content:', rawFormatted);
      return `<div class="recommendation-container"><div class="raw-content">${rawFormatted}</div></div>`;
    } else {
      // Apply main section pattern replacements
      sectionPatterns.forEach(({ pattern, replacement }) => {
        cleanedContent = cleanedContent.replace(pattern, replacement);
      });
    }
    
    // Remove any references to tracking progress (section 5) that might appear in the text
    cleanedContent = cleanedContent.replace(/(?:5\.?\s*)?(?:THEO\s*DÕI\s*TIẾN\s*ĐỘ|theo\s*dõi.*?tiến\s*độ|điều\s*chỉnh.*?kế\s*hoạch):?.*?(?=(?:<div class="section-title">|$))/gi, "");
    
    // Improve formatting for exercise specifications
    let formattedContent = cleanedContent
      // Format exercise names
      .replace(/(Squat|Deadlift|Bench\s*Press|Pull-ups?|Push-ups?|Plank|Cable\s*Row):?\s*/gi, '<strong>$1:</strong> ')
      .replace(/(\d+\s*hiệp\s*x\s*\d+(?:-\d+)?\s*(?:lần|giây))/gi, '<strong>$1</strong>')
      // Format nutrition percentages
      .replace(/(Protein|Carbs|Chất\s*béo):?\s*(\d+%)/gi, '<strong>$1: $2</strong>')
      .replace(/(\d+g\/kg)/gi, '<strong>$1</strong>')
      // Add breaks for better readability
      .replace(/(-\s*)(?=[A-Z])/g, '$1<br/>');
    
    // Ensure we close the last section
    if (!formattedContent.endsWith('</div>')) {
      formattedContent += '</div>';
    }
    
    console.log('🎨 Final formatted content:', formattedContent);
    
    // Wrap the entire content in a container for styling
    return `<div class="recommendation-container">${formattedContent}</div>`;
  };

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ width: '100%' }}>
        <Box sx={{ mb: 3 }}>
          {/* Measurement fields at the very top with styled card design */}
          <Grid container spacing={2} sx={{ mb: 4, mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <Paper 
                elevation={2} 
                sx={{ 
                  p: 2, 
                  borderRadius: '8px',
                  position: 'relative',
                  overflow: 'hidden',
                  background: '#f8f8f8'
                }}
              >
                <Box 
                  sx={{ 
                    position: 'absolute', 
                    left: 0, 
                    top: 0, 
                    backgroundColor: '#3f51b5',
                    color: 'white', 
                    p: '5px 15px', 
                    borderRadius: '0 0 8px 0',
                    fontWeight: 'bold',
                    fontSize: '0.8rem'
                  }}
                >
                  WAIST
                </Box>
                <Box sx={{ pt: 3, pl: 1 }}>
                  <Typography variant="h6" sx={{ fontSize: '1.1rem', mb: 1 }}>
                    Vòng eo (cm)
                  </Typography>
                  <TextField
                    key="waist-input"
                    type="number"
                    fullWidth
                    value={waistCircumference}
                    onChange={handleWaistChange}
                    placeholder="Nhập vòng eo (cm)"
                    variant="outlined"
                    error={!!waistError}
                    helperText={waistError}
                    inputProps={{ 
                      min: 50,
                      max: 200,
                      step: "0.1"
                    }}
                    sx={{ 
                      '& .MuiOutlinedInput-root': {
                        borderRadius: '8px'
                      }
                    }}
                  />
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Paper 
                elevation={2} 
                sx={{ 
                  p: 2, 
                  borderRadius: '8px',
                  position: 'relative',
                  overflow: 'hidden',
                  background: '#f8f8f8'
                }}
              >
                <Box 
                  sx={{ 
                    position: 'absolute', 
                    left: 0, 
                    top: 0, 
                    backgroundColor: '#f50057',
                    color: 'white', 
                    p: '5px 15px', 
                    borderRadius: '0 0 8px 0',
                    fontWeight: 'bold',
                    fontSize: '0.8rem'
                  }}
                >
                  CHEST
                </Box>
                <Box sx={{ pt: 3, pl: 1 }}>
                  <Typography variant="h6" sx={{ fontSize: '1.1rem', mb: 1 }}>
                    Vòng ngực (cm)
                  </Typography>
                  <TextField
                    key="chest-input"
                    type="number"
                    fullWidth
                    value={chestCircumference}
                    onChange={handleChestChange}
                    placeholder="Nhập vòng ngực (cm)"
                    variant="outlined"
                    error={!!chestError}
                    helperText={chestError}
                    inputProps={{ 
                      min: 60,
                      max: 200,
                      step: "0.1"
                    }}
                    sx={{ 
                      '& .MuiOutlinedInput-root': {
                        borderRadius: '8px'
                      }
                    }}
                  />
                </Box>
              </Paper>
            </Grid>
          </Grid>

          <Divider sx={{ mb: 3 }} />

          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ mb: 0, mr: 1 }}>
              Phân tích ảnh cơ thể
            </Typography>
            <Tooltip title="Tải lên ảnh chỉ chứa phần cơ thể (không có khuôn mặt) để AI phân tích chính xác hơn. Hệ thống sẽ phân tích hình dáng cơ thể và đề xuất lộ trình tập luyện phù hợp.">
              <IconButton size="small" color="primary">
                <InfoIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
          
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              <strong>Lưu ý:</strong> Vui lòng tải lên ảnh <strong>CHỈ CHỨA PHẦN CƠ THỂ, KHÔNG CÓ KHUÔN MẶT</strong> để AI có thể phân tích chính xác hình thể của bạn.
            </Typography>
          </Alert>
          
          <input
            type="file"
            accept="image/*"
            id="body-image-input"
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />
          
          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <label htmlFor="body-image-input">
              <Button
                variant="outlined"
                component="span"
                color="primary"
              >
                Chọn ảnh
              </Button>
            </label>
            
            <Button
              variant="contained"
              color="primary"
              onClick={analyzeImage}
              disabled={!selectedFile || loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Phân tích'}
            </Button>

            {(previewUrl || analysisResult) && (
              <Button
                variant="outlined"
                color="secondary"
                onClick={clearAnalysisData}
                disabled={loading}
              >
                Xóa dữ liệu
              </Button>
            )}
          </Box>
          
          {(previewUrl || selectedFile) && (
            <Box sx={{ textAlign: 'center', mb: 2, mt: 2 }}>
              <Box 
                sx={{ 
                  display: 'inline-block',
                  position: 'relative',
                  zIndex: 10,
                  backgroundColor: 'white',
                  padding: 1.5,
                  borderRadius: 3,
                  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                  border: '1px solid #e0e0e0',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    boxShadow: '0 6px 20px rgba(0,0,0,0.15)',
                    transform: 'translateY(-2px)'
                  }
                }}
              >
              <img 
                src={(() => {
                  // Ưu tiên blob URL (nhẹ và persistent)
                  if (previewUrl && previewUrl.startsWith('blob:')) {
                    console.log('🖼️ Using blob URL for display');
                    return previewUrl;
                  }
                  
                  // Nếu có base64 hợp lệ (nhỏ)
                  if (previewUrl && previewUrl.startsWith('data:image/') && previewUrl.length > 100) {
                    console.log('🖼️ Using base64 URL for display');
                    return previewUrl;
                  }
                  
                  // Nếu có selectedFile, tạo blob URL mới
                  if (selectedFile) {
                    console.log('🖼️ Creating new blob URL for selectedFile');
                    try {
                      const newUrl = URL.createObjectURL(selectedFile);
                      // Cập nhật previewUrl và lưu vào localStorage
                      setPreviewUrl(newUrl);
                      saveAnalysisData(analysisResult, newUrl, selectedFile.name);
                      return newUrl;
                    } catch (error) {
                      console.error('🚨 Failed to create object URL:', error);
                      return '';
                    }
                  }
                  
                  console.log('🖼️ No valid image source available');
                  return '';
                })()} 
                alt="Preview" 
                style={{ 
                  maxWidth: 'min(400px, 90vw)',
                  maxHeight: '400px',
                  minWidth: '200px',
                  minHeight: '200px',
                  width: 'auto',
                  height: 'auto',
                  borderRadius: 8,
                  objectFit: 'contain',
                  display: 'block',
                  margin: '0 auto'
                }} 
                onError={(e) => {
                  console.error('🚨 Image load error, src:', e.target.src);
                  console.error('🚨 Attempting to recover with selectedFile');
                  
                  // Nếu có selectedFile, thử tạo URL mới
                  if (selectedFile && !e.target.dataset.recovered) {
                    try {
                      e.target.dataset.recovered = 'true'; // Prevent infinite loop
                      const recoveryUrl = URL.createObjectURL(selectedFile);
                      console.log('🔄 Created recovery URL:', recoveryUrl);
                      e.target.src = recoveryUrl;
                      setPreviewUrl(recoveryUrl);
                      // Lưu URL mới vào localStorage
                      saveAnalysisData(analysisResult, recoveryUrl, selectedFile.name);
                    } catch (error) {
                      console.error('🚨 Recovery failed:', error);
                      // Thay vì ẩn, hiển thị placeholder
                      e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkFuaCBraG9uZyB0YWkgZHVvYzwvdGV4dD48L3N2Zz4=';
                      e.target.style.opacity = '0.5';
                    }
                  } else {
                    console.error('🚨 No recovery option available');
                    // Hiển thị placeholder thay vì ẩn
                    e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkFuaCBraG9uZyB0YWkgZHVvYzwvdGV4dD48L3N2Zz4=';
                    e.target.style.opacity = '0.5';
                  }
                }}
                onLoad={(e) => {
                  console.log('✅ Image loaded successfully');
                  // Add fade-in animation when image loads
                  e.target.style.opacity = '0';
                  e.target.style.transition = 'opacity 0.3s ease-in-out';
                  setTimeout(() => {
                    e.target.style.opacity = '1';
                  }, 50);
                }}
              />
              </Box>
            </Box>
          )}
          
          {error && (
            <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
              {error}
            </Alert>
          )}
          
          {(waistError || chestError) && (
            <Alert severity="warning" sx={{ mt: 2, mb: 2 }}>
              <Typography variant="body2">
                <strong>Lưu ý:</strong> Vui lòng kiểm tra lại thông tin vòng eo và vòng ngực trước khi phân tích ảnh.
              </Typography>
            </Alert>
          )}
          
          {analysisResult && (
            <Paper 
              elevation={2} 
              sx={{ 
                p: 2, 
                mt: 2, 
                backgroundColor: '#f9f9f9',
                borderRadius: 2
              }}
            >
              <Typography variant="h6" color="primary" gutterBottom>
                Kết quả phân tích
              </Typography>
              
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Typography variant="subtitle1" fontWeight="bold" sx={{ mr: 1 }}>
                  Loại cơ thể:
                </Typography>
                <Typography variant="body1">
                  {analysisResult.bodyType || 'Không xác định'}
                </Typography>
              </Box>
              
              <Divider sx={{ my: 1 }} />
              
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                Phân tích chi tiết:
              </Typography>
              {analysisResult.analysis ? (
                <Box className="analysis-content">
                  <div dangerouslySetInnerHTML={{ __html: analysisResult.analysis }} />
                </Box>
              ) : (
                <Typography variant="body2" paragraph>
                  Không có phân tích chi tiết.
                </Typography>
              )}
              
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                Đề xuất:
              </Typography>
              {(() => {
                console.log('🎯 Rendering recommendations:', analysisResult.recommendations);
                console.log('🎯 Recommendations type:', typeof analysisResult.recommendations);
                console.log('🎯 Recommendations length:', analysisResult.recommendations?.length);
                console.log('🔍 Full analysisResult object:', analysisResult);
                
                if (!analysisResult.recommendations) {
                  console.log('❌ No recommendations available, analysisResult:', analysisResult);
                  return (
                    <Typography variant="body2" sx={{ 
                      fontStyle: 'italic', 
                      color: 'text.secondary',
                      p: 2,
                      bgcolor: '#f5f5f5',
                      borderRadius: 1,
                      border: '1px dashed #ccc'
                    }}>
                      Chưa có đề xuất tập luyện. Vui lòng thử phân tích lại hoặc kiểm tra kết nối mạng.
                    </Typography>
                  );
                }
                
                const formattedRecommendations = formatRecommendations(analysisResult.recommendations);
                console.log('🎨 Formatted recommendations result:', formattedRecommendations);
                console.log('🎨 Formatted recommendations length:', formattedRecommendations?.length);
                
                if (!formattedRecommendations || formattedRecommendations.trim() === '') {
                  console.log('⚠️ formatRecommendations returned empty, showing raw content');
                  // If formatRecommendations returns empty, show raw content
                  return (
                    <Box className="analysis-content">
                      <Typography variant="body2" component="div" sx={{ whiteSpace: 'pre-wrap' }}>
                        {analysisResult.recommendations}
                      </Typography>
                    </Box>
                  );
                }
                
                return (
                  <Box className="analysis-content">
                    <div dangerouslySetInnerHTML={{ __html: formattedRecommendations }} />
                  </Box>
                );
              })()}
            </Paper>
          )}
          
          <Typography variant="caption" sx={{ display: 'block', mt: 2, color: 'text.secondary' }}>
            Lưu ý: Ảnh của bạn sẽ được phân tích bởi hệ thống AI để xác định loại hình thể và đề xuất chương trình tập luyện phù hợp. 
            Thông tin và hình ảnh của bạn được bảo mật theo chính sách của chúng tôi.
          </Typography>
          

        </Box>
        
        {/* Add CSS styles for better recommendations display */}
        <style jsx>{`
          .analysis-content {
            line-height: 1.6;
          }
          .analysis-content .section-title {
            font-weight: bold;
            color: #1976d2;
            margin: 16px 0 8px 0;
            padding: 8px 0;
            border-bottom: 2px solid #e3f2fd;
          }
          .analysis-content .section-content {
            margin-bottom: 16px;
            padding-left: 8px;
          }
          .analysis-content .section-break {
            margin: 16px 0;
          }
          .analysis-content strong {
            color: #1565c0;
          }
          .recommendation-container {
            background: #fafafa;
            padding: 16px;
            border-radius: 8px;
            border-left: 4px solid #1976d2;
          }
          .raw-content {
            background: #f8f9fa;
            padding: 16px;
            border-radius: 8px;
            border: 1px solid #e9ecef;
            line-height: 1.8;
            font-size: 14px;
            color: #333;
          }
          .raw-content strong {
            color: #1976d2;
            font-weight: 600;
          }
        `}</style>
      </Box>
    </ThemeProvider>
  );
};

export default BodyImageAnalyzer;
