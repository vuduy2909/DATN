import React, { useState } from 'react';
import axios from 'axios';
import styles from './Login.module.css'
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import { ThemeProvider, Container, CssBaseline, Box, Avatar, Typography, TextField, FormControlLabel, Checkbox, Button, Grid, Link, createTheme } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';

const Login = () => {
    const { setUser } = useAuth();
    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const navigate = useNavigate();

    const defaultTheme = createTheme();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/login', {
                username: username,
                password: password
            });

            console.log("Login response:", response.data);
            console.log("User role:", response.data.roles);
            console.log("Role type:", typeof response.data.roles);
            console.log("Role value:", JSON.stringify(response.data.roles));
            
            if (response.status === 200) {
                const token = response.data.token;
                localStorage.setItem('token', token);
                
                // Set user and check admin status in one step
                if (response.data.roles == 'ROLE_ADMIN') {
                    console.log("User is ADMIN, setting flags and redirecting...");
                    
                    // Đặt cờ để ngăn logic điều hướng khác ghi đè
                    localStorage.setItem('isAdmin', 'true');
                    localStorage.setItem('forceAdminRedirect', 'true');
                    sessionStorage.setItem('blockHomeRedirect', 'true');
                    
                    // Lưu token
                    setUser(token);
                    
                    // QUAN TRỌNG: Sử dụng setTimeout để đảm bảo các state được lưu
                    setTimeout(() => {
                        // Sử dụng window.location.replace thay vì navigate từ React Router
                        window.location.replace('/admin');
                    }, 100);
                } else {
                    setUser(token);
                    localStorage.setItem('isAdmin', 'false');
                    navigate('/', { replace: true });
                }
            } else {
                setUsernameError('Username không hợp lệ')
                setPasswordError('Password không hợp lệ')
                console.error('Đăng nhập không thành công');
            }
        } catch (error) {
            console.error('Đã xảy ra lỗi:', error);
        }
    };

    const handleNavigateToRegister = () => {
        navigate('/register');
    };

    const handleGoogleLoginSuccess = async (response) => {
        try {
            const googleToken = response.credential;
            const googleResponse = await axios.post('http://localhost:8080/api/v1/public/oauth/google', {
                token: googleToken,
            });

            if (googleResponse.status === 200) {
                const token = googleResponse.data.token;
                localStorage.setItem('token', token);
                
                if (googleResponse.data.roles === 'ROLE_ADMIN') {
                    console.log("Google login: User is ADMIN, setting flags and redirecting...");
                    
                    // Đặt cờ để ngăn logic điều hướng khác ghi đè
                    localStorage.setItem('isAdmin', 'true');
                    localStorage.setItem('forceAdminRedirect', 'true');
                    sessionStorage.setItem('blockHomeRedirect', 'true');
                    
                    // Lưu token
                    setUser(token);
                    
                    // QUAN TRỌNG: Sử dụng setTimeout để đảm bảo các state được lưu
                    setTimeout(() => {
                        // Sử dụng window.location.replace thay vì navigate từ React Router
                        window.location.replace('/admin');
                    }, 100);
                } else {
                    setUser(token);
                    localStorage.setItem('isAdmin', 'false');
                    navigate('/', { replace: true });
                }
            } else {
                console.error('Google login not successful');
            }
        } catch (error) {
            console.error('Error during Google login:', error);
        }
    };

    const handleGoogleLoginFailure = (error) => {
        console.error('Google login failed:', error);
    };

    return (
        <div>
            <div className={styles.myContainer}>
                <div className={styles.myCol}>
                    <h1 className={styles.myTitle}>
                        <div className={styles.centered_text}>Unleash Your</div>
                        <div className={styles.centered_text}>Potential</div>
                        <span className={styles.text_red}>with StartGym AI</span>
                    </h1>
                    <p className={styles.mySubtitle}>
                        Discover the future of fitness with our AI-powered platform.
                        Personalized workouts, smart nutrition, and innovative solutions await you.
                        Embark on a journey to a healthier, stronger you – where technology meets transformation.
                        Your fitness evolution starts here.
                    </p>
                </div>
                <div className={styles.myCard}>
                    <div className={styles.myCardBody}>
                        <ThemeProvider theme={defaultTheme}>
                            <Container component="main" maxWidth="xs">
                                <CssBaseline />
                                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <Avatar sx={{ m: 1, bgcolor: 'var(--color-red-body)' }}>
                                        <LockOutlinedIcon />
                                    </Avatar>
                                    <Typography component="h1" variant="h5">
                                        Sign in
                                    </Typography>
                                    <Box component="form" Validate sx={{ mt: 1, width: '100%' }} onSubmit={handleLogin}>
                                        <TextField
                                            margin="normal"
                                            required
                                            fullWidth
                                            id="email"
                                            name="email"
                                            label="User name"
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                            autoFocus
                                            error={!!usernameError}
                                            helperText={usernameError}
                                        />
                                        <TextField
                                            margin="normal"
                                            required
                                            fullWidth
                                            id="password"
                                            name="password"
                                            label="Password"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            type="password"
                                            autoComplete="current-password"
                                            error={!!passwordError}
                                            helperText={passwordError}
                                        />
                                        <FormControlLabel control={<Checkbox value="remember" color="primary" />} label="Remember me" />
                                        <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
                                            Sign In
                                        </Button>
                                        <Grid container>
                                            <Grid item xs>
                                                <Link onClick={handleNavigateToRegister} className={styles.link} variant="body2">
                                                    Forgot password?
                                                </Link>
                                            </Grid>
                                            <Grid item>
                                                <Link onClick={handleNavigateToRegister} className={styles.link} variant="body2">
                                                    {"Don't have an account? Sign Up"}
                                                </Link>
                                            </Grid>
                                            <Grid>
                                                <GoogleOAuthProvider clientId="407042400394-382jp0sktf2dn0eqjg4m9c2ujkc4lgqq.apps.googleusercontent.com">
                                                    <GoogleLogin
                                                        onSuccess={handleGoogleLoginSuccess}
                                                        onFailure={handleGoogleLoginFailure}
                                                        cookiePolicy={'single_host_origin'}
                                                        useOneTap
                                                    />
                                                </GoogleOAuthProvider>

                                            </Grid>
                                        </Grid>
                                    </Box>
                                </Box>
                            </Container>
                        </ThemeProvider>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
