import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import styles from './NotFound.module.css';

const NotFound = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/', { replace: true });
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <div className={styles.errorCode}>404</div>
        <div className={styles.errorMessage}>
          <h1>Oops! Trang không tồn tại</h1>
          <p>Trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc tạm thời không khả dụng.</p>
        </div>
        
        <div className={styles.illustration}>
          <div className={styles.dumbbell}>🏋️‍♂️</div>
          <div className={styles.sadFace}>😔</div>
        </div>

        <Button
          variant="contained"
          size="large"
          startIcon={<HomeIcon />}
          onClick={handleGoHome}
          className={styles.homeButton}
        >
          Về Trang Chủ
        </Button>

        <div className={styles.suggestions}>
          <h3>Có thể bạn đang tìm:</h3>
          <ul>
            <li onClick={() => navigate('/collections')}>Bộ sưu tập bài tập</li>
            <li onClick={() => navigate('/recipe')}>Công thức dinh dưỡng</li>
            <li onClick={() => navigate('/about')}>Về chúng tôi</li>
            <li onClick={() => navigate('/blog')}>Blog fitness</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default NotFound; 