import React from 'react';
import { Card, CardContent, Typography, Box, Button } from '@mui/material';
import { Add as AddIcon, School as SchoolIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useLanguage } from '../../contexts/LanguageContext';

const AddCourseCard = ({ sx = {} }) => {
  const navigate = useNavigate();
  const { language } = useLanguage();

  const handleAddCourse = () => {
    navigate('/collections');
  };

  return (
    <Card 
      sx={{ 
        height: '100%',
        minHeight: 280,
        border: '2px dashed #ccc',
        backgroundColor: '#f9f9f9',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        transition: 'all 0.3s ease',
        cursor: 'pointer',
        '&:hover': {
          borderColor: '#CF4044',
          backgroundColor: '#fff',
          transform: 'translateY(-2px)',
          boxShadow: '0 4px 12px rgba(207, 64, 68, 0.1)'
        },
        ...sx
      }}
      onClick={handleAddCourse}
    >
      <CardContent sx={{ textAlign: 'center', p: 3 }}>
        <Box 
          sx={{
            width: 80,
            height: 80,
            borderRadius: '50%',
            backgroundColor: '#CF4044',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 16px',
            transition: 'transform 0.3s ease',
            '&:hover': {
              transform: 'scale(1.1)'
            }
          }}
        >
          <AddIcon sx={{ fontSize: 40, color: 'white' }} />
        </Box>
        
        <Typography 
          variant="h6" 
          sx={{ 
            fontWeight: 'bold', 
            mb: 1,
            color: '#2c3e50'
          }}
        >
          {language === 'en' ? 'Add Course' : 'Thêm khóa học'}
        </Typography>
        
        <Typography 
          variant="body2" 
          sx={{ 
            color: '#64748b',
            mb: 3,
            lineHeight: 1.6
          }}
        >
          {language === 'en' 
            ? 'Click to add courses that are suitable for you'
            : 'Nhấn thêm khóa học được đề xuất phù hợp với bạn'
          }
        </Typography>

        <Button
          variant="contained"
          startIcon={<SchoolIcon />}
          sx={{
            backgroundColor: '#27ae60',
            color: 'white',
            fontWeight: 'bold',
            px: 3,
            py: 1,
            borderRadius: 2,
            '&:hover': {
              backgroundColor: '#2ecc71',
              transform: 'translateY(-1px)',
              boxShadow: '0 4px 8px rgba(39, 174, 96, 0.3)'
            }
          }}
          onClick={(e) => {
            e.stopPropagation();
            handleAddCourse();
          }}
        >
          {language === 'en' ? 'Get Suggestions' : 'Nhận Gợi Ý'}
        </Button>
      </CardContent>
    </Card>
  );
};

export default AddCourseCard; 