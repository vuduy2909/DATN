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
    Paper
} from '@mui/material';
import {
    ArrowBack as ArrowBackIcon,
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    ExpandMore as ExpandMoreIcon,
    FitnessCenter as FitnessCenterIcon,
    PlayArrow as PlayIcon,
    Schedule as ScheduleIcon
} from '@mui/icons-material';
import { axiosInstance, getImageUrl } from '../../utils/axiosInstance';

const EditCourse = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const [course, setCourse] = useState(null);
    const [courseDays, setCourseDays] = useState([]);
    const [loading, setLoading] = useState(true);
    const [exercises, setExercises] = useState([]);
    
    // Dialog states
    const [dayDialog, setDayDialog] = useState({ open: false, dayData: null, isEdit: false });
    const [exerciseDialog, setExerciseDialog] = useState({ open: false, dayId: null, exerciseData: null, isEdit: false });
    
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
    }, [courseId]);

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
            // Set mock data for now
            setCourseDays([
                {
                    dayId: 1,
                    dayName: 'Day 1',
                    courseId: parseInt(courseId),
                    status: true,
                    exercises: [
                        { exerciseId: 1, exerciseName: 'Push-ups', target: 'Chest', reps: '3x10' },
                        { exerciseId: 2, exerciseName: 'Squats', target: 'Legs', reps: '3x12' }
                    ]
                },
                {
                    dayId: 2,
                    dayName: 'Day 2',
                    courseId: parseInt(courseId),
                    status: true,
                    exercises: [
                        { exerciseId: 3, exerciseName: 'Pull-ups', target: 'Back', reps: '3x8' }
                    ]
                }
            ]);
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
            // Set mock data
            setExercises([
                { exerciseId: 1, exerciseName: 'Push-ups', target: 'Chest', bodyPart: 'upper body' },
                { exerciseId: 2, exerciseName: 'Squats', target: 'Legs', bodyPart: 'lower body' },
                { exerciseId: 3, exerciseName: 'Pull-ups', target: 'Back', bodyPart: 'upper body' },
                { exerciseId: 4, exerciseName: 'Lunges', target: 'Legs', bodyPart: 'lower body' },
                { exerciseId: 5, exerciseName: 'Plank', target: 'Core', bodyPart: 'core' }
            ]);
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
            if (!newDayName.trim()) {
                showSnackbar('Please enter a day name', 'warning');
                return;
            }

            const newDay = {
                dayName: newDayName,
                courseId: parseInt(courseId),
                status: true
            };

            // This endpoint would need to be implemented
            await axiosInstance.post(`/admin/courses/${courseId}/days`, newDay);
            
            showSnackbar('Day added successfully! ✅', 'success');
            setDayDialog({ open: false, dayData: null, isEdit: false });
            setNewDayName('');
            fetchCourseDays();
            
        } catch (error) {
            console.error('Error adding day:', error);
            showSnackbar('Failed to add day', 'error');
        }
    };

    const handleDeleteDay = async (dayId, dayName) => {
        if (window.confirm(`Are you sure you want to delete "${dayName}"?`)) {
            try {
                await axiosInstance.delete(`/admin/courses/${courseId}/days/${dayId}`);
                showSnackbar('Day deleted successfully! 🗑️', 'success');
                fetchCourseDays();
            } catch (error) {
                console.error('Error deleting day:', error);
                showSnackbar('Failed to delete day', 'error');
            }
        }
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
        if (window.confirm(`Are you sure you want to remove "${exerciseName}" from this day?`)) {
            try {
                await axiosInstance.delete(`/admin/courses/${courseId}/days/${dayId}/exercises/${exerciseId}`);
                showSnackbar('Exercise removed successfully! 🗑️', 'success');
                fetchCourseDays();
            } catch (error) {
                console.error('Error removing exercise:', error);
                showSnackbar('Failed to remove exercise', 'error');
            }
        }
    };

    const getTotalDays = () => courseDays.length;
    const getTotalExercises = () => courseDays.reduce((total, day) => total + (day.exercises?.length || 0), 0);

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
                    onClick={() => navigate('/admin')}
                    sx={{ mt: 2 }}
                >
                    Back to Courses
                </Button>
            </Box>
        );
    }

    return (
        <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto' }}>
            {/* Header */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box display="flex" alignItems="center">
                    <Button
                        startIcon={<ArrowBackIcon />}
                        onClick={() => navigate('/admin')}
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
                                src={getImageUrl(course.image)}
                                alt={course.courseName}
                                sx={{ width: 80, height: 80 }}
                                variant="rounded"
                                imgProps={{
                                    onError: (e) => {
                                        console.log('Course image failed to load, using fallback');
                                        e.target.onerror = null; // Prevent infinite loop
                                        // Generate SVG fallback with course initial
                                        const letter = course.courseName?.charAt(0) || 'C';
                                        e.target.src = `data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='80' height='80' style='background:%23d35255' viewBox='0 0 80 80'><text x='50%' y='50%' font-size='40' fill='white' text-anchor='middle' dominant-baseline='middle'>${letter}</text></svg>`;
                                    }
                                }}
                            >
                                {course.courseName?.charAt(0)}
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
                        placeholder="e.g., Day 1, Upper Body, Cardio Day"
                    />
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

export default EditCourse; 