import React, { useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Snackbar,
  Alert,
  Box,
  Typography,
  Button,
  TextField,
  InputAdornment,
  Pagination,
  Chip,
  CircularProgress,
  Avatar
} from '@mui/material';
import { 
  Delete as DeleteIcon, 
  Edit as EditIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Add as AddIcon,
  FitnessCenter as FitnessCenterIcon,
  VisibilityOff as HideIcon,
  Visibility as ShowIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../AuthContext';
import { request } from '../../../utils/axiosInstance';

const ExerciseComponent = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  
  // Data state
  const [exercises, setExercises] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Pagination state
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalExercises, setTotalExercises] = useState(0);

  // Search and filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredExercises, setFilteredExercises] = useState([]);
  const [statusFilter, setStatusFilter] = useState('all'); // all, active, hidden

  // Snackbar state for notifications
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

  const showSnackbar = (message, severity = 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') return;
    setSnackbar({ ...snackbar, open: false });
  };

  // Fetch exercises
  const fetchExercises = async () => {
    setLoading(true);
    try {
      const res = await request.get('/exercise');
      console.log('Exercise API response:', res);
      
      let exerciseData = [];
      if (Array.isArray(res)) {
        exerciseData = res;
      } else if (res && typeof res === 'object') {
        const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
        if (possibleArrays.length > 0) {
          exerciseData = possibleArrays[0];
        }
      }
      
      setExercises(exerciseData);
      setTotalExercises(exerciseData.length);
      setError(null);
    } catch (error) {
      console.error("Error fetching exercises:", error);
      setError('Error loading exercises');
      showSnackbar('Failed to load exercises', 'error');
      setExercises([]);
    } finally {
      setLoading(false);
    }
  };

  // Filter and search logic
  useEffect(() => {
    let filtered = exercises;

    // Search by exercise name
    if (searchTerm) {
      filtered = filtered.filter(exercise =>
        (exercise.name || exercise.exerciseName)?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filter by status
    if (statusFilter !== 'all') {
      filtered = filtered.filter(exercise => {
        const isActive = exercise.isEnable !== undefined ? exercise.isEnable : true;
        if (statusFilter === 'active') return isActive;
        if (statusFilter === 'hidden') return !isActive;
        return true;
      });
    }

    setFilteredExercises(filtered);
    setPage(1); // Reset to first page when filtering
  }, [exercises, searchTerm, statusFilter]);

  // Pagination logic
  const paginatedExercises = filteredExercises.slice(
    (page - 1) * rowsPerPage,
    page * rowsPerPage
  );

  const handlePageChange = (event, newPage) => {
    setPage(newPage);
  };

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(1);
  };

  const handleToggleExerciseStatus = async (exerciseId, exerciseName, currentStatus) => {
    try {
      console.log('Toggling exercise status:', exerciseId, 'current status:', currentStatus);
      
      // Determine new status (toggle between active/hidden)
      const isCurrentlyActive = currentStatus === 1 || currentStatus === true || currentStatus === undefined;
      const newStatus = !isCurrentlyActive;
      
      // Call API to update exercise status
      await request.put(`/admin/exercise/${exerciseId}/status`, {
        status: newStatus
      });
      
      showSnackbar(
        `Exercise "${exerciseName}" ${newStatus ? 'shown' : 'hidden'} successfully! ✅`, 
        'success'
      );
      
      fetchExercises(); // Refresh the list
    } catch (error) {
      console.error('Error toggling exercise status:', error);
      showSnackbar('Failed to update exercise status', 'error');
    }
  };

  useEffect(() => {
    if (user) {
      fetchExercises();
    }
  }, [user]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh" flexDirection="column">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>Loading exercises...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh" flexDirection="column">
        <Typography variant="h6" color="error">{error}</Typography>
        <Button onClick={fetchExercises} sx={{ mt: 2 }}>Retry</Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Exercise Management</Typography>
        <Box display="flex" gap={2}>
          <Button
            startIcon={<AddIcon />}
            variant="contained"
            onClick={() => navigate('/exercise/new')}
            sx={{ bgcolor: '#d35255', '&:hover': { bgcolor: '#b8464a' } }}
          >
            Add Exercise
          </Button>
          <Button
            startIcon={<RefreshIcon />}
            variant="outlined"
            onClick={fetchExercises}
            disabled={loading}
            sx={{ borderColor: '#d35255', color: '#d35255' }}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {/* Stats Summary */}
      <Box display="flex" gap={2} mb={3}>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="primary">Total Exercises: {totalExercises}</Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="success.main">
            Showing: {paginatedExercises.length}
          </Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1 }}>
          <Typography variant="h6" color="text.secondary">
            Page: {page} of {Math.ceil(totalExercises / rowsPerPage)}
          </Typography>
        </Paper>
      </Box>

      {/* Search and Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Search & Filters</Typography>
        <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
          <TextField
            placeholder="Search exercises by name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            }}
            sx={{ minWidth: 300 }}
          />
          
          {/* Status Filter */}
          <TextField
            select
            label="Status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            sx={{ minWidth: 120 }}
            SelectProps={{ native: true }}
          >
            <option value="all">All Status</option>
            <option value="active">Active</option>
            <option value="hidden">Hidden</option>
          </TextField>
          
          <TextField
            select
            label="Rows per page"
            value={rowsPerPage}
            onChange={handleRowsPerPageChange}
            sx={{ minWidth: 120 }}
            SelectProps={{ native: true }}
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
          </TextField>
        </Box>
      </Paper>

      {/* Table */}
      <TableContainer component={Paper} sx={{ boxShadow: 2, borderRadius: 2 }}>
      <Table>
          <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
          <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Exercise</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Description</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Target</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Body Part</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Equipment</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
            {paginatedExercises.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="textSecondary" fontStyle="italic">
                    No exercises found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              paginatedExercises.map((exercise) => (
              <TableRow key={exercise.id || exercise.exerciseId} hover>
                  <TableCell>
                    <Box display="flex" alignItems="center" gap={2}>
                      <Avatar sx={{ bgcolor: '#d35255', width: 40, height: 40 }}>
                        <FitnessCenterIcon />
                      </Avatar>
                      <Box>
                        <Typography variant="subtitle1" fontWeight="bold">
                          {exercise.name || exercise.exerciseName}
                        </Typography>
                        <Typography variant="body2" color="textSecondary">
                          ID: {exercise.id || exercise.exerciseId}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography 
                      variant="body2"
                      sx={{
                        maxWidth: 200,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}
                    >
                      {exercise.description || exercise.exerciseDescription || 'No description'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={exercise.target || 'N/A'}
                      color="primary"
                      variant="outlined"
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={exercise.bodyPart || 'N/A'}
                      color="secondary"
                      variant="outlined"
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={exercise.equipment || 'N/A'}
                      color="info"
                      variant="outlined"
                      size="small"
                    />
                  </TableCell>
                <TableCell>
                    <Box display="flex" gap={1}>
                  <IconButton
                        size="small"
                        onClick={() => navigate(`/admin/exercise/${exercise.id || exercise.exerciseId}/edit`)}
                        color="primary"
                        title="Edit Exercise"
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                        size="small"
                        onClick={() => handleToggleExerciseStatus(
                          exercise.id || exercise.exerciseId,
                          exercise.name || exercise.exerciseName,
                          exercise.isEnable
                        )}
                        color="primary"
                        title={exercise.isEnable ? 'Hide Exercise' : 'Show Exercise'}
                      >
                        {exercise.isEnable ? <HideIcon /> : <ShowIcon />}
                  </IconButton>
                    </Box>
                </TableCell>
              </TableRow>
              ))
            )}
        </TableBody>
      </Table>
      </TableContainer>

      {/* Pagination */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mt={3}>
        <Typography variant="body2" color="textSecondary">
          Showing {((page - 1) * rowsPerPage) + 1} to {Math.min(page * rowsPerPage, totalExercises)} of {totalExercises} exercises
        </Typography>
        <Pagination
          count={Math.ceil(totalExercises / rowsPerPage)}
        page={page}
          onChange={handlePageChange}
          color="primary"
          showFirstButton
          showLastButton
        />
      </Box>

      {/* Snackbar */}
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
    </Box>
  );
};

export default ExerciseComponent;
