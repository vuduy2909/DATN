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
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Badge,
    Tabs,
    Tab,
    Pagination,
    InputAdornment
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Visibility as VisibilityIcon,
    Flag as FlagIcon,
    Comment as CommentIcon,
    Warning as WarningIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Refresh as RefreshIcon,
    Search as SearchIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { useAuth } from '../../../AuthContext';
import { useDialogs } from '../../../components/MaterialUIDialogs';

const CommentComponent = () => {
    const [comments, setComments] = useState([]);
    const [reportedComments, setReportedComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedTab, setSelectedTab] = useState(0);
    const [selectedComment, setSelectedComment] = useState(null);
    const [dialogOpen, setDialogOpen] = useState(false);
    
    // Pagination state
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalComments, setTotalComments] = useState(0);

    // Filter state
    const [filterType, setFilterType] = useState('all'); // all, reported, course, exercise
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredComments, setFilteredComments] = useState([]);
    
    // Snackbar state
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: '',
        severity: 'info'
    });

    const { showConfirm, showAlert, DialogComponents } = useDialogs();

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

    // Fetch all comments
    const fetchComments = async () => {
        setLoading(true);
        
        try {
            console.log('Fetching comments...');
            const response = await axiosInstance.get('/admin/comments');
            console.log('Comments response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setComments(response.data);
                setTotalComments(response.data.length);
                console.log('Comments loaded:', response.data.length);
            } else {
                setComments([]);
                setTotalComments(0);
                showSnackbar('No comments data received', 'warning');
            }
        } catch (error) {
            console.error('Error fetching comments:', error);
            showSnackbar(`Error loading comments: ${error.message}`, 'error');
            setComments([]);
            setTotalComments(0);
        } finally {
            setLoading(false);
        }
    };

    // Fetch reported comments  
    const fetchReportedComments = async () => {
        setLoading(true);
        
        try {
            console.log('Fetching reported comments...');
            const response = await axiosInstance.get('/admin/comments/reported');
            console.log('Reported comments response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setReportedComments(response.data);
                setTotalComments(response.data.length);
                console.log('Reported comments loaded:', response.data.length);
            } else {
                setReportedComments([]);
                setTotalComments(0);
                showSnackbar('No reported comments found', 'info');
            }
        } catch (error) {
            console.error('Error fetching reported comments:', error);
            showSnackbar(`Error loading reported comments: ${error.message}`, 'error');
            setReportedComments([]);
            setTotalComments(0);
        } finally {
            setLoading(false);
        }
    };

    // Update Tab counts and filtering logic
    useEffect(() => {
        // Load both datasets on mount to get accurate counts
        fetchComments();
        fetchReportedComments();
    }, []); // Run once on mount

    useEffect(() => {
        // Switch data display based on selected tab
        if (selectedTab === 0) {
            setFilteredComments(comments);
        } else {
            setFilteredComments(reportedComments);
        }
    }, [selectedTab, comments, reportedComments]);

    // Filter and search logic - Updated to use current data properly
    useEffect(() => {
        const currentDataSet = selectedTab === 0 ? comments : reportedComments;
        let filtered = currentDataSet;

        // Search by comment text or customer name
        if (searchTerm) {
            filtered = filtered.filter(comment =>
                comment.commentText?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                comment.customer?.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredComments(filtered);
        // Don't reset page to 1 here to avoid infinite loop
    }, [comments, reportedComments, searchTerm, selectedTab]);

    // Update total comments based on current filtered results
    useEffect(() => {
        setTotalComments(filteredComments.length);
        setPage(1); // Reset to first page when filter changes
    }, [filteredComments]);

    // Pagination logic
    const paginatedComments = filteredComments.slice(
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

    // Delete comment
    const handleDeleteComment = async (commentId, commentText) => {
        showConfirm(
            'Confirm Deletion',
            `Are you sure you want to delete this comment: "${commentText?.substring(0, 50)}..."?`,
            async () => {
                try {
                    console.log('Deleting comment:', commentId);
                    const response = await axiosInstance.delete(`/admin/comments/${commentId}`);
                    
                    console.log('Delete response:', response);
                    showSnackbar('Comment deleted successfully! 🗑️', 'success');
                    
                    // Refresh the list based on current tab
                    if (selectedTab === 0) {
                        fetchComments();
                    } else {
                        fetchReportedComments();
                    }
                } catch (error) {
                    console.error('Error deleting comment:', error);
                    showSnackbar(`Failed to delete comment: ${error.message}`, 'error');
                }
            }
        );
    };

    // Dismiss report (keep comment, remove from reported list)
    const handleDismissReport = async (commentId) => {
        try {
            console.log('Dismissing report for comment:', commentId);
            await axiosInstance.post(`/admin/comments/${commentId}/dismiss-report`);
            
            showSnackbar('Report dismissed successfully! ✅', 'success');
            fetchReportedComments();
            
        } catch (error) {
            console.error('Error dismissing report:', error);
            const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
            showSnackbar(`Error dismissing report: ${errorMsg}`, 'error');
        }
    };

    // View comment details
    const handleViewComment = (comment) => {
        setSelectedComment(comment);
        setDialogOpen(true);
    };

    // Utility functions
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString('vi-VN');
    };

    const getReportStatusChip = (reportCount) => {
        if (reportCount > 5) {
            return <Chip icon={<WarningIcon />} label={`${reportCount} Reports`} color="error" size="small" />;
        } else if (reportCount > 2) {
            return <Chip icon={<FlagIcon />} label={`${reportCount} Reports`} color="warning" size="small" />;
        } else {
            return <Chip icon={<FlagIcon />} label={`${reportCount} Reports`} color="info" size="small" />;
        }
    };

    const handleTabChange = (event, newValue) => {
        setSelectedTab(newValue);
    };

    if (loading) {
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
                    Loading comments...
                </Typography>
            </Box>
        );
    }

    const currentData = selectedTab === 0 ? comments : reportedComments;

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
                    Comment Management
                </Typography>
                
                <Box display="flex" gap={2}>
                    <Button 
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={() => selectedTab === 0 ? fetchComments() : fetchReportedComments()}
                        disabled={loading}
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

            {/* Tabs */}
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                <Tabs value={selectedTab} onChange={handleTabChange}>
                    <Tab 
                        icon={<CommentIcon />} 
                        label={`All Comments (${comments.length})`} 
                        iconPosition="start"
                    />
                    <Tab 
                        icon={
                            <Badge badgeContent={reportedComments.length} color="error" max={99}>
                                <FlagIcon />
                            </Badge>
                        } 
                        label={`Reported Comments (${reportedComments.length})`} 
                        iconPosition="start"
                    />
                </Tabs>
            </Box>

            {/* Stats Summary */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="primary">
                        Total: {totalComments}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="warning.main">
                        Reported: {reportedComments.length}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="text.secondary">
                        Showing: {paginatedComments.length}
                    </Typography>
                </Paper>
            </Box>

            {/* Search and Filters */}
            <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" gutterBottom>Search & Filters</Typography>
                <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
                    {/* Search Box */}
                    <TextField
                        placeholder="Search comments or customer names..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon />
                                </InputAdornment>
                            )
                        }}
                        sx={{ minWidth: 300 }}
                    />

                    {/* Rows per page */}
                    <TextField
                        select
                        label="Rows per page"
                        value={rowsPerPage}
                        onChange={handleRowsPerPageChange}
                        sx={{ minWidth: 120 }}
                        SelectProps={{ native: true }}
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
                            <TableCell sx={{ fontWeight: 'bold' }}>User</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Comment</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Course</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Day</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Exercise</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                            {selectedTab === 1 && <TableCell sx={{ fontWeight: 'bold' }}>Reports</TableCell>}
                            <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {currentData.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={selectedTab === 1 ? 8 : 7} align="center" sx={{ py: 4 }}>
                                    <Typography variant="body1" color="textSecondary" fontStyle="italic">
                                        {selectedTab === 0 ? 'No comments found' : 'No reported comments found'}
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : (
                            paginatedComments.map((comment) => (
                                <TableRow key={comment.commentId} hover>
                                    <TableCell>
                                        <Box display="flex" alignItems="center">
                                            <Avatar 
                                                src={comment.customer?.customerImg} 
                                                sx={{ mr: 2, width: 32, height: 32 }}
                                            >
                                                {comment.customer?.customerName?.charAt(0)}
                                            </Avatar>
                                            <Typography variant="body2">
                                                {comment.customer?.customerName || 'Unknown User'}
                                            </Typography>
                                        </Box>
                                    </TableCell>
                                    <TableCell>
                                        <Typography 
                                            variant="body2"
                                            sx={{
                                                maxWidth: 200,
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                whiteSpace: 'nowrap',
                                                cursor: 'pointer'
                                            }}
                                            onClick={() => handleViewComment(comment)}
                                        >
                                            {comment.commentText}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {comment.course?.courseName || 'N/A'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {comment.day?.dayName || 'N/A'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {comment.exercise?.exerciseName || 'N/A'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {formatDate(comment.createdAt)}
                                        </Typography>
                                    </TableCell>
                                    {selectedTab === 1 && (
                                        <TableCell>
                                            {getReportStatusChip(comment.reportCount || 1)}
                                        </TableCell>
                                    )}
                                    <TableCell>
                                        <Box display="flex" gap={1}>
                                            <IconButton
                                                color="info"
                                                size="small"
                                                onClick={() => handleViewComment(comment)}
                                                title="View Details"
                                            >
                                                <VisibilityIcon />
                                            </IconButton>
                                            {selectedTab === 1 && (
                                                <IconButton
                                                    color="success"
                                                    size="small"
                                                    onClick={() => handleDismissReport(comment.commentId)}
                                                    title="Dismiss Report"
                                                >
                                                    <CheckCircleIcon />
                                                </IconButton>
                                            )}
                                            <IconButton
                                                color="error"
                                                size="small"
                                                onClick={() => handleDeleteComment(comment.commentId, comment.commentText)}
                                                title="Delete Comment"
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

            {/* Comment Details Dialog */}
            <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
                <DialogTitle>Comment Details</DialogTitle>
                <DialogContent>
                    {selectedComment && (
                        <Box>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Avatar 
                                    src={selectedComment.customer?.customerImg} 
                                    sx={{ mr: 2, width: 50, height: 50 }}
                                >
                                    {selectedComment.customer?.customerName?.charAt(0)}
                                </Avatar>
                                <Box>
                                    <Typography variant="h6">
                                        {selectedComment.customer?.customerName || 'Unknown User'}
                                    </Typography>
                                    <Typography variant="body2" color="textSecondary">
                                        {formatDate(selectedComment.createdAt)}
                                    </Typography>
                                </Box>
                            </Box>
                            
                            <Box mb={2}>
                                <Typography variant="subtitle2" gutterBottom>Comment:</Typography>
                                <TextField
                                    multiline
                                    rows={4}
                                    fullWidth
                                    value={selectedComment.commentText}
                                    variant="outlined"
                                    InputProps={{ readOnly: true }}
                                />
                            </Box>
                            
                            <Box mb={2}>
                                <Typography variant="subtitle2" gutterBottom>Course:</Typography>
                                <Typography variant="body1">
                                    {selectedComment.course?.courseName || 'N/A'}
                                </Typography>
                            </Box>
                            
                            <Box mb={2}>
                                <Typography variant="subtitle2" gutterBottom>Day:</Typography>
                                <Typography variant="body1">
                                    {selectedComment.day?.dayName || 'N/A'}
                                </Typography>
                            </Box>
                            
                            <Box>
                                <Typography variant="subtitle2" gutterBottom>Exercise:</Typography>
                                <Typography variant="body1">
                                    {selectedComment.exercise?.exerciseName || 'N/A'}
                                </Typography>
                            </Box>
                            
                            {selectedComment.reportCount && (
                                <Box mt={2}>
                                    <Typography variant="subtitle2" gutterBottom>Report Status:</Typography>
                                    {getReportStatusChip(selectedComment.reportCount)}
                                </Box>
                            )}
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDialogOpen(false)}>Close</Button>
                    {selectedComment && (
                        <>
                            {selectedTab === 1 && (
                                <Button 
                                    color="success" 
                                    onClick={() => {
                                        handleDismissReport(selectedComment.commentId);
                                        setDialogOpen(false);
                                    }}
                                >
                                    Dismiss Report
                                </Button>
                            )}
                            <Button 
                                color="error" 
                                onClick={() => {
                                    handleDeleteComment(selectedComment.commentId, selectedComment.commentText);
                                    setDialogOpen(false);
                                }}
                            >
                                Delete Comment
                            </Button>
                        </>
                    )}
                </DialogActions>
            </Dialog>

            {/* Pagination */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mt={3}>
                <Typography variant="body2" color="textSecondary">
                    Showing {((page - 1) * rowsPerPage) + 1} to {Math.min(page * rowsPerPage, totalComments)} of {totalComments} comments
                </Typography>
                <Pagination
                    count={Math.ceil(totalComments / rowsPerPage)}
                    page={page}
                    onChange={handlePageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

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

            {/* Material UI Dialogs */}
            <DialogComponents />
        </Box>
    );
};

export default CommentComponent; 