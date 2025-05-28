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
    CircularProgress
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Edit as EditIcon, 
    Add as AddIcon,
    Refresh as RefreshIcon,
    Update as UpdateIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const CustomerTypeComponent = () => {
    const navigate = useNavigate();
    const [customerTypes, setCustomerTypes] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    
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

    // Fetch all customer types
    const fetchCustomerTypes = async () => {
        setIsLoading(true);
        
        try {
            console.log('Fetching customer types...');
            const response = await axiosInstance.get('/admin/customer-types');
            console.log('Response:', response.data);
            
            if (response.data && response.data.customerTypes) {
                setCustomerTypes(response.data.customerTypes);
                console.log('Customer types loaded:', response.data.customerTypes.length);
            } else {
                setCustomerTypes([]);
                showSnackbar('No customer types data received', 'warning');
            }
        } catch (error) {
            console.error('Error fetching customer types:', error);
            if (error.response?.status === 404) {
                showSnackbar('Endpoint not found. Please ensure backend is running.', 'error');
            } else {
                showSnackbar(`Error loading customer types: ${error.message}`, 'error');
            }
            setCustomerTypes([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Delete customer type
    const handleDeleteCustomerType = async (id, customerTypeName, customerCount) => {
        if (customerCount > 0) {
            showSnackbar(`Cannot delete ${customerTypeName}: ${customerCount} customers are using this type`, 'warning');
            return;
        }

        if (window.confirm(`Are you sure you want to delete "${customerTypeName}"?`)) {
            try {
                console.log('Deleting customer type:', id);
                await axiosInstance.delete(`/admin/customer-types/${id}`);
                
                showSnackbar('Customer type deleted successfully! 🗑️', 'success');
                fetchCustomerTypes();
                
            } catch (error) {
                console.error('Error deleting customer type:', error);
                const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
                showSnackbar(`Error deleting customer type: ${errorMsg}`, 'error');
            }
        }
    };

    // Update all customer types
    const updateAllCustomerTypes = async () => {
        try {
            setIsLoading(true);
            console.log('Updating all customer types...');
            
            const response = await axiosInstance.post('/admin/update-all-customer-types');
            console.log('Update all response:', response.data);
            
            showSnackbar('All customer types update process initiated! ✅', 'success');
            fetchCustomerTypes();
            
        } catch (error) {
            console.error('Error updating all customer types:', error);
            const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
            showSnackbar(`Error updating customer types: ${errorMsg}`, 'error');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchCustomerTypes();
    }, []);

    // Utility functions
    const formatCurrency = (amount) => {
        if (typeof amount !== 'number') return '0 đ';
        return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    };

    const formatNumber = (num) => {
        if (typeof num !== 'number') return '0';
        return new Intl.NumberFormat('vi-VN').format(num);
    };

    const getChipColor = (typeName) => {
        const name = typeName.toLowerCase();
        if (name.includes('regular')) return 'default';
        if (name.includes('premium')) return 'warning';
        if (name.includes('vip')) return 'secondary';
        if (name.includes('super')) return 'error';
        return 'primary';
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
                    Loading customer types...
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
                    Customer Type Management
                </Typography>
                
                <Box display="flex" gap={2}>
                    <Button 
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => navigate('/admin/customer-types/new')}
                        sx={{ 
                            bgcolor: '#d35255',
                            '&:hover': {
                                bgcolor: '#b8464a'
                            }
                        }}
                    >
                        Add Customer Type
                    </Button>
                    
                    <Button 
                        variant="contained"
                        color="warning"
                        startIcon={<UpdateIcon />}
                        onClick={updateAllCustomerTypes}
                        disabled={isLoading}
                    >
                        Update All Types
                    </Button>
                    
                    <Button 
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={fetchCustomerTypes}
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
                            <TableCell sx={{ fontWeight: 'bold' }}>ID</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Name</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Threshold (VNĐ)</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Customer Count</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {customerTypes.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                                    <Typography variant="body1" color="textSecondary" fontStyle="italic">
                                        No customer types found
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : (
                            customerTypes.map((type) => (
                                <TableRow key={type.customerTypeId} hover>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight="medium">
                                            {type.customerTypeId}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Chip 
                                            label={type.customerTypeName}
                                            color={getChipColor(type.customerTypeName)}
                                            variant="outlined"
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight="medium">
                                            {formatCurrency(type.price)}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {formatNumber(type.customerCount)} customers
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Box display="flex" gap={1}>
                                            <IconButton
                                                color="primary"
                                                size="small"
                                                onClick={() => navigate(`/admin/customer-types/edit/${type.customerTypeId}`)}
                                                title="Edit"
                                            >
                                                <EditIcon />
                                            </IconButton>
                                            <IconButton
                                                color="error"
                                                size="small"
                                                onClick={() => handleDeleteCustomerType(
                                                    type.customerTypeId, 
                                                    type.customerTypeName, 
                                                    type.customerCount
                                                )}
                                                disabled={type.customerCount > 0}
                                                title={type.customerCount > 0 ? 
                                                    'Cannot delete - customers are using this type' : 
                                                    'Delete'
                                                }
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

export default CustomerTypeComponent; 