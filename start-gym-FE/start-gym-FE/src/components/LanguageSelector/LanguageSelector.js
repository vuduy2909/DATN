import React from 'react';
import { FormControl, Select, MenuItem, Box, Typography } from '@mui/material';
import { useLanguage } from '../../contexts/LanguageContext';
import vietnamFlag from '../../images/vietnam.png';
import englandFlag from '../../images/england.png';

const LanguageSelector = ({ sx = {}, iconOnly = false }) => {
  const { language, setLanguage } = useLanguage();

  const handleLanguageChange = (event) => {
    setLanguage(event.target.value);
  };

  const languages = [
    { code: 'en', name: 'English', flag: englandFlag },
    { code: 'vi', name: 'Tiếng Việt', flag: vietnamFlag }
  ];

  const currentLanguage = languages.find(lang => lang.code === language);

  return (
    <FormControl size="small" sx={{ minWidth: iconOnly ? 50 : 140, ...sx }}>
      <Select
        value={language}
        onChange={handleLanguageChange}
        displayEmpty
        renderValue={() => (
          iconOnly ? (
            <img 
              src={currentLanguage?.flag} 
              alt={`${currentLanguage?.name} flag`}
              style={{ 
                width: '28px', 
                height: '28px', 
                objectFit: 'cover',
                borderRadius: '4px',
                border: '1px solid rgba(255, 255, 255, 0.3)'
              }}
            />
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <img 
                src={currentLanguage?.flag} 
                alt={`${currentLanguage?.name} flag`}
                style={{ 
                  width: '20px', 
                  height: '20px', 
                  objectFit: 'cover',
                  borderRadius: '2px'
                }}
              />
              <Typography sx={{ fontWeight: 500, color: 'white' }}>
                {currentLanguage?.name}
              </Typography>
            </Box>
          )
        )}
        sx={{
          backgroundColor: iconOnly ? 'transparent' : 'rgba(255, 255, 255, 0.1)',
          color: 'white',
          border: iconOnly ? 'none' : '1px solid rgba(255, 255, 255, 0.3)',
          borderRadius: 2,
          minHeight: iconOnly ? '42px' : 'auto',
          '& .MuiSelect-select': {
            padding: iconOnly ? '8px' : '8px 14px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: iconOnly ? 'center' : 'flex-start'
          },
          '& .MuiSelect-icon': {
            color: 'white',
            display: iconOnly ? 'none' : 'block'
          },
          '& .MuiOutlinedInput-notchedOutline': {
            border: 'none'
          },
          '&:hover': {
            backgroundColor: iconOnly ? 'rgba(255, 255, 255, 0.1)' : 'rgba(255, 255, 255, 0.2)',
            '& img': {
              transform: iconOnly ? 'scale(1.05)' : 'none'
            }
          },
          // Smooth transition for hover effects
          transition: 'all 0.3s ease'
        }}
        MenuProps={{
          PaperProps: {
            sx: {
              backgroundColor: 'white',
              boxShadow: '0 8px 32px rgba(0,0,0,0.15)',
              borderRadius: 2,
              mt: 0.5,
              border: '1px solid rgba(0,0,0,0.1)'
            }
          }
        }}
      >
        {languages.map((lang) => (
          <MenuItem 
            key={lang.code} 
            value={lang.code}
            sx={{
              py: 1.5,
              px: 2,
              '&:hover': {
                backgroundColor: 'rgba(207, 64, 68, 0.08)'
              },
              '&.Mui-selected': {
                backgroundColor: 'rgba(207, 64, 68, 0.12)',
                '&:hover': {
                  backgroundColor: 'rgba(207, 64, 68, 0.16)'
                }
              },
              transition: 'all 0.2s ease'
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <img 
                src={lang.flag} 
                alt={`${lang.name} flag`}
                style={{ 
                  width: '24px', 
                  height: '24px', 
                  objectFit: 'cover',
                  borderRadius: '3px',
                  border: '1px solid rgba(0,0,0,0.1)'
                }}
              />
              <Typography sx={{ fontWeight: 500, color: '#2c3e50' }}>
                {lang.name}
              </Typography>
            </Box>
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default LanguageSelector; 