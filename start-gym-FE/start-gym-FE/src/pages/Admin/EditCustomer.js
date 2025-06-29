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
  IconButton,
  InputAdornment
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Person as PersonIcon,
  CloudUpload as CloudUploadIcon
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';
import dayjs from 'dayjs';

const EditCustomer = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
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

    customerCode: '',
    username: '',
    totalVipSpent: 0
  });

  // Snackbar state
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

  // Validation errors and checking states
  const [errors, setErrors] = useState({});
  const [validationChecking, setValidationChecking] = useState({
    email: false,
    phone: false
  });

  // Debounce timers
  const [debounceTimers, setDebounceTimers] = useState({
    email: null,
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
      const response = await request.get(`/admin/check-email-exists?email=${encodeURIComponent(email)}&excludeCustomerId=${id}`);
      
      if (response.exists) {
        setErrors(prev => ({
          ...prev,
          customerEmail: response.message || 'Email đã được sử dụng'
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

  const checkPhoneExists = async (phone) => {
    console.log('=== CHECKING PHONE EXISTS ===');
    console.log('Phone to check:', phone);
    console.log('Exclude customer ID:', id);
    
    if (!phone) {
      console.log('No phone provided, skipping check');
      return;
    }
    
    setValidationChecking(prev => ({ ...prev, phone: true }));
    
    try {
      const response = await request.get(`/admin/check-phone-exists?phone=${encodeURIComponent(phone)}&excludeCustomerId=${id}`);
      
      console.log('Phone validation response:', response);
      
      if (response.exists) {
        // Admin pages hiển thị thông tin customer để quản lý
        const errorMessage = response.customerName 
          ? `Số điện thoại đã được sử dụng bởi khách hàng: ${response.customerName}`
          : 'Số điện thoại đã được sử dụng';
        
        setErrors(prev => ({
          ...prev,
          customerPhone: errorMessage
        }));
      } else {
        setErrors(prev => ({
          ...prev,
          customerPhone: ''
        }));
      }
    } catch (error) {
      console.error('Error checking phone:', error);
      setErrors(prev => ({
        ...prev,
        customerPhone: 'Lỗi kiểm tra số điện thoại'
      }));
    } finally {
      setValidationChecking(prev => ({ ...prev, phone: false }));
    }
  };

  // Debounced validation functions
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
    console.log('debouncedCheckPhone called with:', phone);
    if (debounceTimers.phone) {
      clearTimeout(debounceTimers.phone);
    }
    
    const timerId = setTimeout(() => {
      console.log('Debounce timeout executed for phone:', phone);
      checkPhoneExists(phone);
    }, 800);
    
    setDebounceTimers(prev => ({ ...prev, phone: timerId }));
  };

  // Cleanup debounce timers on unmount
  useEffect(() => {
    return () => {
      Object.values(debounceTimers).forEach(timer => {
        if (timer) clearTimeout(timer);
      });
    };
  }, [debounceTimers]);

  // Fetch customer types
  const fetchCustomerTypes = async () => {
    try {
      const response = await request.get('/admin/customer-types');
      if (response && response.customerTypes) {
        setCustomerTypes(response.customerTypes);
      }
    } catch (error) {
      console.error('Error fetching customer types:', error);
    }
  };
  
  useEffect(() => {
    const fetchCustomerData = async () => {
      if (!user) return;
      
      setFetchLoading(true);
      try {
        // Try the direct admin endpoint first, then fallback to regular endpoint
        let response;
        try {
          response = await request.get(`/admin/customer/${id}`);
        } catch (adminError) {
          console.log('Admin endpoint failed, trying fallback:', adminError);
          response = await request.get(`/customer/list/${id}`);
        }
        
        console.log('Customer data:', response);
        
        setFormData({
          customerName: response.customerName || '',
          customerEmail: response.customerEmail || '',
          customerPhone: response.customerPhone || '',
          customerAddress: response.customerAddress || '',
          dateOfBirth: response.dateOfBirth || response.customerDob || '',
          customerImg: response.customerImg || '',
          isEnable: response.isEnable !== undefined ? response.isEnable : true,
          customerTypeId: response.customerType?.customerTypeId || response.customerTypeId || '',
          customerGender: response.customerGender || '',

          customerCode: response.customerCode || '',
          username: response.username || '',
          totalVipSpent: response.totalVipSpent || 0
        });
      } catch (error) {
        console.error('Error fetching customer information:', error);
        showSnackbar('Failed to load customer data', 'error');
      } finally {
        setFetchLoading(false);
      }
    };

      fetchCustomerData();
    fetchCustomerTypes();
  }, [id, user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    const prevValue = formData[name];
    
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing (except for duplicate check errors)
    if (errors[name] && !['Email đã được sử dụng', 'Số điện thoại đã được sử dụng'].some(msg => errors[name].includes(msg))) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }

    // Trigger real-time validation for specific fields
    switch (name) {
      case 'customerEmail':
        if (value && value.includes('@') && value !== prevValue) {
          console.log('Triggering email validation for:', value, 'prev:', prevValue);
          debouncedCheckEmail(value);
        }
        break;
      case 'customerPhone':
        console.log('Phone input changed:', {
          name,
          value,
          prevValue,
          length: value.length,
          shouldTrigger: value && value.length >= 10 && value !== prevValue
        });
        if (value && value.length >= 10 && value !== prevValue) {
          console.log('Triggering phone validation for:', value);
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
      // Validate file size and type
      if (file.size > 5 * 1024 * 1024) {
        throw new Error('File size must be less than 5MB');
      }
      
      if (!file.type.startsWith('image/')) {
        throw new Error('Please select an image file');
      }
      
      setFileUploading(true);
      showSnackbar('Uploading avatar...', 'info');
      
      // Create FormData for file upload
      const uploadFormData = new FormData();
      uploadFormData.append('file', file);
      
      // Use username or fallback to customer email
      const uploadUsername = formData.username || formData.customerEmail.split('@')[0] || `customer_${id}`;
      uploadFormData.append('username', uploadUsername);
      
      // Upload via backend endpoint
      const response = await request.post('/admin/customers/upload-avatar', uploadFormData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      // Update form data with new URL
      setFormData(prev => ({
        ...prev,
        customerImg: response.avatarUrl
      }));
      
      showSnackbar('Avatar uploaded successfully! ✅', 'success');
      
    } catch (error) {
      console.error('Error uploading avatar:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Upload failed';
      showSnackbar(`Upload failed: ${errorMsg}`, 'error');
    } finally {
      setFileUploading(false);
      // Clear the input
      event.target.value = '';
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Check for validation errors
    const hasErrors = Object.keys(errors).some(key => errors[key]);
    if (hasErrors) {
      showSnackbar('Vui lòng sửa các lỗi trước khi cập nhật', 'error');
      return;
    }
    
    // Validation
    if (!formData.customerName.trim()) {
      showSnackbar('Customer name is required', 'error');
      return;
    }
    if (!formData.customerEmail.trim()) {
      showSnackbar('Email is required', 'error');
      return;
    }
    if (!formData.customerPhone.trim()) {
      showSnackbar('Phone number is required', 'error');
      return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.customerEmail)) {
      showSnackbar('Please enter a valid email address', 'error');
      return;
    }

    // Phone validation
    const phoneRegex = /^[0-9]{10,11}$/;
    if (!phoneRegex.test(formData.customerPhone.replace(/\s/g, ''))) {
      showSnackbar('Please enter a valid phone number (10-11 digits)', 'error');
      return;
    }

    // Age validation - must be at least 16 years old
    if (formData.dateOfBirth) {
      const age = calculateAge(formData.dateOfBirth);
      if (age < 16) {
        showSnackbar('Customer must be at least 16 years old', 'error');
        return;
      }
    }

    if (!user) {
      showSnackbar('Please login to edit customer', 'error');
      return;
    }

    setLoading(true);
    try {
      const customerData = {
        ...formData,
        customerAge: calculateAge(formData.dateOfBirth),
        customerTypeId: formData.customerTypeId ? parseInt(formData.customerTypeId) : null
      };

      console.log('=== FRONTEND UPDATE DATA ===');
      console.log('Form data:', formData);
      console.log('Customer data to send:', customerData);
      console.log('CustomerTypeId value:', formData.customerTypeId, 'Type:', typeof formData.customerTypeId);
      
      // Try the admin endpoint first, then fallback to regular endpoint
      let response;
      try {
        response = await request.put(`/admin/customer/${id}/edit`, customerData);
      } catch (adminError) {
        console.log('Admin endpoint failed, trying fallback:', adminError);
        response = await request.put(`/customer/edit/${id}`, customerData);
      }
      
      console.log('Customer updated:', response);
      
      showSnackbar('Customer updated successfully! 🎉', 'success');
      
      // Redirect to customers list after success
      setTimeout(() => {
        navigate('/admin/customers');
      }, 2000);

    } catch (error) {
      console.error('Error updating customer:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      showSnackbar(`Error updating customer: ${errorMsg}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/customers');
  };

  if (fetchLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh" flexDirection="column">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>Loading customer data...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <IconButton onClick={handleCancel} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" fontWeight="bold">
          Edit Customer
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Form Section */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                {/* Customer Code (Read-only) */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Customer Code"
                    name="customerCode"
                    value={formData.customerCode}
                    variant="outlined"
                    disabled
                  />
                </Grid>

                {/* Customer Name */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Customer Name"
                    name="customerName"
                    value={formData.customerName}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                  />
                </Grid>

                {/* Email and Phone */}
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

                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Phone Number"
                    name="customerPhone"
                    value={formData.customerPhone}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="0123456789"
                    error={!!errors.customerPhone}
                    helperText={errors.customerPhone || (validationChecking.phone ? 'Đang kiểm tra số điện thoại...' : '')}
                    InputProps={{
                      endAdornment: validationChecking.phone ? <CircularProgress size={20} /> : null
                    }}
                  />
                </Grid>

                {/* Date of Birth and Gender */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Date of Birth"
                    name="dateOfBirth"
                    type="date"
                    value={formData.dateOfBirth}
                    onChange={handleInputChange}
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                    helperText="Customer must be at least 16 years old"
                    inputProps={{
                      max: dayjs().subtract(16, 'year').format('YYYY-MM-DD') // Set max date to 16 years ago
                    }}
                  />
                </Grid>

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

                {/* Total VIP Spent */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Total VIP Spent"
                    name="totalVipSpent"
                    type="number"
                    value={formData.totalVipSpent || 0}
                    onChange={handleInputChange}
                    variant="outlined"
                    InputProps={{
                      startAdornment: <InputAdornment position="start">₫</InputAdornment>,
                      inputProps: { min: 0, step: 1000 }
                    }}
                    helperText="Total amount spent on VIP courses"
                  />
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
                        {fileUploading ? 'Uploading...' : 'Upload Avatar'}
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={handleFileUpload}
                        />
                      </Button>
                      {formData.customerImg && (
                        <Avatar 
                          src={formData.customerImg} 
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

                {/* Action Buttons */}
                <Grid item xs={12}>
                  <Box display="flex" gap={2} justifyContent="flex-end">
                    {/* Debug button for testing phone validation */}
                    <Button
                      variant="outlined"
                      onClick={() => {
                        console.log('=== TESTING PHONE VALIDATION ===');
                        console.log('Current phone:', formData.customerPhone);
                        checkPhoneExists(formData.customerPhone);
                      }}
                      sx={{ mr: 'auto' }}
                    >
                      Test Phone
                    </Button>
                    
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
                      {loading ? 'Updating...' : 'Update Customer'}
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
                  src={formData.customerImg} 
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
                  <strong>Code:</strong> {formData.customerCode || 'N/A'}
                </Typography>
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
                  <strong>Type:</strong> {
                    (() => {
                      // Convert both values to string for proper comparison
                      const selectedTypeId = String(formData.customerTypeId);
                      const selectedType = customerTypes.find(type => 
                        String(type.customerTypeId) === selectedTypeId || 
                        Number(type.customerTypeId) === Number(formData.customerTypeId)
                      );
                      return selectedType ? selectedType.customerTypeName : 'No Type';
                    })()
                  }
                </Typography>
                <Typography variant="body2">
                  <strong>Total VIP Spent:</strong> {
                    new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND'
                    }).format(formData.totalVipSpent || 0)
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

export default EditCustomer;
