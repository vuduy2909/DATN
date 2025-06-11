import React, { useState, useEffect } from 'react';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Button,
  Container,
  Paper
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  FitnessCenter as ExerciseIcon,
  People as CustomersIcon,
  Category as CustomerTypeIcon,
  Add as AddIcon,
  Home as HomeIcon,
  School as CourseIcon,
  Comment as CommentIcon,
  MonetizationOn as RevenueIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import ExerciseComponent from '../../components/Admin/ExerciseComponent/ExerciseComponent';
import CustomerComponent from '../../components/Admin/CustomersComponent/CustomerComponent';
import CustomerTypeComponent from '../../components/Admin/CustomerTypeComponent/CustomerTypeComponent';
import DashboardComponent from '../../components/Admin/DashboardComponent/DashboardComponent';
import CourseComponent from '../../components/Admin/CourseComponent/CourseComponent';
import CommentComponent from '../../components/Admin/CommentComponent/CommentComponent';
import RevenueComponent from '../../components/Admin/RevenueComponent/RevenueComponent';
import logo from '../../images/logo_start_gym.png';

const drawerWidth = 220;

const AdminPage = ({ defaultTab = 'dashboard' }) => {
  const [activeTab, setActiveTab] = useState(defaultTab);
  const navigate = useNavigate();
  const location = useLocation();

  // Set active tab based on URL or defaultTab prop
  useEffect(() => {
    setActiveTab(defaultTab);
  }, [defaultTab]);

  // Handle navigation state to switch tabs
  useEffect(() => {
    if (location.state?.activeTab) {
      setActiveTab(location.state.activeTab);
      // Clear the state to prevent unwanted tab switches
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location.state, navigate, location.pathname]);

  const handleTabChange = (newTab) => {
    setActiveTab(newTab);
    // Navigate to the specific admin section URL
    navigate(`/admin/${newTab === 'dashboard' ? '' : newTab}`, { replace: true });
  };

  const goToHomepage = () => {
    sessionStorage.setItem('navigatingFromAdmin', 'true');
    navigate('/', { replace: true, state: { fromAdmin: true } });
  };

  const reloadAdminPage = () => {
    // Reset to dashboard tab and reload the page
    setActiveTab('dashboard');
    window.location.reload();
  };

  const menuItems = [
    { 
      key: 'dashboard', 
      label: 'Dashboard', 
      icon: <DashboardIcon />,
      color: '#d35255'
    },
    { 
      key: 'courses', 
      label: 'Courses', 
      icon: <CourseIcon />,
      color: '#d35255'
    },
    { 
      key: 'exercises', 
      label: 'Exercises', 
      icon: <ExerciseIcon />,
      color: '#d35255'
    },
    { 
      key: 'customers', 
      label: 'Customers', 
      icon: <CustomersIcon />,
      color: '#d35255'
    },
    { 
      key: 'customer-types', 
      label: 'Customer Types', 
      icon: <CustomerTypeIcon />,
      color: '#d35255'
    },
    { 
      key: 'comments', 
      label: 'Comments', 
      icon: <CommentIcon />,
      color: '#d35255'
    },
    { 
      key: 'revenue', 
      label: 'Revenue', 
      icon: <RevenueIcon />,
      color: '#d35255'
    }
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardComponent />;
      case 'courses':
        return <CourseComponent />;
      case 'exercises':
        return (
          <ExerciseComponent />
        );
      case 'customers':
        return (
          <Box sx={{ p: 2 }}>
            <Typography variant="h5" fontWeight="bold" mb={3}>
              Customer Management
            </Typography>
            <CustomerComponent />
          </Box>
        );
      case 'customerType':
        return <CustomerTypeComponent />;
      case 'comments':
        return <CommentComponent />;
      case 'revenue':
        return <RevenueComponent />;
      default:
        return <DashboardComponent />;
    }
  };

  const getCurrentTitle = () => {
    const item = menuItems.find(item => item.key === activeTab);
    return item ? item.label : 'Dashboard';
  };

  return (
    <Box sx={{ display: 'flex' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          width: `calc(100% - ${drawerWidth}px)`,
          ml: `${drawerWidth}px`,
          bgcolor: 'white',
          color: 'black',
          boxShadow: 1,
          zIndex: 1200
        }}
      >
        <Toolbar sx={{ minHeight: '56px !important' }}>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            {getCurrentTitle()}
          </Typography>
          <Button
            startIcon={<HomeIcon />}
            onClick={goToHomepage}
            variant="outlined"
            sx={{ 
              borderRadius: 2,
              borderColor: '#d35255',
              color: '#d35255',
              '&:hover': {
                borderColor: '#b8464a',
                bgcolor: 'rgba(211, 82, 85, 0.04)'
              }
            }}
          >
            Back to Website
          </Button>
        </Toolbar>
      </AppBar>

      {/* Sidebar Drawer */}
      <Drawer
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white'
          },
        }}
        variant="permanent"
        anchor="left"
      >
        {/* Logo Section */}
        <Box
          sx={{
            p: 2,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            borderBottom: '1px solid rgba(255,255,255,0.1)'
          }}
        >
          <Box
            component="img"
            src={logo}
            alt="StartGym Logo"
            onClick={reloadAdminPage}
            sx={{
              width: 70,
              height: 70,
              borderRadius: '50%',
              cursor: 'pointer',
              transition: 'transform 0.3s ease',
              '&:hover': {
                transform: 'scale(1.1)'
              }
            }}
          />
          <Typography variant="h6" fontWeight="bold" mt={1} sx={{ fontSize: '1rem' }}>
            StartGym Admin
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.8, fontSize: '0.8rem' }}>
            Management Portal
          </Typography>
        </Box>

        {/* Navigation Menu */}
        <List sx={{ px: 1, pt: 1 }}>
          {menuItems.map((item) => (
            <ListItem key={item.key} disablePadding sx={{ mb: 0.3 }}>
              <ListItemButton
                selected={activeTab === item.key}
                onClick={() => handleTabChange(item.key)}
                sx={{
                  borderRadius: 2,
                  py: 1,
                  '&.Mui-selected': {
                    bgcolor: 'rgba(255,255,255,0.15)',
                    '&:hover': {
                      bgcolor: 'rgba(255,255,255,0.2)',
                    }
                  },
                  '&:hover': {
                    bgcolor: 'rgba(255,255,255,0.1)',
                  }
                }}
              >
                <ListItemIcon sx={{ color: 'white', minWidth: 32 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText 
                  primary={item.label}
                  primaryTypographyProps={{
                    fontWeight: activeTab === item.key ? 'bold' : 'normal',
                    fontSize: '0.85rem'
                  }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          bgcolor: 'white',
          minHeight: '100vh',
          mt: '56px'
        }}
      >        
        <Box sx={{ p: 0 }}>
          <Paper 
            elevation={0}
            sx={{ 
              p: 0,
              bgcolor: 'transparent'
            }}
          >
            {renderTabContent()}
          </Paper>
        </Box>
      </Box>
    </Box>
  );
};

export default AdminPage; 