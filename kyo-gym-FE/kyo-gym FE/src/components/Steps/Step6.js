import React, { useState } from 'react';
import styles from './Step6.module.css';

import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Button from '@mui/material/Button';
import { Typography, Box, Divider } from '@mui/material';
import BodyImageAnalyzer from '../BodyImageAnalyzer';

const Step6 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [chest, setChest] = useState('');
    const [chestError, setChestError] = useState('');
    const [waist, setWaist] = useState('');
    const [waistError, setWaistError] = useState('');
    const [bodyImage, setBodyImage] = useState(null);

    const handleChestChange = (e) => {
        const value = e.target.value;
        setChest(value);

        if (value < 70 || value > 150) {
            setChestError('Khoảng hợp lệ từ 70 -> 150cm');
        } else {
            setChestError('');
        }
    };

    const handleWaistChange = (e) => {
        const value = e.target.value;
        setWaist(value);

        if (value < 50 || value > 140) {
            setWaistError('Khoảng hợp lệ từ 50 -> 140cm');
        } else {
            setWaistError('');
        }
    };
    
    // Xử lý khi phân tích ảnh hoàn tất
    const handleAnalysisComplete = (bodyType) => {
        handleChange({
            target: {
                name: 'bodyType',
                value: bodyType,
            },
        });
    };

    const handleSubmit = () => {
        handleChange({
            target: {
                name: 'chestSize',
                value: chest,
            },
        });
        handleChange({
            target: {
                name: 'waistSize',
                value: waist,
            },
        });
        if (bodyImage) {
            handleChange({
                target: {
                    name: 'bodyImage',
                    value: bodyImage,
                },
            });
        }
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
                <h4>6. Số đo cơ thể</h4>
                <div className={styles.formContainer}>
                    <TextField
                        type="number"
                        label="Vòng ngực"
                        variant="outlined"
                        fullWidth
                        value={chest}
                        onChange={handleChestChange}
                        error={!!chestError}
                        helperText={chestError}
                        color="error"
                        required
                        sx={{ mb: 3 }}
                        InputProps={{
                            endAdornment: <InputAdornment position="end">cm</InputAdornment>,
                        }}
                    />
                    <TextField
                        type="number"
                        label="Vòng eo"
                        variant="outlined"
                        fullWidth
                        value={waist}
                        onChange={handleWaistChange}
                        error={!!waistError}
                        helperText={waistError}
                        color="error"
                        required
                        sx={{ mb: 3 }}
                        InputProps={{
                            endAdornment: <InputAdornment position="end">cm</InputAdornment>,
                        }}
                    />
                    
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