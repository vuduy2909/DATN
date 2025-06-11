import React, { useState } from 'react';
import { 
  Button, 
  TextField, 
  Typography, 
  Container, 
  Grid, 
  Card,
  CardContent,
  Box,
  Snackbar,
  Alert,
  Divider
} from '@mui/material';
import { Save as SaveIcon, Cancel as CancelIcon } from '@mui/icons-material';
import { useAuth } from '../../AuthContext';
import { axiosInstance } from '../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const NewCustomerType = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [customerTypeName, setCustomerTypeName] = useState('');
  const [price, setPrice] = useState('');
  const [loading, setLoading] = useState(false);
  
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

  const onFinish = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!customerTypeName.trim()) {
      showSnackbar('Customer type name is required', 'error');
      return;
    }
    
    const priceValue = parseFloat(price);
    if (isNaN(priceValue) || priceValue < 0) {
      showSnackbar('Price must be a non-negative number', 'error');
      return;
    }

    try {
      setLoading(true);
      
      if (user) {
        const payload = {
          customerTypeName: customerTypeName.trim(),
          price: priceValue
        };
        
        console.log('Creating customer type:', payload);
        const response = await axiosInstance.post('/admin/customer-types', payload);
        console.log('Create response:', response.data);
        
        showSnackbar('Customer type created successfully! 🎉', 'success');
        
        // Navigate back after a short delay to show success message
        setTimeout(() => {
          navigate('/admin/customer-types');
        }, 1500);
      }
    } catch (error) {
      console.error('Error creating customer type:', error);
      const errorMsg = error.response?.data?.message || 
                      error.response?.data || 
                      error.message || 
                      'Unknown error';
      showSnackbar(`Error creating customer type: ${errorMsg}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Card elevation={3} sx={{ borderRadius: 3 }}>
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box textAlign="center" mb={3}>
            <Typography variant="h4" component="h1" fontWeight="bold" sx={{ color: '#d35255' }}>
              Create New Customer Type
            </Typography>
            <Typography variant="body1" color="textSecondary" mt={1}>
              Add a new customer type to categorize customers based on their spending
            </Typography>
          </Box>

          <Divider sx={{ mb: 3 }} />
          
          <form onSubmit={onFinish}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Customer Type Name"
                  variant="outlined"
                  value={customerTypeName}
                  onChange={(e) => setCustomerTypeName(e.target.value)}
                  placeholder="e.g., Platinum, Gold, Diamond"
                  required
                  InputProps={{
                    sx: { borderRadius: 2 }
                  }}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Threshold Amount (VNĐ)"
                  variant="outlined"
                  type="number"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="e.g., 1000000"
                  inputProps={{ min: 0, step: 1000 }}
                  helperText="Customers with total payments ≥ this amount will be assigned this type"
                  required
                  InputProps={{
                    sx: { borderRadius: 2 }
                  }}
                />
              </Grid>
              
              {/* Info Box */}
              <Grid item xs={12}>
                <Box 
                  sx={{ 
                    backgroundColor: '#e3f2fd', 
                    padding: 2, 
                    borderRadius: 2,
                    border: '1px solid #bbdefb'
                  }}
                >
                  <Typography variant="body2" color="primary" fontWeight="medium">
                    💡 How it works:
                  </Typography>
                  <Typography variant="body2" color="textSecondary" mt={1}>
                    • Customers will be automatically assigned to this type when their total payments reach the threshold amount<br/>
                    • The system will update customer types after each successful payment<br/>
                    • Lower threshold types will be upgraded to higher ones automatically
                  </Typography>
                </Box>
              </Grid>
              
              {/* Action Buttons */}
              <Grid item xs={12}>
                <Box display="flex" gap={2} justifyContent="flex-end">
                  <Button
                    variant="outlined"
                    size="large"
                    startIcon={<CancelIcon />}
                    onClick={() => navigate('/admin/customer-types')}
                    disabled={loading}
                    sx={{ borderRadius: 2, minWidth: 120 }}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    startIcon={<SaveIcon />}
                    disabled={loading}
                    sx={{ 
                        borderRadius: 2, 
                        minWidth: 160,
                        bgcolor: '#d35255',
                        '&:hover': {
                            bgcolor: '#b8464a'
                        }
                    }}
                  >
                    {loading ? 'Creating...' : 'Create Type'}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

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
    </Container>
  );
};

export default NewCustomerType; 