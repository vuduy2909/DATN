import React, { useEffect, useState } from 'react';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper, 
  IconButton, 
  Avatar, 
  Snackbar, 
  Alert,
  Box,
  Typography,
  Button,
  TextField,
  InputAdornment,
  Pagination,
  Chip,
  CircularProgress,
  MenuItem
} from '@mui/material';
import { 
  Delete as DeleteIcon, 
  Edit as EditIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Add as AddIcon,
  FitnessCenter as FitnessCenterIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../AuthContext';
import { request } from '../../../utils/axiosInstance';
import { useDialogs } from '../../../components/MaterialUIDialogs';

const CustomerComponent = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showConfirm, showAlert, DialogComponents } = useDialogs();
  
  // Data state
  const [customers, setCustomers] = useState([]);
  const [customerTypes, setCustomerTypes] = useState([]);
  const [loading, setLoading] = useState(true);

  // Pagination state
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10); // Back to 10 as requested
  const [totalCustomers, setTotalCustomers] = useState(0);

  // Search and filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredCustomers, setFilteredCustomers] = useState([]);
  const [customerTypeFilter, setCustomerTypeFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all'); // all, active, banned

  // Snackbar state for notifications
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

  // Helper function to check if customer is admin
  const isAdminCustomer = (customer) => {
    if (!customer.account || !customer.account.roles) return false;
    return customer.account.roles.some(role => role.roleName === 'ROLE_ADMIN');
  };

  // Fetch customers
  const fetchCustomers = async () => {
    setLoading(true);
    try {
      // First, get total count to determine appropriate page size
      const countParams = {
        page: 0,
        size: 1, // Just get first page to check total
        status: 'all'
      };
      
      const countRes = await request.get('/admin/customers', { params: countParams });
      const totalCount = countRes?.totalElements || 0;
      
      // Now fetch all customers with appropriate size
      const params = {
        page: 0,
        size: Math.max(totalCount, 50), // Dynamic size based on actual count, minimum 50
        status: 'all' // Get both active and banned customers
      };
      
      const res = await request.get('/admin/customers', { params });
      console.log('Admin customers API response:', res);
      
      let customerData = [];
      let totalElements = 0;
      
      if (Array.isArray(res)) {
        customerData = res;
        totalElements = res.length;
      } else if (res && typeof res === 'object') {
        // Check if response has a customers array property
        if (res.customers && Array.isArray(res.customers)) {
          customerData = res.customers;
          totalElements = res.totalElements || res.customers.length;
        } else {
          const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
          if (possibleArrays.length > 0) {
            customerData = possibleArrays[0];
            totalElements = customerData.length;
          }
        }
      }
      
      // Debug: Log each customer's isEnable status
      console.log('Customer data structure:');
      console.log(`Total customers from API: ${totalElements}`);
      customerData.forEach(customer => {
        console.log(`Customer ${customer.customerName}: isEnable = ${customer.isEnable} (type: ${typeof customer.isEnable})`);
      });
      
      setCustomers(customerData);
      setTotalCustomers(totalElements); // Use totalElements from API response
    } catch (error) {
      console.error("Error fetching customers:", error);
      showSnackbar('Failed to load customers', 'error');
      setCustomers([]);
      setTotalCustomers(0);
    } finally {
      setLoading(false);
    }
  };

  // Fetch customer types for filter
  const fetchCustomerTypes = async () => {
    try {
      const response = await request.get('/admin/customer-types');
      setCustomerTypes(response?.customerTypes || response || []);
    } catch (error) {
      console.error('Error fetching customer types:', error);
    }
  };

  // Filter and search logic
  useEffect(() => {
    let filtered = customers;

    // Search by customer name
    if (searchTerm) {
      filtered = filtered.filter(customer =>
        customer.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by active/banned status (bit field: 1 = active, 0 = banned)
    if (statusFilter !== 'all') {
      filtered = filtered.filter(customer => {
        if (statusFilter === 'active') return customer.isEnable === 1 || customer.isEnable === true;
        if (statusFilter === 'banned') return customer.isEnable === 0 || customer.isEnable === false;
        return true;
      });
    }

    // Filter by customer type
    if (customerTypeFilter !== 'all') {
      filtered = filtered.filter(customer =>
        customer.customerType?.customerTypeId?.toString() === customerTypeFilter
      );
    }

    setFilteredCustomers(filtered);
    // Don't overwrite totalCustomers here - keep it as the original count
    setPage(1); // Reset to first page when filtering
  }, [customers, searchTerm, statusFilter, customerTypeFilter]);

  // Pagination logic
  const paginatedCustomers = filteredCustomers.slice(
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

  const handleDeleteCustomer = async (customerId, customerName) => {
    showConfirm(
      'Confirm Deletion',
      `Are you sure you want to delete "${customerName}"?`,
      async () => {
        try {
          await request.delete(`/customer/delete/${customerId}`);
          showSnackbar('Customer deleted successfully! 🗑️', 'success');
          fetchCustomers(); // Refresh the list
        } catch (error) {
          console.error('Error deleting customer:', error);
          showSnackbar('Failed to delete customer', 'error');
        }
      }
    );
  };

  const handleToggleCustomerStatus = async (customerId, customerName, currentStatus) => {
    // Check if admin is trying to ban themselves
    try {
      // Get current user info to check if admin is trying to ban themselves
      const currentUserInfo = await request.get('/users/info');
      const currentUsername = currentUserInfo.username || currentUserInfo.userName;
      
      // Get customer details to check username
      const customerDetails = await request.get(`/admin/customers/${customerId}`);
      const customerUsername = customerDetails.account?.userName || customerDetails.username;
      
      // Prevent admin from banning themselves
      if (currentStatus && currentUsername && customerUsername && 
          currentUsername === customerUsername) {
        showSnackbar('⚠️ Cannot ban yourself! Admin users cannot ban their own account.', 'error');
        return;
      }
      
    } catch (error) {
      console.error('Error checking user permissions:', error);
      // Continue with ban if check fails, but log the error
    }
    
    const action = currentStatus ? 'ban' : 'activate';
    const confirmMessage = `Are you sure you want to ${action} "${customerName}"?`;
    
    showConfirm(
      `Confirm ${action}`,
      confirmMessage,
      async () => {
        try {
          // Use admin endpoint for status toggle
          const response = await request.put(`/admin/customers/${customerId}/toggle-status`);
          
          const newStatus = response.newStatus;
          const statusText = newStatus ? 'activated' : 'banned';
          
          showSnackbar(`Customer "${customerName}" has been ${statusText} successfully! ${newStatus ? '✅' : '🚫'}`, 'success');
          
          // Refresh the customer list to show updated status
          fetchCustomers();
          
        } catch (error) {
          console.error('Error toggling customer status:', error);
          const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
          showSnackbar(`Failed to ${action} customer: ${errorMsg}`, 'error');
        }
      }
    );
  };

  useEffect(() => {
    if (user) {
      fetchCustomers();
      fetchCustomerTypes();
    }
  }, [user]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh" flexDirection="column">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>Loading customers...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Customer Management</Typography>
        <Box display="flex" gap={2}>
          <Button
            startIcon={<AddIcon />}
            variant="contained"
            onClick={() => navigate('/admin/customers/new')}
            sx={{ bgcolor: '#d35255', '&:hover': { bgcolor: '#b8464a' } }}
          >
            Add Customer
          </Button>
          <Button
            startIcon={<RefreshIcon />}
            variant="outlined"
            onClick={fetchCustomers}
            disabled={loading}
            sx={{ borderColor: '#d35255', color: '#d35255' }}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {/* Stats Summary */}
      <Box display="flex" gap={2} mb={3}>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="primary">Total Customers: {customers.length}</Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="success.main">
            Active: {customers.filter(c => c.isEnable === 1 || c.isEnable === true).length}
          </Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="error.main">
            Banned: {customers.filter(c => c.isEnable === 0 || c.isEnable === false).length}
          </Typography>
        </Paper>
      </Box>

      {/* Search and Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Search & Filters</Typography>
        <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
          {/* Search Box */}
          <TextField
            placeholder="Search customers by name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            }}
            sx={{ minWidth: 250 }}
          />

          {/* Status Filter */}
          <TextField
            select
            label="Status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            sx={{ minWidth: 120 }}
            SelectProps={{ native: true }}
          >
            <option value="all">All Status</option>
            <option value="active">Active</option>
            <option value="banned">Banned</option>
          </TextField>

          {/* Customer Type Filter */}
          <TextField
            select
            label="Customer Type"
            value={customerTypeFilter}
            onChange={(e) => setCustomerTypeFilter(e.target.value)}
            sx={{ minWidth: 150 }}
            SelectProps={{ native: true }}
          >
            <option value="all">All Types</option>
            {customerTypes.map((type) => (
              <option key={type.customerTypeId} value={type.customerTypeId}>
                {type.customerTypeName}
              </option>
            ))}
          </TextField>

          {/* Rows per page */}
          <TextField
            select
            label="Rows per page"
            value={rowsPerPage}
            onChange={handleRowsPerPageChange}
            sx={{ minWidth: 120 }}
            SelectProps={{ native: true }}
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
          </TextField>
        </Box>
      </Paper>

      {/* Table */}
      <TableContainer component={Paper} sx={{ boxShadow: 2, borderRadius: 2 }}>
      <Table>
          <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
          <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Customer</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Date of Birth</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Contact</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
            {paginatedCustomers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="textSecondary" fontStyle="italic">
                    No customers found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              paginatedCustomers.map((customer) => (
                <TableRow key={customer.customerId} hover>
                  <TableCell>
                    <Box display="flex" alignItems="center" gap={2}>
                      <Avatar src={customer.customerImg} sx={{ width: 40, height: 40 }}>
                        {customer.customerName?.charAt(0)}
                      </Avatar>
                      <Box>
                        <Typography 
                          variant="subtitle1" 
                          fontWeight="bold"
                          sx={{ 
                            color: isAdminCustomer(customer) ? '#d32f2f' : 'inherit' // Red color for admin
                          }}
                        >
                          {customer.customerName}
                          {isAdminCustomer(customer) && (
                            <Typography 
                              component="span" 
                              variant="caption" 
                              sx={{ 
                                ml: 1, 
                                color: '#d32f2f', 
                                fontWeight: 'bold',
                                backgroundColor: '#ffebee',
                                padding: '2px 6px',
                                borderRadius: '4px'
                              }}
                            >
                              ADMIN
                            </Typography>
                          )}
                        </Typography>
                        <Typography variant="body2" color="textSecondary">
                          ID: {customer.customerId}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>
                  <TableCell>{customer.dateOfBirth || customer.customerDob || 'N/A'}</TableCell>
                  <TableCell>
                    <Typography variant="body2">{customer.customerPhone}</Typography>
                    <Typography variant="body2" color="textSecondary">{customer.customerEmail}</Typography>
                    <Typography variant="caption" color="textSecondary">{customer.customerAddress}</Typography>
                  </TableCell>
                  <TableCell>
                    {customer.customerType ? (
                      <Chip 
                        label={customer.customerType.customerTypeName}
                        color="primary"
                        variant="outlined"
                        size="small"
                      />
                    ) : (
                      <Chip label="No Type" variant="outlined" size="small" />
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={customer.isEnable ? 'Active' : 'Banned'}
                      color={customer.isEnable ? 'success' : 'error'}
                      variant="outlined"
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Box display="flex" gap={1} flexWrap="wrap">
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/admin/customers/${customer.customerId}/courses`)}
                        color="info"
                        title="View Courses"
                      >
                        <FitnessCenterIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/admin/customers/edit/${customer.customerId}`)}
                        color="primary"
                        title="Edit Customer"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleToggleCustomerStatus(
                          customer.customerId, 
                          customer.customerName, 
                          customer.isEnable
                        )}
                        color={customer.isEnable ? 'warning' : 'success'}
                        title={customer.isEnable ? 'Ban Customer' : 'Activate Customer'}
                      >
                        {customer.isEnable ? '🚫' : '✅'}
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleDeleteCustomer(customer.customerId, customer.customerName)}
                        color="error"
                        title="Delete Customer"
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
          Showing {((page - 1) * rowsPerPage) + 1} to {Math.min(page * rowsPerPage, totalCustomers)} of {totalCustomers} customers
        </Typography>
        <Pagination
          count={Math.ceil(totalCustomers / rowsPerPage)}
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

export default CustomerComponent;
