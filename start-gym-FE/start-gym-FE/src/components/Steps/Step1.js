import React, { useState } from 'react';
import beginner from '../../images/beginner.png'
import occasionally from '../../images/occasionally.png'
import moderate from '../../images/moderate.png'
import advanced from '../../images/advanced.png'
import regularly from '../../images/regularly.png'

import {
    Box,
    Card,
    CardContent,
    CardMedia,
    CardActionArea,
    Typography,
    Button,
    Container,
    Stepper,
    Step,
    StepLabel,
    Paper,
    Chip,
    Avatar
} from '@mui/material';
import {
    FitnessCenter,
    CheckCircle
} from '@mui/icons-material';

const Step1 = ({ formData, handleChange, handleNext }) => {
    const [selectedFrequency, setSelectedFrequency] = useState(formData.trainingFrequency || '');

    const steps = [
        'Thông tin tập luyện',
        'Thông tin cơ bản', 
        'Mục tiêu tập luyện',
        'Tần suất mong muốn',
        'Thông tin nghề nghiệp',
        'Số đo cơ thể',
        'Kết quả phân tích'
    ];

    const trainingFrequencies = [
        { value: 'Never', label: 'Không tập', imageSrc: beginner, description: 'Chưa từng tập luyện thể dục' },
        { value: 'Occasionally', label: '2 buổi/tuần', imageSrc: occasionally, description: 'Tập luyện nhẹ nhàng' },
        { value: 'Moderate', label: '3 buổi/tuần', imageSrc: moderate, description: 'Tập luyện đều đặn' },
        { value: 'Regularly', label: '4 buổi/tuần', imageSrc: regularly, description: 'Tập luyện tích cực' },
        { value: 'Advanced', label: '5 buổi/tuần trở lên', imageSrc: advanced, description: 'Tập luyện chuyên nghiệp' },
    ];

    const handleCardClick = (frequency) => {
        setSelectedFrequency(frequency);
        handleChange({
            target: {
                name: 'trainingFrequency',
                value: frequency,
            },
        });
    };

    const handleNextStep = () => {
        if (selectedFrequency) {
            handleNext();
        }
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
                    <Stepper activeStep={0} alternativeLabel>
                        {steps.map((label, index) => (
                            <Step key={label}>
                                <StepLabel>
                                    <Typography variant="caption" sx={{ fontSize: '0.75rem' }}>
                                        {label}
                                    </Typography>
                                </StepLabel>
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
                            <FitnessCenter sx={{ fontSize: 40 }} />
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
                            Thông tin tập luyện hiện tại
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Hãy cho chúng tôi biết về tần suất tập luyện hiện tại của bạn
                        </Typography>
                        
                        <Chip 
                            label="Bước 1/7" 
                            color="primary" 
                            sx={{ 
                                mt: 2, 
                                bgcolor: '#CF4044',
                                fontWeight: 'bold'
                            }} 
                        />
                    </Box>

                    {/* Options Grid */}
                    <Box sx={{ mb: 4 }}>
                        {trainingFrequencies.map((frequency, index) => (
                            <Card 
                                key={frequency.value}
                                elevation={selectedFrequency === frequency.value ? 8 : 2}
                                sx={{ 
                                    mb: 2,
                                    border: selectedFrequency === frequency.value ? '3px solid #CF4044' : '1px solid #e0e0e0',
                                    backgroundColor: selectedFrequency === frequency.value ? 'linear-gradient(135deg, #ffebee 0%, #fce4ec 100%)' : '#ffffff',
                                    transform: selectedFrequency === frequency.value ? 'scale(1.02)' : 'scale(1)',
                                    transition: 'all 0.3s ease-in-out',
                                    cursor: 'pointer',
                                    borderRadius: 3,
                                    '&:hover': {
                                        transform: 'scale(1.01)',
                                        boxShadow: '0 8px 25px rgba(0,0,0,0.15)'
                                    }
                                }} 
                                onClick={() => handleCardClick(frequency.value)}
                            >
                                <CardActionArea sx={{ p: 0 }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', position: 'relative' }}>
                                            {/* Selection Check Icon */}
                                            {selectedFrequency === frequency.value && (
                                                <CheckCircle 
                                                    sx={{ 
                                                        position: 'absolute',
                                                        top: -10,
                                                        right: -10,
                                                        color: '#CF4044',
                                                        fontSize: 30,
                                                        bgcolor: 'white',
                                                        borderRadius: '50%'
                                                    }} 
                                                />
                                            )}
                                            
                                            {/* Icon */}
                                            <Box sx={{ mr: 3 }}>
                                                <CardMedia
                                                    component="img"
                                                    sx={{ 
                                                        height: 80, 
                                                        width: 80,
                                                        borderRadius: 2,
                                                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                                                    }}
                                                    image={frequency.imageSrc}
                                                    alt={frequency.label}
                                                />
                                            </Box>
                                            
                                            {/* Content */}
                                            <Box sx={{ flex: 1 }}>
                                                <Typography 
                                                    variant="h6" 
                                                    sx={{ 
                                                        fontWeight: 'bold',
                                                        color: selectedFrequency === frequency.value ? '#CF4044' : '#2c3e50',
                                                        mb: 0.5
                                                    }}
                                                >
                                                    {frequency.label}
                                                </Typography>
                                                
                                                <Typography 
                                                    variant="body2" 
                                                    sx={{ 
                                                        color: selectedFrequency === frequency.value ? '#8e1538' : '#7f8c8d',
                                                        fontStyle: 'italic'
                                                    }}
                                                >
                                                    {frequency.description}
                                                </Typography>
                                            </Box>
                                            
                                            {/* Badge */}
                                            <Chip 
                                                label={`Lựa chọn ${index + 1}`}
                                                size="small"
                                                variant={selectedFrequency === frequency.value ? "filled" : "outlined"}
                                                color="primary"
                                                sx={{
                                                    bgcolor: selectedFrequency === frequency.value ? '#CF4044' : 'transparent',
                                                    color: selectedFrequency === frequency.value ? 'white' : '#CF4044',
                                                    borderColor: '#CF4044',
                                                    fontWeight: 'bold'
                                                }}
                                            />
                                        </Box>
                                    </CardContent>
                                </CardActionArea>
                            </Card>
                        ))}
                    </Box>

                    {/* Next Button */}
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                        <Button
                            variant="contained"
                            onClick={handleNextStep}
                            disabled={!selectedFrequency}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: selectedFrequency 
                                    ? 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)' 
                                    : '#cccccc',
                                color: 'white',
                                padding: '16px 48px',
                                fontSize: '18px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                boxShadow: selectedFrequency 
                                    ? '0 8px 20px rgba(207, 64, 68, 0.4)' 
                                    : 'none',
                                '&:hover': {
                                    background: selectedFrequency 
                                        ? 'linear-gradient(45deg, #B8363C 30%, #c0392b 90%)'
                                        : '#cccccc',
                                    boxShadow: selectedFrequency 
                                        ? '0 12px 28px rgba(207, 64, 68, 0.5)' 
                                        : 'none',
                                    transform: selectedFrequency ? 'translateY(-2px)' : 'none'
                                },
                                '&:disabled': {
                                    background: '#cccccc',
                                    color: '#666666'
                                },
                                transition: 'all 0.3s ease-in-out'
                            }}
                        >
                            Bước tiếp theo →
                        </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
};

export default Step1;