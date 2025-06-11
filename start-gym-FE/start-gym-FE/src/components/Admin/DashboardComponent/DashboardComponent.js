import React, { useState, useEffect } from 'react';
import {
    Card,
    CardContent,
    Typography,
    Grid,
    Box,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Chip,
    CircularProgress,
    Alert,
    Avatar,
    LinearProgress,
    Pagination,
    TablePagination
} from '@mui/material';
import {
    People,
    School,
    AttachMoney,
    ShoppingCart,
    TrendingUp,
    Visibility,
    VisibilityOff,
    PersonAdd,
    Assessment,
    MonetizationOn,
    Group,
    LibraryBooks
} from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line, AreaChart, Area } from 'recharts';
import { axiosInstance } from '../../../utils/axiosInstance';

const DashboardComponent = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Pagination state for recent activities
    const [recentActivitiesPage, setRecentActivitiesPage] = useState(0);
    const [recentActivitiesSize, setRecentActivitiesSize] = useState(10);

    // Beautiful gradient colors
    const gradientColors = {
        primary: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        success: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        warning: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        error: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        info: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        purple: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
        orange: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
        blue: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)'
    };

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                setError(null);
                
                const response = await axiosInstance.get('/admin/dashboard-overview', {
                    params: {
                        recentActivitiesPage,
                        recentActivitiesSize: recentActivitiesSize
                    }
                });
                
                if (response.data) {
                    setDashboardData(response.data);
                } else {
                    setError('No data received from server');
                }
                
            } catch (error) {
                console.error('Dashboard fetch error:', error);
                setError(`Error loading dashboard: ${error.message}`);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, [recentActivitiesPage, recentActivitiesSize]);

    // Chart data - sử dụng dữ liệu thực từ API
    const userDistributionData = [
        { name: 'Regular', value: dashboardData?.userStats?.regular || 0, color: '#4facfe' },
        { name: 'Premium', value: dashboardData?.userStats?.premium || 0, color: '#fa709a' },
        { name: 'VIP', value: dashboardData?.userStats?.vip || 0, color: '#667eea' }
    ];

    const revenueData = [
        { name: 'VIP', revenue: (dashboardData?.revenueStats?.vip || 0) / 1000000, color: '#667eea' },
        { name: 'Premium', revenue: (dashboardData?.revenueStats?.premium || 0) / 1000000, color: '#fa709a' }
    ];

    // Calculate trends - using real data from backend
    const userTrend = dashboardData?.userTrendPercent || "0%";
    const revenueTrend = dashboardData?.revenueTrendPercent || "0%";
    const orderTrend = dashboardData?.ordersTrendPercent || "0%";

    // Pagination handlers for recent activities
    const handleRecentActivitiesPageChange = (event, newPage) => {
        setRecentActivitiesPage(newPage);
    };

    const handleRecentActivitiesRowsPerPageChange = (event) => {
        setRecentActivitiesSize(parseInt(event.target.value, 10));
        setRecentActivitiesPage(0);
    };

    // Real data from backend API
    const currentMonthUsers = dashboardData?.userStats?.total || 0;
    const previousMonthUsers = dashboardData?.userStats?.previousMonth || 0;
    
    const currentMonthRevenue = dashboardData?.revenueStats?.total || 0;
    const previousMonthRevenue = dashboardData?.revenueStats?.previousMonth || 0;
    
    const currentMonthOrders = dashboardData?.orderStats?.success || 0;
    const previousMonthOrders = dashboardData?.orderStats?.previousMonth || 0;
    
    const currentMonthCourses = dashboardData?.courseStats?.active || 0;
    const previousMonthCourses = dashboardData?.courseStats?.previousMonth || 0;

    const StatCard = ({ title, value, icon, gradient, subtitle, trend, showTrend = false }) => (
        <Card 
            elevation={8} 
            sx={{ 
                height: '100%',
                background: gradient,
                color: 'white',
                position: 'relative',
                overflow: 'hidden',
                transition: 'transform 0.3s ease-in-out',
                '&:hover': {
                    transform: 'translateY(-5px)',
                }
            }}
        >
            <CardContent sx={{ position: 'relative', zIndex: 2 }}>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                    <Box>
                        <Typography variant="body2" sx={{ opacity: 0.9, mb: 1 }}>
                            {title}
                        </Typography>
                        <Typography variant="h3" component="div" sx={{ fontWeight: 'bold', mb: 1 }}>
                            {typeof value === 'number' ? value.toLocaleString() : value}
                        </Typography>
                        {subtitle && (
                            <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                {subtitle}
                            </Typography>
                        )}
                        {showTrend && trend && (
                            <Box 
                                display="flex" 
                                alignItems="center" 
                                mt={1}
                                sx={{ 
                                    bgcolor: trend.includes('+') || trend.includes('Mới') ? 'rgba(0, 255, 0, 0.2)' : 
                                             trend.includes('-') ? 'rgba(255, 0, 0, 0.2)' : 'rgba(128, 128, 128, 0.2)', 
                                    borderRadius: '16px',
                                    px: 1.5,
                                    py: 0.5,
                                    display: 'inline-flex',
                                    width: 'fit-content'
                                }}
                            >
                                {(trend.includes('+') || trend.includes('Mới')) ? (
                                    <TrendingUp sx={{ fontSize: 16, mr: 0.5 }} />
                                ) : trend.includes('-') ? (
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '4px' }}>
                                        <line x1="23" y1="18" x2="17" y2="12"></line>
                                        <line x1="17" y1="18" x2="23" y2="12"></line>
                                        <line x1="1" y1="6" x2="17" y2="6"></line>
                                        <line x1="1" y1="12" x2="11" y2="12"></line>
                                        <line x1="1" y1="18" x2="5" y2="18"></line>
                                    </svg>
                                ) : (
                                    <div style={{ width: 16, height: 16, marginRight: 4 }} />
                                )}
                                <Typography variant="body2" sx={{ opacity: 0.9, fontWeight: 'bold' }}>
                                    {trend} so với tháng trước
                                </Typography>
                            </Box>
                        )}
                    </Box>
                    <Box 
                        sx={{ 
                            fontSize: 60, 
                            opacity: 0.7,
                            position: 'absolute',
                            right: 20,
                            top: '50%',
                            transform: 'translateY(-50%)'
                        }}
                    >
                        {icon}
                    </Box>
                </Box>
            </CardContent>
            {/* Decorative elements */}
            <Box
                sx={{
                    position: 'absolute',
                    top: -20,
                    right: -20,
                    width: 100,
                    height: 100,
                    borderRadius: '50%',
                    background: 'rgba(255,255,255,0.1)',
                    zIndex: 1
                }}
            />
        </Card>
    );

    const StatusChip = ({ status }) => {
        const getStatusColor = (status) => {
            switch (status) {
                case 'SUCCESS': return { bg: '#4caf50', text: 'Thành công' };
                case 'PENDING': return { bg: '#ff9800', text: 'Đang xử lý' };
                case 'FAILED': return { bg: '#f44336', text: 'Thất bại' };
                default: return { bg: '#757575', text: status };
            }
        };

        const { bg, text } = getStatusColor(status);
        return (
            <Chip 
                label={text} 
                sx={{ 
                    backgroundColor: bg, 
                    color: 'white',
                    fontWeight: 'bold',
                    fontSize: '0.75rem'
                }} 
                size="small" 
            />
        );
    };

    if (loading) {
        return (
            <Box 
                display="flex" 
                flexDirection="column"
                justifyContent="center" 
                alignItems="center" 
                minHeight="400px"
                sx={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    borderRadius: 3,
                    color: 'white',
                    p: 4
                }}
            >
                <CircularProgress sx={{ color: 'white', mb: 2 }} size={60} />
                <Typography variant="h6">Đang tải Dashboard...</Typography>
                <Typography variant="body2" sx={{ opacity: 0.8, mt: 1 }}>
                    Vui lòng đợi trong giây lát
                </Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            </Box>
        );
    }

    if (!dashboardData) {
        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="warning" sx={{ mb: 2 }}>
                    Không có dữ liệu để hiển thị. Vui lòng kiểm tra kết nối API.
                </Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ flexGrow: 1, p: 3, backgroundColor: '#f5f7fa', minHeight: '100vh' }}>
            {/* Header */}
            <Box sx={{ mb: 4 }}>
                <Typography 
                    variant="h3" 
                    sx={{ 
                        fontWeight: 'bold',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        backgroundClip: 'text',
                        WebkitBackgroundClip: 'text',
                        color: 'transparent',
                        mb: 1
                    }}
                >
                    Dashboard Quản Trị
                </Typography>
                <Typography variant="h6" color="textSecondary">
                    Tổng quan hệ thống gym StartGym
                </Typography>
            </Box>

            {/* Main Stats Cards */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Tổng Người Dùng"
                        value={dashboardData.userStats?.total || 0}
                        icon={<Group />}
                        gradient={gradientColors.primary}
                        trend={userTrend}
                        showTrend={true}
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Tổng Doanh Thu"
                        value={`${((dashboardData.revenueStats?.total || 0) / 1000000).toFixed(1)}M`}
                        subtitle="VNĐ"
                        icon={<MonetizationOn />}
                        gradient={gradientColors.success}
                        trend={revenueTrend}
                        showTrend={true}
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Khóa Học Hoạt Động"
                        value={dashboardData.courseStats?.active || 0}
                        icon={<LibraryBooks />}
                        gradient={gradientColors.warning}
                        showTrend={false}
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Đơn hàng Thành Công"
                        value={dashboardData.orderStats?.success || 0}
                        icon={<Assessment />}
                        gradient={gradientColors.error}
                        trend={orderTrend}
                        showTrend={false}
                    />
                </Grid>
            </Grid>

            {/* Secondary Stats */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                {/* Dynamic Customer Type Stats */}
                {dashboardData.userStats?.allCustomerTypes?.map((type, index) => (
                    <Grid item xs={12} sm={6} md={2.4} key={type.id || index}>
                        <StatCard
                            title={`${type.name} Users`}
                            value={type.count || 0}
                            icon={<People />}
                            gradient={
                                type.name.toLowerCase().includes('regular') ? gradientColors.blue :
                                type.name.toLowerCase().includes('premium') ? gradientColors.orange :
                                type.name.toLowerCase().includes('vip') ? gradientColors.purple :
                                type.name.toLowerCase().includes('super') ? gradientColors.error :
                                gradientColors.info
                            }
                        />
                    </Grid>
                ))}
                
                {/* Fallback to default if no dynamic types */}
                {(!dashboardData.userStats?.allCustomerTypes || dashboardData.userStats.allCustomerTypes.length === 0) && (
                    <>
                        <Grid item xs={12} sm={6} md={2.4}>
                            <StatCard
                                title="Regular Users"
                                value={dashboardData.userStats?.regular || 0}
                                icon={<People />}
                                gradient={gradientColors.blue}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6} md={2.4}>
                            <StatCard
                                title="Premium Users"
                                value={dashboardData.userStats?.premium || 0}
                                icon={<People />}
                                gradient={gradientColors.orange}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6} md={2.4}>
                            <StatCard
                                title="VIP Users"
                                value={dashboardData.userStats?.vip || 0}
                                icon={<People />}
                                gradient={gradientColors.purple}
                            />
                        </Grid>
                    </>
                )}
                
                <Grid item xs={12} sm={6} md={2.4}>
                    <StatCard
                        title="Đăng Ký Hôm Nay"
                        value={dashboardData.registrationStats?.today || 0}
                        icon={<PersonAdd />}
                        gradient={gradientColors.info}
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={2.4}>
                    <StatCard
                        title="Đơn Hàng Chờ"
                        value={dashboardData.orderStats?.pending || 0}
                        icon={<ShoppingCart />}
                        gradient={gradientColors.warning}
                    />
                </Grid>
            </Grid>

            {/* Charts Section */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid item xs={12} md={6}>
                    <Card elevation={8} sx={{ p: 3, borderRadius: 3 }}>
                        <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#333' }}>
                            Phân Bố Người Dùng
                        </Typography>
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={userDistributionData}
                                    cx="50%"
                                    cy="50%"
                                    outerRadius={100}
                                    fill="#8884d8"
                                    dataKey="value"
                                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                >
                                    {userDistributionData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <Tooltip />
                            </PieChart>
                        </ResponsiveContainer>
                    </Card>
                </Grid>
                <Grid item xs={12} md={6}>
                    <Card elevation={8} sx={{ p: 3, borderRadius: 3 }}>
                        <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#333' }}>
                            Doanh Thu Theo Loại
                        </Typography>
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={revenueData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="name" />
                                <YAxis />
                                <Tooltip formatter={(value) => [`${value}M VNĐ`, 'Doanh thu']} />
                                <Bar dataKey="revenue" fill="#667eea" radius={[8, 8, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </Card>
                </Grid>
            </Grid>

            {/* Trend Chart */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid item xs={12}>
                    <Card elevation={8} sx={{ p: 3, borderRadius: 3 }}>
                        <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#333' }}>
                            Xu Hướng 6 Tháng Qua
                        </Typography>
                        <ResponsiveContainer width="100%" height={400}>
                            <AreaChart data={dashboardData?.monthlyData || []}>
                                <defs>
                                    <linearGradient id="colorUsers" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#667eea" stopOpacity={0.8}/>
                                        <stop offset="95%" stopColor="#667eea" stopOpacity={0.1}/>
                                    </linearGradient>
                                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#fa709a" stopOpacity={0.8}/>
                                        <stop offset="95%" stopColor="#fa709a" stopOpacity={0.1}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="month" />
                                <YAxis yAxisId="left" />
                                <YAxis yAxisId="right" orientation="right" />
                                <Tooltip />
                                <Legend />
                                <Area 
                                    yAxisId="left"
                                    type="monotone" 
                                    dataKey="users" 
                                    stroke="#667eea" 
                                    fillOpacity={1} 
                                    fill="url(#colorUsers)"
                                    name="Người dùng mới"
                                />
                                <Area 
                                    yAxisId="right"
                                    type="monotone" 
                                    dataKey="revenue" 
                                    stroke="#fa709a" 
                                    fillOpacity={1} 
                                    fill="url(#colorRevenue)"
                                    name="Doanh thu (M VNĐ)"
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </Card>
                </Grid>
            </Grid>

            {/* Recent Activities */}
            <Card elevation={8} sx={{ borderRadius: 3 }}>
                <CardContent sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#333', mb: 3 }}>
                        Hoạt Động Gần Đây
                    </Typography>
                    <TableContainer>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Người Dùng</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Khóa Học</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Số Tiền</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Trạng Thái</TableCell>
                                    <TableCell sx={{ fontWeight: 'bold' }}>Thời Gian</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {(dashboardData.recentActivities || []).map((activity, index) => (
                                    <TableRow key={activity.id || index} hover>
                                        <TableCell>
                                            <Box display="flex" alignItems="center">
                                                <Avatar 
                                                    sx={{ 
                                                        mr: 2, 
                                                        background: gradientColors.primary,
                                                        width: 32, 
                                                        height: 32 
                                                    }}
                                                >
                                                    {(activity.userName || activity.user || 'U').charAt(0)}
                                                </Avatar>
                                                {activity.userName || activity.user}
                                            </Box>
                                        </TableCell>
                                        <TableCell>{activity.description || activity.course}</TableCell>
                                        <TableCell sx={{ fontWeight: 'bold', color: '#2e7d32' }}>
                                            {activity.amount.toLocaleString()} VNĐ
                                        </TableCell>
                                        <TableCell>
                                            <StatusChip status={activity.status} />
                                        </TableCell>
                                        <TableCell sx={{ color: 'text.secondary' }}>
                                            {activity.createdAt ? new Date(activity.createdAt).toLocaleString('vi-VN') : activity.time}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    
                    {/* Pagination for Recent Activities */}
                    <TablePagination
                        component="div"
                        count={dashboardData?.recentActivitiesTotal || 0}
                        page={recentActivitiesPage}
                        onPageChange={handleRecentActivitiesPageChange}
                        rowsPerPage={recentActivitiesSize}
                        onRowsPerPageChange={handleRecentActivitiesRowsPerPageChange}
                        rowsPerPageOptions={[5, 10, 25, 50]}
                        labelRowsPerPage="Số dòng mỗi trang:"
                        labelDisplayedRows={({ from, to, count }) => 
                            `${from}–${to} của ${count !== -1 ? count : `hơn ${to}`}`
                        }
                        sx={{ mt: 2 }}
                    />
                </CardContent>
            </Card>
        </Box>
    );
};

export default DashboardComponent; 