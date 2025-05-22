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
            // Prepare API data format
            const apiFormData = {
                training_history: formData.trainingFrequency, // Lịch sử tập luyện hiện tại
                training_frequency: formData.desiredFrequency, // Tần suất tập luyện mong muốn
                age: formData.age,
                gender: formData.gender,
                height: formData.height,
                weight: formData.weight,
                bmi: formData.weight / Math.pow(formData.height/100, 2), // Tính BMI
                training_goals: formData.trainingGoal,
                occupation: formData.occupation,
                work_environment: formData.workEnvironment,
                health_issues: formData.healthIssues ? formData.healthIssues.join(',') : '',
                chest_circumference: formData.chestSize, 
                waist_circumference: formData.waistSize,
                customerId: formData.customerId
            };
            
            console.log('Calling API directly from Step6 with data:', apiFormData);
            
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