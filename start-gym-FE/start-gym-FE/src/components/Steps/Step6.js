import React, { useState } from 'react';
import styles from './Step6.module.css';

import Button from '@mui/material/Button';
import { Divider } from '@mui/material';
import BodyImageAnalyzer from '../BodyImageAnalyzer';
import UserFormService from '../../services/UserFormService';

const Step6 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [bodyImage, setBodyImage] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    
    // Xử lý khi phân tích ảnh hoàn tất hoặc khi các trường đo lường được cập nhật
    const handleAnalysisComplete = (bodyType, measurementChange = null) => {
        // Update body type
        handleChange({
            target: {
                name: 'bodyType',
                value: bodyType,
            },
        });
        
        // If we have measurement changes, update those too
        if (measurementChange) {
            handleChange(measurementChange);
        }
    };

    const handleSubmit = async () => {
        // Prevent double submission
        if (submitting) return;
        
        setSubmitting(true);
        console.log('Step6: handleSubmit called');
        
        // If bodyImage exists, include it
        if (bodyImage) {
            handleChange({
                target: {
                    name: 'bodyImage',
                    value: bodyImage,
                },
            });
        }
        
        console.log('Submitting form with body analysis data:', {
            bodyType: formData.bodyType,
            waist_circumference: formData.waistSize,
            chest_circumference: formData.chestSize
        });
        
        try {
            // Helper to map training frequencies to exact enum values required by database
            const EXACT_ENUM_VALUES = {
                TRAINING_FREQUENCY: ['Never', 'Occasionally', 'Moderate', 'Regularly', 'Advanced'],
                OCCUPATION: ['Student', 'Office worker', 'Delivery staff', 'Lecturer', 'Service staff', 'Other'],
                WORK_ENVIRONMENT: ['Office', 'Shift work', 'Standing-intensive', 'Walking-intensive', 'Driving-intensive', 'Manual labor'],
                TRAINING_GOALS: ['1, Strength Training', '2, Cardiovascular Training', '3, Flexibility and Balance',
                    '4, Train strength and endurance', '5, Mind-Body Wellness', '6, Functional Training', '7, Weight Loss']
            };
            
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
                'Văn phòng': 'Office worker',
                'Giao hàng': 'Delivery staff',
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
                'Đi bộ nhiều': 'Walking-intensive',
                'Lái xe': 'Driving-intensive',
                'Lao động tay chân': 'Manual labor'
            };
            
            // Map training goals values to database ENUM values
            // ENUM('1, Strength Training','2, Cardiovascular Training','3, Flexibility and Balance',
            //      '4, Train strength and endurance','5, Mind-Body Wellness','6, Functional Training','7, Weight Loss')
            const trainingGoalMap = {
                'Tăng cơ': '1, Strength Training',
                'Tăng sức bền tim mạch': '2, Cardiovascular Training',
                'Dẻo dai và thăng bằng': '3, Flexibility and Balance',
                'Tăng sức mạnh và sức bền': '4, Train strength and endurance',
                'Sức khỏe tinh thần': '5, Mind-Body Wellness',
                'Huấn luyện chức năng': '6, Functional Training',
                'Giảm cân tổng thể': '7, Weight Loss'
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
                training_goals: (trainingGoalMap[formData.trainingGoal] || '7, Weight Loss').trim(),
                occupation: (occupationMap[formData.occupation] || 'Student').trim(),
                work_environment: (workEnvironmentMap[formData.workEnvironment] || 'Office').trim(),
                chest_circumference: formData.chestSize, 
                waist_circumference: formData.waistSize,
                customerId: formData.customerId
            };
            
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
            
            // Call API directly from here
            const result = await UserFormService.submitFormData(apiFormData);
            console.log('Step6: API response received:', result);
            
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
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepFull}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumber}>6</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#CF4044" />
                        </svg>
                    </div>
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>7</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                </div>
                <div className={styles.line}></div>
                <h4>6. AI Phân tích cơ thể</h4>
                <div className={styles.formContainer}>
                    <Divider sx={{ my: 3 }} />
                    
                    {/* Component phân tích ảnh */}
                    <BodyImageAnalyzer 
                        formData={formData} 
                        onAnalysisComplete={handleAnalysisComplete}
                    />
                </div>
                <div className={styles.btnGroup}>
                    <Button
                        variant="outlined"
                        onClick={handlePrevious}
                        sx={{ marginTop: 10, borderRadius: 55, color: '#CF4044', border: '1px solid #CF4044' }}
                        color="error"
                        disabled={submitting}
                    >
                        Bước trước
                    </Button>
                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        sx={{ 
                            marginTop: 10, 
                            borderRadius: 55, 
                            color: '#fff', 
                            backgroundColor: submitting ? '#999' : '#CF4044',
                            position: 'relative',
                            minWidth: '180px'
                        }}
                        color="error"
                        disabled={submitting}
                    >
                        {submitting ? (
                            <>
                                <span style={{ marginRight: '10px' }}>Đang xử lý...</span>
                                <div className={styles.spinner} style={{
                                    width: '20px',
                                    height: '20px',
                                    border: '2px solid rgba(255,255,255,0.2)',
                                    borderTop: '2px solid #fff',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite',
                                    position: 'absolute',
                                    right: '20px'
                                }}></div>
                            </>
                        ) : 'Hoàn thành'}
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step6;