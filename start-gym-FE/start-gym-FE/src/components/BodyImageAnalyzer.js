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
  ThemeProvider
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import UserFormService from '../services/UserFormService';

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
        
        /* Section titles */
        .analysis-content .section-title {
          font-weight: bold;
          font-size: 1.05rem;
          color: #1e4a76;
          display: block;
          margin-top: 12px;
          margin-bottom: 4px;
          background-color: #f2f6fa;
          padding: 4px 8px;
          border-radius: 4px;
        }
        
        /* Container formatting */
        .analysis-content .recommendation-container {
          display: flex;
          flex-direction: column;
          gap: 6px;
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

  // Initialize measurements from formData when component mounts or formData changes
  useEffect(() => {
    if (formData.waistSize && waistCircumference !== formData.waistSize) {
      setWaistCircumference(formData.waistSize);
    }
    if (formData.chestSize && chestCircumference !== formData.chestSize) {
      setChestCircumference(formData.chestSize);
    }
  }, [formData.waistSize, formData.chestSize]);

  // Load saved analysis data when component mounts
  useEffect(() => {
    try {
      const savedAnalysisData = localStorage.getItem('bodyImageAnalysisData');
      if (savedAnalysisData) {
        const parsedData = JSON.parse(savedAnalysisData);
        console.log('💾 Restoring saved analysis data:', parsedData);
        
        // Restore analysis result
        if (parsedData.analysisResult) {
          setAnalysisResult(parsedData.analysisResult);
          console.log('✅ Restored analysis result:', parsedData.analysisResult);
        }
        
        // Restore image preview
        if (parsedData.previewUrl) {
          setPreviewUrl(parsedData.previewUrl);
          console.log('✅ Restored image preview');
        }
        
        // Restore file info if available
        if (parsedData.fileName) {
          console.log('📄 Had saved file:', parsedData.fileName);
        }
      }
    } catch (error) {
      console.error('Error loading saved analysis data:', error);
    }
  }, []);

  // Save analysis data to localStorage whenever it changes
  const saveAnalysisData = (newAnalysisResult = null, newPreviewUrl = null, fileName = null) => {
    try {
      const dataToSave = {
        analysisResult: newAnalysisResult || analysisResult,
        previewUrl: newPreviewUrl || previewUrl,
        fileName: fileName,
        timestamp: Date.now()
      };
      
      localStorage.setItem('bodyImageAnalysisData', JSON.stringify(dataToSave));
      console.log('💾 Saved analysis data to localStorage');
    } catch (error) {
      console.error('Error saving analysis data:', error);
    }
  };

  // Clear analysis data
  const clearAnalysisData = () => {
    try {
      localStorage.removeItem('bodyImageAnalysisData');
      setAnalysisResult(null);
      setPreviewUrl('');
      setSelectedFile(null);
      setError('');
      console.log('🗑️ Cleared analysis data');
    } catch (error) {
      console.error('Error clearing analysis data:', error);
    }
  };

  // Xử lý khi người dùng chọn file ảnh
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
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
      };
      reader.readAsDataURL(file);
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
      
      // Create FormData with all parameters
      const formDataToSend = new FormData();
      formDataToSend.append('image', selectedFile);
      formDataToSend.append('height', height);
      formDataToSend.append('weight', weight);
      formDataToSend.append('gender', gender);
      
      // Add new measurements if provided
      if (waistCircumference) {
        formDataToSend.append('waist_circumference', waistCircumference);
      }
      
      if (chestCircumference) {
        formDataToSend.append('chest_circumference', chestCircumference);
      }
      
      const result = await UserFormService.analyzeBodyImage(
        selectedFile, 
        height, 
        weight, 
        gender,
        waistCircumference,
        chestCircumference
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
        
        // FIXED: Call onAnalysisComplete with proper bodyType parameter  
        onAnalysisComplete(result.bodyType);
        
        // Update the measurements as well
        if (waistCircumference) {
          console.log('📏 Also updating waist circumference:', waistCircumference);
          const waistChangeEvent = {
            target: {
              name: 'waistSize',
              value: waistCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, waistChangeEvent);
        }
        
        if (chestCircumference) {
          console.log('📏 Also updating chest circumference:', chestCircumference);
          const chestChangeEvent = {
            target: {
              name: 'chestSize',
              value: chestCircumference,
            },
          };
          onAnalysisComplete(result.bodyType, chestChangeEvent);
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
      
      if (err.response) {
        // Lỗi HTTP có phản hồi từ máy chủ
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
      } else if (err.request) {
        // Yêu cầu đã được gửi nhưng không nhận được phản hồi
        setError('Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng.');
      } else {
        // Lỗi khác
        setError(err.message || 'Không thể phân tích ảnh. Vui lòng thử lại sau.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Format recommendations to have clear numbered sections
  const formatRecommendations = (htmlContent) => {
    if (!htmlContent) return '';
    
    // Clean up any extra spacing or unnecessary line breaks
    let cleanedContent = htmlContent
      .replace(/(\r\n|\n|\r)/gm, " ") // Replace line breaks with spaces
      .replace(/\s+/g, " ") // Replace multiple spaces with single space
      .trim();
    
    // Regular expressions to match various forms of section titles
    const sectionPatterns = [
      // KHÓA TẬP LUYỆN patterns
      { 
        pattern: /(?:1\.?\s*)?KHÓA\s*TẬP\s*LUYỆN:?|(?:1\.?\s*)?Khóa\s*tập\s*luyện:?/gi, 
        replacement: '<span class="section-title">1. KHÓA TẬP LUYỆN:</span>' 
      },
      // BÀI TẬP CHÍNH patterns
      { 
        pattern: /(?:2\.?\s*)?(?:5\s*)?BÀI\s*TẬP\s*CHÍNH:?|(?:2\.?\s*)?(?:5\s*)?Bài\s*tập\s*chính:?/gi, 
        replacement: '<span class="section-title">2. 5 BÀI TẬP CHÍNH:</span>' 
      },
      // CHẾ ĐỘ DINH DƯỠNG patterns
      { 
        pattern: /(?:3\.?\s*)?CHẾ\s*ĐỘ\s*DINH\s*DƯỠNG:?|(?:3\.?\s*)?Chế\s*độ\s*dinh\s*dưỡng:?/gi, 
        replacement: '<span class="section-title">3. CHẾ ĐỘ DINH DƯỠNG:</span>' 
      },
      // LỊCH TẬP VÀ NGHỈ NGƠI patterns - combined into one title
      { 
        pattern: /(?:4\.?\s*)?LỊCH\s*TẬP:?(?:\s*VÀ\s*NGHỈ\s*NGƠI:?)?|(?:4\.?\s*)?Lịch\s*tập:?(?:\s*và\s*nghỉ\s*ngơi:?)?|(?:4\.?\s*)?TẦN\s*SUẤT\s*TẬP:?/gi, 
        replacement: '<span class="section-title">4. LỊCH TẬP VÀ NGHỈ NGƠI:</span>' 
      }
    ];
    
    // Also remove any references to THEO DÕI TIẾN ĐỘ that might appear in the text
    cleanedContent = cleanedContent.replace(/(?:5\.?\s*)?THEO\s*DÕI\s*TIẾN\s*ĐỘ:?.*?(?=(?:<span class="section-title">|$))/gi, "");
    
    // Apply all section pattern replacements
    let formattedContent = cleanedContent;
    sectionPatterns.forEach(({ pattern, replacement }) => {
      formattedContent = formattedContent.replace(pattern, replacement);
    });
    
    // Clean up any separate "VÀ NGHỈ NGƠI:" text that might appear after LỊCH TẬP section title
    formattedContent = formattedContent.replace(/(<span class="section-title">4\. LỊCH TẬP VÀ NGHỈ NGƠI:<\/span><br\/>)(?:\s*VÀ\s*NGHỈ\s*NGƠI:?)/gi, '$1');
    
    // Improve formatting for bench press, pull-ups, etc.
    formattedContent = formattedContent
      // Format exercise specifications
      .replace(/(Bench\s*Press:)/gi, '<strong>$1</strong>')
      .replace(/(Pull-ups:)/gi, '<strong>$1</strong>')
      .replace(/(Shoulder\s*Press:)/gi, '<strong>$1</strong>')
      .replace(/(Squat:)/gi, '<strong>$1</strong>')
      .replace(/(Dead-?lift:)/gi, '<strong>$1</strong>')
      // Highlight reps and sets
      .replace(/(\d+\s*hiệp\s*x\s*\d+(?:-\d+)?\s*lần)/gi, '<strong>$1</strong>')
      // Format protein and nutrient percentages
      .replace(/(Protein:\s*\d+g)/gi, '<strong>$1</strong>')
      .replace(/(\d+g\/kg)/gi, '<strong>$1</strong>')
      .replace(/(\d+-\d+%\s*tổng\s*calories)/gi, '<strong>$1</strong>')
      // Add line breaks after sections to ensure proper spacing
      .replace(/<\/span>/g, '</span><br/>')
      // Add line breaks after bullet points for better spacing
      .replace(/<\/li>/g, '</li><br/>');
    
    // Improve formatting for exercise details and create more structured content
    formattedContent = formattedContent
      // Add spacing and structure to exercise details
      .replace(/(<strong>.*?<\/strong>)/g, '<span class="exercise-detail">$1</span>')
      // Remove excessive line breaks
      .replace(/<br\/><br\/>/g, '<br/>')
      // Ensure proper spacing between sections
      .replace(/<span class="section-title">/g, '</div><div class="recommendation-section"><span class="section-title">');
    
    // Wrap the entire content in a container for styling and close any open divs
    return `<div class="recommendation-container"><div class="recommendation-section">${formattedContent}</div></div>`;
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
