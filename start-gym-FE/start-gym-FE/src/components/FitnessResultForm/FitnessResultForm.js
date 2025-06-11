import React, { useState, useEffect } from 'react';
import { 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions, 
  Button, 
  TextField, 
  Typography, 
  Box,
  CircularProgress,
  Alert,
  Paper,
  Divider,
  Grid,
  Fade
} from '@mui/material';
import { CheckCircle, Info, ArrowUpward, ArrowDownward, HorizontalRule } from '@mui/icons-material';
import { updatePostCourseMetrics } from '../../service/collectionService';

// Function to calculate percentage difference
const calculatePercentageDiff = (before, after) => {
  if (!before || !after) return 0;
  return ((after - before) / before) * 100;
};

// Function to determine icon based on value change and whether decrease is good
const getChangeIcon = (diff, decreaseIsGood = true) => {
  if (diff === 0) return <HorizontalRule sx={{ color: '#757575' }} />;
  if (decreaseIsGood) {
    return diff < 0 ? 
      <ArrowDownward sx={{ color: '#2e7d32' }} /> : 
      <ArrowUpward sx={{ color: '#d32f2f' }} />;
  } else {
    return diff > 0 ? 
      <ArrowUpward sx={{ color: '#2e7d32' }} /> : 
      <ArrowDownward sx={{ color: '#d32f2f' }} />;
  }
};

