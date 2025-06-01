import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box,
    Typography,
    Button,
    Paper,
    CircularProgress,
    Divider,
    Grid,
    Chip,
    TextField,
    Switch,
    FormControlLabel,
    MenuItem,
    Snackbar,
    Alert,
    InputAdornment,
    Avatar,
    IconButton
} from '@mui/material';
import {
    ArrowBack as ArrowBackIcon,
    Edit as EditIcon,
    Save as SaveIcon,
    Cancel as CancelIcon,
    Star as StarIcon,
    AttachMoney as MoneyIcon,
    PhotoCamera as PhotoCameraIcon,
    CloudUpload as CloudUploadIcon
} from '@mui/icons-material';
import { axiosInstance, getImageUrl } from '../../utils/axiosInstance';
import FirebaseStorageService from '../../services/FirebaseStorageService';

const ViewCourse = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editing, setEditing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [courseTypes, setCourseTypes] = useState([]);
    const fileInputRef = useRef(null);
    
    // Image upload state
    const [selectedImage, setSelectedImage] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [uploadingImage, setUploadingImage] = useState(false);
    
    // Form data for editing
    const [formData, setFormData] = useState({
        courseName: '',
        description: '',
        duration: '',
        status: true,
        price: 0,
        isVip: false,
        averageRating: 0,
        recommendedFor: '',
        difficultyLevel: 'Easy',
        weeklySessions: '',
        courseTypeId: '',
        image: ''
    });
    
    // Difficulty level options (matching DB enum values)
    const difficultyLevels = ['Easy', 'Medium', 'Hard'];
    
    // Recommended for options (matching DB enum values)
    const recommendedForOptions = ['Thin', 'Skinny Fat', 'Overweight', 'Normal', 'Fit'];
    
    // Snackbar state
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

    const handleSnackbarClose = () => {
        setSnackbar({ ...snackbar, open: false });
    };

    // Fetch course data and course types
    useEffect(() => {
        const fetchCourse = async () => {
            try {
                setLoading(true);
                console.log(`Fetching course ID: ${courseId}`);
                const response = await axiosInstance.get(`/admin/courses/${courseId}`);
                console.log("API response:", response.data);
                setCourse(response.data);
                
                // Initialize form data
                setFormData({
                    courseName: response.data.courseName || '',
                    description: response.data.description || '',
                    duration: response.data.duration || '',
                    status: response.data.status === undefined ? true : response.data.status,
                    price: response.data.price || 0,
                    isVip: response.data.isVip || false,
                    averageRating: response.data.averageRating || 0,
                    recommendedFor: response.data.recommendedFor || '',
                    difficultyLevel: response.data.difficultyLevel || 'Easy',
                    weeklySessions: response.data.weeklySessions || '',
                    courseTypeId: response.data.courseType?.courseTypeId || '',
                    image: response.data.image || ''
                });
                
                setError(null);
            } catch (err) {
                console.error("Error fetching course:", err);
                setError(err.message || "Failed to load course");
            } finally {
                setLoading(false);
            }
        };

        const fetchCourseTypes = async () => {
            try {
                const response = await axiosInstance.get('/admin/course-types');
                console.log('Course types received:', response.data);
                
                if (Array.isArray(response.data)) {
                    setCourseTypes(response.data);
                } else if (response.data && Array.isArray(response.data.courseTypes)) {
                    setCourseTypes(response.data.courseTypes);
                } else {
                    setCourseTypes([
                        { courseTypeId: 1, courseTypeName: 'Strength Training' },
                        { courseTypeId: 2, courseTypeName: 'Cardiovascular' },
                        { courseTypeId: 3, courseTypeName: 'Flexibility' }
                    ]);
                }
            } catch (err) {
                console.error("Error fetching course types:", err);
                // Default course types
                setCourseTypes([
                    { courseTypeId: 1, courseTypeName: 'Strength Training' },
                    { courseTypeId: 2, courseTypeName: 'Cardiovascular' },
                    { courseTypeId: 3, courseTypeName: 'Flexibility' }
                ]);
            }
        };

        fetchCourse();
        fetchCourseTypes();
    }, [courseId]);

    // Handle form input changes
    const handleInputChange = (field, value) => {
        setFormData(prev => {
            const updatedData = {
                ...prev,
                [field]: value
            };
            
            // Logic for VIP courses and price correlation
            if (field === 'price' && parseFloat(value) > 0) {
                // If price is > 0, automatically make it a VIP course
                updatedData.isVip = true;
            } else if (field === 'price' && parseFloat(value) === 0) {
                // If price is set to 0, disable VIP status
                updatedData.isVip = false;
            } else if (field === 'isVip' && value === true && parseFloat(prev.price) === 0) {
                // If setting to VIP but price is 0, set a default price
                updatedData.price = 1000000; // Default price for VIP courses
            } else if (field === 'isVip' && value === false) {
                // If disabling VIP, set price to 0
                updatedData.price = 0;
            }
            
            return updatedData;
        });
    };

    // Handle image selection
    const handleImageSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            // Validate file size (5MB max)
            if (file.size > 5 * 1024 * 1024) {
                showSnackbar('File size must be less than 5MB', 'error');
                return;
            }
            
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showSnackbar('Please select an image file', 'error');
                return;
            }
            
            setSelectedImage(file);
            setPreviewUrl(URL.createObjectURL(file));
            
            // Auto-update form data with the temporary preview URL
            setFormData(prev => ({
                ...prev,
                // We're not updating the actual image field yet - that happens after upload
                // This just shows the preview
            }));
        }
    };

    // Upload image to Firebase
    const handleImageUpload = async () => {
        if (!selectedImage) {
            showSnackbar('Please select an image first', 'error');
            return;
        }
        
        setUploadingImage(true);
        try {
            // Use the service to upload to Firebase
            const imageUrl = await FirebaseStorageService.uploadCourseImage(
                selectedImage, 
                { 
                    courseTypeId: formData.courseTypeId,
                    courseName: formData.courseName 
                }
            );
            
            // Update form data with the new image URL
            setFormData(prev => ({
                ...prev,
                image: imageUrl
            }));
            
            showSnackbar('Image uploaded successfully! 🎉', 'success');
            setSelectedImage(null);
        } catch (error) {
            console.error('Error uploading image:', error);
            showSnackbar(`Failed to upload image: ${error.message}`, 'error');
        } finally {
            setUploadingImage(false);
        }
    };

    // Handle save changes
    const handleSave = async () => {
        try {
            // Validate that VIP courses have price > 0
            if (formData.isVip && parseFloat(formData.price) <= 0) {
                showSnackbar('VIP courses must have a price greater than 0', 'error');
                return;
            }
            
            setSaving(true);
            console.log('Saving course with data:', formData);
            
            // Ensure numeric fields are properly formatted
            const updateData = {
                ...formData,
                courseId: parseInt(courseId),
                price: parseFloat(formData.price) || 0,
                averageRating: parseFloat(formData.averageRating) || 0,
                courseTypeId: formData.courseTypeId ? parseInt(formData.courseTypeId) : null
            };
            
            const response = await axiosInstance.put(`/admin/courses/${courseId}`, updateData);
            console.log('Update response:', response.data);
            
            // Update local course data
            setCourse({
                ...course,
                ...formData,
                courseType: courseTypes.find(type => type.courseTypeId == formData.courseTypeId) || course.courseType
            });
            
            showSnackbar('Course updated successfully! ✅', 'success');
            setEditing(false);
        } catch (error) {
            console.error('Error updating course:', error);
            showSnackbar(`Failed to update course: ${error.message}`, 'error');
        } finally {
            setSaving(false);
        }
    };

    // Cancel editing
    const handleCancel = () => {
        setEditing(false);
        setSelectedImage(null);
        setPreviewUrl('');
        
        // Reset form data to original course data
        if (course) {
            setFormData({
                courseName: course.courseName || '',
                description: course.description || '',
                duration: course.duration || '',
                status: course.status || false,
                price: course.price || 0,
                isVip: course.isVip || false,
                averageRating: course.averageRating || 0,
                recommendedFor: course.recommendedFor || '',
                difficultyLevel: course.difficultyLevel || '',
                weeklySessions: course.weeklySessions || '',
                courseTypeId: course.courseType?.courseTypeId || '',
                image: course.image || ''
            });
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" height="100vh">
                <CircularProgress />
                <Typography ml={2}>Loading course data...</Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box p={3}>
                <Typography color="error" variant="h6">Error: {error}</Typography>
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/admin')}>
                    Back to Courses
                </Button>
            </Box>
        );
    }

    return (
        <Box p={3}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Button 
                    startIcon={<ArrowBackIcon />} 
                    onClick={() => navigate('/admin')}
                    variant="outlined"
                >
                    Back to Courses
                </Button>
                
                {!editing ? (
                    <Button
                        startIcon={<EditIcon />}
                        variant="contained"
                        onClick={() => setEditing(true)}
                        color="primary"
                    >
                        Edit Course Details
                    </Button>
                ) : (
                    <Box display="flex" gap={1}>
                        <Button
                            startIcon={<SaveIcon />}
                            variant="contained"
                            onClick={handleSave}
                            disabled={saving}
                            color="success"
                        >
                            {saving ? 'Saving...' : 'Save Changes'}
                        </Button>
                        <Button
                            startIcon={<CancelIcon />}
                            variant="outlined"
                            onClick={handleCancel}
                            disabled={saving}
                            color="error"
                        >
                            Cancel
                        </Button>
                    </Box>
                )}
            </Box>
            
            {course && (
                <Paper sx={{ p: 3 }}>
                    <Grid container spacing={3}>
                        {/* Course Image Section */}
                        <Grid item xs={12} display="flex" justifyContent="center" mb={2}>
                            <Box sx={{ position: 'relative' }}>
                                <Avatar
                                    src={previewUrl || getImageUrl(formData.image)}
                                    alt={course.courseName}
                                    variant="rounded"
                                    sx={{
                                        width: 200,
                                        height: 200,
                                        boxShadow: 3,
                                        border: '4px solid #f8f8f8'
                                    }}
                                >
                                    {course.courseName?.charAt(0) || 'C'}
                                </Avatar>
                                
                                {editing && (
                                    <IconButton
                                        component="label"
                                        sx={{
                                            position: 'absolute',
                                            bottom: 5,
                                            right: 5,
                                            backgroundColor: 'rgba(255,255,255,0.8)',
                                            '&:hover': { backgroundColor: 'rgba(255,255,255,0.9)' }
                                        }}
                                    >
                                        <PhotoCameraIcon />
                                        <input
                                            type="file"
                                            hidden
                                            accept="image/*"
                                            onChange={handleImageSelect}
                                            ref={fileInputRef}
                                        />
                                    </IconButton>
                                )}
                            </Box>
                        </Grid>
                        
                        {/* Image Upload Button */}
                        {editing && selectedImage && (
                            <Grid item xs={12} display="flex" justifyContent="center" mb={2}>
                                <Button
                                    variant="contained"
                                    startIcon={uploadingImage ? <CircularProgress size={20} color="inherit" /> : <CloudUploadIcon />}
                                    onClick={handleImageUpload}
                                    disabled={uploadingImage || !selectedImage}
                                    color="primary"
                                >
                                    {uploadingImage ? 'Uploading...' : 'Upload Image'}
                                </Button>
                            </Grid>
                        )}
                        
                        <Grid item xs={12}>
                            <Box mb={2}>
                                {editing ? (
                                    <TextField
                                        fullWidth
                                        label="Course Name"
                                        value={formData.courseName}
                                        onChange={(e) => handleInputChange('courseName', e.target.value)}
                                        variant="outlined"
                                    />
                                ) : (
                                    <Typography variant="h4" gutterBottom>
                                        {course.courseName || "Unnamed Course"}
                                    </Typography>
                                )}
                                
                                <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                                    <Chip 
                                        label={course.status ? "Active" : "Hidden"}
                                        color={course.status ? "success" : "default"}
                                        size="small"
                                    />
                                    {course.isVip && (
                                        <Chip 
                                            icon={<StarIcon />}
                                            label="VIP"
                                            color="warning"
                                            size="small"
                                        />
                                    )}
                                </Box>
                            </Box>
                        </Grid>
                        
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    select
                                    label="Course Type"
                                    value={formData.courseTypeId}
                                    onChange={(e) => handleInputChange('courseTypeId', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                >
                                    {courseTypes.map((type) => (
                                        <MenuItem key={type.courseTypeId} value={type.courseTypeId}>
                                            {type.courseTypeName}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            ) : (
                                <Typography variant="body1">
                                    <strong>Course Type:</strong> {course.courseType?.courseTypeName || "Not specified"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    label="Duration"
                                    value={formData.duration}
                                    onChange={(e) => handleInputChange('duration', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                    placeholder="e.g. 8 weeks"
                                />
                            ) : (
                                <Typography variant="body1">
                                    <strong>Duration:</strong> {course.duration || "Not specified"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    label="Price"
                                    type="number"
                                    value={formData.price}
                                    onChange={(e) => handleInputChange('price', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <MoneyIcon />
                                            </InputAdornment>
                                        )
                                    }}
                                />
                            ) : (
                                <Typography variant="body1">
                                    <strong>Price:</strong> {course.price || "0"} USD
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    label="Rating"
                                    type="number"
                                    value={formData.averageRating}
                                    onChange={(e) => handleInputChange('averageRating', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                    inputProps={{ min: 0, max: 5, step: 0.1 }}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <StarIcon />
                                            </InputAdornment>
                                        )
                                    }}
                                />
                            ) : (
                                <Typography variant="body1">
                                    <strong>Rating:</strong> {course.averageRating || "0"}/5
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    select
                                    label="Difficulty Level"
                                    value={formData.difficultyLevel}
                                    onChange={(e) => handleInputChange('difficultyLevel', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                >
                                    {difficultyLevels.map((level) => (
                                        <MenuItem key={level} value={level}>
                                            {level}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            ) : (
                                <Typography variant="body1">
                                    <strong>Difficulty:</strong> {course.difficultyLevel || "Not specified"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    label="Weekly Sessions"
                                    value={formData.weeklySessions}
                                    onChange={(e) => handleInputChange('weeklySessions', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                />
                            ) : (
                                <Typography variant="body1">
                                    <strong>Weekly Sessions:</strong> {course.weeklySessions || "Not specified"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12} sm={6}>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    select
                                    label="Recommended For"
                                    value={formData.recommendedFor}
                                    onChange={(e) => handleInputChange('recommendedFor', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                >
                                    {recommendedForOptions.map((option) => (
                                        <MenuItem key={option} value={option}>
                                            {option}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            ) : (
                                <Typography variant="body1">
                                    <strong>Recommended For:</strong> {course.recommendedFor || "Everyone"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12}>
                            {editing && (
                                <Box display="flex" gap={2} my={2}>
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
                            )}
                        </Grid>
                        
                        <Grid item xs={12}>
                            <Divider />
                            <Typography variant="h6" gutterBottom mt={2}>Description</Typography>
                            {editing ? (
                                <TextField
                                    fullWidth
                                    multiline
                                    rows={4}
                                    label="Description"
                                    value={formData.description}
                                    onChange={(e) => handleInputChange('description', e.target.value)}
                                    variant="outlined"
                                    margin="normal"
                                />
                            ) : (
                                <Typography variant="body1" paragraph>
                                    {course.description || "No description available"}
                                </Typography>
                            )}
                        </Grid>
                        
                        <Grid item xs={12}>
                            <Divider />
                            
                            <Typography variant="body2" color="textSecondary" mt={2}>
                                Course ID: {course.courseId}
                            </Typography>
                            
                            {course.createdDate && (
                                <Typography variant="body2" color="textSecondary">
                                    Created: {new Date(course.createdDate).toLocaleString()}
                                </Typography>
                            )}
                            
                            {course.updatedDate && (
                                <Typography variant="body2" color="textSecondary">
                                    Last Updated: {new Date(course.updatedDate).toLocaleString()}
                                </Typography>
                            )}
                        </Grid>
                    </Grid>
                </Paper>
            )}
            
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

export default ViewCourse; 