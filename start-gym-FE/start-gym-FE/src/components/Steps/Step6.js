import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Container,
    Paper,
    Typography,
    Stepper,
    Step,
    StepLabel,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Grid,
    Divider,
    Alert,
    CircularProgress,
    Chip,
    Avatar,
    Card,
    CardContent
} from '@mui/material';
import {
    CameraAlt,
    ArrowBack,
    CheckCircle,
    SmartToy
} from '@mui/icons-material';
import BodyImageAnalyzer from '../BodyImageAnalyzer';
import UserFormService from '../../services/UserFormService';
import { useAuth } from '../../AuthContext';

const Step6 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const { user } = useAuth();
    const [submitting, setSubmitting] = useState(false);
    const [analysisTimestamp, setAnalysisTimestamp] = useState(Date.now()); // Force re-render when analysis updates

    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];
    
    // Helper để lấy key localStorage riêng cho từng user cho kết quả phân tích
    const getAnalysisStorageKey = () => {
        const userId = user?.id || user?.customerId || user?.username || 'anonymous';
        return `analysisResult_${userId}`;
    };

    // Helper để lấy key localStorage riêng cho từng user cho ảnh phân tích
    const getImageStorageKey = () => {
        const userId = user?.id || user?.customerId || user?.username || 'anonymous';
        return `analysisImage_${userId}`;
    };

    // Load saved analysis result và ảnh khi component mount
    useEffect(() => {
        try {
            // Load analysis result
            const analysisKey = getAnalysisStorageKey();
            const savedAnalysis = localStorage.getItem(analysisKey);
            if (savedAnalysis) {
                const parsed = JSON.parse(savedAnalysis);
                // Gọi handleChange để cập nhật lại formData nếu cần
                if (parsed && parsed.bodyType && parsed.bodyType !== formData.bodyType) {
                    handleChange({ target: { name: 'bodyType', value: parsed.bodyType } });
                }
                if (parsed && parsed.comment && parsed.comment !== formData.comment) {
                    handleChange({ target: { name: 'comment', value: parsed.comment } });
                }
                if (parsed && parsed.recommendation && parsed.recommendation !== formData.recommendation) {
                    handleChange({ target: { name: 'recommendation', value: parsed.recommendation } });
                }
                setAnalysisTimestamp(Date.now());
            }

            // Load saved image
            const imageKey = getImageStorageKey();
            const savedImageData = localStorage.getItem(imageKey);
            if (savedImageData) {
                const imageData = JSON.parse(savedImageData);
                // Set saved image data để component BodyImageAnalyzer có thể sử dụng
                if (window.setSavedAnalysisImage) {
                    window.setSavedAnalysisImage(imageData);
                }
            }
        } catch (e) {
            console.error('Error loading analysis data from localStorage:', e);
        }
    }, [user]);

    // Xử lý khi phân tích ảnh hoàn tất hoặc khi các trường đo lường được cập nhật
    const handleAnalysisComplete = (bodyType, measurementChange = null, analysisData = null) => {
        console.log('=== STEP6: ANALYSIS COMPLETE ===');
        console.log('Body type received:', bodyType);
        console.log('Analysis data:', analysisData);
        
        // Update body type - ưu tiên kết quả từ Claude API
        if (bodyType && bodyType !== 'Unknown' && typeof bodyType === 'string') {
            console.log('✅ Updating body type from analysis:', bodyType);
            
            // Ensure the bodyType is properly updated in formData
            handleChange({
                target: {
                    name: 'bodyType',
                    value: bodyType.trim(),
                },
            });
            
            // Force re-render of current data display
            setAnalysisTimestamp(Date.now());
        }
        
        // If we have measurement changes, update those too
        if (measurementChange && measurementChange.target) {
            console.log('📏 Updating measurements:', measurementChange);
            handleChange(measurementChange);
            setAnalysisTimestamp(Date.now());
        }

        // Lưu kết quả phân tích vào localStorage theo user
        try {
            const analysisKey = getAnalysisStorageKey();
            const dataToSave = {
                bodyType: bodyType || formData.bodyType,
                comment: analysisData?.analysis || formData.comment,
                recommendation: analysisData?.recommendations || formData.recommendation,
                timestamp: Date.now()
            };
            localStorage.setItem(analysisKey, JSON.stringify(dataToSave));
            console.log('✅ Analysis result saved to localStorage');
        } catch (e) {
            console.error('Error saving analysis result to localStorage:', e);
        }

        // Lưu ảnh phân tích vào localStorage theo user (nếu có)
        if (analysisData && analysisData.imageBase64) {
            try {
                const imageKey = getImageStorageKey();
                const imageDataToSave = {
                    imageBase64: analysisData.imageBase64,
                    bodyType: bodyType,
                    analysis: analysisData.analysis,
                    recommendations: analysisData.recommendations,
                    timestamp: Date.now()
                };
                localStorage.setItem(imageKey, JSON.stringify(imageDataToSave));
                console.log('✅ Analysis image saved to localStorage');
            } catch (e) {
                console.error('Error saving analysis image to localStorage:', e);
            }
        }
        
        console.log('=== END STEP6: ANALYSIS COMPLETE ===');
    };

    // Hàm để xóa ảnh và kết quả phân tích đã lưu
    const clearSavedAnalysis = () => {
        try {
            const analysisKey = getAnalysisStorageKey();
            const imageKey = getImageStorageKey();
            localStorage.removeItem(analysisKey);
            localStorage.removeItem(imageKey);
            
            // Reset formData
            handleChange({ target: { name: 'bodyType', value: '' } });
            handleChange({ target: { name: 'comment', value: '' } });
            handleChange({ target: { name: 'recommendation', value: '' } });
            
            setAnalysisTimestamp(Date.now());
            console.log('✅ Cleared saved analysis data');
        } catch (e) {
            console.error('Error clearing saved analysis:', e);
        }
    };

    // Hàm để lấy ảnh đã lưu
    const getSavedAnalysisImage = () => {
        try {
            const imageKey = getImageStorageKey();
            const savedImageData = localStorage.getItem(imageKey);
            if (savedImageData) {
                return JSON.parse(savedImageData);
            }
        } catch (e) {
            console.error('Error getting saved analysis image:', e);
        }
        return null;
    };

    const handleSubmit = async () => {
        // Prevent double submission
        if (submitting) return;
        
        setSubmitting(true);
        console.log('Step6: handleSubmit called');
        
        console.log('Submitting form with body analysis data:', {
            bodyType: formData.bodyType,
            waist_circumference: formData.waistSize,
            chest_circumference: formData.chestSize
        });
        
        try {
            // Simplified and more reliable mapping function
            const mapTrainingFrequency = (value) => {
                if (!value) return 'Moderate'; // Default to exact enum value
                
                // Ensure clean string before mapping (no extra spaces)
                const valueStr = String(value).trim();
                
                // Direct mapping to enum values - no case conversion to avoid issues
                if (valueStr === 'Never') return 'Never';
                if (valueStr === 'Occasionally') return 'Occasionally';
                if (valueStr === 'Moderate') return 'Moderate';
                if (valueStr === 'Regularly') return 'Regularly';
                if (valueStr === 'Advanced') return 'Advanced';
                
                // Legacy fallback mapping if values come in different formats
                const valueLower = valueStr.toLowerCase();
                if (valueLower.includes("never")) return 'Never';
                if (valueLower.includes("occasion")) return 'Occasionally';
                if (valueLower.includes("moderate")) return 'Moderate';
                if (valueLower.includes("regular")) return 'Regularly';
                if (valueLower.includes("advanced")) return 'Advanced';
                
                // Numeric value mapping for backwards compatibility
                if (valueLower.includes("1")) return 'Occasionally';
                if (valueLower.includes("2")) return 'Moderate';
                if (valueLower.includes("3")) return 'Regularly';
                if (valueLower.includes("4") || valueLower.includes("5")) return 'Advanced';
                
                // Default to exact enum value
                return 'Moderate';
            };
            
            // Map occupation values to database ENUM values
            // ENUM('Student','Office worker','Delivery staff','Lecturer','Service staff','Other')
            const occupationMap = {
                'Sinh viên': 'Student',
                'Nhân viên văn phòng': 'Office worker',
                'Nhân viên giao hàng': 'Delivery staff',
                'Giảng viên': 'Lecturer',
                'Nhân viên dịch vụ': 'Service staff',
                'Khác': 'Other'
            };
            
            // Map work environment values to database ENUM values
            // ENUM('Office','Shift work','Standing-intensive','Walking-intensive','Driving-intensive','Manual labor')
            const workEnvironmentMap = {
                'Văn phòng': 'Office',
                'Làm theo ca': 'Shift work',
                'Đứng nhiều': 'Standing-intensive',
                'Đi lại nhiều': 'Walking-intensive',
                'Lái xe nhiều': 'Driving-intensive',
                'Lao động chân tay': 'Manual labor'
            };
            
            // Map training goals values to database ENUM values
            // ENUM('1, Strength Training','2, Cardiovascular Training','3, Flexibility and Balance',
            //      '4, Train strength and endurance','5, Mind-Body Wellness','6, Functional Training','7, Weight Loss')
            const trainingGoalMap = {
                'Giảm cân tổng thể': '7, Weight Loss',
                'Giảm mỡ, giữ cơ': '7, Weight Loss',
                'Tăng cơ bắp': '1, Strength Training',
                'Tăng sức mạnh': '1, Strength Training',
                'Cải thiện sức chịu đựng': '2, Cardiovascular Training',
                'Tăng tính linh hoạt': '3, Flexibility and Balance',
                'Duy trì vóc dáng': '6, Functional Training'
            };

            // Prepare API data format
            const apiFormData = {
                // Apply trim() to ensure no whitespace issues for ENUM fields
                training_history: mapTrainingFrequency(formData.trainingFrequency).trim(),
                training_frequency: mapTrainingFrequency(formData.desiredFrequency).trim(),
                age: formData.age,
                gender: formData.gender,
                height: formData.height,
                weight: formData.weight,
                bmi: Math.round((formData.weight / Math.pow(formData.height/100, 2)) * 100) / 100, // Tính BMI và làm tròn 2 chữ số thập phân
                body_type: formData.bodyType || 'Normal',
                training_goals: (trainingGoalMap[formData.trainingGoal] || '7, Weight Loss').trim(),
                occupation: (occupationMap[formData.occupation] || 'Student').trim(),
                work_environment: (workEnvironmentMap[formData.workEnvironment] || 'Office').trim(),
                chest_circumference: formData.chestSize, 
                waist_circumference: formData.waistSize,
                customerId: formData.customerId
            };
            
            // Ensure that any direct trainingFrequency properties are not included
            // The backend is only expecting training_history and training_frequency
            if (apiFormData.hasOwnProperty('trainingFrequency')) {
                delete apiFormData.trainingFrequency;
            }
            
            console.log('=== STEP6: API DATA PREPARATION ===');
            console.log('FormData.bodyType from form:', formData.bodyType);
            console.log('Body type in API data:', apiFormData.body_type);
            console.log('Calling API directly from Step6 with data:', apiFormData);
            console.log('Original training history:', formData.trainingFrequency);
            console.log('Mapped training history:', apiFormData.training_history);
            console.log('Original desired frequency:', formData.desiredFrequency);
            console.log('Mapped training frequency:', apiFormData.training_frequency);
            console.log('Training frequency char codes:', [...apiFormData.training_frequency].map(c => c.charCodeAt(0)));
            console.log('Original occupation:', formData.occupation);
            console.log('Mapped occupation:', apiFormData.occupation);
            console.log('Original work environment:', formData.workEnvironment);
            console.log('Mapped work environment:', apiFormData.work_environment);
            console.log('Original training goal:', formData.trainingGoal);
            console.log('Mapped training goal:', apiFormData.training_goals);
            console.log('Database enum values must match exactly what is defined in the schema');
            console.log('Check for: 1. Exact case matching 2. No whitespace 3. No special characters');
            
            // Log complete API request data with all fields
            console.log('=== FINAL API REQUEST DATA ===');
            console.log('API endpoint: /track-data-ai/recommend-course');
            console.log('Request method: POST');
            console.log('Complete request data:', JSON.stringify(apiFormData));
            console.log('Expected backend fields only:', Object.keys(apiFormData).join(', '));
            console.log('=== END API REQUEST DATA ===');
            
            // Call API directly from here
            const result = await UserFormService.submitFormData(apiFormData);
            console.log('Step6: API response received:', result);
            
            // Make sure the result has the necessary properties for step 7
            if (result && result.recommendedCourses) {
                // Ensure recommendedCourseId is explicitly set in both formData and at top level
                if (!result.formData) {
                    result.formData = { ...apiFormData };
                }
                
                if (result.recommendedCourses.courseId) {
                    // Add recommendedCourseId to formData object
                    result.formData.recommendedCourseId = result.recommendedCourses.courseId;
                    // Also add it at the top level for consistent access
                    result.recommendedCourseId = result.recommendedCourses.courseId;
                    
                    console.log('Step6: Added recommendedCourseId to result:', result.recommendedCourses.courseId);
                }
            }
            
            // IMMEDIATELY navigate to next step
            console.log('Step6: Immediately navigating to next step');
            handleNext(result);
            
            // Find and dismiss any dialogs instantly
            const buttons = document.querySelectorAll('button');
            for (let button of buttons) {
                if (button.textContent === 'OK') {
                    console.log('Found and clicking OK button');
                    button.click();
                    break;
                }
            }
        } catch (error) {
            console.error('Error submitting form:', error);
            // Even on error, try to navigate to next step
            handleNext();
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Box sx={{ 
            backgroundColor: '#ffffff', 
            minHeight: '100vh',
            py: 2,
            background: 'linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%)'
        }}>
            <Container maxWidth="md" sx={{ py: 4 }}>
                {/* Material UI Stepper */}
                <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 3 }}>
                    <Stepper activeStep={5} alternativeLabel>
                        {steps.map((label, index) => (
                            <Step key={label}>
                                <StepLabel>
                                    <Typography variant="caption" sx={{ fontSize: '0.75rem' }}>
                                        {label}
                                    </Typography>
                                </StepLabel>
                            </Step>
                        ))}
                    </Stepper>
                </Paper>

                {/* Main Content */}
                <Paper elevation={4} sx={{ p: 4, borderRadius: 4, background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)' }}>
                    {/* Header */}
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        <Avatar sx={{ 
                            bgcolor: '#CF4044', 
                            width: 80, 
                            height: 80, 
                            mx: 'auto', 
                            mb: 2,
                            boxShadow: '0 8px 24px rgba(207, 64, 68, 0.3)'
                        }}>
                            <SmartToy sx={{ fontSize: 40 }} />
                        </Avatar>
                        
                        <Typography variant="h4" sx={{ 
                            fontWeight: 'bold', 
                            color: '#2c3e50', 
                            mb: 1,
                            background: 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)',
                            backgroundClip: 'text',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                        }}>
                            AI Phân tích cơ thể
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Sử dụng AI để phân tích thông số cơ thể và tạo chương trình tối ưu
                        </Typography>
                        
                        <Chip 
                            label="Bước 6/7" 
                            color="primary" 
                            sx={{ 
                                mt: 2, 
                                bgcolor: '#CF4044',
                                fontWeight: 'bold'
                            }} 
                        />
                    </Box>

                    {/* AI Analysis Section */}
                    <Card elevation={3} sx={{ mb: 4, borderRadius: 3, border: '2px solid #e3f2fd' }}>
                        <CardContent sx={{ p: 3 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Avatar sx={{ bgcolor: '#2196f3', mr: 2, width: 50, height: 50 }}>
                                    <CameraAlt />
                                </Avatar>
                                <Box>
                                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                        Phân tích hình ảnh thông minh
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: '#7f8c8d' }}>
                                        Upload ảnh để AI phân tích dáng người và đưa ra khuyến nghị
                                    </Typography>
                                </Box>
                            </Box>

                            <Divider sx={{ my: 3 }} />
                            
                            {/* Hiển thị ảnh đã lưu nếu có */}
                            {(() => {
                                const savedImage = getSavedAnalysisImage();
                                return savedImage ? (
                                    <Card elevation={2} sx={{ mb: 4, borderRadius: 3, bgcolor: '#f8f9fa' }}>
                                        <CardContent sx={{ p: 3 }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                                                <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                                    📸 Ảnh phân tích đã lưu
                                                </Typography>
                                                <Button
                                                    variant="outlined"
                                                    color="error"
                                                    size="small"
                                                    onClick={clearSavedAnalysis}
                                                    sx={{ minWidth: 'auto' }}
                                                >
                                                    Xóa ảnh cũ
                                                </Button>
                                            </Box>
                                            <Grid container spacing={3}>
                                                <Grid item xs={12} md={4}>
                                                    <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                                                        <img
                                                            src={`data:image/jpeg;base64,${savedImage.imageBase64}`}
                                                            alt="Ảnh phân tích đã lưu"
                                                            style={{
                                                                maxWidth: '100%',
                                                                maxHeight: '300px',
                                                                borderRadius: '8px',
                                                                boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                                                            }}
                                                        />
                                                    </Box>
                                                </Grid>
                                                <Grid item xs={12} md={8}>
                                                    <Typography variant="body2" sx={{ mb: 1, color: '#666' }}>
                                                        <strong>Loại hình thể:</strong> {savedImage.bodyType}
                                                    </Typography>
                                                    {savedImage.analysis && (
                                                        <Typography variant="body2" sx={{ mb: 1, color: '#666' }}>
                                                            <strong>Phân tích:</strong> {savedImage.analysis.substring(0, 200)}...
                                                        </Typography>
                                                    )}
                                                    {savedImage.recommendations && (
                                                        <Typography variant="body2" sx={{ color: '#666' }}>
                                                            <strong>Đề xuất:</strong> {savedImage.recommendations.substring(0, 200)}...
                                                        </Typography>
                                                    )}
                                                    <Typography variant="caption" sx={{ display: 'block', mt: 2, color: '#999' }}>
                                                        Lưu lúc: {new Date(savedImage.timestamp).toLocaleString('vi-VN')}
                                                    </Typography>
                                                </Grid>
                                            </Grid>
                                        </CardContent>
                                    </Card>
                                ) : null;
                            })()}

                            {/* Component phân tích ảnh */}
                            <BodyImageAnalyzer 
                                formData={formData} 
                                onAnalysisComplete={handleAnalysisComplete}
                            />
                        </CardContent>
                    </Card>

                    {/* Current Data Summary */}
                    {(formData.bodyType || formData.chestSize || formData.waistSize) && (
                        <Card 
                            key={`current-data-${analysisTimestamp}`}
                            elevation={2} 
                            sx={{ mb: 4, borderRadius: 3, bgcolor: '#f8f9fa' }}
                        >
                            <CardContent sx={{ p: 3 }}>
                                <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50', mb: 2, textAlign: 'center' }}>
                                    📊 Dữ liệu hiện tại
                                </Typography>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, justifyContent: 'center', alignItems: 'center' }}>
                                    {formData.bodyType && (
                                        <Chip
                                            label={`Loại hình thể: ${formData.bodyType || 'Chưa xác định'}`}
                                            color="primary"
                                            sx={{ fontWeight: 'bold', fontSize: 16, bgcolor: '#CF4044', color: 'white' }}
                                        />
                                    )}
                                    {formData.chestSize && (
                                        <Chip 
                                            label={`Vòng ngực: ${formData.chestSize}cm`}
                                            color="secondary"
                                            sx={{ 
                                                bgcolor: '#fff3e0',
                                                color: '#f57c00',
                                                fontWeight: 'bold'
                                            }}
                                        />
                                    )}
                                    {formData.waistSize && (
                                        <Chip 
                                            label={`Vòng eo: ${formData.waistSize}cm`}
                                            color="info"
                                            sx={{ 
                                                bgcolor: '#e3f2fd',
                                                color: '#1565c0',
                                                fontWeight: 'bold'
                                            }}
                                        />
                                    )}
                                </Box>
                            </CardContent>
                        </Card>
                    )}

                    {/* Navigation Buttons */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
                        <Button
                            variant="outlined"
                            onClick={handlePrevious}
                            startIcon={<ArrowBack />}
                            disabled={submitting}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                color: '#CF4044', 
                                borderColor: '#CF4044',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                '&:hover': {
                                    borderColor: '#B8363C',
                                    backgroundColor: 'rgba(207, 64, 68, 0.04)',
                                    transform: 'translateY(-2px)'
                                },
                                '&:disabled': {
                                    borderColor: '#cccccc',
                                    color: '#666666'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            Bước trước
                        </Button>
                        
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={submitting}
                            endIcon={submitting ? <CircularProgress size={20} color="inherit" /> : <CheckCircle />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: submitting 
                                    ? '#999999' 
                                    : 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)',
                                color: 'white',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                minWidth: '180px',
                                boxShadow: submitting 
                                    ? 'none' 
                                    : '0 8px 20px rgba(207, 64, 68, 0.4)',
                                '&:hover': {
                                    background: submitting 
                                        ? '#999999'
                                        : 'linear-gradient(45deg, #B8363C 30%, #c0392b 90%)',
                                    boxShadow: submitting 
                                        ? 'none' 
                                        : '0 12px 28px rgba(207, 64, 68, 0.5)',
                                    transform: submitting ? 'none' : 'translateY(-2px)'
                                },
                                '&:disabled': {
                                    background: '#999999',
                                    color: 'white'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            {submitting ? 'Đang xử lý...' : 'Hoàn thành'}
                        </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
};

export default Step6;