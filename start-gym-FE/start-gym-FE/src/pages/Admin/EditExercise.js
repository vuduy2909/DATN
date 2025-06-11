import React, { useState, useEffect } from 'react';
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  LinearProgress
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  FitnessCenter as FitnessCenterIcon,
  CloudUpload as CloudUploadIcon,
  VideoFile as VideoFileIcon,
  Preview as PreviewIcon
} from '@mui/icons-material';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';
import { uploadExerciseVideo, validateFile, getBodyParts } from '../../utils/firebaseUpload';
import { useNavigate, useParams } from 'react-router-dom';

const EditExercise = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [fileUploading, setFileUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  
  // Video preview states
  const [selectedVideoFile, setSelectedVideoFile] = useState(null);
  const [videoPreviewUrl, setVideoPreviewUrl] = useState('');
  const [previewDialogOpen, setPreviewDialogOpen] = useState(false);
  const [pendingVideoUrl, setPendingVideoUrl] = useState('');

  // Form state
  const [formData, setFormData] = useState({
    bodyPart: '',
    equipment: '',
    exerciseDescription: '',
    exerciseName: '',
    instructions: '',
    target: '',
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

  useEffect(() => {
    const fetchExerciseData = async () => {
      if (!user) return;
      
      setFetchLoading(true);
      try {
        // Try the admin endpoint first, then fallback to regular endpoint
        let response;
        try {
          response = await request.get(`/admin/exercise/${id}`);
        } catch (adminError) {
          console.log('Admin endpoint failed, trying fallback:', adminError);
          response = await request.get(`/exercise/${id}`);
        }
        
        setFormData({
          bodyPart: response.bodyPart || '',
          equipment: response.equipment || '',
          exerciseDescription: response.exerciseDescription || '',
          exerciseName: response.exerciseName || '',
          instructions: response.instructions || '',
          target: response.target || '',
          videoUrl: response.videoUrl || ''
        });
      } catch (error) {
        console.error('Error fetching exercise information:', error);
        showSnackbar('Failed to load exercise data', 'error');
      } finally {
        setFetchLoading(false);
      }
    };

    fetchExerciseData();
  }, [id, user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleVideoUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('video/')) {
      showSnackbar('Please select a video file', 'error');
      return;
    }

    // Validate file size (500MB limit)
    if (file.size > 500 * 1024 * 1024) {
      showSnackbar('Video file must be less than 500MB', 'error');
      return;
    }

    if (!formData.exerciseName.trim()) {
      showSnackbar('Please enter exercise name before uploading video', 'error');
      return;
    }
    
    if (!formData.bodyPart.trim()) {
      showSnackbar('Please select body part before uploading video', 'error');
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
      setFileUploading(true);
      setUploadProgress(0);
      showSnackbar('Uploading video to Firebase...', 'info');
      
      // Simulate upload progress (since uploadExerciseVideo doesn't return progress)
      const progressInterval = setInterval(() => {
        setUploadProgress(prev => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 200);

      // Upload to Firebase using exercise name and body part
      const downloadURL = await uploadExerciseVideo(selectedVideoFile, formData.exerciseName, formData.bodyPart);
      
      clearInterval(progressInterval);
      setUploadProgress(100);
      
      setFileUploading(false);
      return downloadURL;
    } catch (error) {
      setFileUploading(false);
      setUploadProgress(0);
      throw error;
    }
  };

  const handleConfirmUpload = async () => {
    try {
      const uploadedUrl = await uploadVideoToFirebase();
      setPendingVideoUrl(uploadedUrl);
      setPreviewDialogOpen(false);
      showSnackbar('Video uploaded successfully! Click Update to save changes.', 'success');
      
      // Cleanup preview URL
      if (videoPreviewUrl) {
        URL.revokeObjectURL(videoPreviewUrl);
        setVideoPreviewUrl('');
      }
    } catch (error) {
      console.error('Upload error:', error);
      showSnackbar(`Upload failed: ${error.message}`, 'error');
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.exerciseName.trim()) {
      showSnackbar('Exercise name is required', 'error');
      return;
    }
    if (!formData.exerciseDescription.trim()) {
      showSnackbar('Exercise description is required', 'error');
      return;
    }
    if (!formData.target.trim()) {
      showSnackbar('Target is required', 'error');
      return;
    }
    if (!formData.bodyPart.trim()) {
      showSnackbar('Body part is required', 'error');
      return;
    }

    if (!user) {
      showSnackbar('Please login to edit exercise', 'error');
      return;
    }

    setLoading(true);
    try {
      // Use pending video URL if there's a new video uploaded
      const submitData = {
        ...formData,
        videoUrl: pendingVideoUrl || formData.videoUrl
      };
      
      console.log('Updating exercise:', submitData);
      
      // Try the admin endpoint first, then fallback to regular endpoint
      let response;
      try {
        response = await request.put(`/admin/exercise/${id}/edit`, submitData);
      } catch (adminError) {
        console.log('Admin endpoint failed, trying fallback:', adminError);
        response = await request.put(`/exercise/edit/${id}`, submitData);
      }
      
      console.log('Exercise updated:', response);
      
      if (response.message === 'The exercise has successfully edited!!' || response) {
        showSnackbar('Exercise updated successfully! 🎉', 'success');
        
        // Redirect to admin after a delay
        setTimeout(() => {
          navigate('/admin/exercises');
        }, 2000);
      }
    } catch (error) {
      console.error('Error updating exercise:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      showSnackbar(`Error updating exercise: ${errorMsg}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/exercises');
  };

  if (fetchLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh" flexDirection="column">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>Loading exercise data...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <IconButton onClick={handleCancel} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" fontWeight="bold">
          Edit Exercise
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

                {/* Exercise Description */}
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Exercise Description"
                    name="exerciseDescription"
                    value={formData.exerciseDescription}
                    onChange={handleInputChange}
                    required
                    multiline
                    rows={4}
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
                    select
                    fullWidth
                    label="Body Part"
                    name="bodyPart"
                    value={formData.bodyPart}
                    onChange={handleInputChange}
                    required
                    variant="outlined"
                    SelectProps={{
                      native: true,
                    }}
                  >
                    <option value="">Select Body Part</option>
                    {getBodyParts().map((bodyPart) => (
                      <option key={bodyPart} value={bodyPart}>
                        {bodyPart}
                      </option>
                    ))}
                  </TextField>
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
                      disabled={fileUploading}
                    />
                    <label htmlFor="video-upload-button">
                      <Button
                        variant="outlined"
                        component="span"
                        startIcon={fileUploading ? <CircularProgress size={20} /> : <CloudUploadIcon />}
                        disabled={fileUploading || loading}
                        fullWidth
                        sx={{ mb: 1 }}
                      >
                        {fileUploading ? `Uploading... ${Math.round(uploadProgress)}%` : 'Upload New Video'}
                      </Button>
                    </label>
                    
                    {pendingVideoUrl && (
                      <Box mt={1} mb={1} display="flex" alignItems="center" gap={1}>
                        <VideoFileIcon color="success" />
                        <Typography variant="body2" color="success.main">
                          New video uploaded and ready to save
                        </Typography>
                      </Box>
                    )}
                    
                    {formData.videoUrl && (
                      <TextField
                        fullWidth
                        label="Current Video URL"
                        value={formData.videoUrl}
                        variant="outlined"
                        disabled
                        sx={{ mt: 1 }}
                      />
                    )}
                  </Box>
                </Grid>

                {/* Upload Progress */}
                {fileUploading && (
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
                      {loading ? 'Updating...' : 'Update Exercise'}
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
              
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                {formData.exerciseName || 'Exercise Name'}
              </Typography>
              
              <Typography variant="body2" color="textSecondary" gutterBottom>
                {formData.exerciseDescription || 'Exercise description will appear here...'}
              </Typography>

              {(formData.videoUrl || pendingVideoUrl) && (
                <Box mt={2} mb={2}>
                  <video 
                    src={pendingVideoUrl || formData.videoUrl} 
                    style={{ 
                      width: '100%', 
                      height: 150, 
                      borderRadius: 8,
                      backgroundColor: '#f5f5f5'
                    }}
                    controls
                    onError={(e) => {
                      e.target.style.display = 'none';
                    }}
                  />
                  {pendingVideoUrl && (
                    <Typography variant="caption" color="success.main" display="block" sx={{ mt: 1 }}>
                      ✅ Preview of new video (will be saved on update)
                    </Typography>
                  )}
                </Box>
              )}

              <Box>
                <Typography variant="body2">
                  <strong>Target:</strong> {formData.target || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Body Part:</strong> {formData.bodyPart || 'N/A'}
                </Typography>
                <Typography variant="body2">
                  <strong>Equipment:</strong> {formData.equipment || 'Body weight'}
                </Typography>
                {formData.instructions && (
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    <strong>Instructions:</strong> {formData.instructions.substring(0, 100)}...
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Video Preview Dialog */}
      <Dialog
        open={previewDialogOpen}
        onClose={handleCancelUpload}
        maxWidth="md"
        fullWidth
        PaperProps={{
          style: {
            borderRadius: '12px',
            padding: '8px'
          }
        }}
      >
        <DialogTitle sx={{ textAlign: 'center', paddingBottom: 1 }}>
          🎥 Video Preview
        </DialogTitle>
        <DialogContent sx={{ paddingY: 2 }}>
          {selectedVideoFile && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Preview Before Upload
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
              {fileUploading && (
                <Box mt={2}>
                  <Typography variant="body2" gutterBottom>
                    Uploading to Firebase: {uploadProgress.toFixed(1)}%
                  </Typography>
                  <LinearProgress variant="determinate" value={uploadProgress} />
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'center', gap: 1, paddingBottom: 2 }}>
          <Button 
            onClick={handleCancelUpload}
            variant="outlined"
            sx={{ borderRadius: '8px', minWidth: '100px' }}
            disabled={fileUploading}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleConfirmUpload}
            variant="contained"
            color="primary"
            sx={{ borderRadius: '8px', minWidth: '100px' }}
            disabled={fileUploading}
            startIcon={fileUploading ? <CircularProgress size={20} color="inherit" /> : <CloudUploadIcon />}
          >
            {fileUploading ? 'Uploading...' : 'Upload to Firebase'}
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

export default EditExercise;