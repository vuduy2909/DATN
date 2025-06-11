import React, { useEffect, useState, useCallback } from 'react'
import Header from '../components/Header/Header'
import Footer from '../components/Footer/Footer'
import { useAuth } from '../AuthContext'
import { 
    Container, 
    Grid, 
    Card, 
    CardContent, 
    Typography, 
    Button, 
    TextField, 
    Avatar,
    Box,
    CircularProgress,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
    Snackbar,
    Alert,
    Paper,
    Tabs,
    Tab,
    Divider,
    IconButton,
    InputAdornment,
    Skeleton,
    Chip,
    Fade,
    Grow
} from '@mui/material';
import {
    Person,
    PhotoCamera,
    Lock,
    Visibility,
    VisibilityOff,
    Save,
    Edit,
    Settings,
    Security,
    AccountCircle,
    LocalFireDepartment,
    Star,
    FitnessCenter
} from '@mui/icons-material';
import { request } from '../utils/axiosInstance';

const Account = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState({});
    const [loading, setLoading] = useState(false);
    const [loadingUser, setLoadingUser] = useState(true);
    const [currentTab, setCurrentTab] = useState(0);
    
    // StartGym Red Theme Colors with #e74c3c
    const redTheme = {
        primary: '#e74c3c',      // Main red
        secondary: '#c0392b',    // Darker red
        accent: '#f39c12',       // Orange accent
        background: '#fdf2f2',   // Very light red background
        cardBg: '#ffffff',       // White cards
        border: '#fadbd8',       // Light red border
        text: '#2c3e50',         // Dark gray text
        textLight: '#64748b',    // Light gray text
        success: '#27ae60',      // Green for success
        warning: '#f39c12',      // Orange for warning
        error: '#e74c3c'         // Red for errors
    };
    
    // Snackbar state for notifications
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: '',
        severity: 'info'
    });

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
    
    // Password states
    const [passwordData, setPasswordData] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [showPasswords, setShowPasswords] = useState({
        old: false,
        new: false,
        confirm: false
    });
    
    // Password field focus states for helperText
    const [passwordFieldFocus, setPasswordFieldFocus] = useState({
        old: false,
        new: false,
        confirm: false
    });
    
    // Profile edit states
    const [profileData, setProfileData] = useState({
        customerEmail: '',
        customerPhone: '',
        customerAddress: '',
        customerGender: '',
        customerDateOfBirth: ''
    });
    
    // Avatar states
    const [selectedFile, setSelectedFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                setLoadingUser(true);
                const res = await request.get('/customer/detail')
                setUserData(res)
                setPreviewUrl(res.customerImg)
                
                // Process gender properly - handle both string and boolean values
                let processedGender = '';
                if (typeof res.customerGender === 'string') {
                    // If it's a string, use it directly (like "Male" or "Female")
                    processedGender = res.customerGender;
                } else if (typeof res.customerGender === 'boolean') {
                    // If it's boolean, convert to string
                    processedGender = res.customerGender === true ? 'Male' : 'Female';
                }
                
                // Set profile data for editing
                setProfileData({
                    customerEmail: res.customerEmail || '',
                    customerPhone: res.customerPhone || '',
                    customerAddress: res.customerAddress || '',
                    customerGender: processedGender,
                    customerDateOfBirth: res.dateOfBirth ? res.dateOfBirth.split(' ')[0] : ''
                });
            } catch (error) {
                console.log("Error fetching user information", error);
            } finally {
                setLoadingUser(false);
            }
        }

        if (user) {
            fetchUserData();
        }
    }, [user])

    // Handle avatar upload
    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                showSnackbar('Kích thước file phải nhỏ hơn 5MB', 'error');
                return;
            }
            
            if (!file.type.startsWith('image/')) {
                showSnackbar('Vui lòng chọn file hình ảnh', 'error');
                return;
            }
            
            setSelectedFile(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleAvatarUpload = async () => {
        if (!selectedFile) {
            showSnackbar('Vui lòng chọn hình ảnh trước', 'error');
            return;
        }

        setLoading(true);
        const formData = new FormData();
        formData.append('avatar', selectedFile);
        
        // Add old avatar URL for deletion if it exists
        if (userData.customerImg) {
            formData.append('oldAvatarUrl', userData.customerImg);
        }

        try {
            const response = await request.post('/customer/upload-avatar', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            
            showSnackbar('Cập nhật ảnh đại diện thành công! 🎉', 'success');
            setSelectedFile(null);
            
            // Refresh user data and update preview
            await refreshUserData();
            
            // Clear preview URL object to prevent memory leaks
            if (previewUrl && previewUrl.startsWith('blob:')) {
                URL.revokeObjectURL(previewUrl);
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.response?.data || 'Upload thất bại';
            showSnackbar(errorMessage, 'error');
            console.log("Error uploading avatar", error);
        } finally {
            setLoading(false);
        }
    };

    // Refresh user data
    const refreshUserData = async () => {
        try {
            const res = await request.get('/customer/detail');
            setUserData(res);
            setPreviewUrl(res.customerImg);
            
            // Process gender properly - handle both string and boolean values
            let processedGender = '';
            if (typeof res.customerGender === 'string') {
                processedGender = res.customerGender;
            } else if (typeof res.customerGender === 'boolean') {
                processedGender = res.customerGender === true ? 'Male' : 'Female';
            }
            
            // Update profile data
            setProfileData({
                customerEmail: res.customerEmail || '',
                customerPhone: res.customerPhone || '',
                customerAddress: res.customerAddress || '',
                customerGender: processedGender,
                customerDateOfBirth: res.dateOfBirth ? res.dateOfBirth.split(' ')[0] : ''
            });
        } catch (error) {
            console.log("Error refreshing user data", error);
        }
    };

    // Handle profile data changes
    const handleProfileChange = (field, value) => {
        setProfileData(prev => ({ ...prev, [field]: value }));
    };

    // Handle profile form submission
    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        
        setLoading(true);
        try {
            await request.put('/customer/update-profile', profileData);
            showSnackbar('Cập nhật thông tin thành công! 🎉', 'success');
            await refreshUserData();
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.response?.data || 'Cập nhật thất bại';
            showSnackbar(errorMessage, 'error');
            console.log("Error updating profile", error);
        } finally {
            setLoading(false);
        }
    };

    // Password validation function
    const validatePassword = (password) => {
        if (!password) {
            return 'Mật khẩu là bắt buộc';
        }
        if (password.length < 8) {
            return 'Mật khẩu phải có ít nhất 8 ký tự';
        }
        if (!/(?=.*[a-z])/.test(password)) {
            return 'Mật khẩu phải có ít nhất 1 chữ thường';
        }
        if (!/(?=.*[A-Z])/.test(password)) {
            return 'Mật khẩu phải có ít nhất 1 chữ hoa';
        }
        if (!/(?=.*\d)/.test(password)) {
            return 'Mật khẩu phải có ít nhất 1 số';
        }
        if (!/(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])/.test(password)) {
            return 'Mật khẩu phải có ít nhất 1 ký tự đặc biệt';
        }
        return '';
    };

    // Handle password change
    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        
        if (!passwordData.oldPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
            showSnackbar('Vui lòng điền đầy đủ thông tin', 'error');
            return;
        }
        
        // Validate new password strength
        const passwordValidationError = validatePassword(passwordData.newPassword);
        if (passwordValidationError) {
            showSnackbar(passwordValidationError, 'error');
            return;
        }
        
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            showSnackbar('Mật khẩu xác nhận không khớp', 'error');
            return;
        }

        setLoading(true);
        try {
            await request.post('/customer/change-password', {
                oldPassword: passwordData.oldPassword,
                newPassword: passwordData.newPassword
            });
            
            showSnackbar('Đổi mật khẩu thành công! 🎉', 'success');
            setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
            await refreshUserData();
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.response?.data || 'Đổi mật khẩu thất bại. Vui lòng kiểm tra mật khẩu cũ.';
            showSnackbar(errorMessage, 'error');
            console.log("Error changing password", error);
        } finally {
            setLoading(false);
        }
    };

    const togglePasswordVisibility = (field) => {
        setShowPasswords(prev => ({ ...prev, [field]: !prev[field] }));
    };

    // Handle password field focus for helperText
    const handlePasswordFocus = (field) => {
        setPasswordFieldFocus(prev => ({ ...prev, [field]: true }));
    };

    const handlePasswordBlur = (field) => {
        // Keep showing helperText even on blur - only hide when field is empty
        if (field === 'new' && !passwordData.newPassword) {
            setPasswordFieldFocus(prev => ({ ...prev, [field]: false }));
        }
        if (field === 'confirm' && !passwordData.confirmPassword) {
            setPasswordFieldFocus(prev => ({ ...prev, [field]: false }));
        }
    };

    if (loadingUser) {
    return (
        <div>
            <Header />
                <Container maxWidth="lg" sx={{ py: 4 }}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={4}>
                            <Skeleton variant="rectangular" height={300} sx={{ borderRadius: 2 }} />
                        </Grid>
                        <Grid item xs={12} md={8}>
                            <Skeleton variant="rectangular" height={400} sx={{ borderRadius: 2 }} />
                        </Grid>
                    </Grid>
                </Container>
                <Footer />
            </div>
        );
    }

    return (
        <div style={{ backgroundColor: redTheme.background, minHeight: '100vh' }}>
            <Header />
            <Container maxWidth="lg" sx={{ py: 4 }}>
                {/* Header Banner */}
                <Fade in timeout={800}>
                    <Card 
                        elevation={12}
                        sx={{ 
                            mb: 4,
                            background: `linear-gradient(135deg, ${redTheme.primary} 0%, ${redTheme.secondary} 100%)`,
                            color: 'white',
                            position: 'relative',
                            overflow: 'hidden',
                            borderRadius: 3
                        }}
                    >
                        <CardContent sx={{ p: 4, textAlign: 'center' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 2 }}>
                                <FitnessCenter sx={{ fontSize: 40, mr: 2 }} />
                                <Typography variant="h3" sx={{ fontWeight: 'bold' }}>
                                    Cài đặt tài khoản
                                </Typography>
                                <LocalFireDepartment sx={{ fontSize: 40, ml: 2 }} />
                            </Box>
                            <Typography variant="h6" sx={{ opacity: 0.9 }}>
                                Quản lý thông tin cá nhân và bảo mật tài khoản StartGym
                            </Typography>
                        </CardContent>
                    </Card>
                </Fade>

                <Grid container spacing={4}>
                    {/* User Info Sidebar */}
                    <Grid item xs={12} md={4}>
                        <Grow in timeout={1000}>
                            <Card 
                                elevation={8}
                                sx={{ 
                                    background: `linear-gradient(135deg, ${redTheme.primary} 0%, ${redTheme.secondary} 100%)`,
                                    color: 'white',
                                    textAlign: 'center',
                                    borderRadius: 3
                                }}
                            >
                                <CardContent sx={{ p: 4 }}>
                                    <Avatar
                                        src={userData.customerImg}
                                        sx={{ 
                                            width: 100, 
                                            height: 100, 
                                            mx: 'auto',
                                            mb: 2,
                                            border: '4px solid white',
                                            boxShadow: '0 8px 24px rgba(0,0,0,0.2)'
                                        }}
                                    />
                                    <Typography variant="h5" sx={{ fontWeight: 'bold', mb: 1 }}>
                                        {userData.customerName || 'Chưa cập nhật tên'}
                                    </Typography>
                                    <Typography variant="body2" sx={{ opacity: 0.9, mb: 2 }}>
                                        {userData.customerEmail}
                                    </Typography>
                                    <Chip 
                                        icon={<Star />}
                                        label="StartGym Member"
                                        sx={{ 
                                            backgroundColor: 'rgba(255,255,255,0.2)',
                                            color: 'white',
                                            fontWeight: 'bold',
                                            mb: 2,
                                            backdropFilter: 'blur(10px)'
                                        }}
                                    />
                                    <Box sx={{ 
                                        backgroundColor: 'rgba(255,255,255,0.2)',
                                        borderRadius: 2,
                                        p: 2,
                                        backdropFilter: 'blur(10px)'
                                    }}>
                                        <Typography variant="body2" sx={{ mb: 0.5, opacity: 0.9 }}>
                                            Loại tài khoản
                                        </Typography>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                                            {userData.customerTypeName || 'Regular'}
                                        </Typography>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grow>
                        </Grid>
                        
                    {/* Settings Content */}
                    <Grid item xs={12} md={8}>
                        <Grow in timeout={1200}>
                            <Paper 
                                elevation={8} 
                                sx={{ 
                                    overflow: 'hidden',
                                    borderRadius: 3,
                                    border: `2px solid ${redTheme.border}`
                                }}
                            >
                                <Tabs
                                    value={currentTab}
                                    onChange={(e, newValue) => setCurrentTab(newValue)}
                                    variant="fullWidth"
                                    sx={{
                                        backgroundColor: redTheme.primary,
                                        '& .MuiTab-root': {
                                            color: 'rgba(255,255,255,0.8)',
                                            fontWeight: 'bold',
                                            textTransform: 'none',
                                            fontSize: '1rem',
                                            '&.Mui-selected': {
                                                color: 'white'
                                            },
                                            '&:hover': {
                                                backgroundColor: 'rgba(255,255,255,0.1)'
                                            }
                                        },
                                        '& .MuiTabs-indicator': {
                                            backgroundColor: 'white',
                                            height: 3
                                        }
                                    }}
                                >
                                    <Tab 
                                        icon={<Person />} 
                                        label="Hồ sơ" 
                                        iconPosition="start"
                                    />
                                    <Tab 
                                        icon={<PhotoCamera />} 
                                        label="Ảnh đại diện" 
                                        iconPosition="start"
                                    />
                                    <Tab 
                                        icon={<Lock />} 
                                        label="Mật khẩu" 
                                        iconPosition="start"
                                    />
                                </Tabs>

                                <Box sx={{ p: 0 }}>
                                    {currentTab === 0 && (
                                        <Card elevation={0} sx={{ backgroundColor: redTheme.cardBg }}>
                                            <CardContent sx={{ p: 4 }}>
                                                <Typography variant="h5" sx={{ mb: 3, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                                    <Edit sx={{ mr: 1, color: redTheme.primary }} />
                                                    Chỉnh sửa thông tin cá nhân
                                    </Typography>
                                    
                                                <Box component="form" onSubmit={handleProfileSubmit}>
                                                    <Grid container spacing={3}>
                                                        <Grid item xs={12} md={6}>
                                        <TextField
                                                                key="email-field"
                                            fullWidth
                                            label="Email"
                                            value={profileData.customerEmail}
                                            onChange={(e) => handleProfileChange('customerEmail', e.target.value)}
                                                                variant="outlined"
                                                                type="email"
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12} md={6}>
                                        <TextField
                                                                key="phone-field"
                                            fullWidth
                                                                label="Số điện thoại"
                                            value={profileData.customerPhone}
                                            onChange={(e) => handleProfileChange('customerPhone', e.target.value)}
                                                                variant="outlined"
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12}>
                                        <TextField
                                                                key="address-field"
                                            fullWidth
                                                                label="Địa chỉ"
                                            value={profileData.customerAddress}
                                            onChange={(e) => handleProfileChange('customerAddress', e.target.value)}
                                                                variant="outlined"
                                            multiline
                                            rows={2}
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12} md={6}>
                                                            <FormControl fullWidth>
                                                                <InputLabel sx={{ '&.Mui-focused': { color: redTheme.primary } }}>Giới tính</InputLabel>
                                            <Select
                                                value={profileData.customerGender}
                                                                    label="Giới tính"
                                                onChange={(e) => handleProfileChange('customerGender', e.target.value)}
                                                                    sx={{
                                                                        '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: redTheme.primary }
                                                                    }}
                                            >
                                                                    <MenuItem value="Male">Nam</MenuItem>
                                                                    <MenuItem value="Female">Nữ</MenuItem>
                                            </Select>
                                        </FormControl>
                                                        </Grid>
                                        
                                                        <Grid item xs={12} md={6}>
                                        <TextField
                                                                key="dob-field"
                                            fullWidth
                                                                label="Ngày sinh"
                                            value={profileData.customerDateOfBirth}
                                            onChange={(e) => handleProfileChange('customerDateOfBirth', e.target.value)}
                                                                variant="outlined"
                                                                type="date"
                                            InputLabelProps={{ shrink: true }}
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12}>
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            disabled={loading}
                                                                startIcon={loading ? <CircularProgress size={20} /> : <Save />}
                                                                sx={{
                                                                    background: `linear-gradient(135deg, ${redTheme.primary} 0%, ${redTheme.secondary} 100%)`,
                                                                    color: 'white',
                                                                    px: 4,
                                                                    py: 1.5,
                                                                    borderRadius: 2,
                                                                    fontWeight: 'bold',
                                                                    textTransform: 'none',
                                                                    boxShadow: `0 4px 15px ${redTheme.primary}40`,
                                                                    '&:hover': {
                                                                        background: `linear-gradient(135deg, ${redTheme.secondary} 0%, ${redTheme.accent} 100%)`,
                                                                        boxShadow: `0 6px 20px ${redTheme.primary}60`
                                                                    }
                                                                }}
                                                            >
                                                                {loading ? 'Đang cập nhật...' : 'Cập nhật thông tin'}
                                        </Button>
                                                        </Grid>
                                                    </Grid>
                                    </Box>
                                </CardContent>
                            </Card>
                                    )}
                                    
                                    {currentTab === 1 && (
                                        <Card elevation={0} sx={{ backgroundColor: redTheme.cardBg }}>
                                            <CardContent sx={{ p: 4, textAlign: 'center' }}>
                                                <Typography variant="h5" sx={{ mb: 3, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                                    <PhotoCamera sx={{ mr: 1, color: redTheme.primary }} />
                                                    Thay đổi ảnh đại diện
                                                </Typography>

                                                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
                                                    <Box sx={{ position: 'relative' }}>
                                                        <Avatar
                                                            src={previewUrl}
                                                            sx={{ 
                                                                width: 150, 
                                                                height: 150,
                                                                border: `4px solid ${redTheme.primary}`,
                                                                boxShadow: '0 8px 24px rgba(0,0,0,0.15)'
                                                            }}
                                                        />
                                                        <IconButton
                                                            component="label"
                                                            sx={{
                                                                position: 'absolute',
                                                                bottom: 0,
                                                                right: 0,
                                                                backgroundColor: redTheme.primary,
                                                                color: 'white',
                                                                '&:hover': { backgroundColor: redTheme.secondary }
                                                            }}
                                                        >
                                                            <PhotoCamera />
                                                            <input
                                                                type="file"
                                                                hidden
                                                                accept="image/*"
                                                                onChange={handleFileSelect}
                                                            />
                                                        </IconButton>
                                                    </Box>

                                                    {selectedFile && (
                                                        <Alert severity="info" sx={{ mt: 2 }}>
                                                            Đã chọn: {selectedFile.name}
                                                        </Alert>
                                                    )}

                                                    <Button
                                                        variant="contained"
                                                        onClick={handleAvatarUpload}
                                                        disabled={!selectedFile || loading}
                                                        startIcon={loading ? <CircularProgress size={20} /> : <Save />}
                                                        sx={{
                                                            background: `linear-gradient(135deg, ${redTheme.success} 0%, #34d399 100%)`,
                                                            color: 'white',
                                                            px: 4,
                                                            py: 1.5,
                                                            borderRadius: 2,
                                                            fontWeight: 'bold',
                                                            textTransform: 'none',
                                                            boxShadow: `0 4px 15px ${redTheme.success}40`,
                                                            '&:hover': {
                                                                background: `linear-gradient(135deg, #059669 0%, #10b981 100%)`,
                                                                boxShadow: `0 6px 20px ${redTheme.success}60`
                                                            }
                                                        }}
                                                    >
                                                        {loading ? 'Đang tải lên...' : 'Cập nhật ảnh đại diện'}
                                                    </Button>

                                                    <Typography variant="body2" sx={{ color: redTheme.textLight, mt: 2 }}>
                                                        Chọn ảnh có kích thước nhỏ hơn 5MB
                                                    </Typography>
                                                </Box>
                                            </CardContent>
                                        </Card>
                                    )}

                                    {currentTab === 2 && (
                                        <Card elevation={0} sx={{ backgroundColor: redTheme.cardBg }}>
                                            <CardContent sx={{ p: 4 }}>
                                                <Typography variant="h5" sx={{ mb: 3, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                                    <Lock sx={{ mr: 1, color: redTheme.primary }} />
                                                    Đổi mật khẩu
                                                </Typography>

                                                <Box component="form" onSubmit={handlePasswordSubmit}>
                                                    <Grid container spacing={3}>
                                                        <Grid item xs={12}>
                                        <TextField
                                            fullWidth
                                                                label="Mật khẩu hiện tại"
                                                                type={showPasswords.old ? 'text' : 'password'}
                                            value={passwordData.oldPassword}
                                                                onChange={(e) => setPasswordData(prev => ({ ...prev, oldPassword: e.target.value }))}
                                                                onFocus={() => handlePasswordFocus('old')}
                                                                onBlur={() => handlePasswordBlur('old')}
                                                                variant="outlined"
                                                                helperText={passwordFieldFocus.old || passwordData.oldPassword ? "Nhập mật khẩu hiện tại của bạn" : ""}
                                                                InputProps={{
                                                                    endAdornment: (
                                                                        <InputAdornment position="end">
                                                                            <IconButton onClick={() => togglePasswordVisibility('old')}>
                                                                                {showPasswords.old ? <VisibilityOff /> : <Visibility />}
                                                                            </IconButton>
                                                                        </InputAdornment>
                                                                    )
                                                                }}
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12}>
                                        <TextField
                                            fullWidth
                                                                label="Mật khẩu mới"
                                                                type={showPasswords.new ? 'text' : 'password'}
                                            value={passwordData.newPassword}
                                                                onChange={(e) => setPasswordData(prev => ({ ...prev, newPassword: e.target.value }))}
                                                                onFocus={() => handlePasswordFocus('new')}
                                                                onBlur={() => handlePasswordBlur('new')}
                                                                variant="outlined"
                                                                helperText={passwordFieldFocus.new || passwordData.newPassword ? "Tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt" : ""}
                                                                InputProps={{
                                                                    endAdornment: (
                                                                        <InputAdornment position="end">
                                                                            <IconButton onClick={() => togglePasswordVisibility('new')}>
                                                                                {showPasswords.new ? <VisibilityOff /> : <Visibility />}
                                                                            </IconButton>
                                                                        </InputAdornment>
                                                                    )
                                                                }}
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12}>
                                        <TextField
                                            fullWidth
                                                                label="Xác nhận mật khẩu mới"
                                                                type={showPasswords.confirm ? 'text' : 'password'}
                                            value={passwordData.confirmPassword}
                                                                onChange={(e) => setPasswordData(prev => ({ ...prev, confirmPassword: e.target.value }))}
                                                                onFocus={() => handlePasswordFocus('confirm')}
                                                                onBlur={() => handlePasswordBlur('confirm')}
                                                                variant="outlined"
                                                                helperText={passwordFieldFocus.confirm || passwordData.confirmPassword ? "Nhập lại mật khẩu mới để xác nhận" : ""}
                                                                InputProps={{
                                                                    endAdornment: (
                                                                        <InputAdornment position="end">
                                                                            <IconButton onClick={() => togglePasswordVisibility('confirm')}>
                                                                                {showPasswords.confirm ? <VisibilityOff /> : <Visibility />}
                                                                            </IconButton>
                                                                        </InputAdornment>
                                                                    )
                                                                }}
                                                                sx={{
                                                                    '& .MuiOutlinedInput-root': {
                                                                        '&:hover fieldset': { borderColor: redTheme.primary },
                                                                        '&.Mui-focused fieldset': { borderColor: redTheme.primary }
                                                                    },
                                                                    '& .MuiInputLabel-root.Mui-focused': { color: redTheme.primary }
                                                                }}
                                                            />
                                                        </Grid>
                                                        
                                                        <Grid item xs={12}>
                                        <Button
                                            type="submit"
                                            variant="contained"
                                            disabled={loading}
                                                                startIcon={loading ? <CircularProgress size={20} /> : <Security />}
                                                                sx={{
                                                                    background: `linear-gradient(135deg, ${redTheme.warning} 0%, #fbbf24 100%)`,
                                                                    color: 'white',
                                                                    px: 4,
                                                                    py: 1.5,
                                                                    borderRadius: 2,
                                                                    fontWeight: 'bold',
                                                                    textTransform: 'none',
                                                                    boxShadow: `0 4px 15px ${redTheme.warning}40`,
                                                                    '&:hover': {
                                                                        background: `linear-gradient(135deg, #f59e0b 0%, #d97706 100%)`,
                                                                        boxShadow: `0 6px 20px ${redTheme.warning}60`
                                                                    }
                                                                }}
                                                            >
                                                                {loading ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
                                        </Button>
                                                        </Grid>
                                                    </Grid>
                                    </Box>
                                </CardContent>
                            </Card>
                                    )}
                                </Box>
                            </Paper>
                        </Grow>
                        </Grid>
                    </Grid>
                </Container>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
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

            <Footer />
        </div>
    );
};

export default Account;