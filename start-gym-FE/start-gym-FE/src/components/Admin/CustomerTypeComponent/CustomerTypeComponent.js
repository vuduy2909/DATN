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
    TextField,
    InputAdornment,
    Pagination,
    MenuItem
} from '@mui/material';
import { 
    Delete as DeleteIcon, 
    Edit as EditIcon, 
    Add as AddIcon,
    Refresh as RefreshIcon,
    Update as UpdateIcon,
    Search as SearchIcon,
    Category as CategoryIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../AuthContext';
import { request } from '../../../utils/axiosInstance';
import { useDialogs } from '../../MaterialUIDialogs';

const CustomerTypeComponent = () => {
    const navigate = useNavigate();
    const [customerTypes, setCustomerTypes] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const { showConfirm, showAlert, DialogComponents } = useDialogs();
    
    // Pagination state
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalTypes, setTotalTypes] = useState(0);

    // Search and filter state
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredTypes, setFilteredTypes] = useState([]);
    
    // Sorting state
    const [sortBy, setSortBy] = useState('price');
    const [sortOrder, setSortOrder] = useState('asc');
    
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
            
            // Use sorted endpoint with current sort settings
            const response = await axiosInstance.get(`/customer-type/sorted?sortBy=${sortBy}&sortOrder=${sortOrder}`);
            console.log('Sorted endpoint response:', response.data);
            
            // Handle different response formats
            let customerTypesData = [];
            if (response.data && response.data.customerTypes) {
                customerTypesData = response.data.customerTypes;
            } else if (Array.isArray(response.data)) {
                customerTypesData = response.data;
            }
            
            setCustomerTypes(customerTypesData);
            setTotalTypes(customerTypesData.length);
            console.log('Customer types loaded:', customerTypesData.length);
            
            if (customerTypesData.length === 0) {
                showSnackbar('No customer types found', 'info');
            }
        } catch (error) {
            console.error('Error fetching customer types:', error);
            if (error.response?.status === 404) {
                showSnackbar('Customer types endpoint not found. Please check backend.', 'error');
            } else {
                showSnackbar(`Error loading customer types: ${error.message}`, 'error');
            }
            setCustomerTypes([]);
            setTotalTypes(0);
        } finally {
            setIsLoading(false);
        }
    };

    // Handle sort change
    const handleSortChange = (newSortBy) => {
        if (newSortBy === sortBy) {
            // Toggle sort order if same field
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            // Change sort field
            setSortBy(newSortBy);
            setSortOrder('asc');
        }
    };

    // Re-fetch when sort changes
    useEffect(() => {
        fetchCustomerTypes();
    }, [sortBy, sortOrder]);

    // Sorting logic
    const sortedCustomerTypes = React.useMemo(() => {
        let sorted = [...customerTypes];
        
        if (sortBy === 'threshold') {
            sorted.sort((a, b) => {
                const priceA = a.price || 0;
                const priceB = b.price || 0;
                return sortOrder === 'asc' ? priceA - priceB : priceB - priceA;
            });
        }
        
        return sorted;
    }, [customerTypes, sortBy, sortOrder]);

    // Filter and search logic
    useEffect(() => {
        let filtered = sortedCustomerTypes;

        // Search by customer type name
        if (searchTerm) {
            filtered = filtered.filter(type =>
                type.customerTypeName?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredTypes(filtered);
        setTotalTypes(filtered.length);
        setPage(1); // Reset to first page when filtering
    }, [sortedCustomerTypes, searchTerm]);

    // Pagination logic
    const paginatedTypes = filteredTypes.slice(
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

    // Delete customer type with navigation handling
    const handleDeleteCustomerType = async (id, customerTypeName, customerCount) => {
        if (customerCount > 0) {
            showSnackbar(`Cannot delete ${customerTypeName}: ${customerCount} customers are using this type`, 'warning');
            return;
        }

        showConfirm(
            'Confirm Deletion', 
            `Are you sure you want to delete "${customerTypeName}"? This will adjust the IDs of other types to maintain sequential order.`,
            async () => {
                try {
                    console.log('Deleting customer type:', id);
                    const response = await axiosInstance.delete(`/customer-type/${id}`);
                    
                    console.log('Delete response:', response);
                    showSnackbar(`Customer type "${customerTypeName}" deleted successfully! 🗑️`, 'success');
                    
                    // Refresh the list
                    fetchCustomerTypes();
                } catch (error) {
                    console.error('Error deleting customer type:', error);
                    showSnackbar(`Failed to delete customer type: ${error.message}`, 'error');
                }
            }
        );
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

    // Navigation handlers that ensure staying in customer type section
    const handleAddNew = () => {
        navigate('/admin/customer-types/new');
    };
    
    const handleEdit = (typeId) => {
        navigate(`/admin/customer-types/edit/${typeId}`);
    };

    // Sort handler
    const handleSortByThreshold = () => {
        if (sortBy !== 'threshold') {
            setSortBy('threshold');
            setSortOrder('asc');
        } else {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        }
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
        <Box sx={{ p: 3 }}>
            {/* Header */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
                <Typography variant="h4" fontWeight="bold">
                    <CategoryIcon sx={{ mr: 1, verticalAlign: 'text-bottom' }} />
                    Customer Type Management
                </Typography>
                <Box display="flex" gap={2}>
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={handleAddNew}
                        sx={{ 
                            bgcolor: '#d35255', 
                            '&:hover': { bgcolor: '#b8464a' }
                        }}
                    >
                        New Customer Type
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={fetchCustomerTypes}
                        disabled={isLoading}
                        sx={{ borderColor: '#d35255', color: '#d35255' }}
                    >
                        Refresh
                    </Button>
                </Box>
            </Box>

            {/* Stats Summary */}
            <Box display="flex" gap={2} mb={3}>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="primary">Total Types: {totalTypes}</Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                    <Typography variant="h6" color="success.main">
                        Sorted by: {sortBy === 'price' ? 'Threshold' : 'ID'} ({sortOrder.toUpperCase()})
                    </Typography>
                </Paper>
            </Box>

            {/* Search and Controls */}
            <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" gutterBottom>Search & Filter Controls</Typography>
                <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
                    {/* Search Box - Left */}
                    <TextField
                        placeholder="Search customer types by name..."
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
                        variant="outlined"
                        size="small"
                    />
                    
                    {/* Rows per page - Middle */}
                    <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body2" color="textSecondary">Rows:</Typography>
                        <TextField
                            select
                            value={rowsPerPage}
                            onChange={handleRowsPerPageChange}
                            size="small"
                            sx={{ minWidth: 80 }}
                            variant="outlined"
                        >
                            <MenuItem value={5}>5</MenuItem>
                            <MenuItem value={10}>10</MenuItem>
                            <MenuItem value={15}>15</MenuItem>
                            <MenuItem value={25}>25</MenuItem>
                        </TextField>
                    </Box>
                    
                    {/* Sort Button - Right */}
                    <Button
                        variant={sortBy === 'threshold' ? 'contained' : 'outlined'}
                        onClick={handleSortByThreshold}
                        size="small"
                        sx={{
                            bgcolor: sortBy === 'threshold' ? '#d35255' : 'transparent',
                            borderColor: '#d35255',
                            color: sortBy === 'threshold' ? 'white' : '#d35255',
                            '&:hover': {
                                bgcolor: sortBy === 'threshold' ? '#b8464a' : 'rgba(211, 82, 85, 0.04)',
                                borderColor: '#b8464a'
                            }
                        }}
                    >
                        Sort by Threshold {sortBy === 'threshold' ? (sortOrder === 'asc' ? '↑' : '↓') : ''}
                    </Button>
                </Box>
            </Paper>

            {/* Table with enhanced headers */}
            <TableContainer component={Paper} sx={{ boxShadow: 2, borderRadius: 2 }}>
                <Table>
                    <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                        <TableRow>
                            <TableCell sx={{ fontWeight: 'bold' }}>ID</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Name</TableCell>
                            <TableCell 
                                sx={{ fontWeight: 'bold', cursor: 'pointer' }}
                                onClick={() => handleSortChange('price')}
                            >
                                Threshold (VNĐ) {sortBy === 'price' && (sortOrder === 'asc' ? '↑' : '↓')}
                            </TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Customer Count</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filteredTypes.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                                    <Typography variant="body1" color="textSecondary" fontStyle="italic">
                                        No customer types found
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : (
                            paginatedTypes.map((type) => (
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
                                                onClick={() => handleEdit(type.customerTypeId)}
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
                                                title="Delete"
                                                disabled={type.customerCount > 0}
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

            {/* Pagination */}
            <Box display="flex" justifyContent="space-between" alignItems="center" mt={3}>
                <Typography variant="body2" color="textSecondary">
                    Showing {((page - 1) * rowsPerPage) + 1} to {Math.min(page * rowsPerPage, totalTypes)} of {totalTypes} customer types
                </Typography>
                <Pagination
                    count={Math.ceil(totalTypes / rowsPerPage)}
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

export default CustomerTypeComponent; 