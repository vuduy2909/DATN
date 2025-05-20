import React, { useState } from 'react'
import styles from './Step2.module.css'

import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import Button from '@mui/material/Button';

const Step2 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [height, setHeight] = useState('');
    const [heightError, setHeightError] = useState('');
    const [weight, setWeight] = useState('');
    const [weightError, setWeightError] = useState('');
    const [age, setAge] = useState('');
    const [ageError, setAgeError] = useState('');
    const [gender, setGender] = useState('');

    const handleHeightChange = (e) => {
        const value = e.target.value;
        setHeight(value)

        if (value < 150 || value > 210) {
            setHeightError('Chiều cao hợp lệ từ 150 -> 210cm')
        } else {
            setHeightError('')
        }
    }

    const handleWeightChange = (e) => {
        const value = e.target.value;
        setWeight(value)

        if (value < 40 || value > 150) {
            setWeightError('Cân nặng hợp lệ từ 40 -> 150kg')
        } else {
            setWeightError('')
        }
    }

    const handleAgeChange = (e) => {
        const value = e.target.value;
        setAge(value)

        if (value < 15 || value > 80) {
            setAgeError('Độ tuổi hợp lệ từ 15 -> 80')
        } else {
            setAgeError('')
        }
    }

    const handleSubmit = () => {
        // Tính BMI nếu cần
        const heightValue = parseFloat(height);
        const weightValue = parseFloat(weight);
        const ageValue = parseInt(age, 10);
        let genderValue = gender;
        if (gender === 'Nam') genderValue = 'Male';
        if (gender === 'Nữ') genderValue = 'Female';

        if (heightValue > 0 && weightValue > 0 && !isNaN(heightValue) && !isNaN(weightValue)) {
            const bmiValue = (weightValue / Math.pow(heightValue/100, 2)).toFixed(1);
            handleChange({
                target: {
                    name: 'bmi',
                    value: parseFloat(bmiValue),
                },
            });
        }

        // Cập nhật các giá trị cho form
        handleChange({ target: { name: 'height', value: heightValue } });
        handleChange({ target: { name: 'weight', value: weightValue } });
        handleChange({ target: { name: 'age', value: ageValue } });
        handleChange({ target: { name: 'gender', value: genderValue } });
        
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
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>3</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>4</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                    <div className={styles.lineStepNon}></div>
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
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>7</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
                        </svg>
                    </div>
                </div>
                <div className={styles.line}></div>
                <h4>2. Thông tin cơ bản</h4>
                <div className={styles.gridCard}>
                    <TextField
                        type='number'
                        label="Tuổi"
                        id="outlined-start-adornment"
                        sx={{ m: 1, width: '25ch' }}
                        name="age"
                        value={age}
                        onChange={handleAgeChange}
                        color='error'
                        error={!!ageError}
                        helperText={ageError}
                        required
                    />
                    <FormControl sx={{ m: 1, width: '25ch' }}>
                        <InputLabel id="gender-select-label" color='error'>Giới tính</InputLabel>
                        <Select
                            labelId="gender-select-label"
                            id="gender-select"
                            value={gender}
                            label="Giới tính"
                            color='error'
                            required
                            onChange={(e) => setGender(e.target.value)}
                        >
                            <MenuItem value='Nam'>Nam</MenuItem>
                            <MenuItem value='Nữ'>Nữ</MenuItem>
                        </Select>
                    </FormControl>
                    <TextField
                        type='number'
                        label="Chiều cao"
                        id="height-input"
                        sx={{ m: 1, width: '25ch' }}
                        name="height"
                        value={height}
                        color='error'
                        onChange={handleHeightChange}
                        error={!!heightError}
                        helperText={heightError}
                        required
                        InputProps={{
                            endAdornment: <InputAdornment position="end">cm</InputAdornment>,
                        }}
                    />
                    <TextField
                        type='number'
                        label="Cân nặng"
                        id="weight-input"
                        sx={{ m: 1, width: '25ch' }}
                        name="weight"
                        value={weight}
                        color='error'
                        onChange={handleWeightChange}
                        error={!!weightError}
                        helperText={weightError}
                        required
                        InputProps={{
                            endAdornment: <InputAdornment position="end">kg</InputAdornment>,
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
}

export default Step2