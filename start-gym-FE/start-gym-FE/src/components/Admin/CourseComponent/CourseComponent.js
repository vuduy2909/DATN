import React, { useState, useEffect } from 'react';
import {
    Box,
    Card,
    CardContent,
    Typography,
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Chip,
    IconButton,
    Snackbar,
    Alert,
    CircularProgress,
    Avatar,
    Switch,
    FormControlLabel,
    TextField,
    InputAdornment,
    Pagination,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Edit as EditIcon, 
    Add as AddIcon,
    Refresh as RefreshIcon,
    Visibility as VisibilityIcon,
    VisibilityOff as VisibilityOffIcon,
    PlayArrow as PlayIcon,
    Stop as StopIcon,
    Search as SearchIcon,
    Warning as WarningIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const CourseComponent = () => {
    const navigate = useNavigate();
    const [courses, setCourses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    
    // Pagination state
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalCourses, setTotalCourses] = useState(0);
    
    // Search and filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredCourses, setFilteredCourses] = useState([]);
    const [statusFilter, setStatusFilter] = useState('all'); // all, active, hidden
    const [vipFilter, setVipFilter] = useState('all'); // all, free, vip
    const [courseTypeFilter, setCourseTypeFilter] = useState('all');
    const [courseTypes, setCourseTypes] = useState([]);
    
    // Delete confirmation dialog state
    const [deleteDialog, setDeleteDialog] = useState({
        open: false,
        courseId: null,
        courseName: ''
    });
    
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
                setTotalCourses(response.data.length);
            } 
            // If response data has a key for the courses array
            else if (response.data && Array.isArray(response.data.courses)) {
                setCourses(response.data.courses);
                setTotalCourses(response.data.courses.length);
            } 
            // Fallback
            else {
                setCourses([]);
                setTotalCourses(0);
                console.warn('Unexpected response format:', response.data);
            }
        } catch (error) {
            console.error('Error fetching courses:', error);
            showSnackbar(`Error loading courses: ${error.message}`, 'error');
            setCourses([]);
            setTotalCourses(0);
        } finally {
            setIsLoading(false);
        }
    };

    // Fetch course types for filter
    const fetchCourseTypes = async () => {
        try {
            const response = await axiosInstance.get('/admin/course-types');
            setCourseTypes(response.data || []);
        } catch (error) {
            console.error('Error fetching course types:', error);
        }
    };

    // Filter and search logic
    useEffect(() => {
        let filtered = courses;

        // Search by course name
        if (searchTerm) {
            filtered = filtered.filter(course =>
                course.courseName?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Filter by status (bit field: 1 = active, 0 = hidden)
        if (statusFilter !== 'all') {
            filtered = filtered.filter(course => {
                if (statusFilter === 'active') return course.status === 1 || course.status === true;
                if (statusFilter === 'hidden') return course.status === 0 || course.status === false;
                return true;
            });
        }

        // Filter by VIP status (bit field: 1 = vip, 0 = free)
        if (vipFilter !== 'all') {
            filtered = filtered.filter(course => {
                if (vipFilter === 'vip') {
                    // VIP courses should have is_vip = true (or 1)
                    return course.is_vip === true || course.is_vip === 1;
                }
                if (vipFilter === 'free') {
                    // Free courses should have is_vip = false (or 0 or null/undefined)
                    return course.is_vip === false || course.is_vip === 0 || course.is_vip === null || course.is_vip === undefined;
                }
                return true;
            });
        }

        // Filter by course type
        if (courseTypeFilter !== 'all') {
            filtered = filtered.filter(course =>
                course.courseType?.courseTypeId?.toString() === courseTypeFilter
            );
        }

        setFilteredCourses(filtered);
        setTotalCourses(filtered.length);
    }, [courses, searchTerm, statusFilter, vipFilter, courseTypeFilter]);

    // Pagination logic
    const paginatedCourses = filteredCourses.slice(
        (page - 1) * rowsPerPage,
        page * rowsPerPage
    );

    const handlePageChange = (event, newPage) => {
        setPage(newPage);
    };

    const handleRowsPerPageChange = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(1);
    };

    // Toggle course status (active/hidden)
    // Note: Hidden courses (status = 0/false) will not appear in user collections or recommendations
    const handleToggleCourseStatus = async (courseId, currentStatus) => {
        try {
            console.log('Toggling course status:', courseId, 'from', currentStatus);
            
            // Convert to boolean for API call - invert current status
            const isCurrentlyActive = currentStatus === 1 || currentStatus === true;
            const newStatus = !isCurrentlyActive;
            
            // Call API to update status with the correct admin endpoint and payload format
            await axiosInstance.put(`/admin/courses/${courseId}/status`, {
                status: newStatus
            });
            
            showSnackbar(
                `Course ${newStatus ? 'activated' : 'hidden'} successfully! ${newStatus ? '✅' : '👁️‍🗨️ This course is now hidden from users'}`, 
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
        setDeleteDialog({
            open: true,
            courseId,
            courseName
        });
    };

    const handleConfirmDelete = async () => {
            try {
                console.log('Deleting course:', deleteDialog.courseId);
                await axiosInstance.delete(`/admin/courses/${deleteDialog.courseId}`);
                
                showSnackbar('Course deleted successfully! 🗑️', 'success');
            
            // Close dialog and reset state
            setDeleteDialog({ open: false, courseId: null, courseName: '' });
            
            // Refresh the course list
                fetchCourses();
                
            } catch (error) {
                console.error('Error deleting course:', error);
                const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
                showSnackbar(`Error deleting course: ${errorMsg}`, 'error');
            
            // Close dialog even on error
            setDeleteDialog({ open: false, courseId: null, courseName: '' });
        }
    };

    useEffect(() => {
        fetchCourses();
        fetchCourseTypes();
    }, []);

    // Utility functions
    const formatDuration = (duration) => {
        if (!duration) return 'N/A';
        return `${duration} days`;
    };

    const getStatusChip = (status) => {
        // Handle bit field: 1 = active/true, 0 = hidden/false
        const isActive = status === 1 || status === true;
        
        return isActive ? (
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
                        Total Courses: {totalCourses}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="success.main">
                        Active: {filteredCourses.filter(c => c.status).length}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="text.secondary">
                        Hidden: {filteredCourses.filter(c => !c.status).length}
                    </Typography>
                </Paper>
            </Box>

            {/* Search and Filters */}
            <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" gutterBottom>Search & Filters</Typography>
                
                <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
                    {/* Search Box */}
                    <TextField
                        placeholder="Search courses by name..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon />
                                </InputAdornment>
                            )
                        }}
                        sx={{ minWidth: 250 }}
                    />

                    {/* Status Filter */}
                    <TextField
                        select
                        label="Status"
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        sx={{ minWidth: 120 }}
                        SelectProps={{
                            native: true,
                        }}
                    >
                        <option value="all">All Status</option>
                        <option value="active">Active</option>
                        <option value="hidden">Hidden</option>
                    </TextField>

                    {/* VIP Filter */}
                    <TextField
                        select
                        label="Type"
                        value={vipFilter}
                        onChange={(e) => setVipFilter(e.target.value)}
                        sx={{ minWidth: 120 }}
                        SelectProps={{
                            native: true,
                        }}
                    >
                        <option value="all">All Types</option>
                        <option value="free">Free</option>
                        <option value="vip">VIP</option>
                    </TextField>

                    {/* Course Type Filter */}
                    <TextField
                        select
                        label="Course Type"
                        value={courseTypeFilter}
                        onChange={(e) => setCourseTypeFilter(e.target.value)}
                        sx={{ minWidth: 150 }}
                        SelectProps={{
                            native: true,
                        }}
                    >
                        <option value="all">All Course Types</option>
                        {courseTypes.map((type) => (
                            <option key={type.courseTypeId} value={type.courseTypeId}>
                                {type.courseTypeName}
                            </option>
                        ))}
                    </TextField>

                    {/* Rows per page */}
                    <TextField
                        select
                        label="Rows per page"
                        value={rowsPerPage}
                        onChange={handleRowsPerPageChange}
                        sx={{ minWidth: 120 }}
                        SelectProps={{
                            native: true,
                        }}
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                    </TextField>
                </Box>
            </Paper>

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
                            paginatedCourses.map((course) => (
                                <TableRow key={course.courseId} hover>
                                    <TableCell>
                                        <Avatar 
                                            src={course.image} 
                                            alt={course.courseName}
                                            variant="rounded"
                                            sx={{ width: 60, height: 60 }}
                                        >
                                            {course.courseName?.charAt(0)}
                                        </Avatar>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="subtitle1" fontWeight="bold">
                                            {course.courseName || 'Unnamed Course'}
                                        </Typography>
                                        <Box display="flex" gap={1} mt={0.5}>
                                            {course.courseType && (
                                                <Chip 
                                                    label={course.courseType.courseTypeName}
                                                    size="small"
                                                    variant="outlined"
                                                />
                                            )}
                                            {(course.is_vip === 1 || course.is_vip === true) && (
                                                <Chip 
                                                    label="VIP"
                                                    size="small"
                                                    color="secondary"
                                                    variant="filled"
                                                />
                                            )}
                                        </Box>
                                    </TableCell>
                                    <TableCell>
                                        <Typography 
                                            variant="body2" 
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
                                            {formatDuration(course.duration)}
                                    </TableCell>
                                    <TableCell>
                                        {getStatusChip(course.status)}
                                    </TableCell>
                                    <TableCell>
                                        <Box display="flex" gap={1}>
                                            <IconButton
                                                size="small"
                                                onClick={() => navigate(`/admin/courses/${course.courseId}/view`)}
                                                color="primary"
                                                title="View Course"
                                            >
                                                <PlayIcon />
                                            </IconButton>
                                            
                                            <IconButton
                                                size="small"
                                                onClick={() => navigate(`/admin/courses/${course.courseId}/edit`)}
                                                color="info"
                                                title="Edit Course"
                                            >
                                                <EditIcon />
                                            </IconButton>
                                            
                                            <IconButton
                                                size="small"
                                                onClick={() => handleToggleCourseStatus(course.courseId, course.status)}
                                                color={course.status === 1 || course.status === true ? "warning" : "success"}
                                                title={course.status === 1 || course.status === true ? "Hide Course" : "Show Course"}
                                            >
                                                {course.status === 1 || course.status === true ? <VisibilityOffIcon /> : <VisibilityIcon />}
                                            </IconButton>
                                            
                                            <IconButton
                                                size="small"
                                                onClick={() => handleDeleteCourse(course.courseId, course.courseName)}
                                                color="error"
                                                title="Delete Course"
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

            {/* Pagination */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mt={3}>
                <Typography variant="body2" color="textSecondary">
                    Showing {((page - 1) * rowsPerPage) + 1} to {Math.min(page * rowsPerPage, totalCourses)} of {totalCourses} courses
                </Typography>
                <Pagination
                    count={Math.ceil(totalCourses / rowsPerPage)}
                    page={page}
                    onChange={handlePageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

            {/* Delete Confirmation Dialog */}
            <Dialog
                open={deleteDialog.open}
                onClose={() => setDeleteDialog({ ...deleteDialog, open: false })}
                aria-labelledby="admin-course-delete-dialog-title"
                aria-describedby="admin-course-delete-dialog-description"
                maxWidth="sm"
                fullWidth
                PaperProps={{
                    sx: { zIndex: 1400 } // Higher z-index than Collections_card modal
                }}
            >
                <DialogTitle id="admin-course-delete-dialog-title" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <WarningIcon color="warning" />
                    Confirm Course Deletion
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="admin-course-delete-dialog-description" sx={{ fontSize: '1.1rem' }}>
                        Are you sure you want to delete the course <strong>"{deleteDialog.courseName}"</strong>?
                        <br /><br />
                        <em>This action cannot be undone and will permanently remove all associated data including course days and exercises.</em>
                    </DialogContentText>
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button 
                        onClick={() => setDeleteDialog({ ...deleteDialog, open: false })} 
                        variant="outlined"
                        sx={{ minWidth: 100 }}
                    >
                        Cancel
                    </Button>
                    <Button 
                        onClick={handleConfirmDelete} 
                        variant="contained"
                        color="error"
                        sx={{ minWidth: 100 }}
                        autoFocus
                    >
                        Delete Course
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

export default CourseComponent; 