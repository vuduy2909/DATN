import React, { useState } from 'react';
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
  Grid
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import UserFormService from '../services/UserFormService';

const BodyImageAnalyzer = ({ formData, onAnalysisComplete }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState('');
  const [waistCircumference, setWaistCircumference] = useState('');
  const [chestCircumference, setChestCircumference] = useState('');

  // Xử lý khi người dùng chọn file ảnh
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setError('');
      setAnalysisResult(null);
      
      // Tạo URL preview ảnh
      const reader = new FileReader();
      reader.onload = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // Cập nhật waist_circumference vào formData
  const handleWaistChange = (event) => {
    const value = event.target.value;
    setWaistCircumference(value);
    if (onAnalysisComplete) {
      handleChange('waist_circumference', value);
    }
  };

  // Cập nhật chest_circumference vào formData
  const handleChestChange = (event) => {
    const value = event.target.value;
    setChestCircumference(value);
    if (onAnalysisComplete) {
      handleChange('chest_circumference', value);
    }
  };

  // Helper function to update formData
  const handleChange = (name, value) => {
    const changeEvent = {
      target: {
        name: name,
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
      
      // Cập nhật body type trong form dựa trên kết quả phân tích
      if (result && result.bodyType && result.bodyType !== 'Unknown' && onAnalysisComplete) {
        onAnalysisComplete(result.bodyType);
        
        // Update the measurements as well
        if (waistCircumference) {
          handleChange('waist_circumference', waistCircumference);
        }
        
        if (chestCircumference) {
          handleChange('chest_circumference', chestCircumference);
        }
      }
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

  return (
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
          <Typography variant="body2" paragraph>
            {analysisResult.analysis || 'Không có phân tích chi tiết.'}
          </Typography>
          
          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
            Đề xuất:
          </Typography>
          <Typography variant="body2">
            {analysisResult.recommendations || 'Không có đề xuất tập luyện.'}
          </Typography>
        </Paper>
      )}
      
      <Typography variant="caption" sx={{ display: 'block', mt: 2, color: 'text.secondary' }}>
        Lưu ý: Ảnh của bạn sẽ được phân tích bởi hệ thống AI để xác định loại hình thể và đề xuất chương trình tập luyện phù hợp. 
        Thông tin và hình ảnh của bạn được bảo mật theo chính sách của chúng tôi.
      </Typography>
    </Box>
  );
};

export default BodyImageAnalyzer;
