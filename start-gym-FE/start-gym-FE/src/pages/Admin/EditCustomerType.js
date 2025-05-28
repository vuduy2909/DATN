import React, { useState, useEffect } from 'react';
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
  Divider,
  Chip,
  CircularProgress
} from '@mui/material';
import { 
  Save as SaveIcon, 
  Cancel as CancelIcon,
  Warning as WarningIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import { useAuth } from '../../AuthContext';
import { axiosInstance } from '../../utils/axiosInstance';
import { useNavigate, useParams } from 'react-router-dom';

const EditCustomerType = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [customerTypeName, setCustomerTypeName] = useState('');
  const [price, setPrice] = useState('');
  const [originalPrice, setOriginalPrice] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
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

  useEffect(() => {
    const fetchCustomerTypeData = async () => {
      try {
        setLoading(true);
        
        // Fetch customer type details
        const response = await axiosInstance.get(`/admin/customer-types/${id}`);
        console.log('Customer type data:', response.data);
        
        const customerType = response.data.customerType || response.data;
        setCustomerTypeName(customerType.customerTypeName);
        setPrice(customerType.price.toString());
        setOriginalPrice(customerType.price);
        
      } catch (error) {
        console.error('Error fetching customer type information:', error);
        showSnackbar('Failed to load customer type information: ' + (error.message || 'Unknown error'), 'error');
      } finally {
        setLoading(false);
      }
    };

    if (user && id) {
      fetchCustomerTypeData();
    }
  }, [id, user]);

  const formatCurrency = (amount) => {
    if (typeof amount !== 'number') return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
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

    // Validation: New price cannot be higher than original price
    if (priceValue > originalPrice) {
      showSnackbar(`New price (${formatCurrency(priceValue)}) cannot be higher than current price (${formatCurrency(originalPrice)}). This would cause customers to lose their current type.`, 'error');
      return;
    }

    try {
      setSaving(true);
      
      if (user) {
        const payload = {
          customerTypeName: customerTypeName.trim(),
          price: priceValue
        };
        
        console.log('Updating customer type:', payload);
        const response = await axiosInstance.put(`/admin/customer-types/${id}`, payload);
        console.log('Update response:', response.data);
        
        showSnackbar('Customer type updated successfully! ✅', 'success');
        
        // Navigate back after a short delay to show success message
        setTimeout(() => {
          navigate('/admin');
        }, 1500);
      }
    } catch (error) {
      console.error('Error updating customer type:', error);
      const errorMsg = error.response?.data?.message || 
                      error.response?.data || 
                      error.message || 
                      'Unknown error';
      showSnackbar(`Error updating customer type: ${errorMsg}`, 'error');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Container component="main" maxWidth="md" sx={{ mt: 4, mb: 4 }}>
        <Box 
          display="flex" 
          flexDirection="column" 
          alignItems="center" 
          justifyContent="center" 
          minHeight="50vh"
        >
          <CircularProgress size={60} />
          <Typography variant="h6" style={{ marginTop: 16 }}>
            Loading customer type...
          </Typography>
        </Box>
      </Container>
    );
  }

  const currentPriceValue = parseFloat(price);
  const isPriceIncreased = !isNaN(currentPriceValue) && currentPriceValue > originalPrice;

  return (
    <Container component="main" maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Card elevation={3} sx={{ borderRadius: 3 }}>
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box textAlign="center" mb={3}>
            <Typography variant="h4" component="h1" fontWeight="bold" color="primary">
              Edit Customer Type
            </Typography>
            <Typography variant="body1" color="textSecondary" mt={1}>
              Update customer type details and threshold amount
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
                  helperText={`⚠️ Warning: Setting a higher price than current (${formatCurrency(originalPrice)}) will cause customers to lose their current type.`}
                  required
                  error={isPriceIncreased}
                  InputProps={{
                    sx: { borderRadius: 2 }
                  }}
                />
              </Grid>
              
              {/* Current Price Info */}
              <Grid item xs={12}>
                <Box 
                  sx={{ 
                    backgroundColor: '#e8f5e8', 
                    padding: 2, 
                    borderRadius: 2,
                    border: '1px solid #c8e6c9'
                  }}
                >
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <InfoIcon color="primary" fontSize="small" />
                    <Typography variant="body2" color="primary" fontWeight="medium">
                      Current Information:
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="textSecondary">
                    <strong>Current Price:</strong> {formatCurrency(originalPrice)}<br/>
                    <strong>Type Name:</strong> <Chip label={customerTypeName} size="small" color="primary" variant="outlined" />
                  </Typography>
                </Box>
              </Grid>

              {/* Warning Box for Price Increase */}
              {isPriceIncreased && (
                <Grid item xs={12}>
                  <Box 
                    sx={{ 
                      backgroundColor: '#fff3e0', 
                      padding: 2, 
                      borderRadius: 2,
                      border: '1px solid #ffcc02'
                    }}
                  >
                    <Box display="flex" alignItems="center" gap={1} mb={1}>
                      <WarningIcon color="warning" fontSize="small" />
                      <Typography variant="body2" color="warning.main" fontWeight="medium">
                        Price Increase Warning:
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="textSecondary">
                      You are trying to increase the price from {formatCurrency(originalPrice)} to {formatCurrency(currentPriceValue)}. 
                      This may cause existing customers to lose their current customer type status.
                    </Typography>
                  </Box>
                </Grid>
              )}
              
              {/* Rules Info */}
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
                    💡 Editing Rules:
                  </Typography>
                  <Typography variant="body2" color="textSecondary" mt={1}>
                    • New price must be lower than or equal to current price to prevent customers from losing their type status<br/>
                    • Customers will be automatically reassigned to appropriate types after price changes<br/>
                    • Use "Update All Customer Types" button in the main page after making changes
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
                    onClick={() => navigate('/admin')}
                    disabled={saving}
                    sx={{ borderRadius: 2, minWidth: 120 }}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    startIcon={<SaveIcon />}
                    disabled={saving || isPriceIncreased}
                    sx={{ borderRadius: 2, minWidth: 160 }}
                  >
                    {saving ? 'Updating...' : 'Update Type'}
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

export default EditCustomerType; 