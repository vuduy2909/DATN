import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box,
    Container,
    Paper,
    Typography,
    Button,
    Stepper,
    Step,
    StepLabel,
    Chip,
    Avatar,
    Card,
    CardContent,
    Alert,
    CircularProgress,
    Grid,
    Divider,
    List,
    ListItem,
    ListItemText
} from '@mui/material';
import {
    CheckCircle,
    ArrowBack,
    Psychology,
    FitnessCenter,
    Star,
    TrendingUp,
    Assignment
} from '@mui/icons-material';
import { useAuth } from '../../AuthContext';

const Step7 = ({ formData, handlePrevious, handleGoToCollections, recommendationResult, loading, error }) => {
    const navigate = useNavigate();
    const { user } = useAuth();
    console.log('Step7 rendering with props:', { 
        formDataExists: !!formData,
        recommendationResult,
        loading,
        error
    });
    
    const [confirmLoading, setConfirmLoading] = useState(false);
    const [confirmError, setConfirmError] = useState('');
    const [confirmed, setConfirmed] = useState(false);
    const [localRecommendation, setLocalRecommendation] = useState(null);
    
    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];

    // Tạo key riêng cho từng user dựa trên userId
    const getStorageKey = () => {
        const userId = user?.id || 'anonymous';
        return `recommendationData_${userId}`;
    };

    // Load từ localStorage khi component mount
    useEffect(() => {
        try {
            const storageKey = getStorageKey();
            const savedData = localStorage.getItem(storageKey);
            if (savedData) {
                const parsed = JSON.parse(savedData);
                console.log('Loaded recommendation data from localStorage for user:', user?.id);
                setLocalRecommendation(parsed);
            }
        } catch (e) {
            console.error('Error loading recommendation data from localStorage:', e);
        }
    }, [user]);

    useEffect(() => {
        console.log('Step7 useEffect triggered with recommendationResult:', recommendationResult);
        console.log('Original form data from parent component:', formData);
        
        // Try to load from localStorage if no recommendation result
        if (!recommendationResult) {
            try {
                const savedData = localStorage.getItem('recommendationData');
                if (savedData) {
                    const parsed = JSON.parse(savedData);
                    console.log('Loaded recommendation data from localStorage:', parsed);
                    setLocalRecommendation(parsed);
                }
            } catch (e) {
                console.error('Error parsing localStorage recommendation data:', e);
            }
        }
        
        // Check if we have a valid recommendation result from props
        if (recommendationResult) {
            console.log('Received recommendation results:', recommendationResult);
            
            // Lấy dữ liệu khóa học được đề xuất
            const recommendedCourse = recommendationResult.recommendedCourse || recommendationResult.recommendedCourses;
            console.log('Recommended course:', recommendedCourse);
            
            // Ưu tiên lấy dữ liệu metrics từ API nếu có, nếu không thì dùng formData
            const userMetricsData = recommendationResult.userMetrics || formData;
            console.log('User metrics from API:', recommendationResult.userMetrics);
            console.log('Final user metrics being used:', userMetricsData);
            
            // Ensure actions is present with the most accurate data
            const enhancedResult = {
                ...recommendationResult,
                // Đảm bảo luôn có userMetrics
                userMetrics: userMetricsData,
                // Đảm bảo luôn có actions với dữ liệu chính xác nhất
                actions: {
                    // Ưu tiên dữ liệu từ API trước, sau đó đến formData
                    formData: userMetricsData,
                    recommendedCourseId: recommendedCourse?.courseId
                }
            };
            
            console.log('Enhanced recommendation with accurate user data:', enhancedResult);
            
            // Save to localStorage for persistence
            try {
                const storageKey = getStorageKey();
                localStorage.setItem(storageKey, JSON.stringify(enhancedResult));
                console.log('Saved enhanced recommendation data to localStorage for user:', user?.id);
            } catch (e) {
                console.error('Error saving to localStorage:', e);
            }
        }
    }, [recommendationResult, formData, user]);

    // Use recommendation data from props or localStorage
    const activeRecommendation = recommendationResult || localRecommendation;

    const handleConfirmRecommendation = async () => {
        console.log('=== ENHANCED DEBUG CONFIRM RECOMMENDATION ===');
        console.log('handleConfirmRecommendation called with activeRecommendation:', activeRecommendation);
        
        // Lấy dữ liệu chính xác nhất có thể
        let actionFormData = null;
        let recommendedCourseId = null;
        
        // Check if we have the required data
        if (!activeRecommendation) {
            console.error('No recommendation data available to confirm');
            setConfirmError('Không thể xác nhận đề xuất: Thiếu dữ liệu đề xuất.');
            
            setTimeout(() => {
                handleGoToCollections();
            }, 2000);
            
            return;
        }
        
        // Debug chi tiết để xem dữ liệu
        console.log('Active recommendation full data:', JSON.stringify(activeRecommendation, null, 2));
        
        // Kiểm tra và lấy dữ liệu theo thứ tự ưu tiên
        if (activeRecommendation.actions && activeRecommendation.actions.formData) {
            // 1. Ưu tiên dữ liệu từ actions.formData nếu có
            actionFormData = activeRecommendation.actions.formData;
            
            // Đảm bảo lấy đúng courseId là số
            let rawCourseId = activeRecommendation.actions.recommendedCourseId;
            
            if (rawCourseId) {
                console.log('Raw courseId from actions:', rawCourseId, 'Type:', typeof rawCourseId);
                
                // Xử lý trường hợp là object có courseId hoặc id
                if (typeof rawCourseId === 'object') {
                    if (rawCourseId.courseId) {
                        recommendedCourseId = Number(rawCourseId.courseId);
                    } else if (rawCourseId.id) {
                        recommendedCourseId = Number(rawCourseId.id);
                    }
                } 
                // Xử lý trường hợp là số hoặc chuỗi
                else if (typeof rawCourseId === 'number') {
                    recommendedCourseId = rawCourseId;
                } else if (typeof rawCourseId === 'string' && !isNaN(rawCourseId)) {
                    recommendedCourseId = Number(rawCourseId);
                }
            }
            
            console.log('Using formData from actions:', actionFormData, 'CourseId:', recommendedCourseId);
        } 
        
        // Nếu không có courseId từ actions, thử lấy từ recommendedCourse hoặc recommendedCourses
        if (!recommendedCourseId || isNaN(recommendedCourseId)) {
            const recommendedCourse = activeRecommendation.recommendedCourse || activeRecommendation.recommendedCourses;
            console.log('Trying to extract courseId from recommendedCourse:', recommendedCourse);
            
            if (recommendedCourse) {
                if (typeof recommendedCourse === 'object') {
                    if (recommendedCourse.courseId) {
                        recommendedCourseId = Number(recommendedCourse.courseId);
                    } else if (recommendedCourse.id) {
                        recommendedCourseId = Number(recommendedCourse.id);
                    }
                } else if (typeof recommendedCourse === 'number') {
                    recommendedCourseId = recommendedCourse;
                } else if (typeof recommendedCourse === 'string' && !isNaN(recommendedCourse)) {
                    recommendedCourseId = Number(recommendedCourse);
                }
            }
            
            // Nếu vẫn không có form data, lấy từ userMetrics hoặc formData
            if (!actionFormData) {
                actionFormData = activeRecommendation.userMetrics || formData;
                console.log('Using metrics/formData as fallback:', actionFormData);
            }
        }
        
        // Enhanced validation with detailed logging
        console.log('VALIDATION CHECKS:');
        console.log('recommendedCourseId:', recommendedCourseId, 'isNaN:', isNaN(recommendedCourseId));
        console.log('actionFormData exists:', !!actionFormData);
        console.log('actionFormData.customerId:', actionFormData?.customerId);
        
        // Enhanced fallback logic for recommendedCourseId
        if (!recommendedCourseId || isNaN(recommendedCourseId)) {
            console.warn('Attempting fallback methods to find recommendedCourseId...');
            
            // Try to get from localStorage
            try {
                const storageKey = getStorageKey();
                const savedData = localStorage.getItem(storageKey);
                if (savedData) {
                    const parsed = JSON.parse(savedData);
                    const fallbackCourse = parsed.recommendedCourse || parsed.recommendedCourses;
                    if (fallbackCourse && fallbackCourse.courseId) {
                        recommendedCourseId = Number(fallbackCourse.courseId);
                        console.log('Found courseId from localStorage:', recommendedCourseId);
                    }
                }
            } catch (e) {
                console.warn('Failed to get courseId from localStorage:', e);
            }
            
            // If still no courseId, try to get from formData
            if (!recommendedCourseId && actionFormData?.recommendedCourseId) {
                recommendedCourseId = Number(actionFormData.recommendedCourseId);
                console.log('Found courseId from actionFormData:', recommendedCourseId);
            }
            
            // Final fallback: use default course ID
            if (!recommendedCourseId || isNaN(recommendedCourseId)) {
                console.warn('Using default courseId as last fallback');
                recommendedCourseId = 1; // Default course ID
            }
        }
        
        // Kiểm tra cuối cùng trước khi gửi API
        if (!recommendedCourseId || isNaN(recommendedCourseId)) {
            console.error('Missing or invalid recommendedCourseId after all fallbacks', recommendedCourseId);
            setConfirmError('Không thể xác nhận đề xuất: Thiếu hoặc sai ID khóa học.');
            
            setTimeout(() => {
                handleGoToCollections();
            }, 2000);
            
            return;
        }
        
        if (!actionFormData) {
            console.error('No form data available');
            setConfirmError('Không thể xác nhận đề xuất: Không có dữ liệu người dùng.');
            
            setTimeout(() => {
                handleGoToCollections();
            }, 2000);
            
            return;
        }
        
        // Đảm bảo customerId có giá trị hợp lệ
        if (!actionFormData.customerId) {
            console.warn('No customerId in form data, trying to get from localStorage');
            // Thử lấy từ localStorage
            try {
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const user = JSON.parse(userStr);
                    if (user && user.id) {
                        actionFormData.customerId = user.id;
                        console.log('Using customerId from localStorage:', user.id);
                    }
                }
            } catch (e) {
                console.error('Error parsing user from localStorage:', e);
            }
        }
        
        // Log dữ liệu sẽ gửi đi để kiểm tra
        console.log('FINAL DATA BEING SENT TO API:');
        console.log('Form data:', JSON.stringify(actionFormData, null, 2));
        console.log('Course ID:', recommendedCourseId, 'Type:', typeof recommendedCourseId);
        
        setConfirmLoading(true);
        setConfirmError('');
        
        try {
            // Import UserFormService
            const UserFormService = (await import('../../services/UserFormService')).default;
            
            // Debug chi tiết requestData
            console.log('DEBUG CONFIRM REQUEST:');
            console.log('actionFormData:', JSON.stringify(actionFormData, null, 2));
            console.log('recommendedCourseId:', recommendedCourseId);
            console.log('recommendedCourseId type:', typeof recommendedCourseId);
            
            // Call the confirmation API with formData and courseId
            const response = await UserFormService.confirmRecommendation(actionFormData, recommendedCourseId);
            console.log('Confirmation response:', response);
            
            // Mark as confirmed and proceed to collections page
            setConfirmed(true);
            
            // Show success message with userDataId if available
            if (response.userDataId) {
                console.log('User data saved with ID:', response.userDataId);
            }
            
            setTimeout(() => {
                handleGoToCollections();
            }, 1500);
            
        } catch (error) {
            console.error('Error confirming recommendation:', error);
            let errorMessage = 'Có lỗi xảy ra khi xác nhận đề xuất. Vui lòng thử lại sau.';
            
            // Check for specific error from backend
            if (error.response) {
                console.error('API error response:', error.response);
                
                if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                } else if (error.response.data) {
                    if (error.response.data.message) {
                        errorMessage = error.response.data.message;
                    } else {
                        // Trích xuất thông tin lỗi từ các trường khác nếu có
                        errorMessage = JSON.stringify(error.response.data);
                    }
                }
                
                // Thêm mã lỗi nếu có
                if (error.response.status) {
                    errorMessage += ` (Error ${error.response.status})`;
                }
            }
            
            setConfirmError(errorMessage);
            
            // Even on error, let user proceed to collections after some time
            setTimeout(() => {
                handleGoToCollections();
            }, 3000);
        } finally {
            setConfirmLoading(false);
        }
    };

    // Get the recommendation from the API result
    const recommendedCourse = activeRecommendation?.recommendedCourses || activeRecommendation?.recommendedCourse;
    const hasApiRecommendations = !!recommendedCourse;
    const userMetrics = activeRecommendation?.userMetrics || {};
    
    // DEBUG: Enhanced logging to help diagnose the issue
    console.log('=== ENHANCED DEBUG Step7 RENDER ===');
    console.log('DEBUG Step7: activeRecommendation', activeRecommendation);
    console.log('DEBUG Step7: recommendedCourse', recommendedCourse);
    console.log('DEBUG Step7: hasApiRecommendations', hasApiRecommendations);
    console.log('DEBUG Step7: userMetrics', userMetrics);
    console.log('DEBUG Step7: recommendationResult prop', recommendationResult);
    console.log('DEBUG Step7: localRecommendation state', localRecommendation);
    console.log('DEBUG Step7: formData prop', formData);
    
    // Check localStorage directly
    try {
        const storageKey = getStorageKey();
        const savedData = localStorage.getItem(storageKey);
        console.log('DEBUG Step7: localStorage key:', storageKey);
        console.log('DEBUG Step7: localStorage data:', savedData);
        if (savedData) {
            const parsed = JSON.parse(savedData);
            console.log('DEBUG Step7: parsed localStorage data:', parsed);
        }
    } catch (e) {
        console.error('DEBUG Step7: Error checking localStorage:', e);
    }
    
    console.log('=== END ENHANCED DEBUG Step7 RENDER ===');

    // Helper functions to map data
        const mapTrainingFrequency = (value) => {
        if (!value) return "Không xác định";
                switch(value) {
            case "Never": return "Chưa từng tập";
            case "Occasionally": return "2 buổi/tuần";
            case "Moderate": return "3 buổi/tuần";
            case "Regularly": return "4 buổi/tuần";
            case "Advanced": return "5+ buổi/tuần";
                    default: return value;
                }
    };

        const mapTrainingGoal = (value) => {
        if (!value) return "Không xác định";
        if (value.includes("Weight Loss")) return "Giảm cân";
        if (value.includes("Strength Training")) return "Tăng cơ bắp";
        if (value.includes("Cardiovascular")) return "Tăng sức bền";
        if (value.includes("Flexibility")) return "Tăng độ dẻo";
        if (value.includes("Functional")) return "Duy trì vóc dáng";
            return value;
        };
        
    if (loading) {
        return (
            <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
                <CircularProgress size={80} sx={{ color: '#CF4044', mb: 3 }} />
                <Typography variant="h5" sx={{ color: '#7f8c8d' }}>
                    Đang phân tích và tạo đề xuất cho bạn...
                </Typography>
            </Container>
        );
    }

    if (error) {
        return (
            <Container maxWidth="md" sx={{ py: 4 }}>
                <Alert severity="error" sx={{ mb: 3 }}>
                    {error}
                </Alert>
                <Button variant="outlined" onClick={handlePrevious}>
                    Quay lại
                </Button>
            </Container>
        );
    }

    // Show debug info when no recommendations are available
    if (!loading && !error && !hasApiRecommendations) {
        return (
            <Container maxWidth="md" sx={{ py: 4 }}>
                <Alert severity="warning" sx={{ mb: 3 }}>
                    <Typography variant="h6">Không tìm thấy đề xuất khóa học</Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                        DEBUG INFO: activeRecommendation = {JSON.stringify(activeRecommendation, null, 2)}
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                        DEBUG INFO: recommendationResult = {JSON.stringify(recommendationResult, null, 2)}
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                        DEBUG INFO: formData = {JSON.stringify(formData, null, 2)}
                    </Typography>
                </Alert>
                <Button variant="outlined" onClick={handlePrevious} sx={{ mr: 2 }}>
                    Quay lại
                </Button>
                <Button variant="contained" onClick={handleGoToCollections}>
                    Tiếp tục
                </Button>
            </Container>
        );
    }

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
                    <Stepper 
                        activeStep={6} 
                        alternativeLabel
                        sx={{
                            '& .MuiStep-root': {
                                '& .MuiStepIcon-root': {
                                    color: '#e0e0e0', // Inactive step color
                                    '&.Mui-active': {
                                        color: '#CF4044', // Active step color (red)
                                    },
                                    '&.Mui-completed': {
                                        color: '#CF4044', // Completed step color (red)
                                    },
                                },
                                '& .MuiStepConnector-line': {
                                    borderColor: '#e0e0e0', // Connector line color
                                },
                                '& .Mui-active ~ .MuiStepConnector-root .MuiStepConnector-line': {
                                    borderColor: '#CF4044', // Active connector color
                                },
                                '& .Mui-completed ~ .MuiStepConnector-root .MuiStepConnector-line': {
                                    borderColor: '#CF4044', // Completed connector color
                                },
                            },
                            '& .MuiStepConnector-root': {
                                '& .MuiStepConnector-line': {
                                    borderTopWidth: '3px',
                                    borderRadius: '1px',
                                },
                            },
                        }}
                    >
                        {steps.map((label, index) => (
                            <Step key={label}>
                                <StepLabel
                                    sx={{
                                        '& .MuiStepLabel-label': {
                                            display: 'none', // Hide the step label text
                                        },
                                    }}
                                >
                                    {/* No label text */}
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
                            bgcolor: '#4CAF50', 
                            width: 80, 
                            height: 80, 
                            mx: 'auto', 
                            mb: 2,
                            boxShadow: '0 8px 24px rgba(76, 175, 80, 0.3)'
                        }}>
                            <Psychology sx={{ fontSize: 40 }} />
                        </Avatar>
                        
                        <Typography variant="h4" sx={{ 
                            fontWeight: 'bold', 
                            color: '#2c3e50', 
                            mb: 1,
                            background: 'linear-gradient(45deg, #4CAF50 30%, #66BB6A 90%)',
                            backgroundClip: 'text',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                        }}>
                            Kết quả phân tích
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Dựa trên thông tin của bạn, chúng tôi đã tạo ra chương trình tối ưu
                        </Typography>
                        
                        <Chip 
                            label="Hoàn thành" 
                            color="success" 
                            sx={{ 
                                mt: 2, 
                                bgcolor: '#4CAF50',
                                fontWeight: 'bold'
                            }} 
                        />
                    </Box>

                    {/* Debug Alert - Hiển thị thông tin debug về dữ liệu */}
                    {!hasApiRecommendations && (
                        <Alert 
                            severity="warning" 
                            variant="filled"
                            sx={{ mb: 3 }}
                        >
                            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                                Không có dữ liệu khóa học được đề xuất
                            </Typography>
                            <Typography variant="body2">
                                Chi tiết: {activeRecommendation?.message || 'Không có chi tiết lỗi'}
                            </Typography>
                            <Typography variant="body2" sx={{ mt: 1 }}>
                                Thử liên hệ với quản trị viên để được hỗ trợ hoặc chọn khóa học trực tiếp từ danh sách.
                            </Typography>
                        </Alert>
                    )}

                    {/* Recommendation Results */}
                    {hasApiRecommendations ? (
                        <Grid container spacing={3} sx={{ mb: 4 }}>
                            {/* Recommended Course */}
                            <Grid item xs={12}>
                                <Card elevation={3} sx={{ borderRadius: 3, border: '2px solid #4CAF50' }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Avatar sx={{ bgcolor: '#4CAF50', mr: 2 }}>
                                                <Star />
                                            </Avatar>
                                            <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                                Khóa học được đề xuất
                                            </Typography>
                                        </Box>
                                        
                                        <Box sx={{ bgcolor: '#f0f8ff', p: 3, borderRadius: 2, mb: 2 }}>
                                            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1565c0', mb: 1 }}>
                                                {recommendedCourse.name || recommendedCourse.courseName || 'Khóa học chưa xác định'}
                                            </Typography>
                                            <Typography variant="body1" sx={{ color: '#7f8c8d', mb: 2 }}>
                                                {recommendedCourse.description || 'Mô tả khóa học chưa có'}
                                            </Typography>
                                            
                                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                               
                                               <Chip 
                                                    label={`Thời lượng: ${recommendedCourse.duration || 'N/A'}`} 
                                                 color="secondary" 
                                                  size="small" 
                                               />
                                                <Chip 
                                                    label={`Loại: ${recommendedCourse.courseType?.courseTypeName || recommendedCourse.courseType?.name || 'Chưa phân loại'}`} 
                                                    color="info" 
                                                    size="small" 
                                                />
                                                {recommendedCourse.instructor && (
                                                    <Chip 
                                                        label={`HLV: ${recommendedCourse.instructor}`} 
                                                        color="warning" 
                                                        size="small" 
                                                    />
                                                )}
                                            </Box>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>

                            {/* User Physical Information */}
                            <Grid item xs={12} md={6}>
                                <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Avatar sx={{ bgcolor: '#f39c12', mr: 2 }}>
                                                <Assignment />
                                            </Avatar>
                                            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                                Thông tin cơ thể
                                            </Typography>
                                        </Box>

                                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                                            <Typography variant="body2">
                                                <strong>Tuổi:</strong> {userMetrics.age || formData.age} tuổi
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Giới tính:</strong> {userMetrics.gender === 'Male' ? 'Nam' : userMetrics.gender === 'Female' ? 'Nữ' : 'Không xác định'}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Chiều cao:</strong> {userMetrics.height || formData.height} cm
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Cân nặng:</strong> {userMetrics.weight || formData.weight} kg
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>BMI:</strong> {userMetrics.bmi?.toFixed(1) || (formData.bmi ? formData.bmi.toFixed(1) : 'N/A')}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Dáng người:</strong> {userMetrics.body_type || formData.bodyType || 'Không xác định'}
                                            </Typography>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>

                            {/* Body Measurements */}
                            <Grid item xs={12} md={6}>
                                <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Avatar sx={{ bgcolor: '#e91e63', mr: 2 }}>
                                                <FitnessCenter />
                                            </Avatar>
                                            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                                Số đo cơ thể
                                            </Typography>
                                        </Box>

                                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                                            <Typography variant="body2">
                                                <strong>Vòng ngực:</strong> {userMetrics.chest_circumference || formData.chestSize || 'Chưa đo'} cm
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Vòng eo:</strong> {userMetrics.waist_circumference || formData.waistSize || 'Chưa đo'} cm
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Nghề nghiệp:</strong> {userMetrics.occupation || formData.occupation || 'Không xác định'}
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Môi trường làm việc:</strong> {userMetrics.work_environment || formData.workEnvironment || 'Không xác định'}
                                            </Typography>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>

                            {/* Training Analysis */}
                            <Grid item xs={12}>
                                <Card elevation={2} sx={{ borderRadius: 3 }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Avatar sx={{ bgcolor: '#9c27b0', mr: 2 }}>
                                                <TrendingUp />
                                            </Avatar>
                                            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                                Phân tích tập luyện
                                            </Typography>
                                        </Box>

                                        <Grid container spacing={2}>
                                            <Grid item xs={12} sm={6} md={3}>
                                                <Typography variant="body2">
                                                    <strong>Kinh nghiệm:</strong><br/>
                                                    {mapTrainingFrequency(userMetrics.training_history)}
                                                </Typography>
                                            </Grid>
                                            <Grid item xs={12} sm={6} md={3}>
                                                <Typography variant="body2">
                                                    <strong>Tần suất mong muốn:</strong><br/>
                                                    {mapTrainingFrequency(userMetrics.training_frequency)}
                                                </Typography>
                                            </Grid>
                                            <Grid item xs={12} sm={6} md={3}>
                                                <Typography variant="body2">
                                                    <strong>Mục tiêu:</strong><br/>
                                                    {mapTrainingGoal(userMetrics.training_goals)}
                                                </Typography>
                                            </Grid>
                                            <Grid item xs={12} sm={6} md={3}>
                                                <Typography variant="body2">
                                                    <strong>Đánh giá:</strong><br/>
                                                    Phù hợp với khóa học
                                                </Typography>
                                            </Grid>
                                        </Grid>
                                    </CardContent>
                                </Card>
                            </Grid>
                        </Grid>
                    ) : (
                        <Box sx={{ textAlign: 'center', my: 6, p: 4, border: '1px dashed #ccc', borderRadius: 2 }}>
                            <img 
                                src="/images/no-data.svg" 
                                alt="Không có dữ liệu" 
                                style={{ width: 180, height: 180, marginBottom: 16 }}
                                onError={(e) => {e.target.src = 'https://cdn-icons-png.flaticon.com/512/7486/7486754.png'}}
                            />
                            <Typography variant="h5" sx={{ color: '#666', mb: 2 }}>
                                Không có dữ liệu đề xuất
                            </Typography>
                            <Typography variant="body1" sx={{ color: '#888', mb: 3 }}>
                                {activeRecommendation?.message || 'Không thể tìm thấy khóa học phù hợp. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.'}
                            </Typography>
                            <Button 
                                variant="contained"
                                color="primary"
                                onClick={handleGoToCollections}
                                startIcon={<CheckCircle />}
                                sx={{ fontWeight: 'bold' }}
                            >
                                Xem danh sách khóa học
                            </Button>
                        </Box>
                    )}
                    
                    {/* Status Messages */}
                    {confirmError && (
                        <Alert severity="error" sx={{ mb: 3 }}>
                            {confirmError}
                        </Alert>
                    )}
                    
                    {confirmed && (
                        <Alert severity="success" sx={{ mb: 3 }}>
                            ✅ Đề xuất đã được xác nhận thành công! Đang chuyển hướng...
                        </Alert>
                    )}
            
                    {/* Action Buttons */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
                                    <Button
                                        variant="outlined"
                                        onClick={handlePrevious}
                            startIcon={<ArrowBack />}
                                        disabled={confirmLoading || confirmed}
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
                                        Quay lại
                                    </Button>
                                    
                                    <Button
                                        variant="contained"
                                        onClick={handleConfirmRecommendation}
                                        disabled={confirmLoading || confirmed}
                            endIcon={confirmLoading ? <CircularProgress size={20} color="inherit" /> : <FitnessCenter />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: (confirmLoading || confirmed) 
                                    ? '#4CAF50' 
                                    : 'linear-gradient(45deg, #4CAF50 30%, #66BB6A 90%)',
                                color: 'white',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                minWidth: '200px',
                                boxShadow: (confirmLoading || confirmed) 
                                    ? 'none' 
                                    : '0 8px 20px rgba(76, 175, 80, 0.4)',
                                '&:hover': {
                                    background: (confirmLoading || confirmed) 
                                        ? '#4CAF50'
                                        : 'linear-gradient(45deg, #388E3C 30%, #4CAF50 90%)',
                                    boxShadow: (confirmLoading || confirmed) 
                                        ? 'none' 
                                        : '0 12px 28px rgba(76, 175, 80, 0.5)',
                                    transform: (confirmLoading || confirmed) ? 'none' : 'translateY(-2px)'
                                },
                                '&:disabled': {
                                    background: '#4CAF50',
                                    color: 'white'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            {confirmed ? 'Đã xác nhận!' : confirmLoading ? 'Đang xử lý...' : 'Xác nhận và Bắt đầu'}
                                    </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
};

export default Step7;