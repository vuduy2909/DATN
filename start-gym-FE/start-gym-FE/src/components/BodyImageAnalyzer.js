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

const BodyImageAnalyzer = ({ formData, onAnalysisComplete }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState('');
  const [waistCircumference, setWaistCircumference] = useState(formData.waistSize || '');
  const [chestCircumference, setChestCircumference] = useState(formData.chestSize || '');
  const { user } = useAuth();

  // Tạo key riêng cho từng user dựa trên userId
  const getStorageKey = () => {
    const userId = user?.id || 'anonymous';
    return `bodyImageAnalysisData_${userId}`;
  };

  // Initialize measurements from formData when component mounts or formData changes
  useEffect(() => {
    if (formData.waistSize && waistCircumference !== formData.waistSize) {
      setWaistCircumference(formData.waistSize);
    }
    if (formData.chestSize && chestCircumference !== formData.chestSize) {
      setChestCircumference(formData.chestSize);
    }
  }, [formData.waistSize, formData.chestSize]);

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
        
        // Restore analysis result
        const savedResult = {
          bodyType: analysisData.bodyType,
          analysis: analysisData.analysis,
          recommendations: analysisData.recommendations
        };
        
        setAnalysisResult(savedResult);
        
        // Restore image if available
        if (analysisData.imageBase64) {
          setPreviewUrl(analysisData.imageBase64);
        }
        
        // Update form data if measurements are available
        if (analysisData.waist_circumference && onAnalysisComplete) {
          const waistEvent = {
            target: { name: 'waistSize', value: analysisData.waist_circumference }
          };
          onAnalysisComplete(savedResult.bodyType, waistEvent);
        }
        
        if (analysisData.chest_circumference && onAnalysisComplete) {
          const chestEvent = {
            target: { name: 'chestSize', value: analysisData.chest_circumference }
          };
          onAnalysisComplete(savedResult.bodyType, chestEvent);
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

  // Load saved analysis data khi component mount (ưu tiên server, fallback localStorage)
  useEffect(() => {
    const loadAnalysisData = async () => {
      try {
        // Thử load từ server trước
        await loadSavedAnalysis();
        
        // Nếu không có từ server, fallback đến localStorage
        const storageKey = getStorageKey();
        const savedAnalysisData = localStorage.getItem(storageKey);
        if (savedAnalysisData && !analysisResult) {
          const parsedData = JSON.parse(savedAnalysisData);
          console.log('💾 Restoring from localStorage as fallback for user:', user?.id);
          
          if (parsedData.analysisResult) {
            setAnalysisResult(parsedData.analysisResult);
            console.log('✅ Restored analysis result from localStorage:', parsedData.analysisResult);
          }
          
          if (parsedData.previewUrl) {
            setPreviewUrl(parsedData.previewUrl);
            console.log('✅ Restored image preview from localStorage');
          }
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
      const dataToSave = {
        analysisResult: newAnalysisResult || analysisResult,
        previewUrl: newPreviewUrl || previewUrl,
        fileName: fileName,
        timestamp: Date.now()
      };
      
      localStorage.setItem(storageKey, JSON.stringify(dataToSave));
      console.log('💾 Saved analysis data to localStorage for user:', user?.id);
    } catch (error) {
      console.error('Error saving analysis data:', error);
    }
  };

  // Clear analysis data
  const clearAnalysisData = () => {
    try {
      const storageKey = getStorageKey();
      localStorage.removeItem(storageKey);
      
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
      
      console.log('🗑️ Cleared analysis data and reset file input for user:', user?.id);
    } catch (error) {
      console.error('Error clearing analysis data:', error);
    }
  };

  // Xử lý khi người dùng chọn file ảnh
  const handleFileChange = (event) => {
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
      
      // Tạo URL preview ảnh
      const reader = new FileReader();
      reader.onload = () => {
        const newPreviewUrl = reader.result;
        setPreviewUrl(newPreviewUrl);
        
        // Save image preview to localStorage (clear previous analysis)
        saveAnalysisData(null, newPreviewUrl, file.name);
        console.log('📷 New image loaded and preview set:', file.name);
      };
      reader.readAsDataURL(file);
    } else {
      // If no file selected, clear preview
      setPreviewUrl('');
      setSelectedFile(null);
    }
  };

  // Cập nhật waist_circumference vào formData
  const handleWaistChange = (event) => {
    const value = event.target.value;
    setWaistCircumference(value);
    
    // Immediately save to formData with correct field name
    if (onAnalysisComplete) {
      const changeEvent = {
        target: {
          name: 'waistSize',  // Use the correct field name in formData
          value: value,
        },
      };
      onAnalysisComplete(formData.bodyType, changeEvent);
    }
  };

  // Cập nhật chest_circumference vào formData
  const handleChestChange = (event) => {
    const value = event.target.value;
    setChestCircumference(value);
    
    // Immediately save to formData with correct field name
    if (onAnalysisComplete) {
      const changeEvent = {
        target: {
          name: 'chestSize',  // Use the correct field name in formData
          value: value,
        },
      };
      onAnalysisComplete(formData.bodyType, changeEvent);
    }
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

    try {
      setLoading(true);
      setError('');

      // Lấy thông tin từ formData
      const { height, weight, gender } = formData;
      if (!height || !weight || !gender) {
        throw new Error('Vui lòng điền đầy đủ thông tin chiều cao, cân nặng và giới tính trước khi phân tích ảnh');
      }

      // Kiểm tra nếu file quá lớn
      if (selectedFile.size > 5 * 1024 * 1024) { // 5MB
        setError('Kích thước ảnh quá lớn. Vui lòng chọn ảnh dưới 5MB.');
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
      
      const result = await UserFormService.analyzeBodyImage(
        selectedFile, 
        height, 
        weight, 
        gender,
        waistCircumference,
        chestCircumference,
        user?.id // Pass customerId
      );
      console.log('Received analysis result:', result);
      
      setAnalysisResult(result);
      
      // Save analysis result to localStorage
      saveAnalysisData(result, previewUrl, selectedFile?.name);
      
      console.log('=== BODY IMAGE ANALYZER: CLAUDE RESULT ===');
      console.log('Received analysis result:', result);
      console.log('Body type from Claude:', result?.bodyType);
      console.log('Current formData.bodyType:', formData.bodyType);
      
      // Cập nhật body type trong form dựa trên kết quả phân tích
      if (result && result.bodyType && result.bodyType !== 'Unknown' && onAnalysisComplete) {
        console.log('✅ Claude API returned valid body type:', result.bodyType);
        console.log('Calling onAnalysisComplete with body type:', result.bodyType);
        
        // Chuẩn bị analysisData để truyền về Step6
        const analysisDataToSend = {
          imageBase64: previewUrl ? previewUrl.split(',')[1] : null, // Extract base64 part only
          analysis: result.analysis,
          recommendations: result.recommendations,
          bodyType: result.bodyType,
          timestamp: Date.now()
        };
        
        // Call onAnalysisComplete với đầy đủ thông tin  
        onAnalysisComplete(result.bodyType, null, analysisDataToSend);
        
        // Update the measurements as well
        if (waistCircumference) {
          console.log('📏 Also updating waist circumference:', waistCircumference);
          const waistChangeEvent = {
            target: {
              name: 'waistSize',
              value: waistCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, waistChangeEvent, analysisDataToSend);
        }
        
        if (chestCircumference) {
          console.log('📏 Also updating chest circumference:', chestCircumference);
          const chestChangeEvent = {
            target: {
              name: 'chestSize',
              value: chestCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, chestChangeEvent, analysisDataToSend);
        }
        
        console.log('Body type update completed successfully');
      } else {
        console.log('❌ No valid body type from Claude API or onAnalysisComplete not available');
        console.log('Result:', result);
        console.log('Body type:', result?.bodyType);
        console.log('onAnalysisComplete:', !!onAnalysisComplete);
      }
      
      console.log('=== END BODY IMAGE ANALYZER: CLAUDE RESULT ===');
    } catch (err) {
      console.error('Failed to analyze image:', err);
      console.error('Error details:', {
        message: err.message,
        response: err.response,
        status: err.response?.status,
        data: err.response?.data,
        headers: err.response?.headers,
        config: err.config
      });
      
      if (err.response) {
        // Lỗi HTTP có phản hồi từ máy chủ
        console.log('Response status:', err.response.status);
        console.log('Response data:', err.response.data);
        
        if (err.response.status === 401) {
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
    if (!htmlContent) return '';
    
    console.log('🔍 Raw AI content:', htmlContent);
    
    // Clean up any extra spacing or unnecessary line breaks
    let cleanedContent = htmlContent
      .replace(/(\r\n|\n|\r)/gm, " ") // Replace line breaks with spaces
      .replace(/\s+/g, " ") // Replace multiple spaces with single space
      .trim();
    
    console.log('🧹 Cleaned content:', cleanedContent);
    
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
    
    // Xử lý đặc biệt - nếu không tìm thấy pattern tiêu chuẩn, thử tìm theo số thứ tự
    if (!cleanedContent.match(/(?:1\.?\s*)?(?:KHÓA\s*TẬP\s*LUYỆN|Khóa\s*học)/gi)) {
      console.log('🔄 Using fallback patterns for section detection');
      // Fallback patterns - tìm theo số thứ tự đơn giản (chỉ 4 phần)
      const fallbackPatterns = [
        { pattern: /1\.\s*/gi, replacement: '<div class="section-break"></div><div class="section-title">1. KHÓA TẬP LUYỆN:</div><div class="section-content">' },
        { pattern: /2\.\s*/gi, replacement: '</div><div class="section-break"></div><div class="section-title">2. BÀI TẬP CHỦ YẾU:</div><div class="section-content">' },
        { pattern: /3\.\s*/gi, replacement: '</div><div class="section-break"></div><div class="section-title">3. CHẾ ĐỘ DINH DƯỠNG:</div><div class="section-content">' },
        { pattern: /4\.\s*/gi, replacement: '</div><div class="section-break"></div><div class="section-title">4. TẦN SUẤT VÀ LỊCH TẬP:</div><div class="section-content">' }
      ];
      
      fallbackPatterns.forEach(({ pattern, replacement }) => {
        cleanedContent = cleanedContent.replace(pattern, replacement);
      });
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
                    type="number"
                    fullWidth
                    value={waistCircumference}
                    onChange={handleWaistChange}
                    placeholder="Nhập vòng eo (cm)"
                    variant="outlined"
                    InputProps={{
                      inputProps: { min: 0 }
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
                    type="number"
                    fullWidth
                    value={chestCircumference}
                    onChange={handleChestChange}
                    placeholder="Nhập vòng ngực (cm)"
                    variant="outlined"
                    InputProps={{
                      inputProps: { min: 0 }
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
          
          {previewUrl && (
            <Box 
              sx={{ 
                mb: 2, 
                mt: 2, 
                textAlign: 'center', 
                maxWidth: 300, 
                margin: '0 auto'
              }}
            >
              <img 
                src={previewUrl} 
                alt="Preview" 
                style={{ 
                  maxWidth: '100%', 
                  maxHeight: 300, 
                  borderRadius: 8,
                  border: '1px solid #ddd'
                }} 
              />
            </Box>
          )}
          
          {error && (
            <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
              {error}
            </Alert>
          )}
          
          {analysisResult && analysisResult.bodyType !== 'Unknown' && (
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
              {analysisResult.recommendations ? (
                <Box className="analysis-content">
                  <div dangerouslySetInnerHTML={{ __html: formatRecommendations(analysisResult.recommendations) }} />
                </Box>
              ) : (
                <Typography variant="body2">
                  Không có đề xuất tập luyện.
                </Typography>
              )}
            </Paper>
          )}
          
          <Typography variant="caption" sx={{ display: 'block', mt: 2, color: 'text.secondary' }}>
            Lưu ý: Ảnh của bạn sẽ được phân tích bởi hệ thống AI để xác định loại hình thể và đề xuất chương trình tập luyện phù hợp. 
            Thông tin và hình ảnh của bạn được bảo mật theo chính sách của chúng tôi.
          </Typography>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default BodyImageAnalyzer;
