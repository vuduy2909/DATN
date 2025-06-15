import React, { useState } from 'react';
import useKeyboardNavigation from '../../hooks/useKeyboardNavigation';

import {
    Box,
    Container,
    Paper,
    Typography,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
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
    Work,
    ArrowBack,
    ArrowForward,
    Business,
    LocationOn
} from '@mui/icons-material';

const Step5 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [occupation, setOccupation] = useState(formData.occupation || '');
    const [workEnvironment, setWorkEnvironment] = useState(formData.workEnvironment || '');

    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];

    const occupationOptions = [
        { value: 'Sinh viên', icon: '🎓', description: 'Đang theo học tại trường đại học/cao đẳng' },
        { value: 'Nhân viên văn phòng', icon: '💼', description: 'Làm việc chủ yếu tại bàn máy tính' },
        { value: 'Nhân viên giao hàng', icon: '🚚', description: 'Vận chuyển và giao hàng cho khách' },
        { value: 'Giảng viên', icon: '👨‍🏫', description: 'Giảng dạy tại trường học/đại học' },
        { value: 'Nhân viên dịch vụ', icon: '🏪', description: 'Phục vụ khách hàng trong ngành dịch vụ' },
        { value: 'Khác', icon: '⚡', description: 'Nghề nghiệp khác không được liệt kê' }
    ];

    const workEnvironmentOptions = [
        { value: 'Văn phòng', icon: '🏢', description: 'Làm việc trong môi trường văn phòng' },
        { value: 'Đứng nhiều', icon: '🧍', description: 'Công việc yêu cầu đứng lâu trong ngày' },
        { value: 'Đi lại nhiều', icon: '🚶‍♂️', description: 'Thường xuyên di chuyển, đi lại' },
        { value: 'Lái xe nhiều', icon: '🚗', description: 'Phần lớn thời gian ngồi lái xe' },
        { value: 'Lao động chân tay', icon: '🔨', description: 'Công việc cần sức lực và vận động nhiều' }
    ];

    const isFormValid = () => {
        return occupation && workEnvironment;
    };

    // Thêm keyboard navigation
    useKeyboardNavigation(handleNext, handlePrevious, !isFormValid(), 5, 7);

    const handleSubmit = () => {
        if (!isFormValid()) return;

        handleChange({
            target: { name: 'occupation', value: occupation }
        });
        
        handleChange({
            target: { name: 'workEnvironment', value: workEnvironment }
        });
        
        handleNext();
    };

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
                    <Stepper activeStep={4} alternativeLabel
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
                            <Work sx={{ fontSize: 40 }} />
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
                            Thông tin nghề nghiệp
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Giúp chúng tôi hiểu về lối sống và công việc của bạn
                        </Typography>
                        
                        <Chip 
                            label="Bước 5/7" 
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
                        {/* Occupation Field */}
                        <Grid item xs={12}>
                            <Card elevation={2} sx={{ borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                        <Avatar sx={{ bgcolor: '#3498db', mr: 2, width: 50, height: 50 }}>
                                            <Business />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Nghề nghiệp của bạn
                                        </Typography>
                                    </Box>

                                    <FormControl fullWidth required>
                                        <InputLabel 
                                            sx={{
                                                '&.Mui-focused': {
                                                    color: '#CF4044',
                                                },
                                            }}
                                        >
                                            Chọn nghề nghiệp
                                        </InputLabel>
                                        <Select
                                            value={occupation}
                                            label="Chọn nghề nghiệp"
                                            onChange={(e) => setOccupation(e.target.value)}
                                            sx={{
                                                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: '#CF4044',
                                                },
                                            }}
                                        >
                                            {occupationOptions.map((option) => (
                                                <MenuItem key={option.value} value={option.value}>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', py: 1 }}>
                                                        <Typography sx={{ mr: 2, fontSize: '1.2rem' }}>
                                                            {option.icon}
                                                        </Typography>
                                                        <Box>
                                                            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                                                                {option.value}
                                                            </Typography>
                                                            <Typography variant="caption" sx={{ color: '#7f8c8d' }}>
                                                                {option.description}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>

                                    {occupation && (
                                        <Box sx={{ mt: 2 }}>
                                            <Chip 
                                                label={`Đã chọn: ${occupation}`}
                                                color="primary"
                                                sx={{ 
                                                    bgcolor: '#e8f5e8',
                                                    color: '#2e7d32',
                                                    fontWeight: 'bold'
                                                }}
                                            />
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>

                        {/* Work Environment Field */}
                        <Grid item xs={12}>
                            <Card elevation={2} sx={{ borderRadius: 3 }}>
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                        <Avatar sx={{ bgcolor: '#f39c12', mr: 2, width: 50, height: 50 }}>
                                            <LocationOn />
                                        </Avatar>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50' }}>
                                            Môi trường làm việc
                                        </Typography>
                                    </Box>

                                    <FormControl fullWidth required>
                                        <InputLabel 
                                            sx={{
                                                '&.Mui-focused': {
                                                    color: '#CF4044',
                                                },
                                            }}
                                        >
                                            Chọn môi trường làm việc
                                        </InputLabel>
                                        <Select
                                            value={workEnvironment}
                                            label="Chọn môi trường làm việc"
                                            onChange={(e) => setWorkEnvironment(e.target.value)}
                                            sx={{
                                                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: '#CF4044',
                                                },
                                            }}
                                        >
                                            {workEnvironmentOptions.map((option) => (
                                                <MenuItem key={option.value} value={option.value}>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', py: 1 }}>
                                                        <Typography sx={{ mr: 2, fontSize: '1.2rem' }}>
                                                            {option.icon}
                                                        </Typography>
                                                        <Box>
                                                            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                                                                {option.value}
                                                            </Typography>
                                                            <Typography variant="caption" sx={{ color: '#7f8c8d' }}>
                                                                {option.description}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>

                                    {workEnvironment && (
                                        <Box sx={{ mt: 2 }}>
                                            <Chip 
                                                label={`Đã chọn: ${workEnvironment}`}
                                                color="primary"
                                                sx={{ 
                                                    bgcolor: '#e3f2fd',
                                                    color: '#1565c0',
                                                    fontWeight: 'bold'
                                                }}
                                            />
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>

                    {/* Info Box */}
                    {isFormValid() && (
                        <Card elevation={3} sx={{ mb: 4, borderRadius: 3, bgcolor: '#f0f8ff' }}>
                            <CardContent sx={{ p: 3, textAlign: 'center' }}>
                                <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#2c3e50', mb: 1 }}>
                                    ✅ Thông tin đã được cập nhật
                                </Typography>
                                <Typography variant="body2" sx={{ color: '#7f8c8d' }}>
                                    Chúng tôi sẽ thiết kế chương trình tập phù hợp với lối sống công việc của bạn
                                </Typography>
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
};

export default Step5;