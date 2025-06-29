import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './Register.module.css';
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
    MenuItem,
    Divider,
    createTheme,
    ThemeProvider,
    CircularProgress,
    CssBaseline
} from '@mui/material';
import {
    PersonAdd,
    FitnessCenter,
    LocalFireDepartment,
    CheckCircle
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';
import { axiosInstance } from '../../utils/axiosInstance';

// StartGym Theme (same as Login)
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
        MuiCssBaseline: {
            styleOverrides: `
                @keyframes pulse {
                    0% {
                        box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7);
                    }
                    70% {
                        box-shadow: 0 0 0 10px rgba(76, 175, 80, 0);
                    }
                    100% {
                        box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
                    }
                }
            `,
        },
    },
});

const Register = () => {
    const [customerName, setCustomerName] = useState('');
    const [customerAddress, setCustomerAddress] = useState('');
    const [customerPhone, setCustomerPhone] = useState('');
    const [customerEmail, setCustomerEmail] = useState('');
    const [customerGender, setCustomerGender] = useState('');
    const [customerDob, setCustomerDob] = useState(null);
    const [dobError, setDobError] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    
    // Validation states
    const [emailError, setEmailError] = useState('');
    const [phoneError, setPhoneError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    
    // Focus states to show helper text only when focused
    const [emailFocused, setEmailFocused] = useState(false);
    const [phoneFocused, setPhoneFocused] = useState(false);
    const [passwordFocused, setPasswordFocused] = useState(false);
    const [dobFocused, setDobFocused] = useState(false);
    
    const [countdown, setCountdown] = useState(0);
    const [isRegistrationSuccess, setIsRegistrationSuccess] = useState(false);
    
    // Validation checking states for real-time feedback
    const [validationChecking, setValidationChecking] = useState({
        email: false,
        phone: false,
        username: false
    });
    
    const navigate = useNavigate();

    const handleNavigateToLogin = () => {
        navigate('/login');
    };

    // Real-time validation functions
    const checkEmailExists = async (email) => {
        if (!email || !email.includes('@')) return;
        
        try {
            setValidationChecking(prev => ({ ...prev, email: true }));
            const response = await axios.get(`http://localhost:8080/api/v1/public/check-email-exists?email=${encodeURIComponent(email)}`);
            
            if (response.data.exists) {
                setEmailError(response.data.message || 'Email đã được sử dụng');
                return false;
            } else {
                // Email available, but still check format
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(email)) {
                    setEmailError('Please enter a valid email address');
                    return false;
                }
                setEmailError('');
                return true;
            }
        } catch (error) {
            console.error('Error checking email:', error);
            // Don't show error to user, just continue with format validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                setEmailError('Please enter a valid email address');
                return false;
            }
            setEmailError('');
            return true;
        } finally {
            setValidationChecking(prev => ({ ...prev, email: false }));
        }
    };

    const checkPhoneExists = async (phone) => {
        if (!phone || phone.length < 10) return;
        
        try {
            setValidationChecking(prev => ({ ...prev, phone: true }));
            const response = await axios.get(`http://localhost:8080/api/v1/public/check-phone-exists?phone=${encodeURIComponent(phone)}`);
            
            if (response.data.exists) {
                setPhoneError(response.data.message);
                return false;
            } else {
                // Phone available, but still check format
                const phoneRegex = /^(\+84|84|0)[3|5|7|8|9][0-9]{8}$/;
                if (!phoneRegex.test(phone)) {
                    setPhoneError('Please enter a valid Vietnamese phone number');
                    return false;
                }
                setPhoneError('');
                return true;
            }
        } catch (error) {
            console.error('Error checking phone:', error);
            // Don't show error to user, just continue with format validation
            const phoneRegex = /^(\+84|84|0)[3|5|7|8|9][0-9]{8}$/;
            if (!phoneRegex.test(phone)) {
                setPhoneError('Please enter a valid Vietnamese phone number');
                return false;
            }
            setPhoneError('');
            return true;
        } finally {
            setValidationChecking(prev => ({ ...prev, phone: false }));
        }
    };

    const checkUsernameExists = async (username) => {
        if (!username || username.length < 3) return;
        
        try {
            setValidationChecking(prev => ({ ...prev, username: true }));
            const response = await axios.get(`http://localhost:8080/api/v1/public/check-username-exists?username=${encodeURIComponent(username)}`);
            
            if (response.data.exists) {
                // Since we don't have a separate usernameError state, we'll use errorMessage for immediate feedback
                setErrorMessage(response.data.message || 'Tên đăng nhập đã được sử dụng');
                return false;
            } else {
                setErrorMessage('');
                return true;
            }
        } catch (error) {
            console.error('Error checking username:', error);
            return true; // Don't block user if API fails
        } finally {
            setValidationChecking(prev => ({ ...prev, username: false }));
        }
    };

    // Debounced validation functions
    const [debounceTimers, setDebounceTimers] = useState({
        email: null,
        phone: null,
        username: null
    });

    const debouncedCheckEmail = (email) => {
        if (debounceTimers.email) {
            clearTimeout(debounceTimers.email);
        }
        
        const timerId = setTimeout(() => {
            checkEmailExists(email);
        }, 800);
        
        setDebounceTimers(prev => ({ ...prev, email: timerId }));
    };

    const debouncedCheckPhone = (phone) => {
        if (debounceTimers.phone) {
            clearTimeout(debounceTimers.phone);
        }
        
        const timerId = setTimeout(() => {
            checkPhoneExists(phone);
        }, 800);
        
        setDebounceTimers(prev => ({ ...prev, phone: timerId }));
    };

    const debouncedCheckUsername = (username) => {
        if (debounceTimers.username) {
            clearTimeout(debounceTimers.username);
        }
        
        const timerId = setTimeout(() => {
            checkUsernameExists(username);
        }, 800);
        
        setDebounceTimers(prev => ({ ...prev, username: timerId }));
    };

    // Cleanup debounce timers on unmount
    useEffect(() => {
        return () => {
            Object.values(debounceTimers).forEach(timer => {
                if (timer) clearTimeout(timer);
            });
        };
    }, [debounceTimers]);

    // Email validation
    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!email) {
            setEmailError('Email is required');
            return false;
        }
        if (!emailRegex.test(email)) {
            setEmailError('Please enter a valid email address');
            return false;
        }
        setEmailError('');
        return true;
    };

    // Phone validation
    const validatePhone = (phone) => {
        const phoneRegex = /^(\+84|84|0)[3|5|7|8|9][0-9]{8}$/;
        if (!phone) {
            setPhoneError('Phone number is required');
            return false;
        }
        if (!phoneRegex.test(phone)) {
            setPhoneError('Please enter a valid Vietnamese phone number');
            return false;
        }
        setPhoneError('');
        return true;
    };

    // Password validation
    const validatePassword = (password) => {
        if (!password) {
            setPasswordError('Password is required');
            return false;
        }
        if (password.length < 8) {
            setPasswordError('Password must be at least 8 characters long');
            return false;
        }
        if (!/(?=.*[a-z])/.test(password)) {
            setPasswordError('Password must contain at least one lowercase letter');
            return false;
        }
        if (!/(?=.*[A-Z])/.test(password)) {
            setPasswordError('Password must contain at least one uppercase letter');
            return false;
        }
        if (!/(?=.*\d)/.test(password)) {
            setPasswordError('Password must contain at least one number');
            return false;
        }
        if (!/(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])/.test(password)) {
            setPasswordError('Password must contain at least one special character');
            return false;
        }
        setPasswordError('');
        return true;
    };

    const handleDateChange = (newValue) => {
        setCustomerDob(newValue);
        setDobError('');
        
        if (newValue) {
            const today = dayjs();
            const birthDate = dayjs(newValue);
            const age = today.diff(birthDate, 'year');
            
            if (age < 16) {
                setDobError('You must be at least 16 years old to register');
            }
        }
    };

    // Đếm ngược khi đăng ký thành công
    useEffect(() => {
        let timer;
        if (countdown > 0) {
            timer = setTimeout(() => {
                setCountdown(countdown - 1);
                
                // Khi đếm ngược kết thúc, chuyển trang
                if (countdown === 1) {
                    navigate('/login');
                }
            }, 1000);
        }
        
        return () => {
            if (timer) clearTimeout(timer);
        };
    }, [countdown, navigate]);

    const handleRegister = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        // Validate all fields
        const isEmailValid = validateEmail(customerEmail);
        const isPhoneValid = validatePhone(customerPhone);
        const isPasswordValid = validatePassword(password);

        // Validate date of birth is provided
        if (!customerDob) {
            setErrorMessage('Date of birth is required');
            return;
        }

        // Calculate age from date of birth
            const today = dayjs();
            const birthDate = dayjs(customerDob);
        const calculatedAge = today.diff(birthDate, 'year');
            
        if (calculatedAge < 16) {
                setErrorMessage('You must be at least 16 years old to register');
                return;
        }

        if (password !== confirmPassword) {
            setErrorMessage('Mật khẩu xác nhận không khớp');
            return;
        }

        // Stop if any validation fails
        if (!isEmailValid || !isPhoneValid || !isPasswordValid) {
            setErrorMessage('Please correct the errors above');
            return;
        }

        try {
            const registrationData = {
                username: username,
                password: password,
                name: customerName,
                address: customerAddress,
                phone: customerPhone,
                email: customerEmail,
                gender: customerGender,
                dateOfBirth: customerDob.format('YYYY-MM-DD')
            };

            console.log('Registration data:', registrationData);
            
            // Debug thông tin API gọi
            console.log('Calling API endpoint:', axiosInstance.defaults.baseURL + '/public/signup');

            // Sửa endpoint để khớp với backend: /api/v1/public/signup
            const response = await axiosInstance.post('/public/signup', registrationData);

            console.log('API response:', response);

            if (response.status === 200 || response.status === 201) {
                setIsRegistrationSuccess(true);
                setCountdown(3);
                setSuccessMessage(`Đăng ký thành công! Chuyển đến trang đăng nhập sau ${countdown} giây...`);
            }
        } catch (error) {
            console.error('Đã xảy ra lỗi:', error);
            
            // Hiển thị chi tiết lỗi để debug
            if (error.response) {
                // Server trả về lỗi với mã trạng thái
                console.error('Mã lỗi:', error.response.status);
                console.error('Dữ liệu lỗi:', error.response.data);
                setErrorMessage(error.response.data?.message || `Lỗi ${error.response.status}: Đăng ký thất bại`);
            } else if (error.request) {
                // Request đã gửi nhưng không nhận được response
                console.error('Không nhận được phản hồi từ server:', error.request);
                setErrorMessage('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.');
            } else {
                // Lỗi trong quá trình thiết lập request
                console.error('Lỗi:', error.message);
                setErrorMessage(`Lỗi: ${error.message}`);
            }
        }
    };

    // Cập nhật thông báo thành công khi countdown thay đổi
    useEffect(() => {
        if (countdown > 0) {
            setSuccessMessage(`Đăng ký thành công! Chuyển đến trang đăng nhập sau ${countdown} giây...`);
            }
    }, [countdown]);

    return (
        <ThemeProvider theme={startGymTheme}>
            <CssBaseline />
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <Box sx={{ 
                    background: 'linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%)',
                    py: 4
                }}>
                    <Container maxWidth="lg">
                        <Grid container spacing={4}>
                            {/* Left Side - Marketing Content */}
                            <Grid item xs={12} md={5}>
                                <Box 
                                    sx={{ 
                                        textAlign: 'center', 
                                        p: 4,
                                        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
                                        borderRadius: 4,
                                        color: 'black',
                                        boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)',
                                    }}
                                >
                                    <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                                        <FitnessCenter sx={{ fontSize: 60, mr: 2, color: 'black' }} />
                                        <LocalFireDepartment sx={{ fontSize: 60, color: 'black' }} />
                                    </Box>
                                    
                                    <Typography variant="h3" sx={{ mb: 2, fontWeight: 'bold', color: 'black' }}>
                                        Join the
                                    </Typography>
                                    <Typography variant="h3" sx={{ mb: 2, fontWeight: 'bold', color: 'black' }}>
                                        StartGym
                                    </Typography>
                                    <Typography variant="h3" sx={{ mb: 4, fontWeight: 'bold', color: '#CF4044' }}>
                                        Community
                                    </Typography>
                                    
                                    <Typography variant="h6" sx={{ lineHeight: 1.6, color: 'black', opacity: 0.8 }}>
                                        Begin your transformation journey with AI-powered fitness solutions. 
                                        Join thousands of members who've achieved their goals with our 
                                        personalized approach to health and wellness.
                                    </Typography>
                                </Box>
                            </Grid>

                            {/* Right Side - Registration Form */}
                            <Grid item xs={12} md={7}>
                                <Card elevation={12} sx={{ 
                                    borderRadius: 4,
                                    boxShadow: '0 20px 40px rgba(0,0,0,0.1)'
                                }}>
                                    <CardContent sx={{ p: 3 }}>
                                        {/* Messages */}
                                        {errorMessage && (
                                            <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
                                                {errorMessage}
                                            </Alert>
                                        )}
                                        {successMessage && (
                                            <Alert 
                                                severity="success" 
                                                sx={{ 
                                                    mb: 2, 
                                                    borderRadius: 2,
                                                    fontSize: '1rem',
                                                    '& .MuiAlert-icon': {
                                                        fontSize: '1.5rem'
                                                    },
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    animation: 'pulse 1.5s infinite'
                                                }}
                                                variant="filled"
                                                icon={<CheckCircle fontSize="inherit" />}
                                                action={
                                                    <CircularProgress 
                                                        size={20} 
                                                        thickness={5}
                                                        sx={{ color: 'white' }}
                                                    />
                                                }
                                            >
                                                {successMessage}
                                            </Alert>
                                        )}

                                        {/* Registration Form */}
                                        <Box component="form" onSubmit={handleRegister}>
                                            {/* Personal Information Section */}
                                            <Typography variant="h6" sx={{ mb: 1.5, color: 'primary.main', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                                Personal Information
                                            </Typography>
                                            
                                            <Grid container spacing={1.5} sx={{ mb: 2 }}>
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Full Name *"
                                                        value={customerName}
                                                        onChange={(e) => setCustomerName(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={6}>
                                                    <TextField
                                                        select
                                                        fullWidth
                                                        label="Gender *"
                                                        value={customerGender}
                                                        onChange={(e) => setCustomerGender(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                    >
                                                        <MenuItem value="Male">Male</MenuItem>
                                                        <MenuItem value="Female">Female</MenuItem>
                                                        <MenuItem value="Other">Other</MenuItem>
                                                    </TextField>
                                                </Grid>
                                                
                                                <Grid item xs={6}>
                                                    <DatePicker
                                                        label="Date of Birth *"
                                                        value={customerDob}
                                                        onChange={handleDateChange}
                                                        slots={{
                                                            textField: TextField
                                                        }}
                                                        slotProps={{
                                                            textField: {
                                                                fullWidth: true,
                                                                variant: 'outlined',
                                                                size: 'small',
                                                                required: true,
                                                                error: !!dobError,
                                                                helperText: dobError || (dobFocused ? 'Must be at least 16 years old' : ''),
                                                                onFocus: () => setDobFocused(true),
                                                                onBlur: () => setDobFocused(false)
                                                            }
                                                        }}
                                                        maxDate={dayjs().subtract(16, 'year')}
                                                        disabled={isRegistrationSuccess}
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Address *"
                                                        value={customerAddress}
                                                        onChange={(e) => setCustomerAddress(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        multiline
                                                        rows={2}
                                                        disabled={isRegistrationSuccess}
                                                    />
                                                </Grid>
                                            </Grid>

                                            {/* Contact Information Section */}
                                            <Typography variant="h6" sx={{ mb: 1.5, color: 'primary.main', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                                Contact Information
                                            </Typography>
                                            
                                            <Grid container spacing={1.5} sx={{ mb: 2 }}>
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Email *"
                                                        type="email"
                                                        value={customerEmail}
                                                        onChange={(e) => {
                                                            setCustomerEmail(e.target.value);
                                                            if (e.target.value && e.target.value.includes('@')) {
                                                                debouncedCheckEmail(e.target.value);
                                                            } else if (e.target.value) {
                                                                validateEmail(e.target.value);
                                                            }
                                                        }}
                                                        onFocus={() => setEmailFocused(true)}
                                                        onBlur={() => {
                                                            setEmailFocused(false);
                                                            if (customerEmail) {
                                                                if (customerEmail.includes('@')) {
                                                                    checkEmailExists(customerEmail);
                                                                } else {
                                                                    validateEmail(customerEmail);
                                                                }
                                                            }
                                                        }}
                                                        error={!!emailError}
                                                        helperText={emailError || (emailFocused ? 'Enter a valid email address' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                        InputProps={{
                                                            endAdornment: validationChecking.email ? <CircularProgress size={20} /> : null
                                                        }}
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Phone Number *"
                                                        value={customerPhone}
                                                        onChange={(e) => {
                                                            setCustomerPhone(e.target.value);
                                                            if (e.target.value && e.target.value.length >= 10) {
                                                                debouncedCheckPhone(e.target.value);
                                                            } else if (e.target.value) {
                                                                validatePhone(e.target.value);
                                                            }
                                                        }}
                                                        onFocus={() => setPhoneFocused(true)}
                                                        onBlur={() => {
                                                            setPhoneFocused(false);
                                                            if (customerPhone) {
                                                                if (customerPhone.length >= 10) {
                                                                    checkPhoneExists(customerPhone);
                                                                } else {
                                                                    validatePhone(customerPhone);
                                                                }
                                                            }
                                                        }}
                                                        error={!!phoneError}
                                                        helperText={phoneError || (phoneFocused ? 'Format: 0xxxxxxxxx or +84xxxxxxxxx' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                        InputProps={{
                                                            endAdornment: validationChecking.phone ? <CircularProgress size={20} /> : null
                                                        }}
                                                    />
                                                </Grid>
                                            </Grid>

                                            {/* Account Information Section */}
                                            <Typography variant="h6" sx={{ mb: 1.5, color: 'primary.main', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                                Account Information
                                            </Typography>
                                            
                                            <Grid container spacing={1.5} sx={{ mb: 2 }}>
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Username *"
                                                        value={username}
                                                        onChange={(e) => {
                                                            setUsername(e.target.value);
                                                            if (e.target.value && e.target.value.length >= 3) {
                                                                debouncedCheckUsername(e.target.value);
                                                            }
                                                        }}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                        InputProps={{
                                                            endAdornment: validationChecking.username ? <CircularProgress size={20} /> : null
                                                        }}
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={6}>
                                                    <TextField
                                                        fullWidth
                                                        label="Password *"
                                                        type="password"
                                                        value={password}
                                                        onChange={(e) => {
                                                            setPassword(e.target.value);
                                                            if (e.target.value) validatePassword(e.target.value);
                                                        }}
                                                        onFocus={() => setPasswordFocused(true)}
                                                        onBlur={() => {
                                                            setPasswordFocused(false);
                                                            validatePassword(password);
                                                        }}
                                                        error={!!passwordError}
                                                        helperText={passwordError || (passwordFocused ? 'Min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special character' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        disabled={isRegistrationSuccess}
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={6}>
                                                    <TextField
                                                        fullWidth
                                                        label="Confirm Password *"
                                                        type="password"
                                                        value={confirmPassword}
                                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        error={!!(confirmPassword && password !== confirmPassword)}
                                                        helperText={confirmPassword && password !== confirmPassword ? 'Passwords do not match' : ''}
                                                        disabled={isRegistrationSuccess}
                                                    />
                                                </Grid>
                                            </Grid>

                                            {/* Submit Button */}
                                            <Button
                                                type="submit"
                                                fullWidth
                                                variant="contained"
                                                size="medium"
                                                disabled={isRegistrationSuccess}
                                                sx={{ 
                                                    mb: 2,
                                                    py: 1.2,
                                                    background: isRegistrationSuccess ? '#A6A6A6' : 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                    boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                    '&:hover': {
                                                        background: isRegistrationSuccess ? '#A6A6A6' : 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                        boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                    }
                                                }}
                                            >
                                                {isRegistrationSuccess ? 'REGISTRATION COMPLETE' : 'CREATE ACCOUNT'}
                                            </Button>

                                            {/* Login Link */}
                                            <Box sx={{ textAlign: 'center' }}>
                                                <Link 
                                                    component="button"
                                                    variant="body2"
                                                    onClick={handleNavigateToLogin}
                                                    sx={{ 
                                                        color: 'primary.main',
                                                        textDecoration: 'none',
                                                        '&:hover': { textDecoration: 'underline' }
                                                    }}
                                                >
                                                    Already have an account? Sign In
                                                </Link>
                                            </Box>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        </Grid>
                    </Container>
                </Box>
            </LocalizationProvider>
        </ThemeProvider>
    );
};

export default Register;
