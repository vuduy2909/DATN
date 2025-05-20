import React from 'react'
import styles from './Step4.module.css'

import twice from '../../images/beginner.png'
import three from '../../images/occasionally.png'
import four from '../../images/moderate.png'
import five from '../../images/advanced.png'

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import { CardActionArea } from '@mui/material';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';

const Step4 = ({ formData, handleChange, handleNext, handlePrevious }) => {

    const frequencyOptions = [
        { value: 'Occasionally', label: '2 buổi/tuần', imageSrc: twice },
        { value: 'Moderate', label: '3 buổi/tuần', imageSrc: three },
        { value: 'Regularly', label: '4 buổi/tuần', imageSrc: four },
        { value: 'Advanced', label: '5 buổi/tuần', imageSrc: five },
    ];

    const handleCardClick = (selectedFrequency) => {
        handleChange({
            target: {
                name: 'desiredFrequency',
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
                <h4>4. Tần suất tập luyện mong muốn</h4>
                <div className={styles.gridCard}>
                    {frequencyOptions.map((option) => (
                        <Card sx={{ display: 'flex', width: '100%', margin: '10px' }} onClick={() => handleCardClick(option.value)}>
                            <CardActionArea sx={{ padding: '15px' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <CardMedia
                                        component="img"
                                        sx={{ height: 75 }}
                                        image={option.imageSrc}
                                        alt={option.label}
                                    />
                                    <CardContent sx={{ flex: '1 0 auto', width: '70%' }}>
                                        <Typography component="div" variant="subtitle1" sx={{ letterSpacing: '3px', lineHeight: 'unset', fontSize: 21 }}>
                                            {option.label}
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

export default Step4