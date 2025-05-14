import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Component để chuyển hướng người dùng đến một đường dẫn cụ thể
 * Sử dụng cả navigate từ react-router-dom và window.location nếu cần
 */
const NavigationHelper = ({ to, forceReload = false }) => {
  const navigate = useNavigate();

  useEffect(() => {
    console.log(`NavigationHelper: Redirecting to ${to}`);
    
    if (forceReload) {
      console.log(`NavigationHelper: Using window.location.href for ${to}`);
      window.location.href = to;
    } else {
      console.log(`NavigationHelper: Using navigate for ${to}`);
      navigate(to);
    }
  }, [to, navigate, forceReload]);

  return null; // Không render gì cả
};

export default NavigationHelper; 