const FitnessResultForm = ({ 
  open, 
  onClose, 
  userDataId, 
  initialData, 
  disabled = false,
  onSuccess 
}) => {
  const [formData, setFormData] = useState({
    weight_after: '',
    chest_circumference_after: '',
    waist_circumference_after: ''
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [comparisonData, setComparisonData] = useState(null);
  
  // Set initial data when dialog opens
  useEffect(() => {
    if (initialData) {
      setFormData({
        weight_after: initialData.weight || '',
        chest_circumference_after: initialData.chest_circumference || '',
        waist_circumference_after: initialData.waist_circumference || ''
      });
    }
    
    // Reset comparison data when dialog opens
    setComparisonData(null);
  }, [initialData, open]);
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value === '' ? '' : Number(value)
    }));
  };
  
  const handleSubmit = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    
    try {
      // Validate input
      if (!formData.weight_after && !formData.chest_circumference_after && !formData.waist_circumference_after) {
        setError('Vui lòng nhập ít nhất một chỉ số sau khi hoàn thành khóa học');
        setLoading(false);
        return;
      }
      
      // Call API to update metrics
      const response = await updatePostCourseMetrics(userDataId, formData);
      
      setSuccess('Cập nhật chỉ số thành công!');
      console.log('Post-course metrics updated:', response);
      
      // Prepare comparison data
      if (response && response.beforeData && response.afterData) {
        setComparisonData({
          before: response.beforeData,
          after: response.afterData,
          changes: response.changes || {}
        });
      }
      
      // Call the success callback with the response data
      if (onSuccess) {
        onSuccess(response);
      }
      
    } catch (err) {
      console.error('Error updating fitness metrics:', err);
      setError(err.response?.data || 'Có lỗi xảy ra khi cập nhật chỉ số. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ bgcolor: '#CF4044', color: 'white', fontWeight: 'bold' }}>
        Cập nhật chỉ số cơ thể sau khóa học
      </DialogTitle>
      
      <DialogContent sx={{ pt: 3, pb: 2 }}>
        {!disabled ? (
          <Paper elevation={1} sx={{ p: 2, mb: 3, bgcolor: '#e8f5e9', borderRadius: 2, display: 'flex', alignItems: 'center' }}>
            <CheckCircle sx={{ color: '#2e7d32', mr: 2, fontSize: 36 }} />
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#2e7d32' }}>
                Chúc mừng bạn đã hoàn thành khóa học!
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Hãy cập nhật chỉ số cơ thể để theo dõi hiệu quả tập luyện của bạn.
              </Typography>
            </Box>
          </Paper>
        ) : (
          <Paper elevation={1} sx={{ p: 2, mb: 3, bgcolor: '#fff3e0', borderRadius: 2, display: 'flex', alignItems: 'center' }}>
            <Info sx={{ color: '#ed6c02', mr: 2, fontSize: 36 }} />
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#ed6c02' }}>
                Bạn chưa hoàn thành khóa học này!
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Vui lòng hoàn thành khóa học để cập nhật chỉ số cơ thể và đánh giá hiệu quả tập luyện.
              </Typography>
            </Box>
          </Paper>
        )}
        
        {initialData && (
          <Box sx={{ mb: 3, p: 3, bgcolor: '#f7f7f7', borderRadius: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 2, fontSize: '1.1rem', color: '#333' }}>
              Chỉ số ban đầu:
            </Typography>
            <Grid container spacing={2}>
              {initialData.weight && (
                <Grid item xs={12} md={4}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'white', borderRadius: 2, border: '1px solid #eee' }}>
                    <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
                      Cân nặng
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#333' }}>
                      {initialData.weight} kg
                    </Typography>
                  </Box>
                </Grid>
              )}
              {initialData.chest_circumference && (
                <Grid item xs={12} md={4}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'white', borderRadius: 2, border: '1px solid #eee' }}>
                    <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
                      Vòng ngực
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#333' }}>
                      {initialData.chest_circumference} cm
                    </Typography>
                  </Box>
                </Grid>
              )}
              {initialData.waist_circumference && (
                <Grid item xs={12} md={4}>
                  <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'white', borderRadius: 2, border: '1px solid #eee' }}>
                    <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
                      Vòng eo
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#333' }}>
                      {initialData.waist_circumference} cm
                    </Typography>
                  </Box>
                </Grid>
              )}
            </Grid>
          </Box>
        )}
        
        {/* Comparison Results */}
        {comparisonData && (
          <Fade in={!!comparisonData}>
            <Box sx={{ mb: 3 }}>
              <Divider sx={{ my: 2 }}>
                <Typography variant="body1" sx={{ color: '#333', fontWeight: 'bold', px: 2 }}>KẾT QUẢ SO SÁNH</Typography>
              </Divider>
              
              <Grid container spacing={2} sx={{ mt: 1 }}>
                {/* Weight Comparison */}
                {comparisonData.before.weight !== undefined && comparisonData.after.weight_after !== undefined && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 3, bgcolor: '#f8f9fa', borderRadius: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                      <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>Cân nặng:</Typography>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Box sx={{ textAlign: 'center', flex: 1 }}>
                          <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Trước</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                            {comparisonData.before.weight} kg
                          </Typography>
                        </Box>
                        
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mx: 2 }}>
                          {getChangeIcon(comparisonData.changes.weight_change, true)}
                          <Typography 
                            variant="body1" 
                            sx={{ 
                              fontWeight: 'bold', 
                              my: 1,
                              color: comparisonData.changes.weight_change < 0 ? '#2e7d32' : '#d32f2f'
                            }}
                          >
                            {Math.abs(comparisonData.changes.weight_change).toFixed(1)} kg
                          </Typography>
                          <Typography variant="caption" sx={{ fontWeight: 'medium' }}>
                            ({Math.abs(calculatePercentageDiff(comparisonData.before.weight, comparisonData.after.weight_after)).toFixed(1)}%)
                          </Typography>
                        </Box>
                        
                        <Box sx={{ textAlign: 'center', flex: 1 }}>
                          <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Sau</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                            {comparisonData.after.weight_after} kg
                          </Typography>
                        </Box>
                      </Box>
                    </Paper>
                  </Grid>
                )}
                
                {/* Chest and Waist Comparison */}
                <Grid container item spacing={2} xs={12}>
                  {/* Chest Comparison */}
                  {comparisonData.before.chest_circumference !== undefined && comparisonData.after.chest_circumference_after !== undefined && (
                    <Grid item xs={12} sm={6}>
                      <Paper sx={{ p: 3, bgcolor: '#f8f9fa', borderRadius: 2, height: '100%', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>Vòng ngực:</Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                          <Box sx={{ display: 'flex', width: '100%', justifyContent: 'space-between', mb: 2 }}>
                            <Box sx={{ textAlign: 'center', flex: 1 }}>
                              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Trước</Typography>
                              <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                                {comparisonData.before.chest_circumference} cm
                              </Typography>
                            </Box>
                            <Box sx={{ textAlign: 'center', flex: 1 }}>
                              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Sau</Typography>
                              <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                                {comparisonData.after.chest_circumference_after} cm
                              </Typography>
                            </Box>
                          </Box>
                          
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            {getChangeIcon(comparisonData.changes.chest_change, false)}
                            <Typography 
                              variant="body1" 
                              sx={{ 
                                fontWeight: 'bold',
                                mx: 1,
                                color: comparisonData.changes.chest_change > 0 ? '#2e7d32' : '#d32f2f'
                              }}
                            >
                              {Math.abs(comparisonData.changes.chest_change).toFixed(1)} cm
                            </Typography>
                            <Typography variant="caption" sx={{ fontWeight: 'medium' }}>
                              ({Math.abs(calculatePercentageDiff(comparisonData.before.chest_circumference, comparisonData.after.chest_circumference_after)).toFixed(1)}%)
                            </Typography>
                          </Box>
                        </Box>
                      </Paper>
                    </Grid>
                  )}
                  
                  {/* Waist Comparison */}
                  {comparisonData.before.waist_circumference !== undefined && comparisonData.after.waist_circumference_after !== undefined && (
                    <Grid item xs={12} sm={6}>
                      <Paper sx={{ p: 3, bgcolor: '#f8f9fa', borderRadius: 2, height: '100%', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>Vòng eo:</Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                          <Box sx={{ display: 'flex', width: '100%', justifyContent: 'space-between', mb: 2 }}>
                            <Box sx={{ textAlign: 'center', flex: 1 }}>
                              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Trước</Typography>
                              <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                                {comparisonData.before.waist_circumference} cm
                              </Typography>
                            </Box>
                            <Box sx={{ textAlign: 'center', flex: 1 }}>
                              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Sau</Typography>
                              <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333' }}>
                                {comparisonData.after.waist_circumference_after} cm
                              </Typography>
                            </Box>
                          </Box>
                          
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            {getChangeIcon(comparisonData.changes.waist_change, true)}
                            <Typography 
                              variant="body1" 
                              sx={{ 
                                fontWeight: 'bold',
                                mx: 1,
                                color: comparisonData.changes.waist_change < 0 ? '#2e7d32' : '#d32f2f'
                              }}
                            >
                              {Math.abs(comparisonData.changes.waist_change).toFixed(1)} cm
                            </Typography>
                            <Typography variant="caption" sx={{ fontWeight: 'medium' }}>
                              ({Math.abs(calculatePercentageDiff(comparisonData.before.waist_circumference, comparisonData.after.waist_circumference_after)).toFixed(1)}%)
                            </Typography>
                          </Box>
                        </Box>
                      </Paper>
                    </Grid>
                  )}
                </Grid>
              </Grid>
              
              {comparisonData.evaluationReason && (
                <Alert severity={comparisonData.effective ? "success" : "warning"} sx={{ mt: 3, fontSize: '1rem' }}>
                  {comparisonData.evaluationReason}
                </Alert>
              )}
            </Box>
          </Fade>
        )}
        
        {/* Only show the form when comparison is not being displayed or success message is not set */}
        {(!comparisonData && !success) && (
          <>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Vui lòng nhập chỉ số mới sau khi hoàn thành khóa học để đánh giá hiệu quả tập luyện
            </Typography>
            
            <TextField
              fullWidth
              label="Cân nặng sau khi hoàn thành (kg)"
              name="weight_after"
              type="number"
              value={formData.weight_after}
              onChange={handleChange}
              margin="normal"
              disabled={disabled}
              inputProps={{ min: 30, max: 200, step: 0.1 }}
            />
            
            <TextField
              fullWidth
              label="Vòng ngực sau khi hoàn thành (cm)"
              name="chest_circumference_after"
              type="number"
              value={formData.chest_circumference_after}
              onChange={handleChange}
              margin="normal"
              disabled={disabled}
              inputProps={{ min: 50, max: 200, step: 0.1 }}
            />
            
            <TextField
              fullWidth
              label="Vòng eo sau khi hoàn thành (cm)"
              name="waist_circumference_after"
              type="number"
              value={formData.waist_circumference_after}
              onChange={handleChange}
              margin="normal"
              disabled={disabled}
              inputProps={{ min: 40, max: 200, step: 0.1 }}
            />
          </>
        )}
        
        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
        
        {success && !comparisonData && (
          <Alert severity="success" sx={{ mt: 2 }}>
            {success}
          </Alert>
        )}
      </DialogContent>
      
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          {comparisonData ? 'Đóng' : 'Hủy'}
        </Button>
        {!comparisonData && (
          <Button 
            onClick={handleSubmit} 
            variant="contained" 
            color="primary"
            disabled={disabled || loading}
            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : null}
          >
            {loading ? 'Đang cập nhật...' : 'Cập nhật'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default FitnessResultForm; 