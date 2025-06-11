import React, { useState, useEffect } from 'react';
import { request } from '../../utils/axiosInstance';
import { useAuth } from '../../AuthContext'
import styles from './Header.module.css';
import { styled, alpha } from '@mui/material/styles';
import AppBar from '@mui/material/AppBar';
import InputBase from '@mui/material/InputBase';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Menu from '@mui/material/Menu';
import MenuIcon from '@mui/icons-material/Menu';
import Container from '@mui/material/Container';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Tooltip from '@mui/material/Tooltip';
import MenuItem from '@mui/material/MenuItem';
import AdbIcon from '@mui/icons-material/Adb';
import logo from '../../images/logo_start_gym.png'
import { useNavigate, useLocation } from 'react-router-dom';
import { Link } from 'react-router-dom';
import SearchIcon from '@mui/icons-material/Search';
// Import new icons for menu items
import PersonIcon from '@mui/icons-material/Person';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DashboardIcon from '@mui/icons-material/Dashboard';
import LogoutIcon from '@mui/icons-material/Logout';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import LoginIcon from '@mui/icons-material/Login';
import Divider from '@mui/material/Divider';

const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.white, 0.15),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.white, 0.25),
  },
  marginRight: theme.spacing(2),
  marginLeft: 0,
  width: '100%',
  [theme.breakpoints.up('sm')]: {
    marginLeft: theme.spacing(3),
    width: 'auto',
  },
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: '100%',
  position: 'absolute',
  pointerEvents: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
  color: 'inherit',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('md')]: {
      width: '40ch',
    },
  },
}));

