import React, { useState } from 'react';
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
    ThemeProvider
} from '@mui/material';
import {
    PersonAdd,
    FitnessCenter,
    LocalFireDepartment
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';

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
    },
});

const Register = () => {
    const [customerName, setCustomerName] = useState('');
    const [customerAddress, setCustomerAddress] = useState('');
    const [customerAge, setCustomerAge] = useState('');
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
    
    const navigate = useNavigate();

    const handleNavigateToLogin = () => {
        navigate('/login');
    };

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

    const handleRegister = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        // Validate all fields
        const isEmailValid = validateEmail(customerEmail);
        const isPhoneValid = validatePhone(customerPhone);
        const isPasswordValid = validatePassword(password);

        // Validate age from date of birth
        if (customerDob) {
            const today = dayjs();
            const birthDate = dayjs(customerDob);
            const age = today.diff(birthDate, 'year');
            
            if (age < 16) {
                setErrorMessage('You must be at least 16 years old to register');
                return;
            }
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
                customer: {
                    customer_name: customerName,
                    customer_address: customerAddress,
                    customer_age: parseInt(customerAge),
                    customer_phone: customerPhone,
                    customer_email: customerEmail,
                    customer_gender: customerGender,
                    customer_dob: customerDob ? customerDob.format('YYYY-MM-DD') : null
                }
            };

            const response = await axios.post('http://localhost:8080/api/v1/public/register', registrationData);

            if (response.status === 200) {
                setSuccessMessage('Đăng ký thành công! Vui lòng đăng nhập.');
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            }
        } catch (error) {
            console.error('Đã xảy ra lỗi:', error);
            if (error.response && error.response.data) {
                setErrorMessage(error.response.data.message || 'Đăng ký thất bại');
            } else {
                setErrorMessage('Đăng ký thất bại. Vui lòng thử lại.');
            }
        }
    };

    return (
        <ThemeProvider theme={startGymTheme}>
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
                                            <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>
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
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={6}>
                                                    <TextField
                                                        fullWidth
                                                        label="Age *"
                                                        type="number"
                                                        value={customerAge}
                                                        onChange={(e) => setCustomerAge(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                        inputProps={{ min: 1, max: 120 }}
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
                                                    >
                                                        <MenuItem value="Male">Male</MenuItem>
                                                        <MenuItem value="Female">Female</MenuItem>
                                                        <MenuItem value="Other">Other</MenuItem>
                                                    </TextField>
                                                </Grid>
                                                
                                                <Grid item xs={12}>
                                                    <DatePicker
                                                        label="Date of Birth"
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
                                                                error: !!dobError,
                                                                helperText: dobError || (dobFocused ? 'Must be at least 16 years old' : ''),
                                                                onFocus: () => setDobFocused(true),
                                                                onBlur: () => setDobFocused(false)
                                                            }
                                                        }}
                                                        maxDate={dayjs().subtract(16, 'year')}
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
                                                            if (e.target.value) validateEmail(e.target.value);
                                                        }}
                                                        onFocus={() => setEmailFocused(true)}
                                                        onBlur={() => {
                                                            setEmailFocused(false);
                                                            validateEmail(customerEmail);
                                                        }}
                                                        error={!!emailError}
                                                        helperText={emailError || (emailFocused ? 'Enter a valid email address' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
                                                    />
                                                </Grid>
                                                
                                                <Grid item xs={12}>
                                                    <TextField
                                                        fullWidth
                                                        label="Phone Number *"
                                                        value={customerPhone}
                                                        onChange={(e) => {
                                                            setCustomerPhone(e.target.value);
                                                            if (e.target.value) validatePhone(e.target.value);
                                                        }}
                                                        onFocus={() => setPhoneFocused(true)}
                                                        onBlur={() => {
                                                            setPhoneFocused(false);
                                                            validatePhone(customerPhone);
                                                        }}
                                                        error={!!phoneError}
                                                        helperText={phoneError || (phoneFocused ? 'Format: 0xxxxxxxxx or +84xxxxxxxxx' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
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
                                                        rows={1}
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
                                                        onChange={(e) => setUsername(e.target.value)}
                                                        required
                                                        variant="outlined"
                                                        size="small"
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
                                                        helperText={passwordError || (passwordFocused ? 'Min 8 chars, 1 uppercase, 1 lowercase, 1 number' : '')}
                                                        required
                                                        variant="outlined"
                                                        size="small"
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
                                                    />
                                                </Grid>
                                            </Grid>

                                            {/* Submit Button */}
                                            <Button
                                                type="submit"
                                                fullWidth
                                                variant="contained"
                                                size="medium"
                                                sx={{ 
                                                    mb: 2,
                                                    py: 1.2,
                                                    background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                    boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                    '&:hover': {
                                                        background: 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                        boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                    }
                                                }}
                                            >
                                                CREATE ACCOUNT
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
