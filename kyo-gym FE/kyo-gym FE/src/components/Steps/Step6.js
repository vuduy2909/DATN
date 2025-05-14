import React, { useState } from 'react';
import styles from './Step6.module.css';

import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Button from '@mui/material/Button';

const Step6 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [bust, setBust] = useState('');
    const [bustError, setBustError] = useState('');
    const [waist, setWaist] = useState('');
    const [waistError, setWaistError] = useState('');
  //  const [currentStep, setCurrentStep] = useState(6);

    const handleBustChange = (e) => {
        const value = e.target.value;
        setBust(value);

        if (value < 70 || value > 120) {
            setBustError('Range from 70 -> 120cm');
        } else {
            setBustError('');
        }
    };

    const handleWaistChange = (e) => {
        const value = e.target.value;
        setWaist(value);

        if (value < 50 || value > 100) {
            setWaistError('Range from 50 -> 100cm');
        } else {
            setWaistError('');
        }
    };

    const handleSubmit = () => {
        handleChange({
            target: {
                name: 'bust',
                value: bust,
            },
        });
        handleChange({
            target: {
                name: 'waist',
                value: waist,
            },
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
                        <p className={styles.stepNumberNon}>6</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                </div>
                <div className={styles.line}></div>
                <h4>Last step! Enter your body measurements</h4>
                <div className={styles.formContainer}>
                    <TextField
                        type="number"
                        label="Bust (Chest) Measurement"
                        variant="outlined"
                        fullWidth
                        value={bust}
                        onChange={handleBustChange}
                        error={!!bustError}
                        helperText={bustError}
                        color="error"
                        required
                        sx={{ mb: 3 }}
                        InputProps={{
                            endAdornment: <InputAdornment position="end">cm</InputAdornment>,
                        }}
                    />
                    <TextField
                        type="number"
                        label="Waist Measurement"
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
                </div>
                <div className={styles.btnGroup}>
                    <Button
                        variant="outlined"
                        onClick={handlePrevious}
                        sx={{ marginTop: 10, borderRadius: 55, color: '#CF4044', border: '1px solid #CF4044' }}
                        color="error"
                    >
                        Previous step
                    </Button>
                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        sx={{ marginTop: 10, borderRadius: 55, color: '#fff', backgroundColor: '#CF4044' }}
                        color="error"
                    >
                        Complete
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step6;