import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    Grid,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Box,
    Avatar,
    Link,
    Alert,
    Stepper,
    Step,
    StepLabel,
    CircularProgress,
    createTheme,
    ThemeProvider
} from '@mui/material';
import {
    LockReset,
    Email,
    VpnKey,
    ArrowBack
} from '@mui/icons-material';

// StartGym Theme
const startGymTheme = createTheme({
    palette: {
        primary: {
            main: '#CF4044',
            light: '#E55A5E',
            dark: '#B8363C',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#f39c12',
            light: '#f7b733',
            dark: '#e67e22',
        },
    },
    typography: {
        fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
        h3: {
            fontWeight: 700,
        },
        h4: {
            fontWeight: 600,
        },
        h5: {
            fontWeight: 600,
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    textTransform: 'none',
                    fontSize: '1rem',
                    fontWeight: 600,
                    padding: '12px 24px',
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    '& .MuiOutlinedInput-root': {
                        borderRadius: 8,
                        '&:hover fieldset': {
                            borderColor: '#CF4044',
                        },
                        '&.Mui-focused fieldset': {
                            borderColor: '#CF4044',
                        },
                    },
                    '& .MuiInputLabel-root.Mui-focused': {
                        color: '#CF4044',
                    },
                },
            },
        },
    },
});

