import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Snackbar,
  Alert,
  CircularProgress,
  Grid,
  Card,
  CardContent,
  Avatar,
  IconButton
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Person as PersonIcon,
  CloudUpload as CloudUploadIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { axiosInstance } from '../../utils/axiosInstance';
import { uploadCustomerAvatar, validateFile } from '../../utils/firebaseUpload';
import dayjs from 'dayjs';

const NewCustomer = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [customerTypes, setCustomerTypes] = useState([]);
  const [fileUploading, setFileUploading] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    customerAddress: '',
    dateOfBirth: '',
    customerImg: '',
    isEnable: true,
    customerTypeId: '',
    customerGender: '',
    username: '',
    password: '',
    confirmPassword: ''
  });

  // Image preview state
  const [previewUrl, setPreviewUrl] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);

  // Snackbar state
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

  // Form validation
  const [errors, setErrors] = useState({});

  // Validation checking states
  const [validationChecking, setValidationChecking] = useState({
    email: false,
    username: false,
    phone: false
  });

  // Debounce timers
  const [debounceTimers, setDebounceTimers] = useState({
    email: null,
    username: null,
    phone: null
  });

  const showSnackbar = (message, severity = 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') return;
    setSnackbar({ ...snackbar, open: false });
  };

  // Real-time validation functions
  const checkEmailExists = async (email) => {
    if (!email || !email.includes('@')) return;
    
    try {
      setValidationChecking(prev => ({ ...prev, email: true }));
      const response = await axiosInstance.get(`/admin/check-email-exists?email=${encodeURIComponent(email)}`);
      
      if (response.data.exists) {
        setErrors(prev => ({
          ...prev,
          customerEmail: response.data.message || 'Email đã được sử dụng'
        }));
      } else {
        setErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors.customerEmail;
          return newErrors;
        });
      }
    } catch (error) {
      console.error('Error checking email:', error);
    } finally {
      setValidationChecking(prev => ({ ...prev, email: false }));
    }
  };

  const checkUsernameExists = async (username) => {
    if (!username || username.length < 3) return;
    
    try {
      setValidationChecking(prev => ({ ...prev, username: true }));
      const response = await axiosInstance.get(`/admin/check-username-exists?username=${encodeURIComponent(username)}`);
      
      if (response.data.exists) {
        setErrors(prev => ({
          ...prev,
          username: response.data.message || 'Tên đăng nhập đã được sử dụng'
        }));
      } else {
        setErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors.username;
          return newErrors;
        });
      }
    } catch (error) {
      console.error('Error checking username:', error);
    } finally {
      setValidationChecking(prev => ({ ...prev, username: false }));
    }
  };

  const checkPhoneExists = async (phone) => {
    if (!phone || phone.length < 10) return;
    
    try {
      setValidationChecking(prev => ({ ...prev, phone: true }));
      const response = await axiosInstance.get(`/admin/check-phone-exists?phone=${encodeURIComponent(phone)}`);
      
      if (response.data.exists) {
        // Admin pages hiển thị thông tin customer để quản lý
        const errorMessage = response.data.customerName 
          ? `Số điện thoại đã được sử dụng bởi khách hàng: ${response.data.customerName}`
          : response.data.message || 'Số điện thoại đã được sử dụng';
        
        setErrors(prev => ({
          ...prev,
          customerPhone: errorMessage
        }));
      } else {
        setErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors.customerPhone;
          return newErrors;
        });
      }
    } catch (error) {
      console.error('Error checking phone:', error);
    } finally {
      setValidationChecking(prev => ({ ...prev, phone: false }));
    }
  };

  // Debounced validation functions
  const debouncedCheckEmail = (email) => {
    // Clear existing timer
    if (debounceTimers.email) {
      clearTimeout(debounceTimers.email);
    }
    
    // Set new timer
    const timerId = setTimeout(() => {
      checkEmailExists(email);
    }, 800); // 800ms delay
    
    setDebounceTimers(prev => ({ ...prev, email: timerId }));
  };

  const debouncedCheckUsername = (username) => {
    // Clear existing timer
    if (debounceTimers.username) {
      clearTimeout(debounceTimers.username);
    }
    
    // Set new timer
    const timerId = setTimeout(() => {
      checkUsernameExists(username);
    }, 800); // 800ms delay
    
    setDebounceTimers(prev => ({ ...prev, username: timerId }));
  };

  const debouncedCheckPhone = (phone) => {
    // Clear existing timer
    if (debounceTimers.phone) {
      clearTimeout(debounceTimers.phone);
    }
    
    // Set new timer
    const timerId = setTimeout(() => {
      checkPhoneExists(phone);
    }, 800); // 800ms delay
    
    setDebounceTimers(prev => ({ ...prev, phone: timerId }));
  };

  // Fetch customer types
  const fetchCustomerTypes = async () => {
    try {
      const response = await axiosInstance.get('/admin/customer-types');
      if (response.data && response.data.customerTypes) {
        setCustomerTypes(response.data.customerTypes);
      }
    } catch (error) {
      console.error('Error fetching customer types:', error);
      showSnackbar('Failed to load customer types', 'error');
    }
  };

  useEffect(() => {
    fetchCustomerTypes();
  }, []);

  // Cleanup debounce timers on unmount
  useEffect(() => {
    return () => {
      // Clear all debounce timers when component unmounts
      Object.values(debounceTimers).forEach(timer => {
        if (timer) clearTimeout(timer);
      });
    };
  }, [debounceTimers]);

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

  const validateForm = () => {
    const newErrors = {};

    // Basic required field validation
    if (!formData.customerName.trim()) newErrors.customerName = 'Tên khách hàng là bắt buộc';
    if (!formData.customerEmail.trim()) newErrors.customerEmail = 'Email là bắt buộc';
    if (!formData.username.trim()) newErrors.username = 'Tên đăng nhập là bắt buộc';

    // Password validation
    const passwordError = validatePassword(formData.password);
    if (passwordError) newErrors.password = passwordError;

    // Confirm password validation
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp';
    }

    // Email format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (formData.customerEmail && !emailRegex.test(formData.customerEmail)) {
      newErrors.customerEmail = 'Email không hợp lệ';
    }

    // Phone validation
    const phoneRegex = /^[0-9]{10,11}$/;
    if (formData.customerPhone && !phoneRegex.test(formData.customerPhone)) {
      newErrors.customerPhone = 'Số điện thoại phải có 10-11 chữ số';
    }

    // Age validation - must be at least 16 years old
    if (formData.dateOfBirth) {
      const age = calculateAge(formData.dateOfBirth);
      if (age < 16) {
        newErrors.dateOfBirth = 'Khách hàng phải từ 16 tuổi trở lên';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing (except for duplicate check errors)
    if (errors[name] && !['Email đã được sử dụng', 'Tên đăng nhập đã được sử dụng', 'Số điện thoại đã được sử dụng'].some(msg => errors[name].includes(msg))) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }

    // Trigger real-time validation for specific fields
    switch (name) {
      case 'customerEmail':
        if (value && value.includes('@')) {
          debouncedCheckEmail(value);
        }
        break;
      case 'username':
        if (value && value.length >= 3) {
          debouncedCheckUsername(value);
        }
        break;
      case 'customerPhone':
        if (value && value.length >= 10) {
          debouncedCheckPhone(value);
        }
        break;
      default:
        break;
    }
  };

  const handleSwitchChange = (name) => (event) => {
    setFormData(prev => ({
      ...prev,
      [name]: event.target.checked
    }));
  };

  const calculateAge = (dateOfBirth) => {
    if (!dateOfBirth) return 0;
    return dayjs().diff(dayjs(dateOfBirth), 'year');
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      // Validate file
      validateFile(file, 'image', 5); // 5MB limit for images
      
      // Store the file for later upload
      setSelectedFile(file);
      
      // Create preview URL immediately
      const previewUrl = URL.createObjectURL(file);
      setPreviewUrl(previewUrl);
      
      showSnackbar('Image selected successfully! It will be uploaded when you create the customer.', 'success');
      
    } catch (error) {
      console.error('Error selecting file:', error);
      showSnackbar(`File selection failed: ${error.message}`, 'error');
    } finally {
      // Clear the input
      event.target.value = '';
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      showSnackbar('Vui lòng kiểm tra và sửa các lỗi trong form', 'error');
      return;
    }

    setLoading(true);
    try {
      let imageUrl = '';
      
      // Upload avatar to Firebase if a file is selected
      if (selectedFile) {
        setFileUploading(true);
        showSnackbar('Uploading avatar...', 'info');
        
        try {
          imageUrl = await uploadCustomerAvatar(selectedFile, formData.username);
          showSnackbar('Avatar uploaded successfully!', 'success');
        } catch (uploadError) {
          console.error('Avatar upload failed:', uploadError);
          showSnackbar(`Avatar upload failed: ${uploadError.message}`, 'warning');
          // Continue with customer creation without avatar
        } finally {
          setFileUploading(false);
        }
      }

      const customerData = {
        customerName: formData.customerName,
        customerEmail: formData.customerEmail,
        customerPhone: formData.customerPhone || null,
        customerAddress: formData.customerAddress || null,
        dateOfBirth: formData.dateOfBirth || null,
        customerImg: imageUrl || null, // Use uploaded image URL or null
        isEnable: formData.isEnable,
        customerTypeId: formData.customerTypeId || 1,
        customerGender: formData.customerGender || 'Male',
        username: formData.username,
        password: formData.password
      };

      console.log('Submitting customer data:', customerData);
      
      await axiosInstance.post('/admin/customers', customerData);
      
      showSnackbar('Tạo khách hàng thành công! 🎉', 'success');
      
      // Reset form and preview
      setFormData({
        customerName: '',
        customerEmail: '',
        customerPhone: '',
        customerAddress: '',
        dateOfBirth: '',
        customerImg: '',
        isEnable: true,
        customerTypeId: '',
        customerGender: '',
        username: '',
        password: '',
        confirmPassword: ''
      });
      setErrors({});
      setPreviewUrl('');
      setSelectedFile(null);
      
      // Navigate back after delay
      setTimeout(() => {
        navigate('/admin/customers');
      }, 2000);
      
    } catch (error) {
      console.error('Error creating customer:', error);
      const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi tạo khách hàng';
      showSnackbar(errorMessage, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/customers');
  };

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <IconButton onClick={handleCancel} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" fontWeight="bold">
          Create New Customer
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Form Section */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                {/* Customer Name */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Customer Name"
                    name="customerName"
                    value={formData.customerName}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    error={!!errors.customerName}
                    helperText={errors.customerName}
                  />
                </Grid>

                {/* Email */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Email"
                    name="customerEmail"
                    type="email"
                    value={formData.customerEmail}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    error={!!errors.customerEmail}
                    helperText={errors.customerEmail || (validationChecking.email ? 'Đang kiểm tra email...' : '')}
                    InputProps={{
                      endAdornment: validationChecking.email ? <CircularProgress size={20} /> : null
                    }}
                  />
                </Grid>

                {/* Phone */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Phone Number"
                    name="customerPhone"
                    value={formData.customerPhone}
                    onChange={handleInputChange}
                    variant="outlined"
                    placeholder="10-11 digits"
                    error={!!errors.customerPhone}
                    helperText={errors.customerPhone || (validationChecking.phone ? 'Đang kiểm tra số điện thoại...' : '')}
                    InputProps={{
                      endAdornment: validationChecking.phone ? <CircularProgress size={20} /> : null
                    }}
                  />
                </Grid>

                {/* Date of Birth */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Date of Birth"
                    name="dateOfBirth"
                    type="date"
                    value={formData.dateOfBirth}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                    error={!!errors.dateOfBirth}
                    helperText={errors.dateOfBirth || 'Khách hàng phải từ 16 tuổi trở lên'}
                    inputProps={{
                      max: dayjs().subtract(16, 'year').format('YYYY-MM-DD') // Set max date to 16 years ago
                    }}
                  />
                </Grid>

                {/* Customer Type */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Customer Type</InputLabel>
                    <Select
                      name="customerTypeId"
                      value={formData.customerTypeId}
                      onChange={handleInputChange}
                      label="Customer Type"
                    >
                      <MenuItem value="">
                        <em>No Type</em>
                      </MenuItem>
                      {customerTypes.map((type) => (
                        <MenuItem key={type.customerTypeId} value={type.customerTypeId}>
                          {type.customerTypeName}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                {/* Address */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address"
                    name="customerAddress"
                    value={formData.customerAddress}
                    onChange={handleInputChange}
                    multiline
                    rows={2}
                    variant="outlined"
                  />
                </Grid>

                {/* Profile Image URL */}
                <Grid item xs={12}>
                  <Box>
                    <TextField
                      fullWidth
                      label="Profile Image URL"
                      name="customerImg"
                      value={formData.customerImg}
                      onChange={handleInputChange}
                      variant="outlined"
                      disabled={fileUploading}
                    />
                    <Box display="flex" gap={2} mt={1}>
                      <Button
                        variant="outlined"
                        component="label"
                        startIcon={fileUploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        disabled={fileUploading || loading}
                        sx={{ minWidth: 140 }}
                      >
                        {fileUploading ? 'Uploading...' : 'Select Avatar'}
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={handleFileUpload}
                        />
                      </Button>
                      {(previewUrl || formData.customerImg) && (
                        <Avatar 
                          src={previewUrl || formData.customerImg} 
                          sx={{ width: 40, height: 40 }}
                        />
                      )}
                    </Box>
                  </Box>
                </Grid>

                {/* Active Status */}
                <Grid item xs={12}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={formData.isEnable}
                        onChange={handleSwitchChange('isEnable')}
                        color="primary"
                      />
                    }
                    label="Active Status"
                  />
                </Grid>

                {/* Gender */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Gender</InputLabel>
                    <Select
                      name="customerGender"
                      value={formData.customerGender}
                      onChange={handleInputChange}
                      label="Gender"
                    >
                      <MenuItem value="Male">Male</MenuItem>
                      <MenuItem value="Female">Female</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                {/* Username */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Username"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="Enter username for login"
                    error={!!errors.username}
                    helperText={errors.username || (validationChecking.username ? 'Đang kiểm tra tên đăng nhập...' : '')}
                    InputProps={{
                      endAdornment: validationChecking.username ? <CircularProgress size={20} /> : null
                    }}
                  />
                </Grid>

                {/* Password */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Password"
                    name="password"
                    type="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="Enter password (min 8 characters)"
                    error={!!errors.password}
                    helperText={errors.password || "Min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special character"}
                  />
                </Grid>

                {/* Confirm Password */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Confirm Password"
                    name="confirmPassword"
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="Confirm password"
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword}
                  />
                </Grid>

                {/* Action Buttons */}
                <Grid item xs={12}>
                  <Box display="flex" gap={2} justifyContent="flex-end">
                    <Button
                      variant="outlined"
                      startIcon={<CancelIcon />}
                      onClick={handleCancel}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                    <Button
                      type="submit"
                      variant="contained"
                      startIcon={loading ? <CircularProgress size={20} /> : <SaveIcon />}
                      disabled={loading}
                      sx={{ 
                        bgcolor: '#d35255',
                        '&:hover': { bgcolor: '#b8464a' }
                      }}
                    >
                      {loading ? 'Creating...' : 'Create Customer'}
                    </Button>
                  </Box>
                </Grid>
              </Grid>
            </form>
          </Paper>
        </Grid>

        {/* Preview Section */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar sx={{ bgcolor: '#d35255', mr: 2 }}>
                  <PersonIcon />
                </Avatar>
                <Typography variant="h6">Customer Preview</Typography>
              </Box>
              
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar 
                  src={previewUrl || formData.customerImg} 
                  sx={{ width: 60, height: 60, mr: 2 }}
                >
                  {formData.customerName?.charAt(0) || 'C'}
                </Avatar>
                <Box>
                  <Typography variant="subtitle1" fontWeight="bold">
                    {formData.customerName || 'Customer Name'}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {formData.customerEmail || 'email@example.com'}
                  </Typography>
                </Box>
              </Box>

              <Box>
                <Typography variant="body2">
                  <strong>Phone:</strong> {formData.customerPhone || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Age:</strong> {calculateAge(formData.dateOfBirth)} years old
                </Typography>
                <Typography variant="body2">
                  <strong>Gender:</strong> {formData.customerGender || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Address:</strong> {formData.customerAddress || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Username:</strong> {formData.username || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Type:</strong> {
                    customerTypes.find(type => type.customerTypeId.toString() === formData.customerTypeId)?.customerTypeName || 'No Type'
                  }
                </Typography>
                <Typography variant="body2">
                  <strong>Status:</strong> {formData.isEnable ? 'Active' : 'Inactive'}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Snackbar */}
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
    </Box>
  );
};

export default NewCustomer; 