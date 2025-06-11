import React, { useState } from 'react'

import twice from '../../images/beginner.png'
import three from '../../images/occasionally.png'
import four from '../../images/moderate.png'
import five from '../../images/advanced.png'

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
    Schedule,
    CheckCircle,
    ArrowBack,
    ArrowForward
} from '@mui/icons-material';

const Step4 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [selectedFrequency, setSelectedFrequency] = useState(formData.desiredFrequency || '');

    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];

    const frequencyOptions = [
        { 
            value: 'Occasionally', 
            label: '2 buổi/tuần', 
            imageSrc: twice, 
            description: 'Dành cho người mới bắt đầu',
            intensity: 'Nhẹ nhàng',
            color: '#27ae60'
        },
        { 
            value: 'Moderate', 
            label: '3 buổi/tuần', 
            imageSrc: three, 
            description: 'Phù hợp với nhịp sống bận rộn',
            intensity: 'Vừa phải',
            color: '#f39c12'
        },
        { 
            value: 'Regularly', 
            label: '4 buổi/tuần', 
            imageSrc: four, 
            description: 'Dành cho người có kinh nghiệm',
            intensity: 'Tích cực',
            color: '#e67e22'
        },
        { 
            value: 'Advanced', 
            label: '5 buổi/tuần', 
            imageSrc: five, 
            description: 'Cho người muốn tiến bộ nhanh',
            intensity: 'Chuyên nghiệp',
            color: '#e74c3c'
        },
    ];

    const handleCardClick = (frequency) => {
        setSelectedFrequency(frequency);
        handleChange({
            target: {
                name: 'desiredFrequency',
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
                    <Stepper activeStep={3} alternativeLabel>
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
                            <Schedule sx={{ fontSize: 40 }} />
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
                            Tần suất tập luyện mong muốn
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Chọn số buổi tập mỗi tuần phù hợp với bạn
                        </Typography>
                        
                        <Chip 
                            label="Bước 4/7" 
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
                        {frequencyOptions.map((frequency, index) => (
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
                                                        fontStyle: 'italic',
                                                        mb: 0.5
                                                    }}
                                                >
                                                    {frequency.description}
                                                </Typography>

                                                <Chip 
                                                    label={frequency.intensity}
                                                    size="small"
                                                    sx={{
                                                        bgcolor: frequency.color,
                                                        color: 'white',
                                                        fontWeight: 'bold',
                                                        fontSize: '0.7rem'
                                                    }}
                                                />
                                            </Box>
                                            
                                            {/* Recommendation Badge */}
                                            <Box sx={{ textAlign: 'center' }}>
                                                {index === 1 && (
                                                    <Chip 
                                                        label="Được khuyên dùng"
                                                        size="small"
                                                        sx={{
                                                            bgcolor: '#4CAF50',
                                                            color: 'white',
                                                            fontWeight: 'bold',
                                                            mb: 1
                                                        }}
                                                    />
                                                )}
                                                <Typography 
                                                    variant="h5" 
                                                    sx={{ 
                                                        fontWeight: 'bold',
                                                        color: selectedFrequency === frequency.value ? '#CF4044' : frequency.color
                                                    }}
                                                >
                                                    {frequency.label.split('/')[0]}
                                                </Typography>
                                                <Typography 
                                                    variant="caption" 
                                                    sx={{ 
                                                        color: '#7f8c8d',
                                                        display: 'block'
                                                    }}
                                                >
                                                    buổi/tuần
                                        </Typography>
                                            </Box>
                                        </Box>
                                    </CardContent>
                            </CardActionArea>
                        </Card>
                    ))}
                    </Box>

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
                            onClick={handleNextStep}
                            disabled={!selectedFrequency}
                            endIcon={<ArrowForward />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: selectedFrequency 
                                    ? 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)' 
                                    : '#cccccc',
                                color: 'white',
                                padding: '12px 32px',
                                fontSize: '16px',
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
                            Bước tiếp theo
                        </Button>
                    </Box>
                </Paper>
            </Container>
        </Box>
    )
}

export default Step4