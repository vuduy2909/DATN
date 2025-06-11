import React, { useState } from 'react';
import Avatar from '@mui/material/Avatar';
import styles from './Comments.module.css';
import { collectionService } from '../../service/collectionService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar as starFull } from '@fortawesome/free-solid-svg-icons';
import { faStar as starNon } from '@fortawesome/free-regular-svg-icons';
import { useAuth } from '../../AuthContext';

const Comments = ({ commentText, setCommentText, listComment, setListComment, rating, setRating, courseId, dayId, userData, exercise, setLengthComment }) => {
    const { user } = useAuth();

    const handleCommentSubmit = async (e) => {
        e.preventDefault();

        // Chỉ cho phép submit comment khi user đã đăng nhập và có userData
        if (!user || !userData || !userData.customerId) {
            console.log('User not logged in or userData not available');
            return;
        }

        const newComment = {
            commentText,
            rating,
            customer: userData.customerId,
            exercise: exercise.exerciseId
        }

        try {
            await collectionService.postComment(newComment);
            const updatedComments = await collectionService.getComment(courseId, dayId);
            setListComment(updatedComments);
            setCommentText('');
            setRating(1);
            setLengthComment(updatedComments?.length || 0);
        } catch (error) {
            console.log('Error posting comment:', error);
        }
    }

    const [hoveredRating, setHoveredRating] = useState(0);

    const handleStarHover = (rating) => {
        setHoveredRating(rating);
    };

    const handleStarClick = (rating) => {
        setRating(rating);
    };

    return (
        <div className={styles.tab_info_cmt}>
            {/* Hiển thị form comment chỉ khi user đã đăng nhập */}
            {user ? (
                <form onSubmit={handleCommentSubmit} Validate className={styles.summitComment}>
                    <div className={styles.ava_name}>
                        <div className={styles.avatar}>
                            <Avatar src={userData?.customerImg} />
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
                    {Array.isArray(listComment) &&
                        [...listComment].reverse().map((comment) => (
                            (comment.exerciseId === exercise.exerciseId) && (
                                <li key={comment.commentId} className={styles.commentContainer}>
                                    <div className={styles.avatarNameContainer}>
                                        <div className={styles.avatar}>
                                            <img src={comment.customerImg} alt="Customer Avatar" />
                                        </div>
                                        <div className={styles.commentInfo}>
                                            <h6 className={styles.name}>{comment.customerName}</h6>
                                        </div>
                                    </div>
                                    <div className={styles.commentBox}>
                                        <p className={styles.commentText}>{comment.commentText}</p>
                                        <div className={styles.ratingContainer}>
                                            <div className={styles.rating}>Rating: {comment.rating}/5</div>
                                        </div>
                                    </div>
                                </li>
                            )
                        ))}
                </ul>
            </div>
        </div>
    );
};

export default Comments;
