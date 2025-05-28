import React, { useEffect, useState } from 'react'
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
    Alert
} from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera, faKey, faUser } from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';
import styles from '../components/UserInformation/UserInformation.module.css'

const Account = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState({});
    const [loading, setLoading] = useState(false);
    
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
                const res = await request.get('/customer/detail')
                setUserData(res)
                setPreviewUrl(res.customerImg)
                
                // Set profile data for editing
                setProfileData({
                    customerEmail: res.accountEmail || '',
                    customerPhone: res.customerPhone || '',
                    customerAddress: res.customerAddress || '',
                    customerGender: res.customerGender === true ? 'Male' : res.customerGender === false ? 'Female' : res.customerGender || '',
                    customerDateOfBirth: res.dateOfBirth ? res.dateOfBirth.split(' ')[0] : ''
                });
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        if (user) {
            fetchUserData();
        }
    }, [user])

    // Handle profile data change
    const handleProfileChange = (field, value) => {
        setProfileData(prev => ({ ...prev, [field]: value }));
    };

    // Handle profile update
    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        
        // Validate email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(profileData.customerEmail)) {
            showSnackbar('Please enter a valid email address', 'error');
            return;
        }
        
        // Validate phone
        if (!/^\d{10,11}$/.test(profileData.customerPhone)) {
            showSnackbar('Phone number must be 10-11 digits', 'error');
            return;
        }
        
        // Validate date of birth (must be >16 years old)
        if (profileData.customerDateOfBirth) {
            const birthDate = new Date(profileData.customerDateOfBirth);
            const today = new Date();
            const age = today.getFullYear() - birthDate.getFullYear();
            const monthDiff = today.getMonth() - birthDate.getMonth();
            
            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }
            
            if (age < 16) {
                showSnackbar('You must be at least 16 years old', 'error');
                return;
            }
        }

        setLoading(true);
        try {
            await request.put('/customer/update-profile', profileData);
            
            showSnackbar('Profile updated successfully! 🎉', 'success');
            setUserData(prev => ({ ...prev, ...profileData }));
        } catch (error) {
            showSnackbar('Failed to update profile. Please try again.', 'error');
            console.log("Error updating profile", error);
        } finally {
            setLoading(false);
        }
    };

    // Handle avatar upload
    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                showSnackbar('File size must be less than 5MB', 'error');
                return;
            }
            
            if (!file.type.startsWith('image/')) {
                showSnackbar('Please select an image file', 'error');
                return;
            }
            
            setSelectedFile(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleAvatarUpload = async () => {
        if (!selectedFile) {
            showSnackbar('Please select an image first', 'error');
            return;
        }

        setLoading(true);
        const formData = new FormData();
        formData.append('avatar', selectedFile);

        try {
            const response = await request.post('/customer/upload-avatar', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            
            showSnackbar('Avatar updated successfully! 🎉', 'success');
            setUserData(prev => ({ ...prev, customerImg: response.data.avatarUrl }));
            setSelectedFile(null);
        } catch (error) {
            showSnackbar('Failed to upload avatar. Please try again.', 'error');
            console.log("Error uploading avatar", error);
        } finally {
            setLoading(false);
        }
    };

    // Handle password change
    const handlePasswordChange = (field, value) => {
        setPasswordData(prev => ({ ...prev, [field]: value }));
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            showSnackbar('New passwords do not match', 'error');
            return;
        }
        
        if (passwordData.newPassword.length < 6) {
            showSnackbar('New password must be at least 6 characters', 'error');
            return;
        }

        setLoading(true);
        try {
            await request.post('/customer/change-password', {
                oldPassword: passwordData.oldPassword,
                newPassword: passwordData.newPassword
            });
            
            showSnackbar('Password changed successfully! 🎉', 'success');
            setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            showSnackbar('Failed to change password. Please check your current password.', 'error');
            console.log("Error changing password", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <Header />
            <div className={styles.container}>
                <Container component="main" maxWidth="lg">
                    <Typography variant="h4" component="h1" gutterBottom style={{ textAlign: 'center', marginBottom: '30px' }}>
                        Account Settings
                    </Typography>
                    
                    <Grid container spacing={3}>
                        {/* Avatar Section */}
                        <Grid item xs={12} md={4}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" component="div" gutterBottom style={{ display: 'flex', alignItems: 'center' }}>
                                        <FontAwesomeIcon icon={faCamera} style={{ marginRight: '10px', color: 'var(--color-red-body)' }} />
                                        Profile Picture
                                    </Typography>
                                    
                                    <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                                        <Avatar
                                            src={previewUrl || userData.customerImg}
                                            sx={{ width: 120, height: 120 }}
                                        />
                                        
                                        <input
                                            accept="image/*"
                                            style={{ display: 'none' }}
                                            id="avatar-upload"
                                            type="file"
                                            onChange={handleFileSelect}
                                        />
                                        <label htmlFor="avatar-upload">
                                            <Button variant="outlined" component="span">
                                                Choose Image
                                            </Button>
                                        </label>
                                        
                                        {selectedFile && (
                                            <Button
                                                variant="contained"
                                                onClick={handleAvatarUpload}
                                                disabled={loading}
                                                style={{ backgroundColor: 'var(--color-red-body)' }}
                                            >
                                                {loading ? <CircularProgress size={20} /> : 'Upload Avatar'}
                                            </Button>
                                        )}
                                        
                                        <Typography variant="caption" color="textSecondary">
                                            Max file size: 5MB. Supported formats: JPG, PNG, GIF
                                        </Typography>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                        
                        {/* Profile Information Section */}
                        <Grid item xs={12} md={4}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" component="div" gutterBottom style={{ display: 'flex', alignItems: 'center' }}>
                                        <FontAwesomeIcon icon={faUser} style={{ marginRight: '10px', color: 'var(--color-red-body)' }} />
                                        Personal Information
                                    </Typography>
                                    
                                    <Box component="form" onSubmit={handleProfileSubmit} sx={{ mt: 2 }}>
                                        <TextField
                                            fullWidth
                                            type="email"
                                            label="Email"
                                            value={profileData.customerEmail}
                                            onChange={(e) => handleProfileChange('customerEmail', e.target.value)}
                                            margin="normal"
                                            required
                                        />
                                        
                                        <TextField
                                            fullWidth
                                            type="tel"
                                            label="Phone Number"
                                            value={profileData.customerPhone}
                                            onChange={(e) => handleProfileChange('customerPhone', e.target.value)}
                                            margin="normal"
                                            required
                                            helperText="10-11 digits"
                                        />
                                        
                                        <TextField
                                            fullWidth
                                            label="Address"
                                            value={profileData.customerAddress}
                                            onChange={(e) => handleProfileChange('customerAddress', e.target.value)}
                                            margin="normal"
                                            multiline
                                            rows={2}
                                        />
                                        
                                        <FormControl fullWidth margin="normal">
                                            <InputLabel>Gender</InputLabel>
                                            <Select
                                                value={profileData.customerGender}
                                                onChange={(e) => handleProfileChange('customerGender', e.target.value)}
                                                label="Gender"
                                            >
                                                <MenuItem value="Male">Male</MenuItem>
                                                <MenuItem value="Female">Female</MenuItem>
                                                <MenuItem value="Other">Other</MenuItem>
                                            </Select>
                                        </FormControl>
                                        
                                        <TextField
                                            fullWidth
                                            type="date"
                                            label="Date of Birth"
                                            value={profileData.customerDateOfBirth}
                                            onChange={(e) => handleProfileChange('customerDateOfBirth', e.target.value)}
                                            margin="normal"
                                            InputLabelProps={{ shrink: true }}
                                            helperText="Must be at least 16 years old"
                                            inputProps={{
                                                max: new Date(new Date().getFullYear() - 16, new Date().getMonth(), new Date().getDate()).toISOString().split('T')[0]
                                            }}
                                        />
                                        
                                        <Button
                                            type="submit"
                                            fullWidth
                                            variant="contained"
                                            disabled={loading}
                                            sx={{ mt: 3, backgroundColor: 'var(--color-red-body)' }}
                                        >
                                            {loading ? <CircularProgress size={20} /> : 'Update Profile'}
                                        </Button>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                        
                        {/* Password Change Section */}
                        <Grid item xs={12} md={4}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" component="div" gutterBottom style={{ display: 'flex', alignItems: 'center' }}>
                                        <FontAwesomeIcon icon={faKey} style={{ marginRight: '10px', color: 'var(--color-red-body)' }} />
                                        Change Password
                                    </Typography>
                                    
                                    <Box component="form" onSubmit={handlePasswordSubmit} sx={{ mt: 2 }}>
                                        <TextField
                                            fullWidth
                                            type="password"
                                            label="Current Password"
                                            value={passwordData.oldPassword}
                                            onChange={(e) => handlePasswordChange('oldPassword', e.target.value)}
                                            margin="normal"
                                            required
                                        />
                                        
                                        <TextField
                                            fullWidth
                                            type="password"
                                            label="New Password"
                                            value={passwordData.newPassword}
                                            onChange={(e) => handlePasswordChange('newPassword', e.target.value)}
                                            margin="normal"
                                            required
                                            helperText="Minimum 6 characters"
                                        />
                                        
                                        <TextField
                                            fullWidth
                                            type="password"
                                            label="Confirm New Password"
                                            value={passwordData.confirmPassword}
                                            onChange={(e) => handlePasswordChange('confirmPassword', e.target.value)}
                                            margin="normal"
                                            required
                                        />
                                        
                                        <Button
                                            type="submit"
                                            fullWidth
                                            variant="contained"
                                            disabled={loading}
                                            sx={{ mt: 3, backgroundColor: 'var(--color-red-body)' }}
                                        >
                                            {loading ? <CircularProgress size={20} /> : 'Change Password'}
                                        </Button>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>

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
                </Container>
            </div>
            <Footer />
        </div>
    )
}

export default Account 