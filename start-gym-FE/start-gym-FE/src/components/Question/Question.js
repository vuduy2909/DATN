import React, { useState, useEffect } from 'react';
import { Snackbar, Alert } from '@mui/material';
import { collectionService } from '../../service/collectionService';
import Step1 from '../Steps/Step1';
import Step2 from '../Steps/Step2';
import Step3 from '../Steps/Step3';
import Step4 from '../Steps/Step4';
import Step5 from '../Steps/Step5';
import Step6 from '../Steps/Step6';
import Step7 from '../Steps/Step7';

import { useAuth } from '../../AuthContext'
import { request } from '../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const Question = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [recommendationResult, setRecommendationResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Snackbar state for notifications
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: '',
        severity: 'info' // 'error', 'warning', 'info', 'success'
    });

    const navigate = useNavigate();

    const showSnackbar = (message, severity = 'info') => {
        setSnackbar({
            open: true,
            message,
            severity
        });
    };

    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackbar({ ...snackbar, open: false });
    };

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await request.get('/customer/detail')
                setUserData(res)
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        if (user) {
            fetchUserData();
        }
    }, [user])

    const [formData, setFormData] = useState({
        age: '',
        gender: '',
        bmi: '',
        training_goals: '',
        training_history: '',
        occupation: '',
        work_environment: '',
        customerId: undefined
    });

    const [currentStep, setCurrentStep] = useState(1);

    const handleChange = (e) => {
        setFormData((prevFormData) => ({
            ...prevFormData,
            [e.target.name]: e.target.value,
            customerId: userData.customerId
        }));
    };

    const handleNext = async (resultFromStep6) => {
        if (currentStep === 6) {
            if (resultFromStep6) {
                console.log('Question.js: Received result from Step6:', resultFromStep6);
                
                // Check if we have formData from Step6 response
                if (resultFromStep6.formData && resultFromStep6.recommendedCourses) {
                    console.log('Question.js: Found formData and course, preparing Step7 data');
                    
                    try {
                        // Import UserFormService for Step7 call
                        const UserFormService = (await import('../../services/UserFormService')).default;
                        
                        // Convert formData to JSON string for API call
                        const formDataJson = JSON.stringify(resultFromStep6.formData);
                        const recommendedCourseId = resultFromStep6.recommendedCourses.courseId;
                        
                        console.log('Question.js: resultFromStep6.formData keys:', Object.keys(resultFromStep6.formData));
                        console.log('Question.js: resultFromStep6.formData:', resultFromStep6.formData);
                        console.log('=== QUESTION.JS: CHECKING FOR PROBLEMATIC FIELDS ===');
                        console.log('resultFromStep6.formData.trainingGoal:', resultFromStep6.formData.trainingGoal);
                        console.log('resultFromStep6.formData.training_goal:', resultFromStep6.formData.training_goal);
                        console.log('resultFromStep6.formData.training_goals:', resultFromStep6.formData.training_goals);
                        console.log('Question.js: Calling Step7 with formData and courseId:', formDataJson, recommendedCourseId);
                        
                        // Get complete recommendation data with userMetrics including vòng eo/ngực
                        const step7Data = await UserFormService.getRecommendationStep7DataNew(formDataJson, recommendedCourseId);
                        console.log('Question.js: Received complete Step7 data:', step7Data);
                        
                        // Set the complete recommendation result for Step7
                        setRecommendationResult(step7Data);
                        
                    } catch (error) {
                        console.error('Question.js: Error fetching Step7 data:', error);
                        
                        // Fall back to basic recommendation result
                        const basicRecommendationResult = {
                            recommendedCourse: resultFromStep6.recommendedCourses,
                            userMetrics: resultFromStep6.formData || {}, // Use formData as userMetrics fallback
                            explanation: 'Không thể tải thông tin chi tiết. Dữ liệu cơ bản vẫn có sẵn.',
                            actions: {
                                formData: resultFromStep6.formData,
                                recommendedCourseId: resultFromStep6.recommendedCourses?.courseId
                            }
                        };
                        
                        console.log('Question.js: Using fallback recommendation result:', basicRecommendationResult);
                        setRecommendationResult(basicRecommendationResult);
                    }
                } else {
                    // No formData, use basic result
                    console.log('Question.js: No formData found, using basic result');
                    const basicRecommendationResult = {
                        recommendedCourse: resultFromStep6.recommendedCourses,
                        userMetrics: {},
                        explanation: 'Kết quả đề xuất cơ bản dựa trên dữ liệu form.',
                        actions: {
                            formData: null,
                            recommendedCourseId: resultFromStep6.recommendedCourses?.courseId
                        }
                    };
                    
                    setRecommendationResult(basicRecommendationResult);
                }
                
                setCurrentStep(7);
            } else {
                handleSubmit();
            }
        } else {
            setCurrentStep(currentStep + 1);
        }
    };

    const handlePrevious = () => {
        setCurrentStep(currentStep - 1);
    };

    const handleGoToCollections = () => {
        navigate('/collections');
    };

    const handleSubmit = async () => {
        console.log("Form data before submission:", formData);
        
        // Check if any required fields are missing
        const requiredFields = ['age', 'gender', 'bmi', 'customerId'];
        const missingFields = requiredFields.filter(field => formData[field] === '');
        
        if (missingFields.length > 0) {
            console.error("Missing required fields:", missingFields);
            showSnackbar(`Vui lòng điền đầy đủ thông tin: ${missingFields.join(', ')}`, 'warning');
            return;
        }
        
        setLoading(true);
        setError('');
        
        try {
            console.log("Submitting survey data...");
            const survey = await collectionService.postRecommend(formData);
            console.log("Survey response:", survey);
            
            if (survey && survey.recommendedCourses) {
                setRecommendationResult({
                    recommendedCourses: survey.recommendedCourses,
                    userMetrics: formData || {},
                    userDataId: survey.userDataId,
                    actions: {
                        formData: formData,
                        recommendedCourseId: survey.recommendedCourses.courseId
                    }
                });
                setCurrentStep(7); // Move to step 7 instead of navigating
            } else {
                console.error("No recommended courses returned:", survey);
                setError(survey?.message || 'Không có khóa học phù hợp!');
            }
        } catch (error) {
            console.error('Error submitting survey data:', error);
            if (error.response) {
                console.error('Server error response:', error.response.data);
                setError(`Lỗi: ${error.response.data?.message || 'Có lỗi xảy ra khi gửi dữ liệu!'}`);
            } else {
                setError('Có lỗi xảy ra khi gửi dữ liệu!');
            }
        } finally {
            setLoading(false);
        }
    };

    const renderStep = () => {
        switch (currentStep) {
            case 1:
                return (
                    <Step1 formData={formData} handleChange={handleChange} handleNext={handleNext} />
                );
            case 2:
                return (
                    <Step2 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 3:
                return (
                    <Step3 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 4:
                return (
                    <Step4 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 5:
                return (
                    <Step5 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} currentStep={currentStep} />
                );
            case 6:
                return (
                    <Step6 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} currentStep={currentStep} />
                );
            case 7:
                return (
                    <Step7
                        formData={formData}
                        handlePrevious={handlePrevious}
                        handleGoToCollections={handleGoToCollections}
                        recommendationResult={recommendationResult}
                        loading={loading}
                        error={error}
                    />
                );
            default:
                return null;
        }
    };

    return (
        <div>
            {renderStep()}
            
            {/* Snackbar for notifications */}
            <Snackbar 
                open={snackbar.open} 
                autoHideDuration={4000} 
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert 
                    onClose={handleSnackbarClose} 
                    severity={snackbar.severity} 
                    sx={{ width: '100%' }}
                    variant="filled"
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </div>
    );
};

export default Question;
