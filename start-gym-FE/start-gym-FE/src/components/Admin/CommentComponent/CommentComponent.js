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
    Tab
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Visibility as VisibilityIcon,
    Flag as FlagIcon,
    Comment as CommentIcon,
    Warning as WarningIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Refresh as RefreshIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';

const CommentComponent = () => {
    const [comments, setComments] = useState([]);
    const [reportedComments, setReportedComments] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedTab, setSelectedTab] = useState(0);
    const [selectedComment, setSelectedComment] = useState(null);
    const [dialogOpen, setDialogOpen] = useState(false);
    
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

    // Fetch all comments
    const fetchComments = async () => {
        setIsLoading(true);
        
        try {
            console.log('Fetching comments...');
            const response = await axiosInstance.get('/admin/comments');
            console.log('Comments response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setComments(response.data);
                console.log('Comments loaded:', response.data.length);
            } else {
                setComments([]);
                showSnackbar('No comments data received', 'warning');
            }
        } catch (error) {
            console.error('Error fetching comments:', error);
            showSnackbar(`Error loading comments: ${error.message}`, 'error');
            setComments([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Fetch reported comments  
    const fetchReportedComments = async () => {
        setIsLoading(true);
        
        try {
            console.log('Fetching reported comments...');
            const response = await axiosInstance.get('/admin/comments/reported');
            console.log('Reported comments response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setReportedComments(response.data);
                console.log('Reported comments loaded:', response.data.length);
            } else {
                setReportedComments([]);
                showSnackbar('No reported comments found', 'info');
            }
        } catch (error) {
            console.error('Error fetching reported comments:', error);
            showSnackbar(`Error loading reported comments: ${error.message}`, 'error');
            setReportedComments([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Delete comment
    const handleDeleteComment = async (commentId, commentText) => {
        if (window.confirm(`Are you sure you want to delete this comment: "${commentText?.substring(0, 50)}..."?`)) {
            try {
                console.log('Deleting comment:', commentId);
                await axiosInstance.delete(`/admin/comments/${commentId}`);
                
                showSnackbar('Comment deleted successfully! 🗑️', 'success');
                
                // Refresh both lists
                if (selectedTab === 0) {
                    fetchComments();
                } else {
                    fetchReportedComments();
                }
                
            } catch (error) {
                console.error('Error deleting comment:', error);
                const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
                showSnackbar(`Error deleting comment: ${errorMsg}`, 'error');
            }
        }
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

    useEffect(() => {
        if (selectedTab === 0) {
            fetchComments();
        } else {
            fetchReportedComments();
        }
    }, [selectedTab]);

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
                            <Badge badgeContent={reportedComments.length} color="error">
                                <FlagIcon />
                            </Badge>
                        } 
                        label={`Reported Comments`} 
                        iconPosition="start"
                    />
                </Tabs>
            </Box>

            {/* Stats Summary */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="primary">
                        Total Comments: {comments.length}
                    </Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="warning.main">
                        Reported: {reportedComments.length}
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
                            currentData.map((comment) => (
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

export default CommentComponent; 