const Header = () => {

    // Custom styled Menu component with enhanced styling
    const StyledMenu = styled(Menu)(({ theme }) => ({
        '& .MuiPaper-root': {
            borderRadius: 8,
            marginTop: theme.spacing(1),
            minWidth: 200,
            boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
            '& .MuiMenu-list': {
                padding: '8px 0',
            },
            '& .MuiMenuItem-root': {
                padding: '10px 16px',
                '&:hover': {
                    backgroundColor: 'rgba(207, 64, 68, 0.08)',
                },
                '& .MuiSvgIcon-root': {
                    fontSize: 20,
                    marginRight: theme.spacing(2),
                    color: '#CF4044',
                },
            },
        },
    }));

    // Custom styled MenuItem for visual feedback
    const StyledMenuItem = styled(MenuItem)(({ theme }) => ({
        transition: 'all 0.2s ease',
        '&:hover': {
            transform: 'translateX(5px)',
            backgroundColor: 'rgba(207, 64, 68, 0.08)',
        },
    }));

    const pages = ['About', 'Collections', 'Recipe', 'Blog'];
    const settings = ['Profile', 'Account', 'Dashboard', 'Logout'];

    const token = localStorage.getItem('token'); // Lấy token từ Local Storage

    const [anchorElNav, setAnchorElNav] = React.useState(null);
    const [anchorElUser, setAnchorElUser] = React.useState(null);
    const [searchInput, setSearchInput] = React.useState('');

    const handleOpenNavMenu = (event) => {
        setAnchorElNav(event.currentTarget);
    };
    const handleOpenUserMenu = (event) => {
        setAnchorElUser(event.currentTarget);
    };

    const handleCloseNavMenu = () => {
        setAnchorElNav(null);
    };

    const handleCloseUserMenu = () => {
        setAnchorElUser(null);
    };

    const handleNavigate = (setting) => {
        // Handle navigation based on the selected setting
        switch (setting) {
            case 'Profile':
                navigate('/profile');
                break;
            case 'Account':
                navigate('/account');
                break;
            case 'Dashboard':
                navigate('/user');
                break;
            case 'Admin':
                navigate('/admin');
                break;
            default:
                break;
        }

        handleCloseUserMenu();
    };

    const navigate = useNavigate();
    const location = useLocation();
    const currentPath = location.pathname.toLowerCase();

    const handleLogout = () => {
        // Xóa token khỏi Local Storage khi đăng xuất
        localStorage.removeItem('token');
        navigate('/login');
    };

    const handleSearchKeyDown = (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            performSearch();
        }
    };

    const performSearch = () => {
        if (!searchInput.trim()) return;
        
        // Determine which page to search on based on current route
        if (currentPath.includes('collection')) {
            navigate(`/collections?search=${encodeURIComponent(searchInput)}`);
        } else if (currentPath.includes('recipe')) {
            navigate(`/recipe?search=${encodeURIComponent(searchInput)}`);
        } else if (currentPath.includes('blog')) {
            navigate(`/blog?search=${encodeURIComponent(searchInput)}`);
        } else {
            // Default to collections search
            navigate(`/collections?search=${encodeURIComponent(searchInput)}`);
        }
    };

    // Get placeholder text based on current page
    const getSearchPlaceholder = () => {
        if (currentPath.includes('collection')) {
            return "Tìm kiếm khóa học...";
        } else if (currentPath.includes('recipe')) {
            return "Tìm kiếm món ăn dinh dưỡng...";
        } else if (currentPath.includes('blog')) {
            return "Tìm kiếm bài viết...";
        }
        return "Tìm kiếm...";
    };

    // GET USER INFO
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [isAdmin, setIsAdmin] = useState(false);
    
    useEffect(() => {
        const fetchUserData = async () => {
            try {
                // Kiểm tra token trước khi gọi API
                const token = localStorage.getItem('token');
                if (!token || token === 'null' || token === 'undefined') {
                    console.log('No valid token found, skipping user data fetch');
                    setIsAdmin(false);
                    localStorage.removeItem('isAdmin');
                    return;
                }

                // Gọi API để lấy thông tin user với xử lý lỗi tốt hơn
                const res = await request.get('/customer/detail', {
                    validateStatus: function (status) {
                        return status < 500; // Chỉ reject khi lỗi server, xử lý lỗi 401, 403 trong code
                    }
                });
                
                // Kiểm tra response có lỗi hay không
                if (!res || res.status === 401 || res.status === 403) {
                    console.log('User not authenticated or token expired');
                    setUserData([]);
                    setIsAdmin(false);
                    localStorage.removeItem('isAdmin');
                    return;
                }
                
                setUserData(res)
                
                // Check if user has admin role from API
                if (res && res.account && res.account.roles) {
                    // Kiểm tra cả roleId, roleName và nhiều cách viết khác nhau
                    const hasAdminRole = res.account.roles.some(role => {
                        // Kiểm tra theo roleId
                        if (role.roleId === 2 || role.id === 2) {
                            return true;
                        }
                        
                        // Kiểm tra theo roleName (case insensitive)
                        if (role.roleName && typeof role.roleName === 'string') {
                            const roleName = role.roleName.toUpperCase();
                            return roleName.includes("ADMIN") || roleName.includes("ROLE_ADMIN");
                        }
                        
                        // Kiểm tra theo name field
                        if (role.name && typeof role.name === 'string') {
                            const roleName = role.name.toUpperCase();
                            return roleName.includes("ADMIN") || roleName.includes("ROLE_ADMIN");
                        }
                        
                        return false;
                    });
                    
                    // Lưu vào localStorage và set state
                    if (hasAdminRole) {
                        localStorage.setItem('isAdmin', 'true');
                        setIsAdmin(true);
                    } else {
                        // Không reset nếu đã có trong localStorage, có thể API không trả về roles
                        const currentAdminStatus = localStorage.getItem('isAdmin') === 'true';
                        if (!currentAdminStatus) {
                            localStorage.setItem('isAdmin', 'false');
                            setIsAdmin(false);
                        }
                    }
                } else {
                    // Không reset admin status nếu đã có trong localStorage
                    const currentAdminStatus = localStorage.getItem('isAdmin') === 'true';
                    if (currentAdminStatus) {
                        setIsAdmin(true);
                    } else {
                        setIsAdmin(false);
                    }
                }
            } catch (error) {
                console.log("Error fetching user information", error);
                // Reset user data
                setUserData([]);
                setIsAdmin(false);
                localStorage.removeItem('isAdmin');
                
                // If it's an authentication error, clean up tokens
                if (error.response && (error.response.status === 401 || error.response.status === 403)) {
                    localStorage.removeItem('token');
                }
            }
        }

        // Only fetch user data when component mounts or user changes
        const token = localStorage.getItem('token');
        if (token && token !== 'null' && token !== 'undefined') {
            fetchUserData();
        } else {
            // Reset data when no token
            setUserData([]);
            setIsAdmin(false);
            localStorage.removeItem('isAdmin');
        }
    }, [user]) // Dependency is user to re-run when user changes

    // Function to get icon based on setting name
    const getSettingIcon = (setting) => {
        switch (setting) {
            case 'Profile':
                return <PersonIcon />;
            case 'Account':
                return <AccountCircleIcon />;
            case 'Dashboard':
                return <DashboardIcon />;
            case 'Logout':
                return <LogoutIcon />;
            case 'Admin':
                return <AdminPanelSettingsIcon />;
            default:
                return null;
        }
    };

    return (
        <AppBar position="static" className={styles.appBar}>
            <Container maxWidth="xl" className={styles.container}>
                <Toolbar disableGutters sx={{ minHeight: '80px !important', padding: '0 !important' }}>
                    <MenuItem sx={{ 
                        borderRadius: '50%', 
                        height: '90px', 
                        padding: '12px',
                        transition: 'background-color 0.2s ease',
                        '&:hover': {
                            backgroundColor: 'rgba(255,255,255,0.08)'
                        }
                    }}>
                        <Link to='/'><img src={logo} alt='logo' className={styles.startRoundLogo1} /></Link>
                    </MenuItem>
                    
                    {/* Menu item  - Navigation */}
                    <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }} className={styles.menuItem}>
                        {pages.map((page) => (
                            <Button
                                className={styles.menuItemText}
                                key={page}
                                onClick={handleCloseNavMenu}
                                sx={{ my: 2, color: 'white', display: 'block' }}
                            >
                                <Link to={`/${page.toLowerCase()}`} className={styles.menuItemText}>
                                    <Typography textAlign="center">{page}</Typography>
                                </Link>
                            </Button>
                        ))}
                    </Box>

                    {/* Search */}
                    <Search>
                        <SearchIconWrapper>
                            <SearchIcon />
                        </SearchIconWrapper>
                        <StyledInputBase
                            placeholder={getSearchPlaceholder()}
                            inputProps={{ 'aria-label': 'search' }}
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            onKeyDown={handleSearchKeyDown}
                        />
                    </Search>

                    {/* Account */}
                    <Box sx={{ flexGrow: 0 }}>
                        <Tooltip title="Open settings">
                            <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                                <Avatar 
                                    alt="Avatar" 
                                    src={userData.customerImg} 
                                    sx={{ 
                                        width: 40, 
                                        height: 40, 
                                        border: '2px solid white',
                                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
                                        transition: 'transform 0.2s',
                                        '&:hover': {
                                            transform: 'scale(1.05)'
                                        }
                                    }}
                                />
                            </IconButton>
                        </Tooltip>
                        {token ? (
                            // Nếu có token, hiển thị menu settings
                            <StyledMenu
                                sx={{ mt: '45px' }}
                                id="menu-appbar"
                                anchorEl={anchorElUser}
                                anchorOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                keepMounted
                                transformOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                open={Boolean(anchorElUser)}
                                onClose={handleCloseUserMenu}
                            >
                                {/* Show Admin menu item if user is admin */}
                                {isAdmin && (
                                    <StyledMenuItem onClick={() => handleNavigate('Admin')}>
                                        <AdminPanelSettingsIcon sx={{ color: '#CF4044' }} />
                                        <Typography textAlign="left">Admin</Typography>
                                    </StyledMenuItem>
                                )}
                                
                                <StyledMenuItem onClick={() => handleNavigate('Dashboard')}>
                                    <DashboardIcon sx={{ color: '#CF4044' }} />
                                    <Typography textAlign="left">Dashboard</Typography>
                                </StyledMenuItem>
                                
                                <StyledMenuItem onClick={() => handleNavigate('Profile')}>
                                    <PersonIcon sx={{ color: '#CF4044' }} />
                                    <Typography textAlign="left">Profile</Typography>
                                </StyledMenuItem>
                                
                                <StyledMenuItem onClick={() => handleNavigate('Account')}>
                                    <AccountCircleIcon sx={{ color: '#CF4044' }} />
                                    <Typography textAlign="left">Account</Typography>
                                </StyledMenuItem>
                                
                                <Divider sx={{ my: 1 }} />
                                
                                <StyledMenuItem onClick={handleLogout}>
                                    <LogoutIcon sx={{ color: '#CF4044' }} />
                                    <Typography textAlign="left" sx={{ color: '#d32f2f' }}>
                                        Logout
                                    </Typography>
                                </StyledMenuItem>
                            </StyledMenu>
                        ) : (
                            // Nếu không có token, hiển thị nút Login
                            <StyledMenu
                                sx={{ mt: '45px' }}
                                id="menu-appbar"
                                anchorEl={anchorElUser}
                                anchorOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                keepMounted
                                transformOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                open={Boolean(anchorElUser)}
                                onClose={handleCloseUserMenu}
                            >
                                <StyledMenuItem onClick={handleLogout}>
                                    <Link to="/login" style={{ textDecoration: 'none', color: 'inherit', display: 'flex', alignItems: 'center' }}>
                                        <LoginIcon sx={{ color: '#CF4044', mr: 2 }} />
                                        <Typography variant="subtitle1">
                                            Login
                                        </Typography>
                                    </Link>
                                </StyledMenuItem>
                            </StyledMenu>
                        )}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar >
    );
}
export default Header;
