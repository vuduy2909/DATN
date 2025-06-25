import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
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
    Checkbox,
    FormControlLabel,
    Link,
    Alert,
    Paper,
    Divider,
    useTheme,
    createTheme,
    ThemeProvider
} from '@mui/material';
import {
    LockOutlined,
    FitnessCenter,
    LocalFireDepartment
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

const Login = () => {
    const { setUser } = useAuth();
    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();
    const location = useLocation();

    // Get returnUrl from location state if available
    const returnUrl = location.state?.returnUrl || '/';

    const handleLogin = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        
        try {
            const BACKEND_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
            const response = await axios.post(`${BACKEND_URL}/api/v1/public/login`, {
                username: username,
                password: password
            });

            console.log("Login response:", response.data);
            console.log("User role:", response.data.roles);
            
            if (response.status === 200) {
                const token = response.data.token;
                localStorage.setItem('token', token);
                localStorage.setItem('username', username);
                setUser(token);
                
                // Set admin status and redirect accordingly
                if (response.data.roles == 'ROLE_ADMIN') {
                    console.log("User is ADMIN, setting admin status and redirecting...");
                    localStorage.setItem('isAdmin', 'true');
                    navigate('/admin', { replace: true });
                } else {
                    console.log("User is not admin, redirecting...");
                    localStorage.setItem('isAdmin', 'false');
                    // Redirect to returnUrl if provided, otherwise to home
                    navigate(returnUrl, { replace: true });
                }
            } else {
                setErrorMessage('Thông tin đăng nhập không chính xác');
            }
        } catch (error) {
            console.error('Đã xảy ra lỗi:', error);
            console.log("Login error details:", error);

            if (error.response && error.response.data) {
                console.log("Login error response:", error.response.data);
                
                // ===== CASE 1: USER IS BANNED =====
                // Check if backend explicitly returned ban information
                if (error.response.data.isBanned === true) {
                    console.log("🚫 User is banned - redirecting to contact page");
                    navigate('/contact', { 
                        state: { 
                            isBanned: true, 
                            username: username,
                            customerName: error.response.data.customerName || username,
                            reason: error.response.data.reason || error.response.data.banDetails || 'Account is disabled'
                        } 
                    });
                    return;
                }
                
                // ===== CASE 2: CHECK SPECIFIC ERROR MESSAGES =====
                const errorMessage = error.response.data.error || 
                                    error.response.data.message || 
                                    (typeof error.response.data === 'string' ? error.response.data : '');
                
                // Check for ban keywords in error message
                const bannedKeywords = ['banned', 'contact support', 'disabled'];
                const isBannedMessage = bannedKeywords.some(keyword => 
                    errorMessage && errorMessage.toLowerCase().includes(keyword.toLowerCase())
                );
                
                if (isBannedMessage) {
                    console.log("🚫 Ban keyword detected - redirecting to contact page");
                    navigate('/contact', { 
                        state: { 
                            isBanned: true, 
                            username: username,
                            reason: errorMessage
                        } 
                    });
                    return;
                }
                
                // ===== CASE 3: USER NOT FOUND vs INVALID CREDENTIALS =====
                if (error.response.status === 401) {
                    // Backend should return specific messages for these cases
                    if (errorMessage.includes('does not exist') || 
                        errorMessage.includes('not found') ||
                        errorMessage.includes('không tồn tại')) {
                        console.log("ℹ️ User does not exist");
                        setErrorMessage('Tài khoản không tồn tại. Vui lòng kiểm tra lại username hoặc đăng ký tài khoản mới.');
                        return;
                    } else {
                        console.log("ℹ️ Invalid credentials for existing user");
                        setErrorMessage('Thông tin đăng nhập không chính xác. Vui lòng kiểm tra lại username và password.');
                        return;
                    }
                }
                
                // ===== CASE 4: OTHER SPECIFIC ERRORS =====
                if (error.response.status === 500) {
                    console.log("🔧 Server configuration error");
                    setErrorMessage('Lỗi hệ thống. Vui lòng liên hệ hỗ trợ.');
                    return;
                }
                
                // Display specific error message if available
                if (errorMessage) {
                    setErrorMessage(errorMessage);
                    return;
                }
            }

            // Default error message for unexpected errors
            setErrorMessage('Đăng nhập thất bại. Vui lòng thử lại sau.');
        }
    };

    const handleNavigateToRegister = () => {
        navigate('/register');
    };

    const handleGoogleLoginSuccess = async (response) => {
        try {
            const googleToken = response.credential;
            const BACKEND_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
            const googleResponse = await axios.post(`${BACKEND_URL}/api/v1/public/oauth/google`, {
                token: googleToken,
            });

            if (googleResponse.status === 200) {
                const token = googleResponse.data.token;
                localStorage.setItem('token', token);
                setUser(token);
                
                // Store username from Google response if available
                if (googleResponse.data.username || googleResponse.data.email) {
                    localStorage.setItem('username', googleResponse.data.username || googleResponse.data.email);
                }
                
                if (googleResponse.data.roles === 'ROLE_ADMIN') {
                    console.log("Google login: User is ADMIN, setting admin status and redirecting...");
                    localStorage.setItem('isAdmin', 'true');
                    navigate('/admin', { replace: true });
                } else {
                    console.log("Google login: User is not admin, redirecting to home...");
                    localStorage.setItem('isAdmin', 'false');
                    navigate('/', { replace: true });
                }
            } else {
                setErrorMessage('Đăng nhập Google thất bại');
            }
        } catch (error) {
            console.error('Error during Google login:', error);
            
            // Check if this is a banned user error
            if (error.response && error.response.data) {
                console.log("Google login error response:", error.response.data);
                
                // Extract email or username from response if available
                let username = '';
                if (error.response.data.email) {
                    username = error.response.data.email;
                } else if (error.response.data.username) {
                    username = error.response.data.username;
                }
                
                // Check for banned account based on various error message patterns
                const errorMessage = error.response.data.error || 
                                    error.response.data.message || 
                                    (typeof error.response.data === 'string' ? error.response.data : '');
                
                const bannedKeywords = ['banned', 'disabled', 'suspended', 'blocked', 'Customer account is banned'];
                
                const isBanned = bannedKeywords.some(keyword => 
                    errorMessage && errorMessage.toLowerCase().includes(keyword.toLowerCase())
                );
                
                if (isBanned) {
                    console.log("Google user is banned, redirecting to contact page");
                    navigate('/contact', { 
                        state: { 
                            isBanned: true, 
                            username: username 
                        } 
                    });
                    return;
                }
            }
            
            setErrorMessage('Đăng nhập Google thất bại');
        }
    };

    const handleGoogleLoginFailure = (error) => {
        console.error('Google login failed:', error);
        setErrorMessage('Đăng nhập Google thất bại');
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
                <Container maxWidth="lg">
                    <Grid container spacing={4} sx={{ minHeight: '80vh', alignItems: 'center' }}>
                        {/* Left Side - Marketing Content */}
                        <Grid item xs={12} md={6}>
                            <Box sx={{ 
                                textAlign: 'center', 
                                p: 4,
                               // background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
                               //background: 'linear-gradient(135deg, #f1f3f4 0%, #e8eaf6 100%)',
                                 //background: 'linear-gradient(135deg, #fefefe 0%, #f7f7f7 100%)',
                                 background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
                                borderRadius: 4,
                                color: 'black',
                                boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)'
                            }}>
                                <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                                    <FitnessCenter sx={{ fontSize: 60, mr: 2, color: 'black' }} />
                                    <LocalFireDepartment sx={{ fontSize: 60, color: 'black' }} />
                                </Box>
                                
                                <Typography variant="h3" sx={{ mb: 2, fontWeight: 'bold', color: 'black' }}>
                                    Unleash Your
                                </Typography>
                                <Typography variant="h3" sx={{ mb: 2, fontWeight: 'bold', color: 'black' }}>
                                    Potential
                                </Typography>
                                <Typography variant="h3" sx={{ mb: 4, fontWeight: 'bold', color: '#CF4044' }}>
                                    with StartGym AI
                                </Typography>
                                
                                <Typography variant="h6" sx={{ lineHeight: 1.6, color: 'black', opacity: 0.8 }}>
                                    Discover the future of fitness with our AI-powered platform. 
                                    Personalized workouts, smart nutrition, and innovative solutions await you. 
                                    Embark on a journey to a healthier, stronger you.
                                </Typography>
                            </Box>
                        </Grid>

                        {/* Right Side - Login Form */}
                        <Grid item xs={12} md={6}>
                            <Card elevation={12} sx={{ 
                                borderRadius: 4,
                                boxShadow: '0 20px 40px rgba(0,0,0,0.1)'
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
                                            <LockOutlined sx={{ fontSize: 30 }} />
                                        </Avatar>
                                        <Typography variant="h4" sx={{ color: 'text.primary', fontWeight: 'bold' }}>
                                            Sign In
                                        </Typography>
                                        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1 }}>
                                            Welcome back to StartGym
                                        </Typography>
                                    </Box>

                                    {/* Error Message */}
                                    {errorMessage && (
                                        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
                                            {errorMessage}
                                        </Alert>
                                    )}

                                    {/* Login Form */}
                                    <Box component="form" onSubmit={handleLogin}>
                                        <TextField
                                            fullWidth
                                            label="Username or Email"
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                            error={!!usernameError}
                                            helperText={usernameError}
                                            required
                                            sx={{ mb: 3 }}
                                            variant="outlined"
                                        />

                                        <TextField
                                            fullWidth
                                            label="Password"
                                            type="password"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            error={!!passwordError}
                                            helperText={passwordError}
                                            required
                                            sx={{ mb: 3 }}
                                            variant="outlined"
                                        />

                                        <FormControlLabel
                                            control={
                                                <Checkbox 
                                                    checked={rememberMe}
                                                    onChange={(e) => setRememberMe(e.target.checked)}
                                                    sx={{
                                                        color: 'primary.main',
                                                        '&.Mui-checked': {
                                                            color: 'primary.main',
                                                        },
                                                    }}
                                                />
                                            }
                                            label="Remember me"
                                            sx={{ mb: 3 }}
                                        />

                                        <Button
                                            type="submit"
                                            fullWidth
                                            variant="contained"
                                            size="large"
                                            sx={{ 
                                                mb: 3,
                                                py: 1.5,
                                                background: 'linear-gradient(135deg, #CF4044 0%, #B8363C 100%)',
                                                boxShadow: '0 8px 16px rgba(207, 64, 68, 0.3)',
                                                '&:hover': {
                                                    background: 'linear-gradient(135deg, #B8363C 0%, #A62F33 100%)',
                                                    boxShadow: '0 12px 24px rgba(207, 64, 68, 0.4)',
                                                }
                                            }}
                                        >
                                            SIGN IN
                                        </Button>

                                        {/* Links */}
                                        <Box sx={{ textAlign: 'center', mb: 3 }}>
                                            <Link 
                                                component="button"
                                                variant="body2"
                                                onClick={() => navigate('/forgot-password')}
                                                sx={{ 
                                                    color: 'primary.main',
                                                    textDecoration: 'none',
                                                    '&:hover': { textDecoration: 'underline' },
                                                    display: 'block',
                                                    mb: 1
                                                }}
                                            >
                                                Forgot password?
                                            </Link>
                                            <Link 
                                                component="button"
                                                variant="body2"
                                                onClick={handleNavigateToRegister}
                                                sx={{ 
                                                    color: 'primary.main',
                                                    textDecoration: 'none',
                                                    '&:hover': { textDecoration: 'underline' }
                                                }}
                                            >
                                                Don't have an account? Sign Up
                                            </Link>
                                        </Box>

                                        {/* Divider */}
                                        <Divider sx={{ mb: 3 }}>
                                            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                                                OR
                                            </Typography>
                                        </Divider>

                                        {/* Google Login */}
                                        <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                                            <GoogleOAuthProvider clientId="407042400394-382jp0sktf2dn0eqjg4m9c2ujkc4lgqq.apps.googleusercontent.com">
                                                <GoogleLogin
                                                    onSuccess={handleGoogleLoginSuccess}
                                                    onFailure={handleGoogleLoginFailure}
                                                    cookiePolicy={'single_host_origin'}
                                                    useOneTap
                                                />
                                            </GoogleOAuthProvider>
                                        </Box>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </Container>
            </Box>
        </ThemeProvider>
    );
};

export default Login;
