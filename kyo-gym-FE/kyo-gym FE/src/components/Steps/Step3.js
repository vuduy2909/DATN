import React from 'react'
import styles from './Step3.module.css'

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import { CardActionArea } from '@mui/material';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';

const Step3 = ({ formData, handleChange, handleNext, handlePrevious }) => {

    const trainingGoals = [
        { value: 'Giảm cân tổng thể' },
        { value: 'Giảm mỡ, giữ cơ' },
        { value: 'Tăng cơ bắp' },
        { value: 'Tăng sức mạnh' },
        { value: 'Cải thiện sức chịu đựng' },
        { value: 'Tăng tính linh hoạt' },
        { value: 'Duy trì vóc dáng' },
    ];

    const handleCardClick = (selectedGoal) => {
        handleChange({
            target: {
                name: 'trainingGoal',
                value: selectedGoal,
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
                <h4>3. Mục tiêu tập luyện</h4>
                <p>Chọn mục tiêu chính của bạn:</p>
                <div className={styles.gridCard}>
                    {trainingGoals.map((goal) => (
                        <Card 
                            sx={{ 
                                display: 'flex', 
                                width: '100%', 
                                margin: '10px',
                                backgroundColor: '#f5f5f5' 
                            }} 
                            onClick={() => handleCardClick(goal.value)}
                        >
                            <CardActionArea sx={{ padding: '15px' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <CardContent sx={{ flex: '1 0 auto', width: '100%' }}>
                                        <Typography 
                                            component="div" 
                                            variant="subtitle1" 
                                            sx={{ 
                                                letterSpacing: '2px', 
                                                lineHeight: 'unset', 
                                                fontSize: 20,
                                                fontWeight: 'bold',
                                                color: '#CF4044'  
                                            }}
                                        >
                                            {goal.value}
                                        </Typography>
                                    </CardContent>
                                </Box>
                            </CardActionArea>
                        </Card>
                    ))}
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
                </div>
            </div>
        </div>
    )
}

export default Step3