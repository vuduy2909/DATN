import React, { useState } from 'react';
import styles from './Step6.module.css';

import Button from '@mui/material/Button';
import { Divider } from '@mui/material';
import BodyImageAnalyzer from '../BodyImageAnalyzer';

const Step6 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [bodyImage, setBodyImage] = useState(null);
    
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

    const handleSubmit = () => {
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
            waist_circumference: formData.waist_circumference,
            chest_circumference: formData.chest_circumference
        });
        handleNext();
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
                    >
                        Bước trước
                    </Button>
                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        sx={{ marginTop: 10, borderRadius: 55, color: '#fff', backgroundColor: '#CF4044' }}
                        color="error"
                    >
                        Hoàn thành
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step6;