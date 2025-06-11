import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
  Grid,
  Card,
  CardContent,
  Avatar,
  Snackbar,
  Alert,
  CircularProgress,
  Divider,
  InputAdornment,
  Chip
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  CloudUpload as CloudUploadIcon,
  PhotoCamera as PhotoCameraIcon,
  School as SchoolIcon,
  MonetizationOn as MoneyIcon,
  Schedule as ScheduleIcon,
  FitnessCenter as FitnessCenterIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../utils/axiosInstance';
import FirebaseStorageService from '../../services/FirebaseStorageService';

const NewCourse = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [courseTypes, setCourseTypes] = useState([]);
  const [imageUploading, setImageUploading] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');

  // Form state
  const [formData, setFormData] = useState({
    courseName: '',
    description: '',
    durationWeeks: 4,
    weeklySessions: 3,
    courseTypeId: '',
    price: 0,
    isVip: false,
    status: true,
    recommendedFor: 'Normal',
    difficultyLevel: 'Medium',
    image: ''
  });

  // Snackbar state
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

  const showSnackbar = (message, severity = 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // Fetch course types
  useEffect(() => {
    const fetchCourseTypes = async () => {
      try {
        const response = await axiosInstance.get('/admin/course-types');
        setCourseTypes(response.data || []);
      } catch (error) {
        console.error('Error fetching course types:', error);
        showSnackbar('Failed to fetch course types', 'warning');
      }
    };

    fetchCourseTypes();
  }, []);

  // Calculate total days
  const getTotalDays = () => {
    return formData.durationWeeks * formData.weeklySessions;
  };

  // Handle form input changes
  const handleInputChange = (field, value) => {
    setFormData(prev => {
      const updatedData = { ...prev, [field]: value };
      
      // Auto-determine VIP status based on price
      if (field === 'price') {
        const price = parseFloat(value) || 0;
        updatedData.isVip = price > 0;
      } else if (field === 'isVip' && value === false) {
        // If disabling VIP, set price to 0
        updatedData.price = 0;
      } else if (field === 'isVip' && value === true && parseFloat(prev.price) === 0) {
        // If enabling VIP but price is 0, set a default price
        updatedData.price = 1000000; // 1,000,000 VND
      }
      
      return updatedData;
    });
  };

  // Handle image selection
  const handleImageSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      // Validate file
      if (file.size > 5 * 1024 * 1024) {
        showSnackbar('File size must be less than 5MB', 'error');
        return;
      }
      
      if (!file.type.startsWith('image/')) {
        showSnackbar('Please select an image file', 'error');
        return;
      }
      
      setSelectedImage(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  // Upload image to Firebase
  const handleImageUpload = async () => {
    if (!selectedImage) {
      showSnackbar('Please select an image first', 'error');
      return;
    }
    
    if (!formData.courseTypeId) {
      showSnackbar('Please select a course type before uploading image', 'error');
      return;
    }
    
    if (!formData.courseName.trim()) {
      showSnackbar('Please enter course name before uploading image', 'error');
      return;
    }
    
    setImageUploading(true);
    try {
      const imageUrl = await FirebaseStorageService.uploadCourseImage(
        selectedImage,
        {
          courseTypeId: formData.courseTypeId,
          courseName: formData.courseName
        }
      );
      
      setFormData(prev => ({ ...prev, image: imageUrl }));
      showSnackbar('Image uploaded successfully! 🎉', 'success');
      setSelectedImage(null);
      setPreviewUrl('');
    } catch (error) {
      console.error('Error uploading image:', error);
      showSnackbar(`Failed to upload image: ${error.message}`, 'error');
    } finally {
      setImageUploading(false);
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.courseName.trim()) {
      showSnackbar('Course name is required', 'error');
      return;
    }
    
    if (!formData.courseTypeId) {
      showSnackbar('Please select a course type', 'error');
      return;
    }
    
    if (formData.durationWeeks < 1 || formData.durationWeeks > 52) {
      showSnackbar('Duration must be between 1 and 52 weeks', 'error');
      return;
    }
    
    if (formData.weeklySessions < 1 || formData.weeklySessions > 7) {
      showSnackbar('Weekly sessions must be between 1 and 7', 'error');
      return;
    }
    
    setLoading(true);
    try {
      const courseData = {
        ...formData,
        duration: formData.durationWeeks, // Duration in weeks
        weeklySessionss: formData.weeklySessions, // Note: backend expects 'weeklySessionss'
        totalDays: getTotalDays()
      };
      
      console.log('Creating course with data:', courseData);
      
      const response = await axiosInstance.post('/admin/courses/create-with-days', courseData);
      
      console.log('Course created:', response.data);
      showSnackbar(`Course created successfully with ${getTotalDays()} days! 🎉`, 'success');
      
      // Redirect to courses list after success
      setTimeout(() => {
        navigate('/admin', { state: { activeTab: 'courses' } });
      }, 2000);
      
    } catch (error) {
      console.error('Error creating course:', error);
      const errorMsg = error.response?.data?.error || error.response?.data || error.message || 'Unknown error';
      showSnackbar(`Error creating course: ${errorMsg}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin', { state: { activeTab: 'courses' } });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount || 0);
  };

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleCancel}
          sx={{ mr: 2 }}
        >
          Back to Courses
        </Button>
        <Typography variant="h4" fontWeight="bold">
          Create New Course
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Form Section */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                {/* Course Name */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Course Name"
                    value={formData.courseName}
                    onChange={(e) => handleInputChange('courseName', e.target.value)}
                    required
                    variant="outlined"
                    placeholder="Enter course name..."
                  />
                </Grid>

                {/* Description */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Description"
                    value={formData.description}
                    onChange={(e) => handleInputChange('description', e.target.value)}
                    multiline
                    rows={3}
                    variant="outlined"
                    placeholder="Describe the course objectives and content..."
                  />
                </Grid>

                {/* Duration and Weekly Sessions */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Duration (Weeks)"
                    type="number"
                    value={formData.durationWeeks}
                    onChange={(e) => handleInputChange('durationWeeks', parseInt(e.target.value) || 0)}
                    required
                    variant="outlined"
                    inputProps={{ min: 1, max: 52 }}
                    InputProps={{
                      endAdornment: <InputAdornment position="end">weeks</InputAdornment>
                    }}
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Weekly Sessions"
                    type="number"
                    value={formData.weeklySessions}
                    onChange={(e) => handleInputChange('weeklySessions', parseInt(e.target.value) || 0)}
                    required
                    variant="outlined"
                    inputProps={{ min: 1, max: 7 }}
                    InputProps={{
                      endAdornment: <InputAdornment position="end">sessions/week</InputAdornment>
                    }}
                  />
                </Grid>

                {/* Course Type */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth required>
                    <InputLabel>Course Type</InputLabel>
                    <Select
                      value={formData.courseTypeId}
                      onChange={(e) => handleInputChange('courseTypeId', e.target.value)}
                      label="Course Type"
                    >
                      {courseTypes.map((type) => (
                        <MenuItem key={type.courseTypeId} value={type.courseTypeId}>
                          {type.courseTypeName}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                {/* Price */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Price"
                    type="number"
                    value={formData.price}
                    onChange={(e) => handleInputChange('price', parseFloat(e.target.value) || 0)}
                    variant="outlined"
                    inputProps={{ min: 0, step: 1000 }}
                    InputProps={{
                      startAdornment: <InputAdornment position="start">₫</InputAdornment>
                    }}
                    helperText="Set to 0 for free course, >0 for VIP course"
                  />
                </Grid>

                {/* Recommended For and Difficulty */}
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Recommended For</InputLabel>
                    <Select
                      value={formData.recommendedFor}
                      onChange={(e) => handleInputChange('recommendedFor', e.target.value)}
                      label="Recommended For"
                    >
                      <MenuItem value="Thin">Thin</MenuItem>
                      <MenuItem value="Skinny Fat">Skinny Fat</MenuItem>
                      <MenuItem value="Overweight">Overweight</MenuItem>
                      <MenuItem value="Normal">Normal</MenuItem>
                      <MenuItem value="Fit">Fit</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Difficulty Level</InputLabel>
                    <Select
                      value={formData.difficultyLevel}
                      onChange={(e) => handleInputChange('difficultyLevel', e.target.value)}
                      label="Difficulty Level"
                    >
                      <MenuItem value="Easy">Easy</MenuItem>
                      <MenuItem value="Medium">Medium</MenuItem>
                      <MenuItem value="Hard">Hard</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                {/* Image Upload */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Course Image</Typography>
                  <Box display="flex" gap={2} alignItems="center" mb={2}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<PhotoCameraIcon />}
                      disabled={imageUploading}
                    >
                      Select Image
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={handleImageSelect}
                      />
                    </Button>
                    
                    {selectedImage && (
                      <Button
                        variant="contained"
                        startIcon={imageUploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        onClick={handleImageUpload}
                        disabled={imageUploading || !formData.courseTypeId || !formData.courseName.trim()}
                        color="primary"
                      >
                        {imageUploading ? 'Uploading...' : 'Upload to Firebase'}
                      </Button>
                    )}
                  </Box>
                  
                  {(previewUrl || formData.image) && (
                    <Avatar
                      src={previewUrl || formData.image}
                      variant="rounded"
                      sx={{ width: 120, height: 120, border: '2px solid #e0e0e0' }}
                    >
                      <SchoolIcon />
                    </Avatar>
                  )}
                  
                  <Typography variant="caption" color="textSecondary" display="block" mt={1}>
                    Course type and name are required before uploading
                  </Typography>
                </Grid>

                {/* Status Switches */}
                <Grid item xs={12}>
                  <Box display="flex" gap={3}>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.status}
                          onChange={(e) => handleInputChange('status', e.target.checked)}
                          color="success"
                        />
                      }
                      label="Active Course"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.isVip}
                          onChange={(e) => handleInputChange('isVip', e.target.checked)}
                          color="warning"
                        />
                      }
                      label="VIP Course"
                    />
                  </Box>
                </Grid>

                {/* Action Buttons */}
                <Grid item xs={12}>
                  <Divider sx={{ my: 2 }} />
                  <Box display="flex" gap={2} justifyContent="flex-end">
                    <Button
                      variant="outlined"
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
                      {loading ? 'Creating...' : 'Create Course'}
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
              <Typography variant="h6" gutterBottom>
                Course Preview
              </Typography>
              
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar 
                  src={previewUrl || formData.image} 
                  variant="rounded"
                  sx={{ width: 60, height: 60, mr: 2 }}
                >
                  <SchoolIcon />
                </Avatar>
                <Box>
                  <Typography variant="subtitle1" fontWeight="bold">
                    {formData.courseName || 'Course Name'}
                  </Typography>
                  <Box display="flex" gap={1} mt={0.5}>
                    <Chip 
                      label={formData.isVip ? 'VIP' : 'Free'} 
                      size="small" 
                      color={formData.isVip ? 'warning' : 'default'}
                      icon={formData.isVip ? <MoneyIcon /> : undefined}
                    />
                    <Chip 
                      label={formData.status ? 'Active' : 'Hidden'} 
                      size="small" 
                      color={formData.status ? 'success' : 'default'}
                    />
                  </Box>
                </Box>
              </Box>

              <Box>
                <Typography variant="body2" gutterBottom>
                  <strong>Type:</strong> {
                    courseTypes.find(t => t.courseTypeId === formData.courseTypeId)?.courseTypeName || 'Not selected'
                  }
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Duration:</strong> {formData.durationWeeks} weeks
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Sessions/Week:</strong> {formData.weeklySessions}
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Total Days:</strong> {getTotalDays()} days
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Difficulty:</strong> {formData.difficultyLevel}
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Recommended For:</strong> {formData.recommendedFor}
                </Typography>
                <Typography variant="body2" gutterBottom>
                  <strong>Price:</strong> {formData.isVip ? formatCurrency(formData.price) : 'Free'}
                </Typography>
              </Box>

              <Box mt={2} p={2} bgcolor="grey.100" borderRadius={1}>
                <Box display="flex" alignItems="center" gap={1} mb={1}>
                  <ScheduleIcon color="action" fontSize="small" />
                  <Typography variant="caption" fontWeight="bold">
                    Auto-Generated Schedule:
                  </Typography>
                </Box>
                <Typography variant="caption" color="textSecondary">
                  {getTotalDays()} training days will be automatically created as "Day 1", "Day 2", etc.
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

export default NewCourse; 