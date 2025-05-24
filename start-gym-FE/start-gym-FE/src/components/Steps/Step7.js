import React, { useState, useEffect } from 'react';
import styles from './Step7.module.css'; // Use dedicated CSS for Step7
import Button from '@mui/material/Button';
import { Paper, Typography, Box, Divider, Chip, Alert, CircularProgress } from '@mui/material';
import { request } from '../../utils/axiosInstance';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

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
    
    // Add a state to track if we have a valid recommendation 
    const [hasValidRecommendation, setHasValidRecommendation] = useState(false);
    
    // Add state to track local recommendation data
    const [localRecommendation, setLocalRecommendation] = useState(null);

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

                    // Verify if we have the necessary data
                    const hasValidData = parsed.recommendedCourse || 
                                        (parsed.userMetrics && Object.keys(parsed.userMetrics).length > 0);
                    
                    setHasValidRecommendation(hasValidData);
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
            
            // Verify if we have the necessary data from the API
            const hasValidData = recommendationResult.recommendedCourse || 
                                (recommendationResult.userMetrics && Object.keys(recommendationResult.userMetrics).length > 0);
            
            setHasValidRecommendation(hasValidData);
            
            if (!hasValidData) {
                console.warn('Recommendation result does not contain expected data');
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
    
    // Get the explanation from the API
    const explanationText = activeRecommendation?.explanation || '';
    
    // Check if we have API recommendations
    const hasApiRecommendations = !!recommendedCourse;
    
    // Extract user metrics used by the decision tree
    const userMetrics = activeRecommendation?.userMetrics || {};

    // Format backend log data for display
    const formatLogData = () => {
        // Helper function to map database enum values to user-friendly display text
        const mapTrainingFrequency = (value) => {
            if (!value) return "Not specified";
            
            // Return the value directly if it's one of the enum values
            const enumValues = ["Never", "Occasionally", "Moderate", "Regularly", "Advanced"];
            if (enumValues.includes(value)) {
                // For user display, we can add descriptions for clarity
                switch(value) {
                    case "Never": return "Never (First time)";
                    case "Occasionally": return "Occasionally (1 session/week)";
                    case "Moderate": return "Moderate (2-3 sessions/week)";
                    case "Regularly": return "Regularly (3-4 sessions/week)";
                    case "Advanced": return "Advanced (5+ sessions/week)";
                    default: return value;
                }
            }
            
            // For any non-standard value, try to map it
            const valueStr = String(value).toUpperCase();
            if (valueStr.includes("REGULAR")) return "Regularly (3-4 sessions/week)";
            if (valueStr.includes("NEVER")) return "Never (First time)";
            if (valueStr.includes("OCCASION")) return "Occasionally (1 session/week)";
            if (valueStr.includes("MODERATE")) return "Moderate (2-3 sessions/week)";
            if (valueStr.includes("ADVANCED")) return "Advanced (5+ sessions/week)";
            
            return value; // Return as-is if no mapping found
        };
        
        // Map occupation from database enum to user-friendly display
        const mapOccupation = (value) => {
            const displayMap = {
                'Student': 'Sinh viên',
                'Office worker': 'Văn phòng',
                'Delivery staff': 'Giao hàng',
                'Lecturer': 'Giảng viên',
                'Service staff': 'Nhân viên dịch vụ',
                'Other': 'Khác'
            };
            
            return displayMap[value] || value;
        };
        
        // Map work environment from database enum to user-friendly display
        const mapWorkEnvironment = (value) => {
            const displayMap = {
                'Office': 'Văn phòng',
                'Shift work': 'Làm theo ca',
                'Standing-intensive': 'Đứng nhiều',
                'Walking-intensive': 'Đi bộ nhiều',
                'Driving-intensive': 'Lái xe',
                'Manual labor': 'Lao động tay chân'
            };
            
            return displayMap[value] || value;
        };
        
        // Map training goals from database enum to user-friendly display
        const mapTrainingGoal = (value) => {
            // Extract the name part from the enum value (e.g. "7, Weight Loss" -> "Weight Loss")
            if (value && value.includes(', ')) {
                const namePart = value.split(', ')[1];
                
                // Map English names to Vietnamese if needed
                const displayMap = {
                    'Strength Training': 'Tăng cơ',
                    'Cardiovascular Training': 'Tăng sức bền tim mạch',
                    'Flexibility and Balance': 'Dẻo dai và thăng bằng',
                    'Train strength and endurance': 'Tăng sức mạnh và sức bền',
                    'Mind-Body Wellness': 'Sức khỏe tinh thần',
                    'Functional Training': 'Huấn luyện chức năng',
                    'Weight Loss': 'Giảm cân tổng thể'
                };
                
                return displayMap[namePart] || namePart;
            }
            
            return value;
        };
        
        const data = [
            { label: "Độ tuổi", value: userMetrics.age || formData.age },
            { label: "Giới tính", value: userMetrics.gender || formData.gender },
            { label: "Chiều cao", value: `${userMetrics.height || formData.height} cm` },
            { label: "Cân nặng", value: `${userMetrics.weight || formData.weight} kg` },
            { label: "BMI", value: userMetrics.bmi ? Number(userMetrics.bmi).toFixed(2) : (formData.weight / Math.pow(formData.height/100, 2)).toFixed(2) },
            { label: "Loại hình thể", value: userMetrics.body_type || formData.bodyType || "Normal" },
            { label: "Nghề nghiệp", value: mapOccupation(userMetrics.occupation) || mapOccupation(formData.occupation) || "Sinh viên" },
            { label: "Môi trường làm việc", value: mapWorkEnvironment(userMetrics.work_environment) || mapWorkEnvironment(formData.workEnvironment) || "Văn phòng" },
            { label: "Lịch sử tập luyện", value: mapTrainingFrequency(userMetrics.training_history) || mapTrainingFrequency(formData.trainingFrequency) || "Never" },
            { label: "Tần suất mong muốn", value: mapTrainingFrequency(userMetrics.training_frequency) || mapTrainingFrequency(formData.desiredFrequency) || "2 sessions/week" },
            { label: "Mục tiêu tập luyện", value: mapTrainingGoal(userMetrics.training_goals) || mapTrainingGoal(formData.trainingGoal) || "Giảm cân tổng thể" },
        ];

        // Add measurements with more prominence
        if (userMetrics.waist_circumference || formData.waistSize) {
            data.push({ 
                label: "Vòng eo", 
                value: `${userMetrics.waist_circumference || formData.waistSize} cm`,
                isHighlighted: true
            });
        } else {
            data.push({ label: "Vòng eo", value: "Không có" });
        }

        if (userMetrics.chest_circumference || formData.chestSize) {
            data.push({ 
                label: "Vòng ngực", 
                value: `${userMetrics.chest_circumference || formData.chestSize} cm`,
                isHighlighted: true
            });
        } else {
            data.push({ label: "Vòng ngực", value: "Không có" });
        }

        // Add health information
        if (userMetrics.heart_disease === true || userMetrics.heart_disease === "true") {
            data.push({ label: "Vấn đề tim mạch", value: "Có" });
        }
        
        if (userMetrics.respiratory_disease === true || userMetrics.respiratory_disease === "true") {
            data.push({ label: "Vấn đề hô hấp", value: "Có" });
        }

        if (userMetrics.recent_injury === true || userMetrics.recent_injury === "true") {
            data.push({ label: "Chấn thương gần đây", value: "Có" });
        }

        return data;
    };

    const logData = formatLogData();

    return (
        <div className={styles.container}>
            <div className={styles.stepIndicator}>
                <div className={styles.stepLabel}>Bước 7/7: Xác nhận đề xuất khóa học</div>
            </div>
            
            <div className={styles.content}>
                <div className={styles.card}>
                    <div className={styles.cardHeader}>
                        <h2>Thông tin của bạn đã được ghi nhận</h2>
                    </div>
                    
                    {loading && (
                        <div className={styles.loadingContainer}>
                            <CircularProgress color="error" />
                        </div>
                    )}
                    
                    {error && (
                        <Alert severity="error" className={styles.alert}>
                            {error}
                        </Alert>
                    )}
                    
                    {confirmError && (
                        <Alert severity="error" className={styles.alert}>
                            {confirmError}
                        </Alert>
                    )}
                    
                    {confirmed && (
                        <Alert severity="success" className={styles.alert} icon={<CheckCircleIcon fontSize="inherit" />}>
                            <strong>Đề xuất khóa học đã được xác nhận thành công!</strong>
                            <p>Đang chuyển hướng đến trang khóa học đề xuất...</p>
                        </Alert>
                    )}
            
                    {loading ? (
                        <div className={styles.loadingContainer}>
                            <Typography variant="h6">Đang tạo đề xuất từ dữ liệu của bạn...</Typography>
                            <div className={styles.spinner}></div>
                        </div>
                    ) : (
                        <div className={styles.analysisResults}>
                            {hasApiRecommendations && (
                                <Alert severity="success" className={styles.alert}>
                                    <strong>Hoàn tất phân tích!</strong> Đã tìm được khóa học phù hợp với thông tin của bạn.
                                </Alert>
                            )}
                            
                            {/* Hiển thị dữ liệu log từ backend */}
                            <Paper elevation={1} className={styles.section}>
                                <Typography variant="h6" color="error" gutterBottom>
                                    Thông số được sử dụng để đưa ra đề xuất
                                </Typography>
                                
                                <div className={styles.logDataContainer}>
                                    {logData.map((item, index) => (
                                        <div key={index} className={`${styles.logItem} ${item.isHighlighted ? styles.highlightedLogItem : ''}`}>
                                            <span className={styles.logLabel}>{item.label}:</span>
                                            <span className={styles.logValue}>{item.value}</span>
                                        </div>
                                    ))}
                                </div>
                            </Paper>
                            
                            {/* Hiển thị giải thích từ API */}
                            {explanationText && (
                                <Paper elevation={1} className={styles.section}>
                                    <Typography variant="h6" color="error" gutterBottom>
                                        Giải thích đề xuất
                                    </Typography>
                                    <Typography paragraph style={{ whiteSpace: 'pre-line' }}>
                                        {explanationText}
                                    </Typography>
                                </Paper>
                            )}
                            
                            {/* Hiển thị thông tin khóa học đề xuất */}
                            <Typography variant="h6" color="error" gutterBottom className={styles.sectionTitle}>
                                Khóa học đề xuất
                            </Typography>
                            
                            <div className={styles.courseContainer}>
                                {recommendedCourse ? (
                                    <Paper 
                                        elevation={2}
                                        className={styles.courseCard}
                                    >
                                        <div className={styles.courseHeader}>
                                            <Typography variant="subtitle1" color="error" fontWeight="bold">
                                                {recommendedCourse.name || "Khóa học được đề xuất"}
                                            </Typography>
                                            <Chip 
                                                label={recommendedCourse.courseType?.name || "General"}
                                                size="small"
                                                color="error"
                                                variant="outlined"
                                            />
                                        </div>
                                        <Divider className={styles.divider} />
                                        <Typography variant="body2" paragraph>
                                            {recommendedCourse.description || "Khóa học được cá nhân hóa dựa trên thông tin của bạn"}
                                        </Typography>
                                        <div className={styles.courseFooter}>
                                            <Typography variant="body2">
                                                <strong>Thời gian:</strong> {recommendedCourse.duration || "N/A"}
                                            </Typography>
                                            <Typography variant="body2" color="error">
                                                Được đề xuất cho hồ sơ của bạn
                                            </Typography>
                                        </div>
                                    </Paper>
                                ) : (
                                    <Alert severity="info" className={styles.alert}>
                                        <strong>Không tìm thấy thông tin khóa học đề xuất.</strong> 
                                        <p>Có thể có lỗi khi lưu dữ liệu hoặc tạo đề xuất. Các lý do có thể bao gồm:</p>
                                        <ul>
                                            <li>Dữ liệu không phù hợp với định dạng yêu cầu</li>
                                            <li>Lỗi kết nối với máy chủ</li>
                                            <li>Không có khóa học phù hợp với tiêu chí của bạn</li>
                                        </ul>
                                        <p>Bạn vẫn có thể xem danh sách khóa học đề xuất bằng cách nhấn nút bên dưới.</p>
                                    </Alert>
                                )}
                            </div>
                            
                            {/* Phần xác nhận */}
                            <div className={styles.confirmationBox}>
                                <Typography variant="body1" gutterBottom>
                                    <strong>{recommendedCourse ? 'Xác nhận lưu thông tin và đăng ký khóa học này?' : 'Xác nhận lưu thông tin và xem danh sách khóa học?'}</strong>
                                </Typography>
                                <Typography variant="body2" color="text.secondary" gutterBottom>
                                    {recommendedCourse ? 'Khi xác nhận, thông tin của bạn sẽ được lưu vào cơ sở dữ liệu và khóa học sẽ được thêm vào danh sách của bạn' : 'Xác nhận để lưu thông tin vào cơ sở dữ liệu và chuyển đến trang danh sách khóa học'}
                                </Typography>
                                
                                <div className={styles.buttonGroup}>
                                    <Button
                                        variant="outlined"
                                        onClick={handlePrevious}
                                        className={styles.backButton}
                                        disabled={confirmLoading || confirmed}
                                    >
                                        Quay lại
                                    </Button>
                                    
                                    <Button
                                        variant="contained"
                                        className={styles.confirmButton}
                                        onClick={handleConfirmRecommendation}
                                        disabled={confirmLoading || confirmed}
                                    >
                                        {confirmLoading ? (
                                            <CircularProgress size={24} color="inherit" />
                                        ) : (
                                            <>
                                                {recommendedCourse ? 
                                                    'Xác nhận & đăng ký khóa học' : 
                                                    'Xác nhận & lưu thông tin'
                                                }
                                            </>
                                        )}
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Step7;