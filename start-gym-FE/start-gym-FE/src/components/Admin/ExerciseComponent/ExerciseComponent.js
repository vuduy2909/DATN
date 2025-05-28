import React, { useEffect, useState } from 'react';
import styles from './ExerciseComponent.module.css';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  TablePagination,
  Snackbar,
  Alert,
} from '@mui/material';
import { Delete as DeleteIcon, Edit as EditIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../AuthContext';
import { request } from '../../../utils/axiosInstance';

const ExerciseComponent = () => {
  const navigate = useNavigate();

  // GET Exercise INFO
  const { user } = useAuth();
  const [exerciseData, setExerciseData] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Snackbar state for notifications
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

  const showSnackbar = (message, severity = 'info') => {
    setSnackbar({
      open: true,
      message,
      severity
    });
  };

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setSnackbar({ ...snackbar, open: false });
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await request.get('/exercise');
        console.log('Exercise API response:', res);
        
        // Đảm bảo exerciseData luôn là một mảng
        if (Array.isArray(res)) {
          setExerciseData(res);
        } else if (res && typeof res === 'object') {
          // Nếu res là object, thử tìm mảng trong object
          const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
          if (possibleArrays.length > 0) {
            setExerciseData(possibleArrays[0]);
          } else {
            // Nếu không tìm thấy mảng, tạo mảng rỗng
            setExerciseData([]);
            setError('API response is not an array');
            console.error('API response is not an array:', res);
          }
        } else {
          // Fallback nếu res không phải là mảng hoặc object
          setExerciseData([]);
          setError('Unexpected API response format');
          console.error('Unexpected API response format:', res);
        }
      } catch (error) {
        console.log("Error fetching exercise information: ", error);
        setExerciseData([]); // Đảm bảo exerciseData là mảng rỗng khi có lỗi
        setError('Error loading exercises');
      } finally {
        setLoading(false);
      }
    }

    if (user) {
      fetchData();
    }
  }, [user])

  const handleDeleteExercise = async (id) => {
    try {
      await request.remove(`/exercise/delete/${id}`);
      const res = await request.get('/exercise');
      
      // Áp dụng cùng logic xử lý mảng như trên
      if (Array.isArray(res)) {
        setExerciseData(res);
      } else if (res && typeof res === 'object') {
        const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
        if (possibleArrays.length > 0) {
          setExerciseData(possibleArrays[0]);
        } else {
          setExerciseData([]);
        }
      } else {
        setExerciseData([]);
      }
      showSnackbar('Exercise deleted successfully! 🗑️', 'success');
    } catch (error) {
      console.error('Error deleting exercise:', error);
      showSnackbar('Failed to delete exercise', 'error');
    }
  };

  if (loading) {
    return <h1 className='text-center pt-4'>Loading exercises...</h1>;
  }

  if (error) {
    return <h1 className='text-center pt-4'>{error}</h1>;
  }

  if (!exerciseData || exerciseData.length === 0) {
    return <h1 className='text-center pt-4'>No exercises found</h1>;
  }

  return (
    <TableContainer component={Paper} className={styles.tab_container}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Id</TableCell>
            <TableCell>Name</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Instructions</TableCell>
            <TableCell>Target</TableCell>
            <TableCell>Body part</TableCell>
            <TableCell>Equipment</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {exerciseData
            .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
            .map((exercise) => (
              <TableRow key={exercise.id || exercise.exerciseId} hover>
                <TableCell>{exercise.id || exercise.exerciseId}</TableCell>
                <TableCell>{exercise.name || exercise.exerciseName}</TableCell>
                <TableCell>{exercise.description || exercise.exerciseDescription}</TableCell>
                <TableCell>{exercise.instructions}</TableCell>
                <TableCell>{exercise.target}</TableCell>
                <TableCell>{exercise.bodyPart}</TableCell>
                <TableCell>{exercise.equipment}</TableCell>
                <TableCell>
                  <IconButton
                    color='primary'
                    onClick={() => navigate(`/editexercise/${exercise.id || exercise.exerciseId}`)}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                    color='primary'
                    onClick={() => handleDeleteExercise(exercise.id || exercise.exerciseId)}
                  >
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>
      <TablePagination
        rowsPerPageOptions={[10, 25, 100]}
        component="div"
        count={exerciseData.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      {/* Snackbar for notifications */}
      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={4000} 
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert 
          onClose={handleSnackbarClose} 
          severity={snackbar.severity} 
          sx={{ width: '100%' }}
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </TableContainer>
  );
};

export default ExerciseComponent;
