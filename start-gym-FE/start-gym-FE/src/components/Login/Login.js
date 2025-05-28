import React, { useState } from 'react';
import axios from 'axios';
import styles from './Login.module.css'
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import { TextField, Button } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';

const Login = () => {
    const { setUser } = useAuth();
    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/api/v1/public/login', {
                username: username,
                password: password
            });

            console.log("Login response:", response.data);
            console.log("User role:", response.data.roles);
            
            if (response.status === 200) {
                const token = response.data.token;
                localStorage.setItem('token', token);
                setUser(token);
                
                // Set admin status and redirect accordingly
                if (response.data.roles == 'ROLE_ADMIN') {
                    console.log("User is ADMIN, setting admin status and redirecting...");
                    localStorage.setItem('isAdmin', 'true');
                    navigate('/admin', { replace: true });
                } else {
                    console.log("User is not admin, redirecting to home...");
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
                setUser(token);
                
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
        <div className={styles.container}>
            {/* Left Side - Marketing Content */}
            <div className={styles.leftSide}>
                <div className={styles.contentWrapper}>
                    <h1 className={styles.title}>Unleash Your</h1>
                    <h1 className={styles.title}>Potential</h1>
                    <h1 className={styles.titleRed}><span className={styles.startGymText}>with StartGym AI</span></h1>
                    <p className={styles.subtitle}>
                        Discover the future of fitness with our AI-powered platform. Personalized workouts, smart nutrition, and innovative solutions await you. Embark on a journey to a healthier, stronger you – where technology meets transformation. Your fitness evolution starts here.
                    </p>
                </div>
            </div>

            {/* Right Side - Login Form */}
            <div className={styles.rightSide}>
                <div className={styles.formContainer}>
                    <div className={styles.formHeader}>
                        <div className={styles.iconWrapper}>
                            <LockOutlinedIcon />
                        </div>
                        <h2 className={styles.formTitle}>Sign in</h2>
                    </div>

                    <form onSubmit={handleLogin} className={styles.form}>
                        <div className={styles.formField}>
                            <TextField
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                fullWidth
                                id="username"
                                label="User name *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!usernameError}
                                helperText={usernameError}
                            />
                        </div>

                        <div className={styles.formField}>
                            <TextField
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                fullWidth
                                id="password"
                                type="password"
                                label="Password *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!passwordError}
                                helperText={passwordError}
                            />
                        </div>

                        <div className={styles.checkboxWrapper}>
                            <label className={styles.checkboxLabel}>
                                <input type="checkbox" className={styles.checkbox} />
                                Remember me
                            </label>
                        </div>

                        <Button 
                            type="submit" 
                            fullWidth 
                            variant="contained" 
                            className={styles.submitButton}
                        >
                            SIGN IN
                        </Button>

                        <div className={styles.links}>
                            <span onClick={handleNavigateToRegister} className={styles.link}>
                                Forgot password?
                            </span>
                            <span onClick={handleNavigateToRegister} className={styles.link}>
                                Don't have an account? Sign Up
                            </span>
                        </div>

                        <div className={styles.googleLogin}>
                            <GoogleOAuthProvider clientId="407042400394-382jp0sktf2dn0eqjg4m9c2ujkc4lgqq.apps.googleusercontent.com">
                                <GoogleLogin
                                    onSuccess={handleGoogleLoginSuccess}
                                    onFailure={handleGoogleLoginFailure}
                                    cookiePolicy={'single_host_origin'}
                                    useOneTap
                                />
                            </GoogleOAuthProvider>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;
