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
import Checkbox from '@mui/material/Checkbox';
import FormGroup from '@mui/material/FormGroup';

const Step5 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [occupation, setOccupation] = useState('');
    const [workEnvironment, setWorkEnvironment] = useState('');
    const [healthIssues, setHealthIssues] = useState([]);

    const occupationOptions = [
        'Sinh viên', 
        'Nhân viên văn phòng', 
        'Nhân viên giao hàng', 
        'Giảng viên', 
        'Nhân viên dịch vụ', 
        'Khác'
    ];

    const workEnvironmentOptions = [
        'Văn phòng', 
        'Đứng nhiều', 
        'Đi lại nhiều', 
        'Lái xe nhiều', 
        'Lao động chân tay'
    ];

    const healthIssueOptions = [
        { value: 'heart', label: 'Bệnh tim mạch' },
        { value: 'respiratory', label: 'Vấn đề về hô hấp' },
        { value: 'injury', label: 'Chấn thương xương khớp' }
    ];

    const handleHealthIssueChange = (event) => {
        const { value, checked } = event.target;
        
        if (checked) {
            setHealthIssues([...healthIssues, value]);
        } else {
            setHealthIssues(healthIssues.filter(issue => issue !== value));
        }
    };

    const handleSubmit = () => {
        handleChange({
            target: { name: 'occupation', value: occupation }
        });
        
        handleChange({
            target: { name: 'workEnvironment', value: workEnvironment }
        });
        
        handleChange({
            target: { name: 'healthIssues', value: healthIssues }
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
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>6</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
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
                <h4>5. Thông tin về nghề nghiệp và sức khỏe</h4>

                <div className={styles.formContainer}>
                    <FormControl fullWidth sx={{ mb: 3 }}>
                        <InputLabel color="error">Nghề nghiệp</InputLabel>
                        <Select
                            value={occupation}
                            label="Nghề nghiệp"
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
                        <InputLabel color="error">Môi trường làm việc</InputLabel>
                        <Select
                            value={workEnvironment}
                            label="Môi trường làm việc"
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
                            Tiền sử bệnh lý (Có thể chọn nhiều)
                        </FormLabel>
                        <FormGroup>
                            {healthIssueOptions.map((option) => (
                                <FormControlLabel
                                    key={option.value}
                                    control={
                                        <Checkbox 
                                            checked={healthIssues.includes(option.value)}
                                            onChange={handleHealthIssueChange}
                                            value={option.value}
                                            color="error"
                                        />
                                    }
                                    label={option.label}
                                />
                            ))}
                        </FormGroup>
                    </FormControl>
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
                        Bước tiếp theo
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Step5;