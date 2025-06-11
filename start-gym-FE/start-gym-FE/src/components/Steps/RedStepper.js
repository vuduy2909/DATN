import React from 'react';
import { Stepper, Step, StepLabel, Paper } from '@mui/material';

const RedStepper = ({ activeStep }) => {
    const steps = ['1', '2', '3', '4', '5', '6', '7'];

    return (
        <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 3 }}>
            <Stepper 
                activeStep={activeStep} 
                alternativeLabel
                sx={{
                    '& .MuiStep-root': {
                        '& .MuiStepIcon-root': {
                            color: '#e0e0e0', // Inactive step color
                            '&.Mui-active': {
                                color: '#CF4044', // Active step color (red)
                            },
                            '&.Mui-completed': {
                                color: '#CF4044', // Completed step color (red)
                            },
                        },
                        '& .MuiStepConnector-line': {
                            borderColor: '#e0e0e0', // Connector line color
                        },
                        '& .Mui-active ~ .MuiStepConnector-root .MuiStepConnector-line': {
                            borderColor: '#CF4044', // Active connector color
                        },
                        '& .Mui-completed ~ .MuiStepConnector-root .MuiStepConnector-line': {
                            borderColor: '#CF4044', // Completed connector color
                        },
                    },
                    '& .MuiStepConnector-root': {
                        '& .MuiStepConnector-line': {
                            borderTopWidth: '3px',
                            borderRadius: '1px',
                        },
                    },
                }}
            >
                {steps.map((label, index) => (
                    <Step key={label}>
                        <StepLabel
                            sx={{
                                '& .MuiStepLabel-label': {
                                    color: index <= activeStep ? '#CF4044' : '#9e9e9e', // Red for completed/active, gray for inactive
                                    fontWeight: 'bold',
                                    fontSize: '1rem',
                                },
                            }}
                        >
                            {label}
                        </StepLabel>
                    </Step>
                ))}
            </Stepper>
        </Paper>
    );
};

export default RedStepper; 