import React, { useState } from 'react'
import useKeyboardNavigation from '../../hooks/useKeyboardNavigation';

import {
    Box,
    Container,
    Paper,
    Typography,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    InputAdornment,
    Button,
    Stepper,
    Step,
    StepLabel,
    Chip,
    Avatar,
    Grid,
    Card,
    CardContent
} from '@mui/material';
import {
    Person,
    ArrowBack,
    ArrowForward,
    Height,
    FitnessCenter,
    Cake,
    Wc
} from '@mui/icons-material';

const Step2 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [height, setHeight] = useState(formData.height || '');
    const [heightError, setHeightError] = useState('');
    const [weight, setWeight] = useState(formData.weight || '');
    const [weightError, setWeightError] = useState('');
    const [age, setAge] = useState(formData.age || '');
    const [ageError, setAgeError] = useState('');
    const [gender, setGender] = useState(formData.gender === 'Male' ? 'Nam' : formData.gender === 'Female' ? 'Nữ' : '');

    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];

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

    const isFormValid = () => {
        return height && weight && age && gender && 
               !heightError && !weightError && !ageError &&
               height >= 150 && height <= 210 &&
               weight >= 40 && weight <= 150 &&
               age >= 15 && age <= 80;
    };

    // Thêm keyboard navigation
    useKeyboardNavigation(handleNext, handlePrevious, !isFormValid(), 2, 7);

    const calculateBMI = () => {
        const heightValue = parseFloat(height);
        const weightValue = parseFloat(weight);
        if (heightValue > 0 && weightValue > 0) {
            return (weightValue / Math.pow(heightValue/100, 2)).toFixed(1);
        }
        return null;
    };

    const getBMICategory = (bmi) => {
        if (bmi < 18.5) return { text: 'Thiếu cân', color: '#3498db' };
        if (bmi < 25) return { text: 'Bình thường', color: '#27ae60' };
        if (bmi < 30) return { text: 'Thừa cân', color: '#f39c12' };
        return { text: 'Béo phì', color: '#e74c3c' };
    };

    const handleSubmit = () => {
        if (!isFormValid()) return;

        // Tính BMI
        const heightValue = parseFloat(height);
        const weightValue = parseFloat(weight);
        const ageValue = parseInt(age, 10);
        let genderValue = gender;
        if (gender === 'Nam') genderValue = 'Male';
        if (gender === 'Nữ') genderValue = 'Female';

        if (heightValue > 0 && weightValue > 0 && !isNaN(heightValue) && !isNaN(weightValue)) {
            const bmiValue = (weightValue / Math.pow(heightValue/100, 2)).toFixed(2);
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

    const bmi = calculateBMI();
    const bmiCategory = bmi ? getBMICategory(parseFloat(bmi)) : null;

    return (
        <Box sx={{ 
            backgroundColor: '#ffffff', 
            minHeight: '100vh',
            py: 2,
            background: 'linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%)'
        }}>
            <Container maxWidth="md" sx={{ py: 4 }}>
                {/* Material UI Stepper */}
                <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 3 }}>
                    <Stepper activeStep={1} alternativeLabel
                        sx={{
                            '& .MuiStep-root': {
                                '& .MuiStepIcon-root': {
                                    color: '#e0e0e0',
                                    '&.Mui-active': { color: '#CF4044' },
                                    '&.Mui-completed': { color: '#CF4044' },
                                },
                                '& .MuiStepConnector-line': { borderColor: '#e0e0e0' },
                                '& .Mui-active ~ .MuiStepConnector-root .MuiStepConnector-line': { borderColor: '#CF4044' },
                                '& .Mui-completed ~ .MuiStepConnector-root .MuiStepConnector-line': { borderColor: '#CF4044' },
                            },
                            '& .MuiStepConnector-root': {
                                '& .MuiStepConnector-line': { borderTopWidth: '3px', borderRadius: '1px' },
                            },
                        }}
                    >
                        {steps.map((label, index) => (
                            <Step key={label}>
                                <StepLabel sx={{ '& .MuiStepLabel-label': { display: 'none' } }} />
                            </Step>
                        ))}
                    </Stepper>
                </Paper>

                {/* Main Content */}
                <Paper elevation={4} sx={{ p: 4, borderRadius: 4, background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)' }}>
                    {/* Header */}
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        <Avatar sx={{ 
                            bgcolor: '#CF4044', 
                            width: 80, 
                            height: 80, 
                            mx: 'auto', 
                            mb: 2,
                            boxShadow: '0 8px 24px rgba(207, 64, 68, 0.3)'
                        }}>
                            <Person sx={{ fontSize: 40 }} />
                        </Avatar>
                        
                        <Typography variant="h4" sx={{ 
                            fontWeight: 'bold', 
                            color: '#2c3e50', 
                            mb: 1,
                            background: 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)',
                            backgroundClip: 'text',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                        }}>
                            Thông tin cơ bản
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Cung cấp thông tin để chúng tôi tạo chương trình phù hợp
                        </Typography>
                        
                        <Chip 
                            label="Bước 2/7" 
                            color="primary" 
                            sx={{ 
                                mt: 2, 
                                bgcolor: '#CF4044',
                                fontWeight: 'bold'
                            }} 
                        />
                    </Box>

                    {/* Form Fields */}
                    <Grid container spacing={3} sx={{ mb: 4 }}>
                        {/* Age Field */}
                        <Grid item xs={12} sm={6}>
                            <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Avatar sx={{ bgcolor: '#3498db', mr: 2, width: 50, height: 50 }}>
                                            <Cake />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Độ tuổi
                                        </Typography>
                                    </Box>
                                    <TextField
                                        type='number'
                                        label="Tuổi của bạn"
                                        fullWidth
                                        name="age"
                                        value={age}
                                        onChange={handleAgeChange}
                                        error={!!ageError}
                                        helperText={ageError || "Từ 15 đến 80 tuổi"}
                                        required
                                        size="small"
                                        sx={{
                                            '& .MuiOutlinedInput-root': {
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#CF4044',
                                                },
                                            },
                                            '& .MuiInputLabel-root.Mui-focused': {
                                                color: '#CF4044',
                                            },
                                        }}
                                        InputProps={{
                                            endAdornment: <InputAdornment position="end">tuổi</InputAdornment>,
                                        }}
                                    />
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Gender Field */}
                        <Grid item xs={12} sm={6}>
                            <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Avatar sx={{ bgcolor: '#9b59b6', mr: 2, width: 50, height: 50 }}>
                                            <Wc />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Giới tính
                                        </Typography>
                                    </Box>
                                    <FormControl fullWidth required size="small">
                                        <InputLabel 
                                            sx={{
                                                '&.Mui-focused': {
                                                    color: '#CF4044',
                                                },
                                            }}
                                        >
                                            Chọn giới tính
                                        </InputLabel>
                                        <Select
                                            value={gender}
                                            label="Chọn giới tính"
                                            onChange={(e) => setGender(e.target.value)}
                                            sx={{
                                                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: '#CF4044',
                                                },
                                            }}
                                        >
                                            <MenuItem value='Nam'>👨 Nam</MenuItem>
                                            <MenuItem value='Nữ'>👩 Nữ</MenuItem>
                                        </Select>
                                    </FormControl>
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Height Field */}
                        <Grid item xs={12} sm={6}>
                            <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Avatar sx={{ bgcolor: '#27ae60', mr: 2, width: 50, height: 50 }}>
                                            <Height />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Chiều cao
                                        </Typography>
                                    </Box>
                                    <TextField
                                        type='number'
                                        label="Chiều cao của bạn"
                                        fullWidth
                                        name="height"
                                        value={height}
                                        onChange={handleHeightChange}
                                        error={!!heightError}
                                        helperText={heightError || "Từ 150 đến 210 cm"}
                                        required
                                        size="small"
                                        sx={{
                                            '& .MuiOutlinedInput-root': {
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#CF4044',
                                                },
                                            },
                                            '& .MuiInputLabel-root.Mui-focused': {
                                                color: '#CF4044',
                                            },
                                        }}
                                        InputProps={{
                                            endAdornment: <InputAdornment position="end">cm</InputAdornment>,
                                        }}
                                    />
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Weight Field */}
                        <Grid item xs={12} sm={6}>
                            <Card elevation={2} sx={{ height: '100%', borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Avatar sx={{ bgcolor: '#e67e22', mr: 2, width: 50, height: 50 }}>
                                            <FitnessCenter />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Cân nặng
                                        </Typography>
                                    </Box>
                                    <TextField
                                        type='number'
                                        label="Cân nặng của bạn"
                                        fullWidth
                                        name="weight"
                                        value={weight}
                                        onChange={handleWeightChange}
                                        error={!!weightError}
                                        helperText={weightError || "Từ 40 đến 150 kg"}
                                        required
                                        size="small"
                                        sx={{
                                            '& .MuiOutlinedInput-root': {
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#CF4044',
                                                },
                                            },
                                            '& .MuiInputLabel-root.Mui-focused': {
                                                color: '#CF4044',
                                            },
                                        }}
                                        InputProps={{
                                            endAdornment: <InputAdornment position="end">kg</InputAdornment>,
                                        }}
                                    />
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>

                    {/* BMI Display */}
                    {bmi && (
                        <Card elevation={3} sx={{ mb: 4, borderRadius: 3, bgcolor: '#f8f9fa' }}>
                            <CardContent sx={{ p: 3, textAlign: 'center' }}>
                                <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50', mb: 2 }}>
                                    Chỉ số BMI của bạn
                                </Typography>
                                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 2 }}>
                                    <Chip 
                                        label={`BMI: ${bmi}`}
                                        sx={{ 
                                            fontSize: '1.1rem', 
                                            fontWeight: 'bold',
                                            bgcolor: '#CF4044',
                                            color: 'white',
                                            px: 2,
                                            py: 1
                                        }}
                                    />
                                    {bmiCategory && (
                                        <Chip 
                                            label={bmiCategory.text}
                                            sx={{ 
                                                fontSize: '1rem', 
                                                fontWeight: 'bold',
                                                bgcolor: bmiCategory.color,
                                                color: 'white',
                                                px: 2,
                                                py: 1
                                            }}
                                        />
                                    )}
                                </Box>
                            </CardContent>
                        </Card>
                    )}

                    {/* Navigation Buttons */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
                        <Button
                            variant="outlined"
                            onClick={handlePrevious}
                            startIcon={<ArrowBack />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                color: '#CF4044', 
                                borderColor: '#CF4044',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                '&:hover': {
                                    borderColor: '#B8363C',
                                    backgroundColor: 'rgba(207, 64, 68, 0.04)',
                                    transform: 'translateY(-2px)'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            Bước trước
                        </Button>
                        
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={!isFormValid()}
                            endIcon={<ArrowForward />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: isFormValid() 
                                    ? 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)' 
                                    : '#cccccc',
                                color: 'white',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                boxShadow: isFormValid() 
                                    ? '0 8px 20px rgba(207, 64, 68, 0.4)' 
                                    : 'none',
                                '&:hover': {
                                    background: isFormValid() 
                                        ? 'linear-gradient(45deg, #B8363C 30%, #c0392b 90%)'
                                        : '#cccccc',
                                    boxShadow: isFormValid() 
                                        ? '0 12px 28px rgba(207, 64, 68, 0.5)' 
                                        : 'none',
                                    transform: isFormValid() ? 'translateY(-2px)' : 'none'
                                },
                                '&:disabled': {
                                    background: '#cccccc',
                                    color: '#666666'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            Bước tiếp theo
                        </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
}

export default Step2