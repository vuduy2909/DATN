import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Snackbar,
  Alert,
  CircularProgress,
  Box,
  Typography,
  Avatar
} from '@mui/material';
import {
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  QuestionMark as QuestionMarkIcon
} from '@mui/icons-material';

// Confirmation Dialog Component
export const ConfirmDialog = ({ 
  open, 
  title, 
  message, 
  onConfirm, 
  onCancel, 
  confirmText = "Confirm", 
  cancelText = "Cancel",
  severity = "warning" // warning, error, info
}) => {
  const getIcon = () => {
    switch (severity) {
      case 'error':
        return <ErrorIcon color="error" sx={{ fontSize: 40 }} />;
      case 'warning':
        return <WarningIcon color="warning" sx={{ fontSize: 40 }} />;
      case 'info':
        return <InfoIcon color="info" sx={{ fontSize: 40 }} />;
      default:
        return <QuestionMarkIcon color="primary" sx={{ fontSize: 40 }} />;
    }
  };

  const getConfirmButtonColor = () => {
    switch (severity) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      default:
        return 'primary';
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-description"
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle id="confirm-dialog-title">
        <Box display="flex" alignItems="center" gap={2}>
          {getIcon()}
          <Typography variant="h6">{title}</Typography>
        </Box>
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="confirm-dialog-description">
          {message}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} color="inherit">
          {cancelText}
        </Button>
        <Button 
          onClick={onConfirm} 
          color={getConfirmButtonColor()}
          variant="contained"
          autoFocus
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Alert Dialog Component
export const AlertDialog = ({ 
  open, 
  title, 
  message, 
  onClose, 
  severity = "info", // success, error, warning, info
  closeText = "OK"
}) => {
  const getIcon = () => {
    switch (severity) {
      case 'success':
        return <CheckCircleIcon color="success" sx={{ fontSize: 40 }} />;
      case 'error':
        return <ErrorIcon color="error" sx={{ fontSize: 40 }} />;
      case 'warning':
        return <WarningIcon color="warning" sx={{ fontSize: 40 }} />;
      case 'info':
        return <InfoIcon color="info" sx={{ fontSize: 40 }} />;
      default:
        return <InfoIcon color="info" sx={{ fontSize: 40 }} />;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="alert-dialog-title"
      aria-describedby="alert-dialog-description"
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle id="alert-dialog-title">
        <Box display="flex" alignItems="center" gap={2}>
          {getIcon()}
          <Typography variant="h6">{title}</Typography>
        </Box>
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="alert-dialog-description">
          {message}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button 
          onClick={onClose} 
          color="primary"
          variant="contained"
          autoFocus
        >
          {closeText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Loading Dialog Component
export const LoadingDialog = ({ 
  open, 
  title = "Processing...", 
  message = "Please wait while we process your request."
}) => {
  return (
    <Dialog
      open={open}
      aria-labelledby="loading-dialog-title"
      aria-describedby="loading-dialog-description"
      maxWidth="sm"
      fullWidth
      disableEscapeKeyDown
    >
      <DialogTitle id="loading-dialog-title">
        <Box display="flex" alignItems="center" gap={2}>
          <CircularProgress size={24} />
          <Typography variant="h6">{title}</Typography>
        </Box>
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="loading-dialog-description">
          {message}
        </DialogContentText>
      </DialogContent>
    </Dialog>
  );
};

// Toast Notification Component (replacement for simple alerts)
export const ToastNotification = ({ 
  open, 
  message, 
  severity = "success", 
  onClose, 
  autoHideDuration = 6000,
  anchorOrigin = { vertical: 'top', horizontal: 'right' }
}) => {
  return (
    <Snackbar
      open={open}
      autoHideDuration={autoHideDuration}
      onClose={onClose}
      anchorOrigin={anchorOrigin}
    >
      <Alert 
        onClose={onClose} 
        severity={severity} 
        variant="filled"
        sx={{ width: '100%' }}
      >
        {message}
      </Alert>
    </Snackbar>
  );
};

// Preview Dialog Component for file uploads
export const PreviewDialog = ({ 
  open, 
  title, 
  onClose, 
  onConfirm, 
  previewContent,
  confirmText = "Upload",
  cancelText = "Cancel",
  loading = false
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="preview-dialog-title"
      maxWidth="md"
      fullWidth
    >
      <DialogTitle id="preview-dialog-title">
        <Typography variant="h6">{title}</Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 1 }}>
          {previewContent}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          {cancelText}
        </Button>
        <Button 
          onClick={onConfirm} 
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={18} /> : null}
        >
          {loading ? 'Uploading...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Custom hook for using dialogs
export const useDialogs = () => {
  const [confirmDialog, setConfirmDialog] = React.useState({
    open: false,
    title: '',
    message: '',
    onConfirm: () => {},
    severity: 'warning'
  });

  const [alertDialog, setAlertDialog] = React.useState({
    open: false,
    title: '',
    message: '',
    severity: 'info'
  });

  const [loadingDialog, setLoadingDialog] = React.useState({
    open: false,
    title: 'Processing...',
    message: 'Please wait...'
  });

  const [toast, setToast] = React.useState({
    open: false,
    message: '',
    severity: 'success'
  });

  const showConfirm = (title, message, onConfirm, severity = 'warning') => {
    setConfirmDialog({
      open: true,
      title,
      message,
      onConfirm: () => {
        onConfirm();
        setConfirmDialog(prev => ({ ...prev, open: false }));
      },
      severity
    });
  };

  const showAlert = (title, message, severity = 'info') => {
    setAlertDialog({
      open: true,
      title,
      message,
      severity
    });
  };

  const showLoading = (title = 'Processing...', message = 'Please wait...') => {
    setLoadingDialog({
      open: true,
      title,
      message
    });
  };

  const hideLoading = () => {
    setLoadingDialog(prev => ({ ...prev, open: false }));
  };

  const showToast = (message, severity = 'success') => {
    setToast({
      open: true,
      message,
      severity
    });
  };

  const DialogComponents = () => (
    <>
      <ConfirmDialog
        open={confirmDialog.open}
        title={confirmDialog.title}
        message={confirmDialog.message}
        onConfirm={confirmDialog.onConfirm}
        onCancel={() => setConfirmDialog(prev => ({ ...prev, open: false }))}
        severity={confirmDialog.severity}
      />
      
      <AlertDialog
        open={alertDialog.open}
        title={alertDialog.title}
        message={alertDialog.message}
        onClose={() => setAlertDialog(prev => ({ ...prev, open: false }))}
        severity={alertDialog.severity}
      />
      
      <LoadingDialog
        open={loadingDialog.open}
        title={loadingDialog.title}
        message={loadingDialog.message}
      />
      
      <ToastNotification
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={() => setToast(prev => ({ ...prev, open: false }))}
      />
    </>
  );

  return {
    showConfirm,
    showAlert,
    showLoading,
    hideLoading,
    showToast,
    DialogComponents
  };
}; 