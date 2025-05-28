import React, { useState, useEffect } from 'react';
import { 
    Table, 
    TableBody, 
    TableCell, 
    TableContainer, 
    TableHead, 
    TableRow, 
    Paper, 
    Button,
    Typography,
    Box,
    Snackbar,
    Alert,
    CircularProgress,
    Avatar,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    TextField,
    Grid,
    Card,
    CardContent,
    Chip
} from '@mui/material';
import { 
    FilterList as FilterIcon,
    Refresh as RefreshIcon,
    MonetizationOn as MoneyIcon,
    Assessment as AssessmentIcon,
    TrendingUp as TrendingUpIcon,
    Clear as ClearIcon
} from '@mui/icons-material';
import { axiosInstance } from '../../../utils/axiosInstance';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, LineChart, Line } from 'recharts';

const RevenueComponent = () => {
    const [transactions, setTransactions] = useState([]);
    const [filteredTransactions, setFilteredTransactions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [totalRevenue, setTotalRevenue] = useState(0);
    const [chartData, setChartData] = useState([]);
    
    // Filter states
    const [filterType, setFilterType] = useState('all'); // all, day, month, year
    const [selectedDate, setSelectedDate] = useState('');
    const [selectedMonth, setSelectedMonth] = useState('');
    const [selectedYear, setSelectedYear] = useState('');
    
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

    // Fetch all successful transactions
    const fetchTransactions = async () => {
        setIsLoading(true);
        
        try {
            console.log('Fetching revenue transactions...');
            const response = await axiosInstance.get('/admin/revenue-transactions');
            console.log('Revenue response:', response.data);
            
            if (response.data && Array.isArray(response.data)) {
                setTransactions(response.data);
                setFilteredTransactions(response.data);
                calculateTotalRevenue(response.data);
                generateChartData(response.data);
                console.log('Revenue transactions loaded:', response.data.length);
            } else {
                setTransactions([]);
                setFilteredTransactions([]);
                showSnackbar('No revenue data received', 'warning');
            }
        } catch (error) {
            console.error('Error fetching revenue transactions:', error);
            showSnackbar(`Error loading revenue data: ${error.message}`, 'error');
            setTransactions([]);
            setFilteredTransactions([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Calculate total revenue
    const calculateTotalRevenue = (data) => {
        const total = data.reduce((sum, transaction) => sum + (transaction.amount || 0), 0);
        setTotalRevenue(total);
    };

    // Generate chart data for visualization
    const generateChartData = (data) => {
        // Group by month for chart
        const monthlyData = {};
        
        data.forEach(transaction => {
            const date = new Date(transaction.createdDate);
            const monthKey = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;
            
            if (!monthlyData[monthKey]) {
                monthlyData[monthKey] = {
                    month: monthKey,
                    revenue: 0,
                    count: 0
                };
            }
            
            monthlyData[monthKey].revenue += transaction.amount;
            monthlyData[monthKey].count += 1;
        });

        const chartArray = Object.values(monthlyData)
            .sort((a, b) => a.month.localeCompare(b.month))
            .map(item => ({
                ...item,
                revenue: item.revenue / 1000000 // Convert to millions for better chart display
            }));
            
        setChartData(chartArray);
    };

    // Apply filters
    const applyFilters = () => {
        let filtered = [...transactions];
        
        if (filterType === 'day' && selectedDate) {
            filtered = filtered.filter(transaction => {
                const transactionDate = new Date(transaction.createdDate).toISOString().split('T')[0];
                return transactionDate === selectedDate;
            });
        } else if (filterType === 'month' && selectedMonth) {
            filtered = filtered.filter(transaction => {
                const transactionMonth = new Date(transaction.createdDate).toISOString().slice(0, 7);
                return transactionMonth === selectedMonth;
            });
        } else if (filterType === 'year' && selectedYear) {
            filtered = filtered.filter(transaction => {
                const transactionYear = new Date(transaction.createdDate).getFullYear().toString();
                return transactionYear === selectedYear;
            });
        }
        
        setFilteredTransactions(filtered);
        calculateTotalRevenue(filtered);
        generateChartData(filtered);
        showSnackbar(`Filter applied: ${filtered.length} transactions found`, 'info');
    };

    // Clear all filters
    const clearFilters = () => {
        setFilterType('all');
        setSelectedDate('');
        setSelectedMonth('');
        setSelectedYear('');
        setFilteredTransactions(transactions);
        calculateTotalRevenue(transactions);
        generateChartData(transactions);
        showSnackbar('Filters cleared', 'info');
    };

    useEffect(() => {
        fetchTransactions();
    }, []);

    // Utility functions
    const formatCurrency = (amount) => {
        if (typeof amount !== 'number') return '0 đ';
        return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString('vi-VN');
    };

    const getStatusChip = (status) => {
        return status === 'SUCCESS' ? (
            <Chip label="Success" color="success" size="small" />
        ) : (
            <Chip label={status} color="default" size="small" />
        );
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
                    Loading revenue data...
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
                    Revenue Management
                </Typography>
                
                <Box display="flex" gap={2}>
                    <Button 
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={fetchTransactions}
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

            {/* Summary Cards */}
            <Grid container spacing={3} sx={{ mb: 3 }}>
                <Grid item xs={12} md={4}>
                    <Card sx={{ bgcolor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: '#1a1a1a' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h6" gutterBottom sx={{ color: '#2c3e50', fontWeight: 'bold' }}>
                                        Total Revenue
                                    </Typography>
                                    <Typography variant="h4" fontWeight="bold" sx={{ color: '#1a1a1a' }}>
                                        {formatCurrency(totalRevenue)}
                                    </Typography>
                                </Box>
                                <MoneyIcon sx={{ fontSize: 50, opacity: 0.6, color: '#2c3e50' }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                    <Card sx={{ bgcolor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: '#1a1a1a' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h6" gutterBottom sx={{ color: '#2c3e50', fontWeight: 'bold' }}>
                                        Total Transactions
                                    </Typography>
                                    <Typography variant="h4" fontWeight="bold" sx={{ color: '#1a1a1a' }}>
                                        {filteredTransactions.length}
                                    </Typography>
                                </Box>
                                <AssessmentIcon sx={{ fontSize: 50, opacity: 0.6, color: '#2c3e50' }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                    <Card sx={{ bgcolor: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', color: '#1a1a1a' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h6" gutterBottom sx={{ color: '#2c3e50', fontWeight: 'bold' }}>
                                        Average Transaction
                                    </Typography>
                                    <Typography variant="h4" fontWeight="bold" sx={{ color: '#1a1a1a' }}>
                                        {formatCurrency(filteredTransactions.length > 0 ? totalRevenue / filteredTransactions.length : 0)}
                                    </Typography>
                                </Box>
                                <TrendingUpIcon sx={{ fontSize: 50, opacity: 0.6, color: '#2c3e50' }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Filter Section */}
            <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Filters
                </Typography>
                <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} md={2}>
                        <FormControl fullWidth size="small">
                            <InputLabel>Filter Type</InputLabel>
                            <Select
                                value={filterType}
                                onChange={(e) => setFilterType(e.target.value)}
                                label="Filter Type"
                            >
                                <MenuItem value="all">All</MenuItem>
                                <MenuItem value="day">By Day</MenuItem>
                                <MenuItem value="month">By Month</MenuItem>
                                <MenuItem value="year">By Year</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid>
                    
                    {filterType === 'day' && (
                        <Grid item xs={12} md={3}>
                            <TextField
                                fullWidth
                                size="small"
                                type="date"
                                label="Select Date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Grid>
                    )}
                    
                    {filterType === 'month' && (
                        <Grid item xs={12} md={3}>
                            <TextField
                                fullWidth
                                size="small"
                                type="month"
                                label="Select Month"
                                value={selectedMonth}
                                onChange={(e) => setSelectedMonth(e.target.value)}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Grid>
                    )}
                    
                    {filterType === 'year' && (
                        <Grid item xs={12} md={3}>
                            <TextField
                                fullWidth
                                size="small"
                                type="number"
                                label="Select Year"
                                value={selectedYear}
                                onChange={(e) => setSelectedYear(e.target.value)}
                                inputProps={{ min: 2020, max: 2030 }}
                            />
                        </Grid>
                    )}
                    
                    <Grid item xs={12} md={2}>
                        <Button
                            variant="contained"
                            startIcon={<FilterIcon />}
                            onClick={applyFilters}
                            sx={{ 
                                bgcolor: '#d35255',
                                '&:hover': { bgcolor: '#b8464a' }
                            }}
                        >
                            Filter
                        </Button>
                    </Grid>
                    
                    <Grid item xs={12} md={2}>
                        <Button
                            variant="outlined"
                            startIcon={<ClearIcon />}
                            onClick={clearFilters}
                            sx={{
                                borderColor: '#d35255',
                                color: '#d35255',
                                '&:hover': {
                                    borderColor: '#b8464a',
                                    bgcolor: 'rgba(211, 82, 85, 0.04)'
                                }
                            }}
                        >
                            Clear
                        </Button>
                    </Grid>
                </Grid>
            </Paper>

            {/* Revenue Chart */}
            {chartData.length > 0 && (
                <Paper sx={{ p: 2, mb: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        Revenue Trend (in Millions VNĐ)
                    </Typography>
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="month" />
                            <YAxis />
                            <Tooltip formatter={(value) => [`${value}M VNĐ`, 'Revenue']} />
                            <Legend />
                            <Line 
                                type="monotone" 
                                dataKey="revenue" 
                                stroke="#d35255" 
                                strokeWidth={3}
                                dot={{ fill: '#d35255', strokeWidth: 2, r: 6 }}
                                name="Revenue"
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </Paper>
            )}

            {/* Transactions Table */}
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
                            <TableCell sx={{ fontWeight: 'bold' }}>Transaction ID</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Customer</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Course</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Amount</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filteredTransactions.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                    <Typography variant="body1" color="textSecondary" fontStyle="italic">
                                        No transactions found
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ) : (
                            filteredTransactions
                                .sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate)) // Sort by newest first
                                .map((transaction) => (
                                <TableRow key={transaction.paymentId} hover>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight="medium">
                                            #{transaction.paymentId}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Box display="flex" alignItems="center">
                                            <Avatar 
                                                src={transaction.customer?.customerImg} 
                                                sx={{ mr: 2, width: 32, height: 32 }}
                                            >
                                                {transaction.customer?.customerName?.charAt(0)}
                                            </Avatar>
                                            <Typography variant="body2">
                                                {transaction.customer?.customerName || 'Unknown Customer'}
                                            </Typography>
                                        </Box>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2">
                                            {transaction.course?.courseName || 'Unknown Course'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" fontWeight="bold" color="success.main">
                                            {formatCurrency(transaction.amount)}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        {getStatusChip(transaction.paymentStatus)}
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" color="textSecondary">
                                            {formatDate(transaction.createdDate)}
                                        </Typography>
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

export default RevenueComponent; 