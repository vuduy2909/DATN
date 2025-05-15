import React, { useState, useEffect } from 'react';
import styles from './Step6.module.css'; // Re-use CSS từ Step6
import Button from '@mui/material/Button';
import { Paper, Typography, Box, List, ListItem, ListItemText, Divider, Chip } from '@mui/material';

const Step7 = ({ formData, handlePrevious }) => {
    const [analysisComplete, setAnalysisComplete] = useState(false);
    const [bodyAnalysis, setBodyAnalysis] = useState('');
    const [goalFeedback, setGoalFeedback] = useState('');
    const [recommendedCourses, setRecommendedCourses] = useState([]);

    useEffect(() => {
        // Giả lập quá trình phân tích dữ liệu
        setTimeout(() => {
            analyzeUserData();
            setAnalysisComplete(true);
        }, 1500);
    }, []);

    const analyzeUserData = () => {
        // Phân tích hình thể dựa trên BMI và body type
        const bmi = parseFloat(formData.weight) / Math.pow(parseFloat(formData.height) / 100, 2);
        let bodyShape = '';
        
        if (bmi < 18.5) {
            bodyShape = "thiên về gầy";
        } else if (bmi >= 18.5 && bmi < 25) {
            bodyShape = "cân đối";
        } else if (bmi >= 25 && bmi < 30) {
            bodyShape = "hơi thừa cân";
        } else {
            bodyShape = "béo phì";
        }
        
        setBodyAnalysis(`Thân hình của bạn ${bodyShape} với chỉ số BMI là ${bmi.toFixed(1)}. ` +
            `Với chiều cao ${formData.height}cm và cân nặng ${formData.weight}kg, ` +
            `hình dạng cơ thể của bạn thuộc loại ${formData.bodyType}.`);
        
        // Phân tích mục tiêu và tần suất tập luyện
        let goalCompatibility = '';
        if (formData.trainingFrequency === "Không tập" && 
            (formData.desiredFrequency === "4 buổi/tuần" || formData.desiredFrequency === "5 buổi/tuần")) {
            goalCompatibility = "Bạn đang chuyển từ không tập luyện sang tập luyện với cường độ cao. " +
                "Nên bắt đầu từ từ với 2-3 buổi/tuần trong vài tuần đầu để cơ thể thích nghi.";
        } else if (formData.trainingFrequency === "2 buổi/tuần" && formData.desiredFrequency === "5 buổi/tuần") {
            goalCompatibility = "Việc tăng từ 2 buổi lên 5 buổi/tuần có thể gây quá tải. " +
                "Hãy cân nhắc tăng dần lên 3-4 buổi/tuần trước.";
        } else {
            goalCompatibility = `Với tần suất tập luyện hiện tại là ${formData.trainingFrequency} và mong muốn ` +
                `tập ${formData.desiredFrequency}, mục tiêu ${formData.trainingGoal} của bạn là hoàn toàn khả thi.`;
        }
        
        setGoalFeedback(goalCompatibility);
        
        // Đề xuất khóa học dựa trên mục tiêu, tần suất và tình trạng sức khỏe
        const courseRecommendations = [];
        
        if (formData.trainingGoal === "Giảm cân tổng thể" || formData.trainingGoal === "Giảm mỡ, giữ cơ") {
            courseRecommendations.push({
                type: "Weight Loss & Fat Burning",
                title: "Giảm cân toàn diện",
                description: "Chương trình 12 tuần kết hợp cardio cường độ cao và sức mạnh cốt lõi",
                intensity: "Trung bình đến cao",
                compatibility: "Rất phù hợp với mục tiêu của bạn"
            });
            courseRecommendations.push({
                type: "Cardiovascular Training",
                title: "Cardio đốt mỡ",
                description: "Tăng cường trao đổi chất và đốt cháy mỡ thừa hiệu quả",
                intensity: "Trung bình",
                compatibility: "Phù hợp với người mới bắt đầu"
            });
        }
        
        if (formData.trainingGoal === "Tăng cơ bắp" || formData.trainingGoal === "Tăng sức mạnh") {
            courseRecommendations.push({
                type: "Strength Training",
                title: "Xây dựng cơ bắp cơ bản",
                description: "Tập trung vào các bài tập compound để phát triển sức mạnh và cơ bắp",
                intensity: "Trung bình đến cao",
                compatibility: "Tối ưu cho mục tiêu tăng cơ của bạn"
            });
        }
        
        if (formData.trainingGoal === "Cải thiện sức chịu đựng") {
            courseRecommendations.push({
                type: "Train Strength and Endurance",
                title: "Rèn luyện sức bền",
                description: "Kết hợp các bài tập sức bền và sức mạnh để nâng cao khả năng chịu đựng",
                intensity: "Cao",
                compatibility: "Phù hợp với mục tiêu của bạn"
            });
        }
        
        if (formData.trainingGoal === "Tăng tính linh hoạt") {
            courseRecommendations.push({
                type: "Flexibility and Balance",
                title: "Yoga & Stretch",
                description: "Tăng cường tính linh hoạt và cân bằng cơ thể",
                intensity: "Thấp đến trung bình",
                compatibility: "Hoàn hảo cho mục tiêu tăng tính linh hoạt"
            });
        }
        
        if (formData.trainingGoal === "Duy trì vóc dáng") {
            courseRecommendations.push({
                type: "Functional Training",
                title: "Tập luyện chức năng",
                description: "Cải thiện sức mạnh và sức bền cho các hoạt động hàng ngày",
                intensity: "Trung bình",
                compatibility: "Phù hợp với mục tiêu duy trì vóc dáng"
            });
        }
        
        // Điều chỉnh đề xuất dựa trên tiền sử bệnh lý
        if (formData.healthIssues && formData.healthIssues.includes('heart')) {
            courseRecommendations.push({
                type: "Mind-Body Wellness",
                title: "Tập luyện an toàn cho tim mạch",
                description: "Chương trình được thiết kế đặc biệt cho người có tiền sử bệnh tim",
                intensity: "Thấp",
                compatibility: "Được điều chỉnh cho tình trạng sức khỏe của bạn"
            });
        }
        
        setRecommendedCourses(courseRecommendations);
    };

    return (
        <div className={styles.groupCard}>
            <div className={styles.wrapCard}>
                <div className={styles.loadSteps}>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>1</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>2</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>3</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>4</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>5</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>6</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>7</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.2012" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                </div>
                <div className={styles.line}></div>
                <h4>7. Phân tích và đề xuất chương trình</h4>
                
                {!analysisComplete ? (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <Typography variant="h6">Đang phân tích dữ liệu...</Typography>
                        <div className={styles.loadingSpinner}></div>
                    </Box>
                ) : (
                    <Box sx={{ mt: 3 }}>
                        <Paper elevation={1} sx={{ p: 3, mb: 3 }}>
                            <Typography variant="h6" color="error" gutterBottom>
                                Phân tích hình thể
                            </Typography>
                            <Typography paragraph>
                                {bodyAnalysis}
                            </Typography>
                        </Paper>
                        
                        <Paper elevation={1} sx={{ p: 3, mb: 3 }}>
                            <Typography variant="h6" color="error" gutterBottom>
                                Đánh giá mục tiêu và tần suất tập luyện
                            </Typography>
                            <Typography paragraph>
                                {goalFeedback}
                            </Typography>
                        </Paper>
                        
                        <Typography variant="h6" color="error" gutterBottom sx={{ mt: 4, mb: 2 }}>
                            Khóa học đề xuất
                        </Typography>
                        
                        {recommendedCourses.map((course, index) => (
                            <Paper 
                                key={index} 
                                elevation={1} 
                                sx={{ 
                                    p: 3, 
                                    mb: 2,
                                    border: '1px solid #f5f5f5',
                                    '&:hover': {
                                        boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
                                        cursor: 'pointer'
                                    }
                                }}
                            >
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                    <Typography variant="h6">
                                        {course.title}
                                    </Typography>
                                    <Chip 
                                        label={course.type} 
                                        color="error" 
                                        variant="outlined" 
                                        size="small" 
                                    />
                                </Box>
                                <Typography color="text.secondary" paragraph>
                                    {course.description}
                                </Typography>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <Typography variant="body2">
                                        <strong>Cường độ:</strong> {course.intensity}
                                    </Typography>
                                    <Typography variant="body2" color="error">
                                        {course.compatibility}
                                    </Typography>
                                </Box>
                            </Paper>
                        ))}
                    </Box>
                )}
                
                <div className={styles.btnGroup}>
                    <Button
                        variant="outlined"
                        onClick={handlePrevious}
                        sx={{ marginTop: 10, borderRadius: 55, color: '#CF4044', border: '1px solid #CF4044' }}
                        color="error"
                    >
                        Bước trước
                    </Button>
                    <Button
                        variant="contained"
                        sx={{ marginTop: 10, borderRadius: 55, color: '#fff', backgroundColor: '#CF4044' }}
                        color="error"
                        href="/courses"
                    >
                        Xem tất cả khóa học
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step7;