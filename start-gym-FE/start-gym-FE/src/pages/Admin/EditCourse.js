import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box,
    Card,
    CardContent,
    Typography,
    Button,
    Grid,
    Avatar,
    Chip,
    CircularProgress,
    Snackbar,
    Alert,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    List,
    ListItem,
    ListItemText,
    ListItemSecondaryAction,
    IconButton,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    MenuItem,
    Divider,
    Paper,
    Switch,
    FormControlLabel,
    InputAdornment,
    ListItemIcon,
    Select,
    FormControl,
    InputLabel,
    Tabs,
    Tab
} from '@mui/material';
import {
    ArrowBack as ArrowBackIcon,
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    ExpandMore as ExpandMoreIcon,
    FitnessCenter as FitnessCenterIcon,
    PlayArrow as PlayIcon,
    Schedule as ScheduleIcon,
    Save as SaveIcon,
    AddCircle as AddCircleIcon,
    Done as DoneIcon,
    Cancel as CancelIcon,
    Image as ImageIcon,
    CloudUpload as CloudUploadIcon
} from '@mui/icons-material';
import { axiosInstance, getImageUrl } from '../../utils/axiosInstance';
import { useDialogs } from '../../components/MaterialUIDialogs';
import { uploadCourseImage, deleteFileFromFirebaseByURL, validateFile } from '../../utils/firebaseUpload';

