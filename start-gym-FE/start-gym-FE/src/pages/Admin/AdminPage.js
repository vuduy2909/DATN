import React, { useState } from 'react';
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
  Home as HomeIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import ExerciseComponent from '../../components/Admin/ExerciseComponent/ExerciseComponent';
import CustomerComponent from '../../components/Admin/CustomersComponent/CustomerComponent';
import CustomerTypeComponent from '../../components/Admin/CustomerTypeComponent/CustomerTypeComponent';
import DashboardComponent from '../../components/Admin/DashboardComponent/DashboardComponent';
import logo from '../../images/logo_start_gym.png';

const drawerWidth = 280;

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const navigate = useNavigate();

  const goToHomepage = () => {
    sessionStorage.setItem('navigatingFromAdmin', 'true');
    navigate('/', { replace: true, state: { fromAdmin: true } });
  };

  const menuItems = [
    { 
      key: 'dashboard', 
      label: 'Dashboard', 
      icon: <DashboardIcon />,
      color: '#1976d2'
    },
    { 
      key: 'exercises', 
      label: 'Exercises', 
      icon: <ExerciseIcon />,
      color: '#2e7d32'
    },
    { 
      key: 'customers', 
      label: 'Customers', 
      icon: <CustomersIcon />,
      color: '#ed6c02'
    },
    { 
      key: 'customerType', 
      label: 'Customer Types', 
      icon: <CustomerTypeIcon />,
      color: '#9c27b0'
    }
  ];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardComponent />;
      case 'exercises':
        return (
          <Box>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Typography variant="h5" fontWeight="bold">
                Exercise Management
              </Typography>
              <Button 
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => navigate('/newexercise')}
                sx={{ borderRadius: 2 }}
              >
                Add Exercise
              </Button>
            </Box>
            <ExerciseComponent />
          </Box>
        );
      case 'customers':
        return (
          <Box>
            <Typography variant="h5" fontWeight="bold" mb={3}>
              Customer Management
            </Typography>
            <CustomerComponent />
          </Box>
        );
      case 'customerType':
        return <CustomerTypeComponent />;
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
          boxShadow: 1
        }}
      >
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            {getCurrentTitle()}
          </Typography>
          <Button
            startIcon={<HomeIcon />}
            onClick={goToHomepage}
            variant="outlined"
            sx={{ borderRadius: 2 }}
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
            p: 3,
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
            onClick={goToHomepage}
            sx={{
              width: 80,
              height: 80,
              borderRadius: '50%',
              cursor: 'pointer',
              transition: 'transform 0.3s ease',
              '&:hover': {
                transform: 'scale(1.1)'
              }
            }}
          />
          <Typography variant="h6" fontWeight="bold" mt={2}>
            StartGym Admin
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.8 }}>
            Management Portal
          </Typography>
        </Box>

        {/* Navigation Menu */}
        <List sx={{ px: 2, pt: 2 }}>
          {menuItems.map((item) => (
            <ListItem key={item.key} disablePadding sx={{ mb: 1 }}>
              <ListItemButton
                selected={activeTab === item.key}
                onClick={() => setActiveTab(item.key)}
                sx={{
                  borderRadius: 2,
                  py: 1.5,
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
                <ListItemIcon sx={{ color: 'white', minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText 
                  primary={item.label}
                  primaryTypographyProps={{
                    fontWeight: activeTab === item.key ? 'bold' : 'normal'
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
          bgcolor: '#f5f5f5',
          minHeight: '100vh',
          ml: `${drawerWidth}px`
        }}
      >
        <Toolbar /> {/* This creates space for the AppBar */}
        
        <Container maxWidth="xl" sx={{ py: 3 }}>
          <Paper 
            elevation={0}
            sx={{ 
              p: 0,
              bgcolor: 'transparent'
            }}
          >
            {renderTabContent()}
          </Paper>
        </Container>
      </Box>
    </Box>
  );
};

export default AdminPage; 