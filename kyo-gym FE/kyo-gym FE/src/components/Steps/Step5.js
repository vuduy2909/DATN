import React, { useState } from 'react';
import styles from './Step5.module.css';

import FormControl from '@mui/material/FormControl';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormLabel from '@mui/material/FormLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';

const Step5 = ({ formData, handleChange, handleNext, handlePrevious, currentStep }) => {
    const [occupation, setOccupation] = useState('');
    const [workEnvironment, setWorkEnvironment] = useState('');
    const [heartCondition, setHeartCondition] = useState('');
    const [respiratoryCondition, setRespiratoryCondition] = useState('');
    const [injury, setInjury] = useState('');

    const occupationOptions = [
        'Student', 
        'Office worker', 
        'Lecturer', 
        'Delivery staff', 
        'Service staff', 
        'Other'
    ];

    const workEnvironmentOptions = [
        'Office', 
        'Remote work', 
        'Shift work', 
        'Walking-intensive', 
        'Vehicle-based'
    ];

    const handleSubmit = () => {
        const formData = {
            occupation,
            work_environment: workEnvironment,
            heart_condition: heartCondition,
            respiratory_condition: respiratoryCondition,
            injury
        };

        Object.entries(formData).forEach(([name, value]) => {
            handleChange({
                target: { name, value }
            });
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
                        <p className={styles.stepNumberNon}>5</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>6</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                </div>

                <div className={styles.line}></div>
                <h4>Tell us about your occupation and health history</h4>

                <div className={styles.formContainer}>
                    <FormControl fullWidth sx={{ mb: 3 }}>
                        <InputLabel color="error">Occupation</InputLabel>
                        <Select
                            value={occupation}
                            label="Occupation"
                            onChange={(e) => setOccupation(e.target.value)}
                            color="error"
                            required
                        >
                            {occupationOptions.map((option) => (
                                <MenuItem key={option} value={option}>
                                    {option}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth sx={{ mb: 3 }}>
                        <InputLabel color="error">Work Environment</InputLabel>
                        <Select
                            value={workEnvironment}
                            label="Work Environment"
                            onChange={(e) => setWorkEnvironment(e.target.value)}
                            color="error"
                            required
                        >
                            {workEnvironmentOptions.map((option) => (
                                <MenuItem key={option} value={option}>
                                    {option}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    
                    <FormControl component="fieldset" sx={{ mb: 3, width: '100%' }}>
                        <FormLabel component="legend" sx={{ color: '#CF4044' }}>
                            Do you have any heart conditions?
                        </FormLabel>
                        <RadioGroup
                            value={heartCondition}
                            onChange={(e) => setHeartCondition(e.target.value)}
                            row
                        >
                            <FormControlLabel value="yes" control={<Radio color="error" />} label="Yes" />
                            <FormControlLabel value="no" control={<Radio color="error" />} label="No" />
                        </RadioGroup>
                    </FormControl>

                    <FormControl component="fieldset" sx={{ mb: 3, width: '100%' }}>
                        <FormLabel component="legend" sx={{ color: '#CF4044' }}>
                            Do you have any respiratory conditions?
                        </FormLabel>
                        <RadioGroup
                            value={respiratoryCondition}
                            onChange={(e) => setRespiratoryCondition(e.target.value)}
                            row
                        >
                            <FormControlLabel value="yes" control={<Radio color="error" />} label="Yes" />
                            <FormControlLabel value="no" control={<Radio color="error" />} label="No" />
                        </RadioGroup>
                    </FormControl>

                    <FormControl component="fieldset" sx={{ mb: 3, width: '100%' }}>
                        <FormLabel component="legend" sx={{ color: '#CF4044' }}>
                            Are you recovering from any injuries?
                        </FormLabel>
                        <RadioGroup
                            value={injury}
                            onChange={(e) => setInjury(e.target.value)}
                            row
                        >
                            <FormControlLabel value="yes" control={<Radio color="error" />} label="Yes" />
                            <FormControlLabel value="no" control={<Radio color="error" />} label="No" />
                        </RadioGroup>
                    </FormControl>
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
                        Next step
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step5;