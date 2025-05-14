import React, { useEffect, useState } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, IconButton, Avatar } from '@mui/material';
import { Delete as DeleteIcon, Edit as EditIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../AuthContext';
import { request } from '../../../utils/axiosInstance';
import styles from './CustomerComponent.module.css';

const CustomerComponent = () => {
  const navigate = useNavigate();

  // GET USER INFO
  const { user } = useAuth();
  const [userData, setUserData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const res = await request.get('/customer/list');
        console.log('API response:', res);
        
        // Đảm bảo userData luôn là một mảng
        if (Array.isArray(res)) {
          setUserData(res);
        } else if (res && typeof res === 'object') {
          // Nếu res là object, thử tìm mảng trong object
          const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
          if (possibleArrays.length > 0) {
            setUserData(possibleArrays[0]);
          } else {
            // Nếu không tìm thấy mảng, tạo mảng rỗng
            setUserData([]);
            console.error('API response is not an array:', res);
          }
        } else {
          // Fallback nếu res không phải là mảng hoặc object
          setUserData([]);
          console.error('Unexpected API response format:', res);
        }
      } catch (error) {
        console.log("Error fetching user information", error);
        setUserData([]); // Đảm bảo userData là mảng rỗng khi có lỗi
      } finally {
        setLoading(false);
      }
    }

    if (user) {
      fetchUserData();
    }
  }, [user])

  const handleDeleteUser = async (customerId) => {
    try {
      await request.remove(`/customer/delete/${customerId}`);
      const res = await request.get('/customer/list');
      
      // Áp dụng cùng logic xử lý mảng như trên
      if (Array.isArray(res)) {
        setUserData(res);
      } else if (res && typeof res === 'object') {
        const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
        if (possibleArrays.length > 0) {
          setUserData(possibleArrays[0]);
        } else {
          setUserData([]);
        }
      } else {
        setUserData([]);
      }
    } catch (error) {
      console.error('Error deleting user:', error);
      alert('Failed to delete user');
    }
  };

  if (loading) {
    return <h1 className='text-center pt-4'>Loading customers...</h1>;
  }

  if (!userData || userData.length === 0) {
    return <h1 className='text-center pt-4'>No customers found</h1>;
  }

  return (
    <TableContainer component={Paper} className={styles.tab_container}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Day of Birth</TableCell>
            <TableCell>Address</TableCell>
            <TableCell>Phone</TableCell>
            <TableCell>Email</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {/* Thêm dấu ? để sử dụng optional chaining - đề phòng userData có thể là null */}
          {userData?.map((record) => (
            <TableRow
              key={record.customerId}
              hover
            >
              <TableCell className={styles.customerTableCell}>
                <div className={styles.avatarContainer}>
                  <Avatar src={record.customerImg} />
                </div>
                {record.customerName}
              </TableCell>
              <TableCell>{record.dateOfBirth}</TableCell>
              <TableCell>{record.customerAddress}</TableCell>
              <TableCell>{record.customerPhone}</TableCell>
              <TableCell>{record.customerEmail}</TableCell>
              <TableCell>
                <IconButton
                  color='primary'
                  onClick={() => navigate(`/editcustomer/${record.customerId}`)}
                >
                  <EditIcon />
                </IconButton>
                <IconButton
                  color='primary'
                  onClick={() => handleDeleteUser(record.customerId)}
                >
                  <DeleteIcon />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default CustomerComponent;
