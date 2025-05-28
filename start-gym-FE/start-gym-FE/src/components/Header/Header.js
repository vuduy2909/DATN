import React, { useState, useEffect } from 'react';
import { request } from '../../utils/axiosInstance';
import { useAuth } from '../../AuthContext'
import styles from './Header.module.css';
import { styled } from '@mui/material/styles';
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

const Header = () => {

    const StyledInputBase = styled(InputBase)(({ theme }) => ({
        color: 'inherit',
        '& .MuiInputBase-input': {
            padding: theme.spacing(1, 1, 1, 0),
            // vertical padding + font size from searchIcon
            paddingLeft: `calc(1em + ${theme.spacing(4)})`,
            transition: theme.transitions.create('width'),
            width: '100%',
            [theme.breakpoints.up('md')]: {
                width: '20ch',
            },
        },
    }));

    const pages = ['About', 'Collections', 'Recipe', 'Blog'];
    const settings = ['Profile', 'Account', 'Dashboard', 'Logout'];

    const token = localStorage.getItem('token'); // Lấy token từ Local Storage

    const [anchorElNav, setAnchorElNav] = React.useState(null);
    const [anchorElUser, setAnchorElUser] = React.useState(null);

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

    const handleLogout = () => {
        // Xóa token khỏi Local Storage khi đăng xuất
        localStorage.removeItem('token');
        navigate('/login');
    };

    // GET USER INFO
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [isAdmin, setIsAdmin] = useState(false);
    
    // Separate effect for checking localStorage on mount
    useEffect(() => {
        const isAdminFromStorage = localStorage.getItem('isAdmin') === 'true';
        console.log("Initial isAdmin from localStorage:", isAdminFromStorage);
        if (isAdminFromStorage) {
            setIsAdmin(true);
        }
    }, []);
    
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

                // Gọi API để lấy thông tin user
                const res = await request.get('/customer/detail')
                console.log("Full API response:", res);
                console.log("API response keys:", Object.keys(res || {}));
                setUserData(res)
                
                // Check if user has admin role from API
                if (res && res.account && res.account.roles) {
                    console.log("User roles:", res.account.roles);
                    
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
                    
                    console.log("Is admin from API:", hasAdminRole);
                    
                    // Lưu vào localStorage và set state
                    if (hasAdminRole) {
                        localStorage.setItem('isAdmin', 'true');
                        setIsAdmin(true);
                        console.log("Admin access granted from API - menu should show");
                    } else {
                        // Không reset nếu đã có trong localStorage, có thể API không trả về roles
                        const currentAdminStatus = localStorage.getItem('isAdmin') === 'true';
                        if (!currentAdminStatus) {
                            localStorage.setItem('isAdmin', 'false');
                            setIsAdmin(false);
                            console.log("No admin role found in API and localStorage - removing admin access");
                        } else {
                            console.log("API doesn't show admin role but localStorage has it - keeping admin status");
                        }
                    }
                } else {
                    console.log("No roles found in response, checking localStorage...");
                    console.log("Response structure - account:", res?.account ? Object.keys(res.account) : 'no account');
                    
                    // Không reset admin status nếu đã có trong localStorage
                    // Có thể API này không trả về roles nhưng user đã được xác thực admin ở nơi khác
                    const currentAdminStatus = localStorage.getItem('isAdmin') === 'true';
                    if (currentAdminStatus) {
                        setIsAdmin(true);
                        console.log("No roles in API but localStorage has admin status - keeping it");
                    } else {
                        setIsAdmin(false);
                        console.log("No roles in API and no admin status in localStorage");
                    }
                }
            } catch (error) {
                console.log("Error fetching user information", error);
                // Reset user data nếu có lỗi
                setUserData([]);
                setIsAdmin(false);
                localStorage.setItem('isAdmin', 'false');
            }
        }

        // Kiểm tra token và gọi API nếu có token
        const token = localStorage.getItem('token');
        if (token && token !== 'null' && token !== 'undefined') {
            fetchUserData();
        } else {
            // Reset data khi không có token
            setUserData([]);
            setIsAdmin(false);
            localStorage.removeItem('isAdmin');
        }
    }, [user]) // Dependency vẫn là user để re-run khi user thay đổi
    
    // Additional effect to check admin status when token changes or component mounts
    useEffect(() => {
        const checkAdminStatus = () => {
            const token = localStorage.getItem('token');
            const storedIsAdmin = localStorage.getItem('isAdmin') === 'true';
            
            console.log("checkAdminStatus - token exists:", !!token, "stored isAdmin:", storedIsAdmin, "current isAdmin state:", isAdmin);
            
            if (token && token !== 'null' && token !== 'undefined') {
                if (storedIsAdmin && !isAdmin) {
                    // Nếu localStorage có admin nhưng state chưa có, cập nhật state
                    setIsAdmin(true);
                    console.log("Admin status restored from localStorage");
                } else if (storedIsAdmin) {
                    console.log("Admin status already confirmed");
                }
                // Không làm gì nếu không có admin status trong localStorage
            } else {
                // Chỉ reset khi không có token
                setIsAdmin(false);
                localStorage.removeItem('isAdmin');
                console.log("No token found, removing admin status");
            }
        };
        
        checkAdminStatus();
        
        // Also check when localStorage changes (in case of tab switching)
        const handleStorageChange = (e) => {
            if (e.key === 'isAdmin' || e.key === 'token') {
                console.log("localStorage changed:", e.key, e.newValue);
                checkAdminStatus();
            }
        };
        
        window.addEventListener('storage', handleStorageChange);
        
        return () => {
            window.removeEventListener('storage', handleStorageChange);
        };
    }, [location, isAdmin]); // Add isAdmin to dependencies

    return (
        <AppBar position="static" className={styles.appBar}>
            <Container maxWidth="xl" className={styles.container}>
                <Toolbar disableGutters>
                    <MenuItem style={{ borderRadius: '50%', height: '135px' }}>
                        <Link to='/'><img src={logo} alt='logo' className={styles.kyoRoundLogo1} /></Link>
                    </MenuItem>
                    <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }}>
                        <IconButton
                            size="large"
                            aria-label="account of current user"
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={handleOpenNavMenu}
                            color="inherit"
                        >
                            <MenuIcon />
                        </IconButton>
                        <Menu
                            className={styles.menu}
                            id="menu-appbar"
                            anchorEl={anchorElNav}
                            anchorOrigin={{
                                vertical: 'bottom',
                                horizontal: 'left',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'left',
                            }}
                            open={Boolean(anchorElNav)}
                            onClose={handleCloseNavMenu}
                            sx={{
                                display: { xs: 'block', md: 'none' },
                                color: '#fff'
                            }}
                        >
                            {pages.map((page) => (
                                <MenuItem key={page} onClick={handleCloseNavMenu}>
                                    <Typography textAlign="center">{page}</Typography>
                                </MenuItem>
                            ))}
                        </Menu>
                    </Box>
                    <AdbIcon sx={{ display: { xs: 'flex', md: 'none' }, mr: 1 }} />
                    <Typography
                        variant="h5"
                        noWrap
                        component="a"
                        href="#app-bar-with-responsive-menu"
                        sx={{
                            mr: 2,
                            display: { xs: 'flex', md: 'none' },
                            flexGrow: 1,
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            letterSpacing: '.3rem',
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        LOGO
                    </Typography>

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

                    <div className={styles.Search}>
                        <div className={styles.SearchIconWrapper}>
                            <SearchIcon />
                        </div>
                        <StyledInputBase
                            placeholder="Search…"
                            inputProps={{ 'aria-label': 'search' }}
                        />
                    </div>

                    {/* Account */}
                    <Box sx={{ flexGrow: 0 }}>
                        <Tooltip title="Open settings">
                            <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                                <Avatar alt="Avatar" src={userData.customerImg} />
                            </IconButton>
                        </Tooltip>
                        {token ? (
                            // Nếu có token, hiển thị menu settings
                            <Menu
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
                                {console.log("Menu render - isAdmin:", isAdmin, "token:", !!token, "location:", location.pathname)}
                                {isAdmin && (
                                    <MenuItem onClick={() => handleNavigate('Admin')}>
                                        <Typography textAlign="center">Admin</Typography>
                                    </MenuItem>
                                )}
                                {settings.map((setting) => {
                                    if (setting === 'Logout') {
                                        return (
                                            <MenuItem key={setting} onClick={handleLogout}>
                                                <Typography textAlign="center">{setting}</Typography>
                                            </MenuItem>
                                        );
                                    }
                                    return (
                                        <MenuItem key={setting} onClick={() => handleNavigate(setting)}>
                                            <Typography textAlign="center">{setting}</Typography>
                                        </MenuItem>
                                    );
                                })}
                            </Menu>
                        ) : (
                            // Nếu không có token, hiển thị nút Login
                            <Menu
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
                                <MenuItem onClick={handleLogout}>
                                    <Link to="/login" style={{ textDecoration: 'none', color: 'inherit' }}>
                                        <Typography variant="subtitle1" onClick={handleCloseUserMenu}>
                                            Login
                                        </Typography>
                                    </Link>
                                </MenuItem>
                            </Menu>
                        )}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar >
    );
}
export default Header;
