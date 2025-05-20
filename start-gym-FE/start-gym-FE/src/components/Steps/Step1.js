import React from 'react';
import beginner from '../../images/beginner.png'
import occasionally from '../../images/occasionally.png'
import moderate from '../../images/moderate.png'
import advanced from '../../images/advanced.png'
import regularly from '../../images/regularly.png'
import styles from './Step1.module.css'

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import { CardActionArea } from '@mui/material';
import Typography from '@mui/material/Typography';

const Step1 = ({ formData, handleChange, handleNext }) => {
    const trainingFrequencies = [
        { value: 'Never', label: 'Không tập', imageSrc: beginner },
        { value: 'Occasionally', label: '2 buổi/tuần', imageSrc: occasionally },
        { value: 'Moderate', label: '3 buổi/tuần', imageSrc: moderate },
        { value: 'Regularly', label: '4 buổi/tuần', imageSrc: regularly },
        { value: 'Advanced', label: '5 buổi/tuần trở lên', imageSrc: advanced },
    ];

    const handleCardClick = (selectedFrequency) => {
        handleChange({
            target: {
                name: 'trainingFrequency',
                value: selectedFrequency,
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
                    <div className={styles.lineStepNon}></div>
                    <div className={styles.stepCircle}>
                        <p className={styles.stepNumberNon}>2</p>
                        <svg xmlns="http://www.w3.org/2000/svg" width="35" height="35" viewBox="0 0 35 35" fill="none">
                            <circle cx="17.3809" cy="17.3235" r="17" fill="#EFF0F7" />
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
                <h4>1. Thông tin tập luyện hiện tại</h4>
                <p>Tần suất tập luyện hiện tại:</p>
                <div className={styles.gridCard}>
                    {trainingFrequencies.map((frequency) => (
                        <Card sx={{ display: 'flex', width: '100%', margin: '10px' }} onClick={() => handleCardClick(frequency.value)}>
                            <CardActionArea sx={{ padding: '15px' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <CardMedia
                                        component="img"
                                        sx={{ height: 75 }}
                                        image={frequency.imageSrc}
                                        alt={frequency.label}
                                    />
                                    <CardContent sx={{ flex: '1 0 auto', width: '70%' }}>
                                        <Typography component="div" variant="subtitle1" sx={{ letterSpacing: '3px', lineHeight: 'unset', fontSize: 21 }}>
                                            {frequency.label}
                                        </Typography>
                                    </CardContent>
                                </Box>
                            </CardActionArea>
                        </Card>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Step1;