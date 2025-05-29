import React, { useState, useEffect } from 'react';
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
    Divider
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

const Step7 = ({ formData, handlePrevious, handleGoToCollections, recommendationResult, loading, error }) => {
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
        'Thông tin tập luyện',
        'Thông tin cơ bản', 
        'Mục tiêu tập luyện',
        'Tần suất mong muốn',
        'Thông tin nghề nghiệp',
        'Số đo cơ thể',
        'Kết quả phân tích'
    ];

    useEffect(() => {
        console.log('Step7 useEffect triggered with recommendationResult:', recommendationResult);
        
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
            console.log('Recommended course:', recommendationResult.recommendedCourse);
            console.log('User metrics:', recommendationResult.userMetrics);
            
            // Save to localStorage for persistence
            try {
                localStorage.setItem('recommendationData', JSON.stringify(recommendationResult));
                console.log('Saved recommendation data to localStorage');
            } catch (e) {
                console.error('Error saving to localStorage:', e);
            }
        }
    }, [recommendationResult]);

    // Use recommendation data from props or localStorage
    const activeRecommendation = recommendationResult || localRecommendation;

    const handleConfirmRecommendation = async () => {
        console.log('handleConfirmRecommendation called');
        
        // Check if we have the required data from actions
        if (!activeRecommendation || !activeRecommendation.actions) {
            console.error('No actions data available to confirm recommendation');
            setConfirmError('Không thể xác nhận đề xuất: Thiếu thông tin cần thiết.');
            
            // Even without proper data, we can navigate to collections
            setTimeout(() => {
                handleGoToCollections();
            }, 1000);
            
            return;
        }
        
        const { formData: actionFormData, recommendedCourseId } = activeRecommendation.actions;
        
        if (!actionFormData || !recommendedCourseId) {
            console.error('Missing formData or recommendedCourseId in actions');
            setConfirmError('Không thể xác nhận đề xuất: Thiếu dữ liệu form hoặc ID khóa học.');
            
            setTimeout(() => {
                handleGoToCollections();
            }, 1000);
            
            return;
        }
        
        setConfirmLoading(true);
        setConfirmError('');
        
        try {
            // Import UserFormService
            const UserFormService = (await import('../../services/UserFormService')).default;
            
            console.log('Confirming recommendation with formData and courseId:', actionFormData, recommendedCourseId);
            
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
            if (error.response && error.response.data) {
                if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                } else if (error.response.data.message) {
                    errorMessage = error.response.data.message;
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
    const recommendedCourse = activeRecommendation?.recommendedCourse;
    const hasApiRecommendations = !!recommendedCourse;
    const userMetrics = activeRecommendation?.userMetrics || {};

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
                    <Stepper activeStep={6} alternativeLabel>
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
                                                {recommendedCourse.title || recommendedCourse.course_name || 'Khóa học chưa xác định'}
                                            </Typography>
                                            <Typography variant="body1" sx={{ color: '#7f8c8d', mb: 2 }}>
                                                {recommendedCourse.description || recommendedCourse.course_description || 'Mô tả khóa học chưa có'}
                                            </Typography>
                                            
                                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                                <Chip 
                                                    label={`Cấp độ: ${recommendedCourse.level || recommendedCourse.course_level || 'Chưa xác định'}`} 
                                                    color="primary" 
                                                    size="small" 
                                                />
                                                <Chip 
                                                    label={`Thời lượng: ${recommendedCourse.duration || recommendedCourse.course_duration || 'N/A'} phút`} 
                                                    color="secondary" 
                                                    size="small" 
                                                />
                                                <Chip 
                                                    label={`Loại: ${recommendedCourse.category || recommendedCourse.course_category || recommendedCourse.course_type || 'Chưa phân loại'}`} 
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
                        <Alert severity="info" sx={{ mb: 4 }}>
                            Không có dữ liệu đề xuất. Vui lòng thử lại hoặc liên hệ hỗ trợ.
                        </Alert>
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
                            {confirmed ? 'Đã xác nhận!' : confirmLoading ? 'Đang xử lý...' : 'Xác nhận & Bắt đầu'}
                        </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
};

export default Step7;