const ForgotPassword = () => {
    const navigate = useNavigate();
    const [activeStep, setActiveStep] = useState(0);
    const [loading, setLoading] = useState(false);
    
    // Form data
    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    
    // Validation states
    const [emailError, setEmailError] = useState('');
    const [otpError, setOtpError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState('');
    
    // Messages
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    
    // OTP countdown
    const [countdown, setCountdown] = useState(0);
    const [canResend, setCanResend] = useState(true);

    const steps = ['Nhập Email', 'Xác Thực OTP', 'Đặt Lại Mật Khẩu'];

    // Email validation
    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!email) {
            setEmailError('Email là bắt buộc');
            return false;
        }
        if (!emailRegex.test(email)) {
            setEmailError('Email không hợp lệ');
            return false;
        }
        setEmailError('');
        return true;
    };

    // Password validation
    const validatePassword = (password) => {
        if (!password) {
            setPasswordError('Mật khẩu là bắt buộc');
            return false;
        }
        if (password.length < 8) {
            setPasswordError('Mật khẩu phải có ít nhất 8 ký tự');
            return false;
        }
        if (!/(?=.*[a-z])/.test(password)) {
            setPasswordError('Mật khẩu phải có ít nhất 1 chữ thường');
            return false;
        }
        if (!/(?=.*[A-Z])/.test(password)) {
            setPasswordError('Mật khẩu phải có ít nhất 1 chữ hoa');
            return false;
        }
        if (!/(?=.*\d)/.test(password)) {
            setPasswordError('Mật khẩu phải có ít nhất 1 số');
            return false;
        }
        if (!/(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])/.test(password)) {
            setPasswordError('Mật khẩu phải có ít nhất 1 ký tự đặc biệt');
            return false;
        }
        setPasswordError('');
        return true;
    };

    // OTP validation
    const validateOTP = (otp) => {
        if (!otp || otp.length !== 6) {
            setOtpError('Vui lòng nhập mã OTP 6 số');
            return false;
        }
        if (!/^\d{6}$/.test(otp)) {
            setOtpError('OTP chỉ được chứa số');
            return false;
        }
        setOtpError('');
        return true;
    };

    // Confirm password validation
    const validateConfirmPassword = (confirmPassword) => {
        if (newPassword !== confirmPassword) {
            setConfirmPasswordError('Mật khẩu xác nhận không khớp');
            return false;
        }
        setConfirmPasswordError('');
        return true;
    };

    // Handle countdown for resend OTP
    useEffect(() => {
        let timer;
        if (countdown > 0) {
            timer = setTimeout(() => {
                setCountdown(countdown - 1);
            }, 1000);
        } else if (countdown === 0 && !canResend) {
            setCanResend(true);
        }
        return () => {
            if (timer) clearTimeout(timer);
        };
    }, [countdown, canResend]);

    // Step 1: Send OTP to email
    const handleSendOTP = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!validateEmail(email)) {
            return;
        }

        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/forgot-password', {
                email: email
            });

            setSuccessMessage(response.data.message);
            setActiveStep(1);
            setCountdown(300); // 5 minutes countdown
            setCanResend(false);
            
        } catch (error) {
            setErrorMessage(error.response?.data || 'Có lỗi xảy ra khi gửi email');
        } finally {
            setLoading(false);
        }
    };

    // Resend OTP
    const handleResendOTP = async () => {
        if (!canResend) return;
        
        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/forgot-password', {
                email: email
            });

            setSuccessMessage('Mã OTP mới đã được gửi');
            setCountdown(300); // 5 minutes countdown
            setCanResend(false);
            
        } catch (error) {
            setErrorMessage(error.response?.data || 'Có lỗi xảy ra khi gửi lại email');
        } finally {
            setLoading(false);
        }
    };

    // Step 2: Verify OTP and Reset Password
    const handleVerifyOTP = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!validateOTP(otp)) {
            return;
        }

        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/verify-otp', {
                email: email,
                otp: otp
            });

            setSuccessMessage(response.data.message);
            setActiveStep(2); // Move to step 3 (password reset)
            
        } catch (error) {
            setErrorMessage(error.response?.data || 'Mã OTP không chính xác');
        } finally {
            setLoading(false);
        }
    };

    // Step 3: Reset password
    const handleResetPassword = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!validatePassword(newPassword) || !validateConfirmPassword(confirmPassword)) {
            return;
        }

        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/reset-password', {
                email: email,
                otp: otp,
                newPassword: newPassword
            });

            setSuccessMessage(response.data.message);
            setActiveStep(3); // Move to success step
            
            // Redirect to login after 2 seconds
            setTimeout(() => {
                navigate('/login');
            }, 2000);
            
        } catch (error) {
            setErrorMessage(error.response?.data || 'Có lỗi xảy ra khi đặt lại mật khẩu');
        } finally {
            setLoading(false);
        }
    };

    const handleBackToLogin = () => {
        navigate('/login');
    };

    const formatTime = (seconds) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    return (
        <ThemeProvider theme={startGymTheme}>
            <Box sx={{ 
                minHeight: '100vh', 
                background: 'linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%)',
                display: 'flex',
                alignItems: 'center',
                py: 4
            }}>
                <Container maxWidth="md">
                    <Card elevation={12} sx={{ 
                        borderRadius: 4,
                        boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
                        overflow: 'visible'
                    }}>
                        <CardContent sx={{ p: 5 }}>
                            {/* Header */}
                            <Box sx={{ textAlign: 'center', mb: 4 }}>
                                <Avatar sx={{ 
                                    bgcolor: 'primary.main', 
                                    width: 60, 
                                    height: 60, 
                                    mx: 'auto', 
                                    mb: 2,
                                    boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)'
                                }}>
                                    <LockReset sx={{ fontSize: 30 }} />
                                </Avatar>
                                <Typography variant="h4" sx={{ color: 'text.primary', fontWeight: 'bold' }}>
                                    Quên Mật Khẩu
                                </Typography>
                                <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1 }}>
                                    Đặt lại mật khẩu cho tài khoản StartGym của bạn
                                </Typography>
                            </Box>

                            {/* Stepper */}
                            <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
                                {steps.map((label) => (
                                    <Step key={label}>
                                        <StepLabel>{label}</StepLabel>
                                    </Step>
                                ))}
                            </Stepper>

                            {/* Messages */}
                            {errorMessage && (
                                <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
                                    {errorMessage}
                                </Alert>
                            )}
                            {successMessage && (
                                <Alert severity="success" sx={{ mb: 3, borderRadius: 2 }}>
                                    {successMessage}
                                </Alert>
                            )}

                            {/* Step 1: Enter Email */}
                            {activeStep === 0 && (
                                <Box component="form" onSubmit={handleSendOTP}>
                                    <Typography variant="h6" sx={{ mb: 2, color: 'text.primary' }}>
                                        Nhập địa chỉ email của bạn
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                                        Chúng tôi sẽ gửi mã xác thực 6 số đến email này
                                    </Typography>
                                    
                                    <TextField
                                        fullWidth
                                        label="Email"
                                        type="email"
                                        value={email}
                                        onChange={(e) => {
                                            setEmail(e.target.value);
                                            if (e.target.value) validateEmail(e.target.value);
                                        }}
                                        error={!!emailError}
                                        helperText={emailError}
                                        required
                                        sx={{ mb: 3 }}
                                        variant="outlined"
                                        InputProps={{
                                            startAdornment: <Email sx={{ mr: 1, color: 'action.active' }} />
                                        }}
                                    />

                                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'space-between' }}>
                                        <Button
                                            variant="outlined"
                                            startIcon={<ArrowBack />}
                                            onClick={handleBackToLogin}
                                            sx={{ 
                                                borderColor: 'primary.main',
                                                color: 'primary.main'
                                            }}
                                        >
                                            Quay lại đăng nhập
                                        </Button>
                                        
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            disabled={loading}
                                            startIcon={loading ? <CircularProgress size={20} /> : <Email />}
                                            sx={{ 
                                                background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                '&:hover': {
                                                    background: 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                    boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                }
                                            }}
                                        >
                                            {loading ? 'Đang gửi...' : 'Gửi mã xác thực'}
                                        </Button>
                                    </Box>
                                </Box>
                            )}

                            {/* Step 2: Verify OTP and Reset Password */}
                            {activeStep === 1 && (
                                <Box component="form" onSubmit={handleVerifyOTP}>
                                    <Typography variant="h6" sx={{ mb: 2, color: 'text.primary' }}>
                                        Nhập mã xác thực
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                                        Mã xác thực đã được gửi đến: <strong>{email}</strong>
                                    </Typography>
                                    
                                    <TextField
                                        fullWidth
                                        label="Mã OTP (6 số)"
                                        value={otp}
                                        onChange={(e) => {
                                            setOtp(e.target.value);
                                            setOtpError('');
                                        }}
                                        error={!!otpError}
                                        helperText={otpError}
                                        required
                                        sx={{ mb: 3 }}
                                        variant="outlined"
                                        inputProps={{ maxLength: 6, pattern: '[0-9]*' }}
                                    />

                                    {/* Countdown and Resend */}
                                    <Box sx={{ textAlign: 'center', mb: 3 }}>
                                        {countdown > 0 ? (
                                            <Typography variant="body2" color="text.secondary">
                                                Gửi lại mã sau: {formatTime(countdown)}
                                            </Typography>
                                        ) : (
                                            <Button
                                                variant="text"
                                                onClick={handleResendOTP}
                                                disabled={!canResend || loading}
                                                sx={{ color: 'primary.main' }}
                                            >
                                                Gửi lại mã OTP
                                            </Button>
                                        )}
                                    </Box>

                                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'space-between' }}>
                                        <Button
                                            variant="outlined"
                                            startIcon={<ArrowBack />}
                                            onClick={() => setActiveStep(0)}
                                            sx={{ 
                                                borderColor: 'primary.main',
                                                color: 'primary.main'
                                            }}
                                        >
                                            Quay lại
                                        </Button>
                                        
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            disabled={loading}
                                            startIcon={loading ? <CircularProgress size={20} /> : <VpnKey />}
                                            sx={{ 
                                                background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                '&:hover': {
                                                    background: 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                    boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                }
                                            }}
                                        >
                                            {loading ? 'Đang xử lý...' : 'Xác thực'}
                                        </Button>
                                    </Box>
                                </Box>
                            )}

                            {/* Step 3: Reset Password */}
                            {activeStep === 2 && (
                                <Box component="form" onSubmit={handleResetPassword}>
                                    <Typography variant="h6" sx={{ mb: 2, color: 'text.primary' }}>
                                        Đặt lại mật khẩu
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                                        Nhập mật khẩu mới cho tài khoản của bạn
                                    </Typography>

                                    <TextField
                                        fullWidth
                                        label="Mật khẩu mới"
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => {
                                            setNewPassword(e.target.value);
                                            if (e.target.value) validatePassword(e.target.value);
                                        }}
                                        error={!!passwordError}
                                        helperText={passwordError || 'Ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt'}
                                        required
                                        sx={{ mb: 3 }}
                                        variant="outlined"
                                        InputProps={{
                                            startAdornment: <VpnKey sx={{ mr: 1, color: 'action.active' }} />
                                        }}
                                    />

                                    <TextField
                                        fullWidth
                                        label="Xác nhận mật khẩu"
                                        type="password"
                                        value={confirmPassword}
                                        onChange={(e) => {
                                            setConfirmPassword(e.target.value);
                                            if (e.target.value) validateConfirmPassword(e.target.value);
                                        }}
                                        error={!!confirmPasswordError}
                                        helperText={confirmPasswordError}
                                        required
                                        sx={{ mb: 3 }}
                                        variant="outlined"
                                    />

                                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'space-between' }}>
                                        <Button
                                            variant="outlined"
                                            startIcon={<ArrowBack />}
                                            onClick={() => setActiveStep(1)}
                                            sx={{ 
                                                borderColor: 'primary.main',
                                                color: 'primary.main'
                                            }}
                                        >
                                            Quay lại
                                        </Button>
                                        
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            disabled={loading}
                                            startIcon={loading ? <CircularProgress size={20} /> : <VpnKey />}
                                            sx={{ 
                                                background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                '&:hover': {
                                                    background: 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                    boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                }
                                            }}
                                        >
                                            {loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
                                        </Button>
                                    </Box>
                                </Box>
                            )}

                            {/* Success Step */}
                            {activeStep === 3 && (
                                <Box sx={{ textAlign: 'center' }}>
                                    <Typography variant="h6" sx={{ mb: 2, color: 'success.main' }}>
                                        🎉 Đặt lại mật khẩu thành công!
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                                        Bạn sẽ được chuyển đến trang đăng nhập sau 2 giây...
                                    </Typography>
                                    
                                    <Button
                                        variant="contained"
                                        onClick={handleBackToLogin}
                                        sx={{ 
                                            background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                            boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                        }}
                                    >
                                        Đăng nhập ngay
                                    </Button>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                </Container>
            </Box>
        </ThemeProvider>
    );
};

export default ForgotPassword; 