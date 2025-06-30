import React, { useState } from 'react'
import useKeyboardNavigation from '../../hooks/useKeyboardNavigation';

import {
    Box,
    Card,
    CardContent,
    CardActionArea,
    Typography,
    Button,
    Container,
    Stepper,
    Step,
    StepLabel,
    Paper,
    Chip,
    Avatar,
    Grid
} from '@mui/material';
import {
    TrackChanges,
    CheckCircle,
    ArrowBack,
    ArrowForward
} from '@mui/icons-material';

const Step3 = ({ formData, handleChange, handleNext, handlePrevious }) => {
    const [selectedGoal, setSelectedGoal] = useState(formData.training_goal || '');

    // Thêm keyboard navigation
    useKeyboardNavigation(handleNext, handlePrevious, !selectedGoal, 3, 7);

    const steps = [
        '1',
        '2', 
        '3',
        '4',
        '5',
        '6',
        '7'
    ];

    const training_goals = [
        { 
            value: 'Tăng sức mạnh',
            icon: '💥',
            description: 'Nâng cao sức mạnh tổng thể',
            color: '#8e44ad'
        },
        { 
            value: 'Cải thiện tim mạch',
            icon: '❤️',
            description: 'Tăng cường sức khỏe tim mạch',
            color: '#e74c3c'
        },
        { 
            value: 'Tăng tính linh hoạt',
            icon: '🤸‍♀️',
            description: 'Cải thiện độ dẻo dai và cân bằng',
            color: '#1abc9c'
        },
        { 
            value: 'Sức mạnh & sức bền',
            icon: '⚡',
            description: 'Kết hợp sức mạnh và sức chịu đựng',
            color: '#3498db'
        },
        { 
            value: 'Duy trì vóc dáng',
            icon: '✨',
            description: 'Giữ gìn thể hình hiện tại',
            color: '#95a5a6'
        },
        { 
            value: 'Giảm mỡ giữ cơ',
            icon: '💪',
            description: 'Cải thiện thành phần cơ thể',
            color: '#f39c12'
        },
        { 
            value: 'Giảm cân tổng thể',
            icon: '🏃‍♀️',
            description: 'Giảm cân an toàn và hiệu quả',
            color: '#27ae60'
        },
    ];

    const handleCardClick = (goal) => {
        setSelectedGoal(goal);
        handleChange({
            target: {
                name: 'training_goal',
                value: goal,
            },
        });
    };

    const handleNextStep = () => {
        if (selectedGoal) {
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
                    <Stepper activeStep={2} alternativeLabel
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
                            <TrackChanges sx={{ fontSize: 40 }} />
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
                            Mục tiêu tập luyện
                        </Typography>
                        
                        <Typography variant="h6" sx={{ color: '#7f8c8d', fontWeight: 400 }}>
                            Chọn mục tiêu chính mà bạn muốn đạt được
                        </Typography>
                        
                        <Chip 
                            label="Bước 3/7" 
                            color="primary" 
                            sx={{ 
                                mt: 2, 
                                bgcolor: '#CF4044',
                                fontWeight: 'bold'
                            }} 
                        />
                    </Box>

                    {/* Options Grid */}
                    <Grid container spacing={2} sx={{ mb: 4 }}>
                        {training_goals.map((goal, index) => (
                            <Grid 
                                item 
                                xs={12} 
                                sm={6} 
                                key={goal.value}
                            >
                                <Card 
                                    elevation={selectedGoal === goal.value ? 8 : 2}
                                    sx={{ 
                                        height: '100%',
                                        width: '100%',
                                        border: selectedGoal === goal.value ? '3px solid #CF4044' : '1px solid #e0e0e0',
                                        backgroundColor: selectedGoal === goal.value ? 'linear-gradient(135deg, #ffebee 0%, #fce4ec 100%)' : '#ffffff',
                                        transform: selectedGoal === goal.value ? 'scale(1.02)' : 'scale(1)',
                                        transition: 'all 0.3s ease-in-out',
                                        cursor: 'pointer',
                                        borderRadius: 3,
                                        position: 'relative',
                                        '&:hover': {
                                            transform: 'scale(1.01)',
                                            boxShadow: '0 8px 25px rgba(0,0,0,0.15)'
                                        }
                                    }} 
                                    onClick={() => handleCardClick(goal.value)}
                                >
                                    <CardActionArea sx={{ height: '100%', p: 0 }}>
                                        <CardContent sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
                                            {/* Selection Check Icon */}
                                            {selectedGoal === goal.value && (
                                                <CheckCircle 
                                                    sx={{ 
                                                        position: 'absolute',
                                                        top: 10,
                                                        right: 10,
                                                        color: '#CF4044',
                                                        fontSize: 30,
                                                        bgcolor: 'white',
                                                        borderRadius: '50%'
                                                    }} 
                                                />
                                            )}
                                            
                                            {/* Icon */}
                                            <Box sx={{ textAlign: 'center', mb: 2 }}>
                                                <Typography 
                                                    variant="h2" 
                                                    sx={{ 
                                                        fontSize: '3rem',
                                                        filter: selectedGoal === goal.value ? 'brightness(1.2)' : 'brightness(0.8)'
                                                    }}
                                                >
                                                    {goal.icon}
                                                </Typography>
                                            </Box>
                                            
                                            {/* Content */}
                                            <Box sx={{ flex: 1, textAlign: 'center' }}>
                                                <Typography 
                                                    variant="h6" 
                                                    sx={{ 
                                                        fontWeight: 'bold',
                                                        color: selectedGoal === goal.value ? '#CF4044' : goal.color,
                                                        mb: 1,
                                                        fontSize: '1.1rem'
                                                    }}
                                                >
                                                    {goal.value}
                                                </Typography>
                                                
                                                <Typography 
                                                    variant="body2" 
                                                    sx={{ 
                                                        color: selectedGoal === goal.value ? '#8e1538' : '#7f8c8d',
                                                        fontStyle: 'italic',
                                                        lineHeight: 1.4
                                                    }}
                                                >
                                                    {goal.description}
                                                </Typography>
                                            </Box>
                                            
                                            {/* Badge */}
                                            <Box sx={{ mt: 2, textAlign: 'center' }}>
                                                <Chip 
                                                    label={selectedGoal === goal.value ? "Đã chọn" : "Chọn mục tiêu này"}
                                                    size="small"
                                                    variant={selectedGoal === goal.value ? "filled" : "outlined"}
                                                    sx={{
                                                        bgcolor: selectedGoal === goal.value ? '#CF4044' : 'transparent',
                                                        color: selectedGoal === goal.value ? 'white' : goal.color,
                                                        borderColor: selectedGoal === goal.value ? '#CF4044' : goal.color,
                                                        fontWeight: 'bold',
                                                        fontSize: '0.75rem'
                                                    }}
                                                />
                                            </Box>
                                        </CardContent>
                                    </CardActionArea>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>

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
                            disabled={!selectedGoal}
                            endIcon={<ArrowForward />}
                            size="large"
                            sx={{ 
                                borderRadius: 6,
                                background: selectedGoal 
                                    ? 'linear-gradient(45deg, #CF4044 30%, #e74c3c 90%)' 
                                    : '#cccccc',
                                color: 'white',
                                padding: '12px 32px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                textTransform: 'none',
                                boxShadow: selectedGoal 
                                    ? '0 8px 20px rgba(207, 64, 68, 0.4)' 
                                    : 'none',
                                '&:hover': {
                                    background: selectedGoal 
                                        ? 'linear-gradient(45deg, #B8363C 30%, #c0392b 90%)'
                                        : '#cccccc',
                                    boxShadow: selectedGoal 
                                        ? '0 12px 28px rgba(207, 64, 68, 0.5)' 
                                        : 'none',
                                    transform: selectedGoal ? 'translateY(-2px)' : 'none'
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

export default Step3