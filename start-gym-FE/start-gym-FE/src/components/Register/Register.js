import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './Register.module.css'
import { TextField, FormControl, InputLabel, Select, MenuItem, Button, Snackbar, Alert } from '@mui/material';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import dayjs from 'dayjs';

const Register = () => {
    const [name, setName] = useState('');
    const [nameError, setNameError] = useState('');

    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');

    const [gender, setGender] = useState('');

    const [phone, setPhone] = useState('');
    const [phoneError, setPhoneError] = useState('');

    const [dateOfBirth, setDateOfBirth] = useState(null);
    const [dateOfBirthError, setDateOfBirthError] = useState('');

    const [address, setAddress] = useState('');
    const [addressError, setAddressError] = useState('');

    const [email, setEmail] = useState('');
    const [emailError, setEmailError] = useState('');

    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');

    const [confirmPassword, setConfirmPassword] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState('');

    // Snackbar state for notifications
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: '',
        severity: 'info' // 'error', 'warning', 'info', 'success'
    });

    const navigate = useNavigate();

    const showSnackbar = (message, severity = 'info') => {
        setSnackbar({
            open: true,
            message,
            severity
        });
    };

    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackbar({ ...snackbar, open: false });
    };

    const handleLogin = async () => {
        navigate('/login')
    };

    const handleNameChange = (e) => {
        const value = e.target.value;
        setName(value);

        if (value.length < 3 || value.length > 50) {
            setNameError('Họ tên phải có độ dài từ 3 đến 50 ký tự');
        } else if (!/^[a-zA-ZÀ-Ỹà-ỹ ]+$/.test(value)) {
            setNameError('Họ tên không được chứa ký tự đặc biệt hoặc số');
        } else {
            setNameError('');
        }
    };

    const handleUsernameChange = (e) => {
        const value = e.target.value;
        setUsername(value);

        if (value.length < 3 || value.length > 50) {
            setUsernameError('Tên đăng nhập phải có độ dài từ 3 đến 50 ký tự');
        } else {
            setUsernameError('');
        }
    };

    const handlePhoneNumberChange = (e) => {
        const value = e.target.value;
        setPhone(value);

        if (!/^\d+$/.test(value)) {
            setPhoneError('Vui lòng chỉ nhập số');
        } else if (value.length !== 10) {
            setPhoneError('Số điện thoại phải chứa 10 số')
        } else {
            const prefixes = /^(086|096|097|098|038|037|036|035|034|033|032|091|094|088|081|082|083|084|085|070|076|077|078|079|089|090|093|092|052|056|058|099|059|087)/;
            if (!prefixes.test(value.substr(0, 3))) {
                setPhoneError('Số điện thoại không hợp lệ, vui lòng kiểm tra lại tiền tố');
            } else {
                setPhoneError('');
            }
        }
    }

    const handleDateOfBirthChange = (newDate) => {
        setDateOfBirth(newDate);
        
        if (!newDate) {
            setDateOfBirthError('Vui lòng chọn ngày sinh');
            return;
        }

        // Calculate age
        const today = dayjs();
        const birthDate = dayjs(newDate);
        const age = today.diff(birthDate, 'year');
        
        if (age < 16) {
            setDateOfBirthError('Bạn phải từ 16 tuổi trở lên để đăng ký');
        } else {
            setDateOfBirthError('');
        }
    };

    const handleAddressChange = (e) => {
        const value = e.target.value;
        setAddress(value)

        if (value.length < 5) {
            setAddressError('Địa chỉ ngắn quá (< 5 ký tự)')
        } else if (value.length > 100) {
            setAddressError('Địa chỉ dài quá (> 100 ký tự)')
        } else {
            setAddressError('')
        }
    }

    const handleEmailChange = (e) => {
        const value = e.target.value;
        setEmail(value)
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailRegex.test(value)) {
            setEmailError('Email không đúng định dạng')
        } else if (value.length > 30) {
            setEmailError('Email quá dài (> 30 ký tự)')
        } else {
            setEmailError('')
        }
    }

    const handlePasswordChange = (e) => {
        const value = e.target.value;
        setPassword(value)

        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;
        if (!passwordRegex.test(value)) {
            setPasswordError('Password chưa đủ mạnh. Yêu cầu ít nhất 8 ký tự, chứa ít nhất một chữ cái và một chữ số.');
        } else {
            setPasswordError('');
        }
    }

    const handleConfirmPasswordChange = (e) => {
        const value = e.target.value;
        setConfirmPassword(value);

        if (value !== password) {
            setConfirmPasswordError('Confirm password không trùng khớp')
        } else {
            setConfirmPasswordError('')
        }
    }

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            // Validate form trước khi submit
            if (!name || !username || !gender || !phone || !dateOfBirth || !address || !email || !password || !confirmPassword) {
                showSnackbar('Vui lòng điền đầy đủ thông tin', 'warning');
                return;
            }

            if (nameError || usernameError || phoneError || dateOfBirthError || addressError || emailError || passwordError || confirmPasswordError) {
                showSnackbar('Vui lòng sửa các lỗi trong form', 'warning');
                return;
            }

            // Double check age validation
            const today = dayjs();
            const birthDate = dayjs(dateOfBirth);
            const age = today.diff(birthDate, 'year');
            
            if (age < 16) {
                showSnackbar('Bạn phải từ 16 tuổi trở lên để đăng ký', 'warning');
                return;
            }

            // Format date of birth
            const formattedDate = dateOfBirth ? dateOfBirth.format('YYYY-MM-DD') : null;

            const userData = {
                name: name,
                username: username,
                gender: gender === true ? 'Male' : 'Female',
                dateOfBirth: formattedDate,
                address: address,
                phone: phone,
                email: email,
                password: password
            };

            const response = await axios.post('http://localhost:8080/api/v1/public/signup', userData);

            if (response.status === 200) {
                showSnackbar('Đăng ký thành công! 🎉', 'success');
                setTimeout(() => navigate('/login'), 1500); // Delay để user thấy thông báo
            } else {
                console.error('Đăng ký không thành công');
                showSnackbar('Đăng ký không thành công. Vui lòng thử lại.', 'error');
            }
        } catch (error) {
            console.error('Đã xảy ra lỗi:', error);
            if (error.response && error.response.data) {
                showSnackbar('Lỗi: ' + (error.response.data.message || 'Đăng ký thất bại'), 'error');
            } else {
                showSnackbar('Đã xảy ra lỗi. Vui lòng thử lại.', 'error');
            }
        }
    };

    // Calculate max date (16 years ago from today)
    const maxDate = dayjs().subtract(16, 'year');

    return (
        <div className={styles.container}>
            {/* Left Side - Marketing Content */}
            <div className={styles.leftSide}>
                <div className={styles.contentWrapper}>
                    <h1 className={styles.title}>Transform Your Fitness</h1>
                    <h1 className={styles.title}>Journey with</h1>
                    <h1 className={styles.titleRed}><span className={styles.startGymText}>StartGym</span></h1>
                    <p className={styles.subtitle}>
                        Discover the future of fitness with our AI-powered platform. Personalized workouts, smart nutrition, and innovative solutions await you. Embark on a journey to a healthier, stronger you – where technology meets transformation. Your fitness evolution starts here.
                    </p>
                </div>
            </div>

            {/* Right Side - Registration Form */}
            <div className={styles.rightSide}>
                <div className={styles.formContainer}>
                    <div className={styles.formHeader}>
                        <div className={styles.iconWrapper}>
                            <LockOutlinedIcon />
                        </div>
                        <h2 className={styles.formTitle}>Sign up</h2>
                    </div>

                    <form onSubmit={handleRegister} className={styles.form}>
                        <div className={styles.formRow}>
                            <div className={styles.formField}>
                                <TextField
                                    value={name}
                                    onChange={handleNameChange}
                                    fullWidth
                                    id="name"
                                    label="Full name * *"
                                    variant="outlined"
                                    size="small"
                                    required
                                    error={!!nameError}
                                    helperText={nameError}
                                />
                            </div>
                            <div className={styles.formField}>
                                <TextField
                                    value={username}
                                    onChange={handleUsernameChange}
                                    fullWidth
                                    id="username"
                                    label="User name ..."
                                    variant="outlined"
                                    size="small"
                                    required
                                    error={!!usernameError}
                                    helperText={usernameError}
                                />
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.formFieldSmall}>
                                <FormControl fullWidth required size="small">
                                    <InputLabel id="gender-label">Gender</InputLabel>
                                    <Select
                                        labelId="gender-label"
                                        label="Gender"
                                        value={gender}
                                        onChange={(e) => setGender(e.target.value)}
                                    >
                                        <MenuItem value={true}>Male</MenuItem>
                                        <MenuItem value={false}>Female</MenuItem>
                                    </Select>
                                </FormControl>
                            </div>
                            <div className={styles.formFieldLarge}>
                                <TextField
                                    value={phone}
                                    onChange={handlePhoneNumberChange}
                                    fullWidth
                                    id="phone"
                                    label="Phone number * *"
                                    variant="outlined"
                                    size="small"
                                    required
                                    error={!!phoneError}
                                    helperText={phoneError}
                                />
                            </div>
                        </div>

                        <div className={styles.formField}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DatePicker
                                    label="Date of Birth * *"
                                    value={dateOfBirth}
                                    onChange={handleDateOfBirthChange}
                                    maxDate={maxDate}
                                    slotProps={{
                                        textField: {
                                            fullWidth: true,
                                            required: true,
                                            size: 'small',
                                            error: !!dateOfBirthError,
                                            helperText: dateOfBirthError || 'Phải từ 16 tuổi trở lên'
                                        }
                                    }}
                                />
                            </LocalizationProvider>
                        </div>

                        <div className={styles.formField}>
                            <TextField
                                value={address}
                                onChange={handleAddressChange}
                                fullWidth
                                id="address"
                                label="Address * *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!addressError}
                                helperText={addressError}
                            />
                        </div>

                        <div className={styles.formField}>
                            <TextField
                                value={email}
                                onChange={handleEmailChange}
                                fullWidth
                                id="email"
                                label="Email * *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!emailError}
                                helperText={emailError}
                            />
                        </div>

                        <div className={styles.formField}>
                            <TextField
                                value={password}
                                onChange={handlePasswordChange}
                                fullWidth
                                id="password"
                                type="password"
                                label="Password * *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!passwordError}
                                helperText={passwordError}
                            />
                        </div>

                        <div className={styles.formField}>
                            <TextField
                                value={confirmPassword}
                                onChange={handleConfirmPasswordChange}
                                fullWidth
                                id="confirmPassword"
                                type="password"
                                label="Confirm Password * *"
                                variant="outlined"
                                size="small"
                                required
                                error={!!confirmPasswordError}
                                helperText={confirmPasswordError}
                            />
                        </div>

                        <Button 
                            type="submit" 
                            fullWidth 
                            variant="contained" 
                            className={styles.submitButton}
                        >
                            SIGN UP
                        </Button>

                        <div className={styles.loginLink}>
                            <span onClick={handleLogin} className={styles.link}>
                                Already have an account? Sign in
                            </span>
                        </div>
                    </form>
                </div>
            </div>

            {/* Snackbar for notifications */}
            <Snackbar 
                open={snackbar.open} 
                autoHideDuration={4000} 
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert 
                    onClose={handleSnackbarClose} 
                    severity={snackbar.severity} 
                    sx={{ width: '100%' }}
                    variant="filled"
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </div>
    );
};

export default Register;