const EditCourse = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const { showConfirm, showAlert, DialogComponents } = useDialogs();
    
    const [course, setCourse] = useState(null);
    const [courseDays, setCourseDays] = useState([]);
    const [loading, setLoading] = useState(true);
    const [exercises, setExercises] = useState([]);
    const [courseTypes, setCourseTypes] = useState([]);
    const [imageFile, setImageFile] = useState(null);
    const [uploadLoading, setUploadLoading] = useState(false);
    
    // Dialog states
    const [dayDialog, setDayDialog] = useState({ open: false, dayData: null, isEdit: false });
    const [exerciseDialog, setExerciseDialog] = useState({ open: false, dayId: null, exerciseData: null, isEdit: false });
    const [imageDialog, setImageDialog] = useState(false);
    
    // Form states
    const [newDayName, setNewDayName] = useState('');
    const [selectedExercise, setSelectedExercise] = useState('');
    const [exerciseOrder, setExerciseOrder] = useState(1);
    
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

    // Fetch data
    useEffect(() => {
        fetchCourseDetails();
        fetchCourseDays();
        fetchExercises();
        fetchCourseTypes();
    }, [courseId]);

    const fetchCourseTypes = async () => {
        try {
            const response = await axiosInstance.get('/admin/course-types');
            setCourseTypes(response.data || []);
        } catch (error) {
            console.error('Error fetching course types:', error);
            showSnackbar('Failed to load course types', 'error');
        }
    };

    const fetchCourseDetails = async () => {
        try {
            setLoading(true);
            // Use the admin API endpoint
            const response = await axiosInstance.get(`/admin/courses/${courseId}`);
            setCourse(response.data);
        } catch (error) {
            console.error('Error fetching course details:', error);
            showSnackbar('Failed to load course details', 'error');
        }
    };

    const fetchCourseDays = async () => {
        try {
            setLoading(true);
            // This endpoint would need to be implemented in the backend
            const response = await axiosInstance.get(`/admin/courses/${courseId}/days`);
            setCourseDays(response.data || []);
        } catch (error) {
            console.error('Error fetching course days:', error);
            setCourseDays([]);
        } finally {
            setLoading(false);
        }
    };

    const fetchExercises = async () => {
        try {
            // Use the admin endpoint for exercises
            const response = await axiosInstance.get('/admin/exercises');
            setExercises(response.data || []);
        } catch (error) {
            console.error('Error fetching exercises:', error);
            setExercises([]);
        }
    };

    const fetchDayExercises = async (dayId) => {
        try {
            // Use the admin endpoint instead of regular course endpoint
            const response = await axiosInstance.get(`/admin/courses/${courseId}/days/${dayId}/exercises`);
            return response.data || [];
        } catch (error) {
            console.error('Error fetching day exercises:', error);
            return [];
        }
    };

    // Day management
    const handleAddDay = async () => {
        try {
            // Auto-generate day name based on the highest day ID + 1
            let nextDayNumber = 1;
            if (courseDays.length > 0) {
                // Find the highest day number in existing days
                const dayNumbers = courseDays.map(day => {
                    const match = day.dayName.match(/Day (\d+)/);
                    return match ? parseInt(match[1]) : 0;
                });
                nextDayNumber = Math.max(...dayNumbers) + 1;
            }
            
            const dayName = newDayName.trim() || `Day ${nextDayNumber}`;

            const newDay = {
                dayName: dayName,
                courseId: parseInt(courseId),
                status: true
            };

            // Add the day
            await axiosInstance.post(`/admin/courses/${courseId}/days`, newDay);
            
            showSnackbar('Day added successfully! ✅', 'success');
            setDayDialog({ open: false, dayData: null, isEdit: false });
            setNewDayName('');
            fetchCourseDays(); // Refresh the list of days
            
        } catch (error) {
            console.error('Error adding day:', error);
            const errorMsg = error.response?.data?.message || 'Failed to add day';
            showSnackbar(errorMsg, 'error');
        }
    };

    const handleDeleteDay = async (dayId, dayName) => {
        showConfirm(
            'Confirm Deletion',
            `Are you sure you want to delete "${dayName}"?\n\nNote: All subsequent days will be renumbered to maintain order.`,
            async () => {
                try {
                    await axiosInstance.delete(`/admin/courses/${courseId}/days/${dayId}`);
                    
                    // After deletion, we need to renumber the remaining days
                    // The backend should handle this, but we'll refresh the list to show the updated names
                    await fetchCourseDays();
                    
                    showSnackbar('Day deleted successfully! Subsequent days have been renumbered. 🗑️', 'success');
                    
                } catch (error) {
                    console.error('Error deleting day:', error);
                    const errorMsg = error.response?.data?.message || 'Failed to delete day';
                    showSnackbar(errorMsg, 'error');
                }
            }
        );
    };

    // Exercise management
    const handleAddExercise = async () => {
        try {
            if (!selectedExercise) {
                showSnackbar('Please select an exercise', 'warning');
                return;
            }

            const exerciseData = {
                dayId: exerciseDialog.dayId,
                exerciseId: selectedExercise,
                courseId: parseInt(courseId),
                order: exerciseOrder,
                status: true
            };

            // This endpoint would need to be implemented
            await axiosInstance.post(`/admin/courses/${courseId}/days/${exerciseDialog.dayId}/exercises`, exerciseData);
            
            showSnackbar('Exercise added successfully! ✅', 'success');
            setExerciseDialog({ open: false, dayId: null, exerciseData: null, isEdit: false });
            setSelectedExercise('');
            setExerciseOrder(1);
            fetchCourseDays();
            
        } catch (error) {
            console.error('Error adding exercise:', error);
            showSnackbar('Failed to add exercise', 'error');
        }
    };

    const handleDeleteExercise = async (dayId, exerciseId, exerciseName) => {
        showConfirm(
            'Confirm Deletion',
            `Are you sure you want to remove "${exerciseName}" from this day?`,
            async () => {
                try {
                    await axiosInstance.delete(`/admin/courses/${courseId}/days/${dayId}/exercises/${exerciseId}`);
                    
                    // Refresh the day exercises
                    const updatedDays = [...courseDays];
                    const dayIndex = updatedDays.findIndex(day => day.dayId === dayId);
                    
                    if (dayIndex !== -1) {
                        // Get fresh exercise list
                        const exercises = await fetchDayExercises(dayId);
                        updatedDays[dayIndex].exercises = exercises;
                        setCourseDays(updatedDays);
                    }
                    
                    showSnackbar('Exercise removed successfully! 🗑️', 'success');
                    
                } catch (error) {
                    console.error('Error removing exercise:', error);
                    const errorMsg = error.response?.data?.message || 'Failed to remove exercise';
                    showSnackbar(errorMsg, 'error');
                }
            }
        );
    };

    const getTotalDays = () => courseDays.length;
    const getTotalExercises = () => courseDays.reduce((total, day) => total + (day.exercises?.length || 0), 0);

    // Image handling
    const handleImageClick = () => {
        setImageDialog(true);
    };

    const handleImageDialogClose = () => {
        setImageDialog(false);
        setImageFile(null);
    };

    const handleFileChange = (event) => {
        if (event.target.files && event.target.files[0]) {
            setImageFile(event.target.files[0]);
        }
    };

    const handleImageUpload = async () => {
        if (!imageFile) {
            showSnackbar('Please select an image file', 'warning');
            return;
        }

        try {
            // Validate image file
            validateFile(imageFile, 'image', 5); // 5MB limit
            
            setUploadLoading(true);
            
            // Get the course type name for folder organization
            const courseType = courseTypes.find(t => t.courseTypeId === course.courseTypeId);
            const courseTypeName = courseType?.courseTypeName || 'Other';

            // Upload new image with automatic deletion of old image
            const imageUrl = await uploadCourseImage(imageFile, course.courseName, courseTypeName, course.image);
            
            // Update course in database with new image URL
            await axiosInstance.patch(`/admin/courses/${courseId}`, {
                image: imageUrl
            });
            
            // Update local state
            setCourse({
                ...course,
                image: imageUrl
            });
            
            showSnackbar('Course image updated successfully! 🖼️', 'success');
            handleImageDialogClose();
            
        } catch (error) {
            console.error('Error uploading image:', error);
            showSnackbar(error.message || 'Error uploading image', 'error');
        } finally {
            setUploadLoading(false);
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <CircularProgress size={60} />
                <Typography variant="h6" sx={{ ml: 2 }}>Loading course structure...</Typography>
            </Box>
        );
    }

    if (!course) {
        return (
            <Box sx={{ p: 3 }}>
                <Typography variant="h6" color="error">Course not found</Typography>
                <Button 
                    startIcon={<ArrowBackIcon />} 
                    onClick={() => navigate('/admin', { state: { activeTab: 'courses' } })}
                    sx={{ mt: 2 }}
                >
                    Back to Courses
                </Button>
            </Box>
        );
    }

    return (
        <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto', backgroundColor: '#fff', minHeight: '100vh' }}>
            {/* Header */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box display="flex" alignItems="center">
                    <Button
                        startIcon={<ArrowBackIcon />}
                        onClick={() => navigate('/admin', { state: { activeTab: 'courses' } })}
                        variant="outlined"
                        sx={{ mr: 2 }}
                    >
                        Back to Courses
                    </Button>
                    <Typography variant="h4" fontWeight="bold">
                        Edit Course Structure
                    </Typography>
                </Box>
                
                <Button
                    startIcon={<AddIcon />}
                    variant="contained"
                    onClick={() => setDayDialog({ open: true, dayData: null, isEdit: false })}
                    sx={{ 
                        bgcolor: '#d35255',
                        '&:hover': { bgcolor: '#b8464a' }
                    }}
                >
                    Add Day
                </Button>
            </Box>

            {/* Course Info Card */}
            <Card elevation={3} sx={{ mb: 3 }}>
                <CardContent>
                    <Grid container spacing={3} alignItems="center">
                        <Grid item>
                            <Avatar
                                src={getImageUrl(course?.image)}
                                alt={course?.courseName}
                                sx={{ 
                                    width: 80, 
                                    height: 80,
                                    cursor: 'pointer',
                                    '&:hover': {
                                        opacity: 0.8,
                                        '&::after': {
                                            content: '"Edit"',
                                            position: 'absolute',
                                            top: '50%',
                                            left: '50%',
                                            transform: 'translate(-50%, -50%)',
                                            color: '#fff',
                                            fontSize: '14px'
                                        }
                                    }
                                }}
                                variant="rounded"
                                onClick={handleImageClick}
                                imgProps={{
                                    onError: (e) => {
                                        console.log('Course image failed to load, using fallback');
                                        e.target.onerror = null; // Prevent infinite loop
                                        // Generate SVG fallback with course initial
                                        const letter = course?.courseName?.charAt(0) || 'C';
                                        e.target.src = `data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='80' height='80' style='background:%23d35255' viewBox='0 0 80 80'><text x='50%' y='50%' font-size='40' fill='white' text-anchor='middle' dominant-baseline='middle'>${letter}</text></svg>`;
                                    }
                                }}
                            >
                                {course?.courseName?.charAt(0)}
                            </Avatar>
                        </Grid>
                        <Grid item xs>
                            <Typography variant="h5" fontWeight="bold" gutterBottom>
                                {course.courseName}
                            </Typography>
                            <Typography variant="body2" color="textSecondary" gutterBottom>
                                {course.description}
                            </Typography>
                            <Box display="flex" gap={1}>
                                <Chip 
                                    label={`${getTotalDays()} Days`} 
                                    color="primary" 
                                    size="small"
                                    icon={<ScheduleIcon />}
                                />
                                <Chip 
                                    label={`${getTotalExercises()} Exercises`} 
                                    color="secondary" 
                                    size="small"
                                    icon={<FitnessCenterIcon />}
                                />
                                <Chip 
                                    label={course.status ? 'Active' : 'Hidden'} 
                                    color={course.status ? 'success' : 'default'}
                                    size="small"
                                />
                            </Box>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            {/* Course Days */}
            <Typography variant="h5" fontWeight="bold" gutterBottom>
                Course Days & Exercises
            </Typography>
            
            {courseDays.length === 0 ? (
                <Paper sx={{ p: 4, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                    <ScheduleIcon sx={{ fontSize: 60, color: '#ccc', mb: 2 }} />
                    <Typography variant="h6" color="textSecondary" gutterBottom>
                        No days added yet
                    </Typography>
                    <Typography variant="body2" color="textSecondary" mb={3}>
                        Start by adding the first day to your course
                    </Typography>
                    <Button
                        startIcon={<AddIcon />}
                        variant="contained"
                        onClick={() => setDayDialog({ open: true, dayData: null, isEdit: false })}
                        sx={{ 
                            bgcolor: '#d35255',
                            '&:hover': { bgcolor: '#b8464a' }
                        }}
                    >
                        Add First Day
                    </Button>
                </Paper>
            ) : (
                <Box>
                    {courseDays.map((day, index) => (
                        <Accordion key={day.dayId} sx={{ mb: 2 }}>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Box display="flex" justifyContent="space-between" alignItems="center" width="100%">
                                    <Box display="flex" alignItems="center">
                                        <Avatar sx={{ bgcolor: '#d35255', mr: 2, width: 32, height: 32 }}>
                                            {index + 1}
                                        </Avatar>
                                        <Typography variant="h6" fontWeight="bold">
                                            {day.dayName}
                                        </Typography>
                                        <Chip 
                                            label={`${day.exercises?.length || 0} exercises`}
                                            size="small"
                                            sx={{ ml: 2 }}
                                        />
                                    </Box>
                                    <Box sx={{ mr: 2 }}>
                                        <IconButton
                                            size="small"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setExerciseDialog({ 
                                                    open: true, 
                                                    dayId: day.dayId, 
                                                    exerciseData: null, 
                                                    isEdit: false 
                                                });
                                            }}
                                            color="primary"
                                        >
                                            <AddIcon />
                                        </IconButton>
                                        <IconButton
                                            size="small"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleDeleteDay(day.dayId, day.dayName);
                                            }}
                                            color="error"
                                        >
                                            <DeleteIcon />
                                        </IconButton>
                                    </Box>
                                </Box>
                            </AccordionSummary>
                            <AccordionDetails>
                                {day.exercises && day.exercises.length > 0 ? (
                                    <List>
                                        {day.exercises.map((exercise, exerciseIndex) => (
                                            <ListItem key={exercise.exerciseId} divider={exerciseIndex < day.exercises.length - 1}>
                                                <Avatar sx={{ bgcolor: '#f0f0f0', color: '#666', mr: 2, width: 24, height: 24 }}>
                                                    {exerciseIndex + 1}
                                                </Avatar>
                                                <ListItemText
                                                    primary={exercise.exerciseName}
                                                    secondary={`Target: ${exercise.target} • Reps: ${exercise.reps || 'Not specified'}`}
                                                />
                                                <ListItemSecondaryAction>
                                                    <IconButton
                                                        size="small"
                                                        onClick={() => handleDeleteExercise(day.dayId, exercise.exerciseId, exercise.exerciseName)}
                                                        color="error"
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                </ListItemSecondaryAction>
                                            </ListItem>
                                        ))}
                                    </List>
                                ) : (
                                    <Box sx={{ textAlign: 'center', py: 3 }}>
                                        <FitnessCenterIcon sx={{ fontSize: 40, color: '#ccc', mb: 1 }} />
                                        <Typography variant="body2" color="textSecondary" gutterBottom>
                                            No exercises added to this day
                                        </Typography>
                                        <Button
                                            startIcon={<AddIcon />}
                                            size="small"
                                            onClick={() => setExerciseDialog({ 
                                                open: true, 
                                                dayId: day.dayId, 
                                                exerciseData: null, 
                                                isEdit: false 
                                            })}
                                        >
                                            Add Exercise
                                        </Button>
                                    </Box>
                                )}
                            </AccordionDetails>
                        </Accordion>
                    ))}
                </Box>
            )}

            {/* Add Day Dialog */}
            <Dialog open={dayDialog.open} onClose={() => setDayDialog({ open: false, dayData: null, isEdit: false })}>
                <DialogTitle>Add New Day</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Day Name"
                        fullWidth
                        variant="outlined"
                        value={newDayName}
                        onChange={(e) => setNewDayName(e.target.value)}
                        placeholder={`e.g., Day ${courseDays.length + 1}`}
                    />
                    <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mt: 1 }}>
                        Leave empty to auto-generate the day name
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDayDialog({ open: false, dayData: null, isEdit: false })}>
                        Cancel
                    </Button>
                    <Button onClick={handleAddDay} variant="contained">
                        Add Day
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Add Exercise Dialog */}
            <Dialog 
                open={exerciseDialog.open} 
                onClose={() => setExerciseDialog({ open: false, dayId: null, exerciseData: null, isEdit: false })}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle>Add Exercise to Day</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2} sx={{ mt: 1 }}>
                        <Grid item xs={12}>
                            <TextField
                                select
                                fullWidth
                                label="Select Exercise"
                                value={selectedExercise}
                                onChange={(e) => setSelectedExercise(e.target.value)}
                                variant="outlined"
                            >
                                <MenuItem value="">
                                    <em>Choose an exercise</em>
                                </MenuItem>
                                {exercises.map((exercise) => (
                                    <MenuItem key={exercise.exerciseId} value={exercise.exerciseId}>
                                        {exercise.exerciseName} - {exercise.target}
                                    </MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Exercise Order"
                                type="number"
                                value={exerciseOrder}
                                onChange={(e) => setExerciseOrder(parseInt(e.target.value) || 1)}
                                variant="outlined"
                                inputProps={{ min: 1 }}
                            />
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setExerciseDialog({ open: false, dayId: null, exerciseData: null, isEdit: false })}>
                        Cancel
                    </Button>
                    <Button onClick={handleAddExercise} variant="contained">
                        Add Exercise
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Image Upload Dialog */}
            <Dialog open={imageDialog} onClose={handleImageDialogClose}>
                <DialogTitle>Update Course Image</DialogTitle>
                <DialogContent>
                    <Box sx={{ textAlign: 'center', my: 2 }}>
                        {imageFile ? (
                            <img 
                                src={URL.createObjectURL(imageFile)} 
                                alt="Preview" 
                                style={{ maxWidth: '100%', maxHeight: '200px', borderRadius: '8px' }} 
                            />
                        ) : course?.image ? (
                            <img
                                src={getImageUrl(course.image)}
                                alt="Course Preview"
                                style={{ maxWidth: '100%', maxHeight: '300px', marginBottom: '15px' }}
                                onError={(e) => {
                                    e.target.src = 'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22286%22%20height%3D%22180%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20286%20180%22%20preserveAspectRatio%3D%22none%22%3E%3Cdefs%3E%3Cstyle%20type%3D%22text%2Fcss%22%3E%23holder_18c66a8c7b3%20text%20%7B%20fill%3A%23999%3Bfont-weight%3Anormal%3Bfont-family%3AArial%2C%20Helvetica%2C%20Open%20Sans%2C%20sans-serif%2C%20monospace%3Bfont-size%3A14pt%20%7D%20%3C%2Fstyle%3E%3C%2Fdefs%3E%3Cg%20id%3D%22holder_18c66a8c7b3%22%3E%3Crect%20width%3D%22286%22%20height%3D%22180%22%20fill%3D%22%23373940%22%3E%3C%2Frect%3E%3Cg%3E%3Ctext%20x%3D%22107.1875%22%20y%3D%2296.3%22%3EImage%20not%20available%3C%2Ftext%3E%3C%2Fg%3E%3C%2Fg%3E%3C%2Fsvg%3E';
                                    e.target.onerror = null;
                                }}
                            />
                        ) : (
                            <Box 
                                sx={{ 
                                    width: '100%', 
                                    height: '200px', 
                                    bgcolor: '#f5f5f5', 
                                    display: 'flex', 
                                    alignItems: 'center', 
                                    justifyContent: 'center',
                                    borderRadius: '8px'
                                }}
                            >
                                <ImageIcon sx={{ fontSize: 80, color: '#ccc' }} />
                            </Box>
                        )}
                    </Box>
                    <Button
                        variant="outlined"
                        component="label"
                        startIcon={<CloudUploadIcon />}
                        fullWidth
                        sx={{ mt: 2 }}
                    >
                        Select Image
                        <input
                            type="file"
                            hidden
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                    </Button>
                    <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mt: 1 }}>
                        Recommended: 16:9 ratio, max 5MB, JPG or PNG
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleImageDialogClose}>Cancel</Button>
                    <Button 
                        onClick={handleImageUpload} 
                        variant="contained" 
                        disabled={!imageFile || uploadLoading}
                        startIcon={uploadLoading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        sx={{ 
                            bgcolor: '#d35255',
                            '&:hover': { bgcolor: '#b8464a' }
                        }}
                    >
                        {uploadLoading ? 'Uploading...' : 'Upload'}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Snackbar */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            >
                <Alert onClose={handleSnackbarClose} severity={snackbar.severity}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
            
            {DialogComponents}
        </Box>
    );
};

export default EditCourse; 