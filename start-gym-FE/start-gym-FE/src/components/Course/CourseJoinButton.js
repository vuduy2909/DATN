import React, { useState } from 'react';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';
import { 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions, 
  Button, 
  Typography,
  Snackbar,
  Alert,
  CircularProgress
} from '@mui/material';
import './CourseJoinButton.css';

const CourseJoinButton = ({ courseId, courseName, onJoinSuccess }) => {
  const [isJoining, setIsJoining] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });
  const { user } = useAuth();

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

  const handleJoinClick = () => {
    if (!user) {
      showSnackbar('Vui lòng đăng nhập để tham gia khóa học', 'error');
      return;
    }
    setShowConfirmDialog(true);
  };

  const handleConfirmJoin = async () => {
    setIsJoining(true);
    setShowConfirmDialog(false);
    
    try {
      const response = await request.post(`/customer-course/join/${courseId}`);
      
      if (response.success) {
        showSnackbar('Tham gia khóa học thành công!', 'success');
        
        // Callback để update UI parent component
        if (onJoinSuccess) {
          onJoinSuccess(courseId);
        }
      } else {
        showSnackbar(response.message || 'Có lỗi xảy ra khi tham gia khóa học', 'error');
      }
    } catch (error) {
      console.error('CourseJoinButton error:', error);
      
      if (error.response?.status === 409) {
        showSnackbar('Bạn đã tham gia khóa học này rồi!', 'warning');
      } else if (error.response?.status === 401) {
        const errorMessage = error.response?.data?.message || 'Phiên đăng nhập đã hết hạn';
        showSnackbar(`Lỗi xác thực: ${errorMessage}. Vui lòng đăng nhập lại.`, 'error');
      } else {
        const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.';
        showSnackbar(`Lỗi: ${errorMessage}`, 'error');
      }
    } finally {
      setIsJoining(false);
    }
  };

  const handleCancelJoin = () => {
    setShowConfirmDialog(false);
  };

  return (
    <div className="course-join-container">
      <button 
        className="join-course-btn"
        onClick={handleJoinClick}
        disabled={isJoining}
      >
        {isJoining ? (
          <div className="loading-spinner">
            <CircularProgress size={16} color="inherit" />
            Đang tham gia...
          </div>
        ) : (
          <>
            <span className="join-icon">➕</span>
            <span>Tham gia<br/>khóa học</span>
          </>
        )}
      </button>

      {/* Confirmation Dialog */}
      <Dialog 
        open={showConfirmDialog} 
        onClose={handleCancelJoin}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Xác nhận tham gia khóa học</DialogTitle>
        <DialogContent>
          <Typography variant="body1" gutterBottom>
            Bạn có muốn tham gia khóa học <strong>"{courseName}"</strong> không?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Sau khi tham gia, bạn sẽ có thể truy cập tất cả tài liệu và bài tập của khóa học.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelJoin}>Hủy</Button>
          <Button 
            variant="contained" 
            onClick={handleConfirmJoin}
            disabled={isJoining}
            sx={{
              backgroundColor: '#4CAF50',
              '&:hover': {
                backgroundColor: '#45a049'
              }
            }}
          >
            {isJoining ? (
              <>
                <CircularProgress size={16} color="inherit" sx={{ mr: 1 }} />
                Đang tham gia...
              </>
            ) : (
              'Có, tham gia'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
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
    </div>
  );
};

export default CourseJoinButton; 