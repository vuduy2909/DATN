import React, { useState, useMemo } from 'react';
import Avatar from '@mui/material/Avatar';
import styles from './Comments.module.css';
import { collectionService } from '../../service/collectionService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar as starFull } from '@fortawesome/free-solid-svg-icons';
import { faStar as starNon } from '@fortawesome/free-regular-svg-icons';
import { useAuth } from '../../AuthContext';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import IconButton from '@mui/material/IconButton';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Rating from '@mui/material/Rating';

import Button from '@mui/material/Button';
import Pagination from '@mui/material/Pagination';
import Box from '@mui/material/Box';

const Comments = ({ commentText, setCommentText, listComment, setListComment, rating, setRating, courseId, dayId, userData, exercise, setLengthComment }) => {
    const { user } = useAuth();

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [commentsPerPage] = useState(5); // 5 comments per page

    // Edit state
    const [editingComment, setEditingComment] = useState(null);
    const [editText, setEditText] = useState('');
    const [editRating, setEditRating] = useState(1);
    
    // Dialog states
    const [anchorEl, setAnchorEl] = useState(null);
    const [selectedComment, setSelectedComment] = useState(null);
    const [reportDialogOpen, setReportDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

    // Helper function to get avatar URL with fallback
    const getAvatarUrl = (customerImg, customerName) => {
        // Nếu có customerImg và là URL hợp lệ
        if (customerImg && (customerImg.startsWith('http') || customerImg.startsWith('/') || customerImg.startsWith('data:'))) {
            return customerImg;
        }
        
        // Fallback: tạo avatar từ chữ cái đầu của tên
        if (customerName) {
            const initial = customerName.charAt(0).toUpperCase();
            // Tạo avatar SVG với chữ cái đầu
            return `data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='45' height='45' style='background:%23d35255' viewBox='0 0 45 45'><text x='50%' y='50%' font-size='20' fill='white' text-anchor='middle' dominant-baseline='middle'>${initial}</text></svg>`;
        }
        
        // Fallback cuối cùng: sử dụng logo StartGym
        return '/logo_start_gym.png';
    };

    // Helper function to handle avatar error
    const handleAvatarError = (e, customerName) => {
        // Nếu lỗi, thử sử dụng chữ cái đầu của tên
        if (customerName) {
            const initial = customerName.charAt(0).toUpperCase();
            e.target.src = `data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='45' height='45' style='background:%23d35255' viewBox='0 0 45 45'><text x='50%' y='50%' font-size='20' fill='white' text-anchor='middle' dominant-baseline='middle'>${initial}</text></svg>`;
        } else {
            // Cuối cùng sử dụng logo
            e.target.src = '/logo_start_gym.png';
        }
        e.target.onerror = null; // Prevent infinite loop
    };

    const handleCommentSubmit = async (e) => {
        e.preventDefault();

        // Chỉ cho phép submit comment khi user đã đăng nhập và có userData
        if (!user || !userData || !userData.customerId) {
            return;
        }

        const newComment = {
            commentText,
            rating,
            customer: userData.customerId,
            exercise: exercise.exerciseId,
            course: courseId,  // Thêm courseId
            day: dayId         // Thêm dayId
        }

        try {
            await collectionService.postComment(newComment);
            const updatedComments = await collectionService.getComment(courseId, dayId);
            setListComment(updatedComments);
            setCommentText('');
            setRating(1);
            setLengthComment(updatedComments?.length || 0);
            setCurrentPage(1); // Reset to first page after new comment
        } catch (error) {
            console.error('Error posting comment:', error);
        }
    }

    const [hoveredRating, setHoveredRating] = useState(0);

    const handleStarHover = (rating) => {
        setHoveredRating(rating);
    };

    const handleStarClick = (rating) => {
        setRating(rating);
    };

    const handleMenuOpen = (event, comment) => {
        setAnchorEl(event.currentTarget);
        setSelectedComment(comment);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleReportClick = () => {
        setReportDialogOpen(true);
        handleMenuClose();
    };

    const handleReportSubmit = async () => {
        try {
            await collectionService.reportComment({
                commentId: selectedComment.commentId,
                reportReason: 'Reported by user'
            });
            setReportDialogOpen(false);
            setSelectedComment(null);
            // Optionally reload comments
        } catch (error) {
            // handle error
        }
    };

    // Filter and paginate comments - API đã lọc theo course/day, chỉ cần lọc theo exercise
    const filteredComments = useMemo(() => {
        if (!Array.isArray(listComment)) return [];
        
        return [...listComment]
            .filter(comment => {
                // Chỉ lọc theo exerciseId vì API đã lọc theo course/day rồi
                return comment.exerciseId === exercise.exerciseId;
            })
            .reverse(); // Show newest first
    }, [listComment, exercise.exerciseId, courseId, dayId]);

    const totalPages = Math.ceil(filteredComments.length / commentsPerPage);
    const paginatedComments = useMemo(() => {
        const startIndex = (currentPage - 1) * commentsPerPage;
        const endIndex = startIndex + commentsPerPage;
        return filteredComments.slice(startIndex, endIndex);
    }, [filteredComments, currentPage, commentsPerPage]);

    const handlePageChange = (event, page) => {
        setCurrentPage(page);
    };

    // Helper function to check if current user owns the comment
    const isOwner = (comment) => {
        return user && userData && comment && comment.customerId === userData.customerId;
    };

    // Edit comment handlers
    const handleEditComment = (comment) => {
        setEditingComment(comment.commentId);
        setEditText(comment.commentText);
        setEditRating(comment.rating || 1);
        handleMenuClose();
    };

    const handleSaveEdit = async (commentId) => {
        try {
            await collectionService.updateComment(commentId, {
                commentText: editText,
                rating: editRating
            });
            
            // Reload comments
            const updatedComments = await collectionService.getComment(courseId, dayId);
            setListComment(updatedComments);
            setLengthComment(updatedComments?.length || 0);
            
            // Reset edit state
            setEditingComment(null);
            setEditText('');
            setEditRating(1);
        } catch (error) {
            console.error('Error updating comment:', error);
        }
    };

    const handleCancelEdit = () => {
        setEditingComment(null);
        setEditText('');
        setEditRating(1);
    };

    // Delete comment handlers
    const handleDeleteComment = async () => {
        try {
            await collectionService.deleteComment(selectedComment.commentId);
            
            // Reload comments
            const updatedComments = await collectionService.getComment(courseId, dayId);
            setListComment(updatedComments);
            setLengthComment(updatedComments?.length || 0);
            
            // Reset current page if needed
            const newTotalPages = Math.ceil(updatedComments.length / commentsPerPage);
            if (currentPage > newTotalPages && newTotalPages > 0) {
                setCurrentPage(newTotalPages);
            }
            
            setDeleteDialogOpen(false);
            setSelectedComment(null);
        } catch (error) {
            console.error('Error deleting comment:', error);
        }
    };

    return (
        <div className={styles.tab_info_cmt}>
            {/* Hiển thị form comment chỉ khi user đã đăng nhập */}
            {user ? (
                <form onSubmit={handleCommentSubmit} Validate className={styles.summitComment}>
                    <div className={styles.ava_name}>
                        <div className={styles.avatar}>
                            <img 
                                src={getAvatarUrl(userData?.customerImg, userData?.customerName)} 
                                alt="Your Avatar"
                                onError={(e) => handleAvatarError(e, userData?.customerName)}
                            />
                        </div>
                        <div className={styles.name}>{userData?.customerName || 'Loading...'}</div>
                    </div>
                    <div className={styles.starRating}>
                        <div className={styles.name}>Your rating</div>
                        <div className={styles.boxRating}>
                            {[1, 2, 3, 4, 5].map((star) => (
                                <span
                                    key={star}
                                    className={`${styles.star} ${star <= (hoveredRating || rating) ? styles.starFilled : ''}`}
                                    onMouseEnter={() => handleStarHover(star)}
                                    onMouseLeave={() => setHoveredRating(0)}
                                    onClick={() => handleStarClick(star)}
                                >
                                    <FontAwesomeIcon icon={star <= (hoveredRating || rating) ? starFull : starNon} className={styles.star} />
                                </span>
                            ))}
                        </div>
                    </div>
                    <textarea type="text"
                        placeholder="Add a comment"
                        className={styles.commentInput}
                        value={commentText}
                        onChange={(e) => setCommentText(e.target.value)}
                        required
                    />
                    <button 
                        type="submit" 
                        className={styles.commentSubmit} 
                        disabled={!userData || !userData.customerId}
                    >
                        {!userData || !userData.customerId ? 'Loading...' : 'Submit'}
                    </button>
                </form>
            ) : (
                // Hiển thị thông báo đăng nhập chỉ khi user chưa đăng nhập hoặc chưa có userData
                <div className={styles.loginPrompt}>
                    <p>Please <a href="/login">login</a> to add comments</p>
                </div>
            )}
            
            {/* Luôn hiển thị danh sách comment cho tất cả user, kể cả chưa đăng nhập */}
            <div className={styles.customerReview}>
                <ul className={styles.ulComment}>
                    {paginatedComments.map((comment) => (
                        <li key={comment.commentId} className={styles.commentContainer}>
                            <div className={styles.avatarNameContainer}>
                                <div className={styles.avatar}>
                                    <img 
                                        src={getAvatarUrl(comment.customerImg, comment.customerName)} 
                                        alt="Customer Avatar"
                                        onError={(e) => handleAvatarError(e, comment.customerName)}
                                    />
                                </div>
                                <div className={styles.commentInfo}>
                                    <h6 className={styles.name}>{comment.customerName}</h6>
                                </div>
                                <IconButton 
                                    onClick={(e) => handleMenuOpen(e, comment)}
                                    size="small"
                                    style={{ marginLeft: 'auto' }}
                                >
                                    <MoreVertIcon fontSize="small" />
                                </IconButton>
                            </div>
                            
                            <div className={styles.commentBox}>
                                {editingComment === comment.commentId ? (
                                    // Edit mode
                                    <div style={{ marginBottom: '10px' }}>
                                        <TextField
                                            fullWidth
                                            multiline
                                            rows={3}
                                            value={editText}
                                            onChange={(e) => setEditText(e.target.value)}
                                            variant="outlined"
                                            size="small"
                                            placeholder="Edit your comment..."
                                        />
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
                                            <Rating
                                                value={editRating}
                                                onChange={(event, newValue) => setEditRating(newValue)}
                                                size="small"
                                            />
                                            <Box sx={{ display: 'flex', gap: 1 }}>
                                                <IconButton
                                                    onClick={() => handleSaveEdit(comment.commentId)}
                                                    size="small"
                                                    color="primary"
                                                    disabled={!editText.trim()}
                                                >
                                                    <SaveIcon fontSize="small" />
                                                </IconButton>
                                                <IconButton
                                                    onClick={handleCancelEdit}
                                                    size="small"
                                                    color="secondary"
                                                >
                                                    <CancelIcon fontSize="small" />
                                                </IconButton>
                                            </Box>
                                        </Box>
                                    </div>
                                ) : (
                                    // View mode
                                    <>
                                        <p className={styles.commentText}>{comment.commentText}</p>
                                        <div className={styles.ratingContainer}>
                                            <div className={styles.rating}>⭐ {comment.rating}/5</div>
                                        </div>
                                    </>
                                )}
                            </div>
                            
                            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
                                {selectedComment && isOwner(selectedComment) && (
                                    <>
                                        <MenuItem onClick={() => handleEditComment(selectedComment)}>
                                            <EditIcon fontSize="small" sx={{ mr: 1 }} />
                                            Edit
                                        </MenuItem>
                                        <MenuItem onClick={() => {
                                            setDeleteDialogOpen(true);
                                            handleMenuClose();
                                        }}>
                                            <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
                                            Delete
                                        </MenuItem>
                                    </>
                                )}
                                <MenuItem onClick={handleReportClick}>Report</MenuItem>
                            </Menu>
                        </li>
                    ))}
                    
                    {filteredComments.length === 0 && (
                        <li className={styles.commentContainer}>
                            <p style={{ textAlign: 'center', color: '#666' }}>No comments yet. Be the first to comment!</p>
                        </li>
                    )}
                </ul>

                {/* Pagination - chỉ hiển thị khi có nhiều hơn 1 trang */}
                {totalPages > 1 && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3, mb: 2 }}>
                        <Pagination
                            count={totalPages}
                            page={currentPage}
                            onChange={handlePageChange}
                            color="primary"
                            size="medium"
                            showFirstButton
                            showLastButton
                            sx={{
                                '& .MuiPaginationItem-root': {
                                    fontSize: '0.875rem',
                                },
                                '& .MuiPaginationItem-page.Mui-selected': {
                                    backgroundColor: '#d35255',
                                    color: 'white',
                                    '&:hover': {
                                        backgroundColor: '#b94649',
                                    },
                                },
                            }}
                        />
                    </Box>
                )}
                
                {/* Hiển thị thông tin trang hiện tại nếu có nhiều comment */}
                {filteredComments.length > 0 && (
                    <Box sx={{ textAlign: 'center', mt: 1, color: '#666', fontSize: '0.875rem' }}>
                        Showing {Math.min((currentPage - 1) * commentsPerPage + 1, filteredComments.length)} - {Math.min(currentPage * commentsPerPage, filteredComments.length)} of {filteredComments.length} comments
                    </Box>
                )}
            </div>

            {/* Report Comment Dialog */}
            <Dialog open={reportDialogOpen} onClose={() => setReportDialogOpen(false)}>
                <DialogTitle>Report Comment</DialogTitle>
                <DialogContent>
                    <p>Are you sure you want to report this comment?</p>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setReportDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleReportSubmit} color="error">Report</Button>
                </DialogActions>
            </Dialog>

            {/* Delete Comment Dialog */}
            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Delete Comment</DialogTitle>
                <DialogContent>
                    <p>Are you sure you want to delete this comment? This action cannot be undone.</p>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleDeleteComment} color="error" variant="contained">Delete</Button>
                </DialogActions>
            </Dialog>
        </div>
    );
};

export default Comments;
