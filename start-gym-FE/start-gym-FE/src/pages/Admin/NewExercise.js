import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Snackbar,
  Alert,
  CircularProgress,
  Grid,
  Card,
  CardContent,
  Avatar,
  IconButton,
  LinearProgress
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  FitnessCenter as FitnessCenterIcon,
  CloudUpload as CloudUploadIcon,
  VideoFile as VideoFileIcon
} from '@mui/icons-material';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { storage } from '../../config/firebaseConfig';
import { useDialogs, PreviewDialog } from '../../components/MaterialUIDialogs';

const NewExercise = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { showAlert, showConfirm, showLoading, hideLoading, showToast, DialogComponents } = useDialogs();
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [selectedVideoFile, setSelectedVideoFile] = useState(null);
  const [videoPreviewUrl, setVideoPreviewUrl] = useState('');
  const [previewDialogOpen, setPreviewDialogOpen] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    exerciseName: '',
    exerciseDescription: '',
    bodyPart: '',
    target: '',
    equipment: '',
    instructions: '',
    videoUrl: ''
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

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') return;
    setSnackbar({ ...snackbar, open: false });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validation
    if (!formData.exerciseName.trim()) {
      showAlert('Validation Error', 'Exercise name is required', 'error');
      return;
    }

    if (!formData.bodyPart.trim()) {
      showAlert('Validation Error', 'Body part is required', 'error');
      return;
    }

    try {
      showLoading('Creating Exercise', 'Please wait while we create the exercise...');

      const response = await request.post('/exercise/new', formData);
      
      hideLoading();
      showAlert('Success', 'Exercise created successfully!', 'success');
      
      // Navigate back to exercises list after a short delay
      setTimeout(() => {
        navigate('/admin/exercises');
      }, 1500);

    } catch (error) {
      hideLoading();
      console.error('Error creating exercise:', error);
      const errorMsg = error.response?.data?.message || 'Failed to create exercise';
      showAlert('Creation Failed', errorMsg, 'error');
    }
  };

  const handleVideoUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('video/')) {
      showAlert('Invalid File', 'Please select a video file', 'error');
      return;
    }

    // Validate file size (1000MB limit)
    if (file.size > 1000 * 1024 * 1024) {
      showAlert('File Too Large', 'Video file must be less than 1GB', 'error');
      return;
    }

    setSelectedVideoFile(file);
    
    // Create preview URL
    const previewUrl = URL.createObjectURL(file);
    setVideoPreviewUrl(previewUrl);
    
    // Show preview dialog
    setPreviewDialogOpen(true);
  };

  const uploadVideoToFirebase = async () => {
    if (!selectedVideoFile) return null;

    try {
      setUploading(true);
      
      // Create storage reference
      const timestamp = Date.now();
      const fileName = `exercises/videos/${timestamp}_${selectedVideoFile.name}`;
      const storageRef = ref(storage, fileName);

      // Upload file with progress tracking
      const uploadTask = uploadBytesResumable(storageRef, selectedVideoFile);

      return new Promise((resolve, reject) => {
        uploadTask.on(
          'state_changed',
          (snapshot) => {
            const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
            setUploadProgress(progress);
          },
          (error) => {
            console.error('Upload error:', error);
            setUploading(false);
            reject(error);
          },
          async () => {
            try {
              const downloadURL = await getDownloadURL(uploadTask.snapshot.ref);
              setUploading(false);
              resolve(downloadURL);
            } catch (error) {
              setUploading(false);
              reject(error);
            }
          }
        );
      });
    } catch (error) {
      setUploading(false);
      throw error;
    }
  };

  const handleConfirmUpload = async () => {
    try {
      const uploadedUrl = await uploadVideoToFirebase();
      setFormData(prev => ({ ...prev, videoUrl: uploadedUrl }));
      setPreviewDialogOpen(false);
      showToast('Video uploaded successfully!', 'success');
      
      // Cleanup preview URL
      if (videoPreviewUrl) {
        URL.revokeObjectURL(videoPreviewUrl);
        setVideoPreviewUrl('');
      }
    } catch (error) {
      console.error('Upload error:', error);
      showAlert('Upload Failed', 'Failed to upload video. Please try again.', 'error');
    }
  };

  const handleCancelUpload = () => {
    setPreviewDialogOpen(false);
    setSelectedVideoFile(null);
    if (videoPreviewUrl) {
      URL.revokeObjectURL(videoPreviewUrl);
      setVideoPreviewUrl('');
    }
  };

  const handleCancel = () => {
    navigate('/admin/exercises');
  };

  const renderPreviewContent = () => {
    if (!selectedVideoFile) return null;

    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          Video Preview
        </Typography>
        <Box mb={2}>
          <Typography variant="body2" color="textSecondary">
            File: {selectedVideoFile.name}
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Size: {(selectedVideoFile.size / (1024 * 1024)).toFixed(2)} MB
          </Typography>
        </Box>
        {videoPreviewUrl && (
          <video
            src={videoPreviewUrl}
            controls
            style={{ width: '100%', maxHeight: '400px' }}
          />
        )}
        {uploading && (
          <Box mt={2}>
            <Typography variant="body2" gutterBottom>
              Uploading: {uploadProgress.toFixed(1)}%
            </Typography>
            <LinearProgress variant="determinate" value={uploadProgress} />
          </Box>
        )}
      </Box>
    );
  };

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <IconButton onClick={handleCancel} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" fontWeight="bold">
          Create New Exercise
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Form Section */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                {/* Exercise Name */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Exercise Name"
                    name="exerciseName"
                    value={formData.exerciseName}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                  />
                </Grid>

                {/* Description */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Description"
                    name="exerciseDescription"
                    value={formData.exerciseDescription}
                    onChange={handleInputChange}
                    multiline
                    rows={3}
                    variant="outlined"
                  />
                </Grid>

                {/* Target and Body Part */}
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Target Muscle"
                    name="target"
                    value={formData.target}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="e.g. biceps, chest, legs"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Body Part"
                    name="bodyPart"
                    value={formData.bodyPart}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    placeholder="e.g. upper arms, chest, lower body"
                  />
                </Grid>

                {/* Equipment */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Equipment"
                    name="equipment"
                    value={formData.equipment}
                    onChange={handleInputChange}
                    variant="outlined"
                    placeholder="e.g. dumbbell, barbell, body weight"
                  />
                </Grid>

                {/* Instructions */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Instructions"
                    name="instructions"
                    value={formData.instructions}
                    onChange={handleInputChange}
                    multiline
                    rows={3}
                    variant="outlined"
                    placeholder="Step-by-step instructions for the exercise"
                  />
                </Grid>

                {/* Video Upload */}
                <Grid item xs={12}>
                  <Box>
                    <Typography variant="subtitle2" gutterBottom>
                      Exercise Video
                    </Typography>
                    <input
                      accept="video/*"
                      style={{ display: 'none' }}
                      id="video-upload-button"
                      type="file"
                      onChange={handleVideoUpload}
                      disabled={uploading}
                    />
                    <label htmlFor="video-upload-button">
                      <Button
                        variant="outlined"
                        component="span"
                        startIcon={uploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        disabled={uploading}
                        fullWidth
                        sx={{ mb: 1 }}
                      >
                        {uploading ? `Uploading... ${Math.round(uploadProgress)}%` : 'Upload Video'}
                      </Button>
                    </label>
                    {formData.videoUrl && (
                      <Box mt={1} display="flex" alignItems="center" gap={1}>
                        <VideoFileIcon color="success" />
                        <Typography variant="body2" color="success.main">
                          Video uploaded successfully
                        </Typography>
                      </Box>
                    )}
                    
                    {formData.videoUrl && (
                      <TextField
                        fullWidth
                        label="Video URL"
                        value={formData.videoUrl}
                        variant="outlined"
                        disabled
                        sx={{ mt: 2 }}
                      />
                    )}
                  </Box>
                </Grid>

                {/* Video Progress */}
                {uploading && (
                  <Grid item xs={12}>
                    <LinearProgress variant="determinate" value={uploadProgress} />
                    <Typography variant="caption" color="textSecondary" align="center">
                      Uploading video... {Math.round(uploadProgress)}%
                    </Typography>
                  </Grid>
                )}

                {/* Action Buttons */}
                <Grid item xs={12}>
                  <Box display="flex" gap={2} justifyContent="flex-end">
                    <Button
                      variant="outlined"
                      startIcon={<CancelIcon />}
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
                      {loading ? 'Creating...' : 'Create Exercise'}
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
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar sx={{ bgcolor: '#d35255', mr: 2 }}>
                  <FitnessCenterIcon />
                </Avatar>
                <Typography variant="h6">Exercise Preview</Typography>
              </Box>
              
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">
                  {formData.exerciseName || 'Exercise Name'}
                </Typography>
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  {formData.exerciseDescription || 'Exercise description will appear here...'}
                </Typography>
                
                <Typography variant="body2" mt={2}>
                  <strong>Body Part:</strong> {formData.bodyPart || 'Not specified'}
                </Typography>
                <Typography variant="body2">
                  <strong>Target:</strong> {formData.target || 'Not specified'}
                </Typography>
                <Typography variant="body2">
                  <strong>Equipment:</strong> {formData.equipment || 'Not specified'}
                </Typography>
                
                {formData.instructions && (
                  <Typography variant="body2" mt={2}>
                    <strong>Instructions:</strong> {formData.instructions.substring(0, 100)}
                    {formData.instructions.length > 100 && '...'}
                  </Typography>
                )}
                
                {formData.videoUrl && (
                  <Box mt={2}>
                    <Typography variant="body2" mb={1}>
                      <strong>Video:</strong>
                    </Typography>
                    <video
                      src={formData.videoUrl}
                      controls
                      style={{ width: '100%', maxHeight: '150px' }}
                    />
                  </Box>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Preview Dialog */}
      <PreviewDialog
        open={previewDialogOpen}
        title="Preview Video Upload"
        onClose={handleCancelUpload}
        onConfirm={handleConfirmUpload}
        previewContent={renderPreviewContent()}
        loading={uploading}
        confirmText="Upload to Firebase"
        cancelText="Cancel"
      />

      <DialogComponents />

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

export default NewExercise;
