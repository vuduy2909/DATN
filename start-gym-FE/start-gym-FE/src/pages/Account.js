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
    Alert,
    CircularProgress
} from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera, faKey } from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';
import styles from '../components/UserInformation/UserInformation.module.css'

const Account = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState({});
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });
    
    // Password states
    const [passwordData, setPasswordData] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
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
            } catch (error) {
                console.log("Error fetching user information", error);
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
                setMessage({ type: 'error', text: 'File size must be less than 5MB' });
                return;
            }
            
            if (!file.type.startsWith('image/')) {
                setMessage({ type: 'error', text: 'Please select an image file' });
                return;
            }
            
            setSelectedFile(file);
            setPreviewUrl(URL.createObjectURL(file));
            setMessage({ type: '', text: '' });
        }
    };

    const handleAvatarUpload = async () => {
        if (!selectedFile) {
            setMessage({ type: 'error', text: 'Please select an image first' });
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
            
            setMessage({ type: 'success', text: 'Avatar updated successfully!' });
            setUserData(prev => ({ ...prev, customerImg: response.data.avatarUrl }));
            setSelectedFile(null);
        } catch (error) {
            setMessage({ type: 'error', text: 'Failed to upload avatar. Please try again.' });
            console.log("Error uploading avatar", error);
        } finally {
            setLoading(false);
        }
    };

    // Handle password change
    const handlePasswordChange = (field, value) => {
        setPasswordData(prev => ({ ...prev, [field]: value }));
        setMessage({ type: '', text: '' });
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            setMessage({ type: 'error', text: 'New passwords do not match' });
            return;
        }
        
        if (passwordData.newPassword.length < 6) {
            setMessage({ type: 'error', text: 'New password must be at least 6 characters' });
            return;
        }

        setLoading(true);
        try {
            await request.post('/customer/change-password', {
                oldPassword: passwordData.oldPassword,
                newPassword: passwordData.newPassword
            });
            
            setMessage({ type: 'success', text: 'Password changed successfully!' });
            setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            setMessage({ type: 'error', text: 'Failed to change password. Please check your current password.' });
            console.log("Error changing password", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <Header />
            <div className={styles.container}>
                <Container component="main" maxWidth="md">
                    <Typography variant="h4" component="h1" gutterBottom style={{ textAlign: 'center', marginBottom: '30px' }}>
                        Account Settings
                    </Typography>
                    
                    {message.text && (
                        <Alert severity={message.type} style={{ marginBottom: '20px' }}>
                            {message.text}
                        </Alert>
                    )}
                    
                    <Grid container spacing={3}>
                        {/* Avatar Section */}
                        <Grid item xs={12} md={6}>
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
                        
                        {/* Password Change Section */}
                        <Grid item xs={12} md={6}>
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
                </Container>
            </div>
            <Footer />
        </div>
    )
}

export default Account 