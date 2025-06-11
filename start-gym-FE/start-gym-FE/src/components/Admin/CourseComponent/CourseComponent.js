import React, { useState, useEffect } from 'react';
import { 
    Table, 
    TableBody, 
    TableCell, 
    TableContainer, 
    TableHead, 
    TableRow, 
    Paper, 
    IconButton, 
    Button,
    Chip,
    Typography,
    Box,
    Snackbar,
    Alert,
    CircularProgress,
    Avatar,
    Switch,
    FormControlLabel
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Edit as EditIcon, 
    Add as AddIcon,
    Refresh as RefreshIcon,
    Visibility as VisibilityIcon,
    VisibilityOff as VisibilityOffIcon,
    PlayArrow as PlayIcon,
    Stop as StopIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const CourseComponent = () => {
    const navigate = useNavigate();
    const [courses, setCourses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    
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

    const handleSnackbarClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setSnackbar({ ...snackbar, open: false });
    };

    // Fetch all courses
    const fetchCourses = async () => {
        setIsLoading(true);
        
        try {
            console.log('Fetching courses...');
            const response = await axiosInstance.get('/course'); // Sử dụng endpoint course hiện có
            console.log('Response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setCourses(response.data);
                console.log('Courses loaded:', response.data.length);
            } else {
                setCourses([]);
                showSnackbar('No courses data received', 'warning');
            }
        } catch (error) {
            console.error('Error fetching courses:', error);
            showSnackbar(`Error loading courses: ${error.message}`, 'error');
            setCourses([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Toggle course status (active/hidden)
    const handleToggleCourseStatus = async (courseId, currentStatus) => {
        try {
            console.log('Toggling course status:', courseId, 'from', currentStatus);
            
            // Gọi API để update status
            await axiosInstance.put(`/course/${courseId}/status`, {
                status: !currentStatus
            });
            
            showSnackbar(
                `Course ${!currentStatus ? 'activated' : 'hidden'} successfully! ✅`, 
                'success'
            );
            
            fetchCourses(); // Refresh danh sách
            
        } catch (error) {
            console.error('Error toggling course status:', error);
            const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
            showSnackbar(`Error updating course status: ${errorMsg}`, 'error');
        }
    };

    // Delete course
    const handleDeleteCourse = async (courseId, courseName) => {
        if (window.confirm(`Are you sure you want to delete "${courseName}"?`)) {
            try {
                console.log('Deleting course:', courseId);
                await axiosInstance.delete(`/course/${courseId}`);
                
                showSnackbar('Course deleted successfully! 🗑️', 'success');
                fetchCourses();
                
            } catch (error) {
                console.error('Error deleting course:', error);
                const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
                showSnackbar(`Error deleting course: ${errorMsg}`, 'error');
            }
        }
    };

    useEffect(() => {
        fetchCourses();
    }, []);

    // Utility functions
    const formatDuration = (duration) => {
        if (!duration) return 'N/A';
        return `${duration} days`;
    };

    const getStatusChip = (status) => {
        return status ? (
            <Chip 
                icon={<VisibilityIcon />}
                label="Active" 
                color="success"
                variant="outlined"
                size="small"
            />
        ) : (
            <Chip 
                icon={<VisibilityOffIcon />}
                label="Hidden" 
                color="default"
                variant="outlined"
                size="small"
            />
        );
    };

    if (isLoading) {
        return (
            <Box 
                display="flex" 
                justifyContent="center" 
                alignItems="center" 
                minHeight="50vh"
                flexDirection="column"
            >
                <CircularProgress size={60} />
                <Typography variant="h6" style={{ marginTop: 16 }}>
                    Loading courses...
                </Typography>
            </Box>
        );
    }

    return (
        <Box sx={{ padding: 2 }}>
            {/* Header */}
            <Box 
                display="flex" 
                justifyContent="space-between" 
                alignItems="center" 
                marginBottom={3}
                sx={{
                    background: 'white',
                    padding: 2,
                    borderRadius: 2,
                    boxShadow: 1
                }}
            >
                <Typography variant="h4" component="h1" fontWeight="bold">
                    Course Management
                </Typography>
                
                <Box display="flex" gap={2}>
                    <Button 
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => navigate('/admin/courses/new')}
                        sx={{ 
                            bgcolor: '#d35255',
                            '&:hover': {
                                bgcolor: '#b8464a'
                            }
                        }}
                    >
                        Add Course
                    </Button>
                    
                    <Button 
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={fetchCourses}
                        disabled={isLoading}
                        sx={{
                            borderColor: '#d35255',
                            color: '#d35255',
                            '&:hover': {
                                borderColor: '#b8464a',
                                bgcolor: 'rgba(211, 82, 85, 0.04)'
                            }
                        }}
                    >
                        Refresh
                    </Button>
                </Box>
            </Box>

            {/* Stats Summary */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="primary">
                        Total Courses: {courses.length}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="success.main">
                        Active: {courses.filter(c => c.status).length}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="text.secondary">
                        Hidden: {courses.filter(c => !c.status).length}
                    </Typography>
                </Paper>
            </Box>

            {/* Table */}
            <TableContainer 
                component={Paper} 
                sx={{ 
                    boxShadow: 2,
                    borderRadius: 2,
                    overflow: 'hidden'
                }}
            >
                <Table>
                    <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                        <TableRow>
                            <TableCell sx={{ fontWeight: 'bold' }}>Image</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Course Name</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Description</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Duration</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {courses.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                    <Typography variant="body1" color="textSecondary" fontStyle="italic">
                                        No courses found
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : (
                            courses.map((course) => (
                                <TableRow key={course.courseId} hover>
                                    <TableCell>
                                        <Avatar 
                                            src={course.image} 
                                            alt={course.courseName}
                                            sx={{ width: 50, height: 50 }}
                                            variant="rounded"
                                        >
                                            {course.courseName?.charAt(0)}
                                        </Avatar>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body1" fontWeight="medium">
                                            {course.courseName}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography 
                                            variant="body2" 
                                            color="textSecondary"
                                            sx={{
                                                maxWidth: 200,
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                whiteSpace: 'nowrap'
                                            }}
                                        >
                                            {course.description || 'No description'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2">
                                            {formatDuration(course.duration)}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        {getStatusChip(course.status)}
                                    </TableCell>
                                    <TableCell>
                                        <Box display="flex" gap={1}>
                                            <IconButton
                                                color="info"
                                                size="small"
                                                onClick={() => navigate(`/admin/courses/view/${course.courseId}`)}
                                                title="View Details"
                                            >
                                                <VisibilityIcon />
                                            </IconButton>
                                            <IconButton
                                                color="primary"
                                                size="small"
                                                onClick={() => navigate(`/admin/courses/edit/${course.courseId}`)}
                                                title="Edit"
                                            >
                                                <EditIcon />
                                            </IconButton>
                                            <IconButton
                                                color={course.status ? "warning" : "success"}
                                                size="small"
                                                onClick={() => handleToggleCourseStatus(course.courseId, course.status)}
                                                title={course.status ? "Hide Course" : "Activate Course"}
                                            >
                                                {course.status ? <VisibilityOffIcon /> : <VisibilityIcon />}
                                            </IconButton>
                                            <IconButton
                                                color="error"
                                                size="small"
                                                onClick={() => handleDeleteCourse(course.courseId, course.courseName)}
                                                title="Delete"
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

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

export default CourseComponent; 