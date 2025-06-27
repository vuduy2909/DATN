import React, { useState, useEffect } from "react";
import styles from "./VideoPlayer.module.css"
import Comments from "../Comments/Comments";
import Instructions from "../Instructions/Instructions";
import ReactPlayer from 'react-player'
import { request } from '../../utils/axiosInstance';
import { useAuth } from '../../AuthContext'

import { useParams } from 'react-router-dom';
import { collectionService } from '../../service/collectionService';
import ListVideo from "../ListVideo/ListVideo";
import Chip from '@mui/material/Chip';

const VideoPlayer = ({ exercise, meals, videos, onVideoSelect }) => {
    const [lengthComment, setLengthComment] = useState(0);
    const { courseId, dayId } = useParams();

    const handleDuration = (duration) => {
        console.log('Thời lượng video:', duration);
    };

    // GET USER INFO
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    
    // PUT DONE EXERCISE - chỉ gọi khi user đã đăng nhập
    const [isRequestSent, setIsRequestSent] = useState(false);

    const handleEnded = async () => {
        // Chỉ gọi API khi user đã đăng nhập và có username
        if (!isRequestSent && user && user.username) {
            try {
                await collectionService.putDoneExercise(dayId, exercise.exerciseId);
                setIsRequestSent(true);
                console.log('Exercise marked as completed');
            } catch (error) {
                console.error('Lỗi khi gửi request PUT', error);
            }
        } else if (!user || !user.username) {
            console.log('User not logged in, skipping exercise completion tracking');
        }
    };

    useEffect(() => {
        setIsRequestSent(false);
    }, [exercise.videoUrl]);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await request.get('/customer/detail')
                setUserData(res)
                console.log('User data fetched successfully');
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        // Chỉ gọi API khi user đã đăng nhập và có username
        if (user && user.username) {
            fetchUserData();
        }
    }, [user])

    // GET COMMENT
    const [listComment, setListComment] = useState([]);
    const [commentText, setCommentText] = useState([]);
    const [rating, setRating] = useState(1);

    useEffect(() => {
        const getCommentOfExercise = async () => {
            try {
                const comment = await collectionService.getComment(courseId, dayId);
                
                // API đã lọc theo courseId và dayId rồi, không cần lọc thêm
                const commentsArray = Array.isArray(comment) ? comment : [];
                
                setListComment(commentsArray);
                setLengthComment(commentsArray?.length || 0);
            } catch (error) {
                console.error('Error fetching comments:', error);
                // Nếu lỗi, set empty array để tránh crash
                setListComment([]);
                setLengthComment(0);
            }
        };

        // Luôn gọi API comment vì đây là API công khai
        if (courseId && dayId && exercise.exerciseId) {
            getCommentOfExercise();
        }
    }, [courseId, dayId, exercise.exerciseId]);

    return (
        <div className={styles.container}>
            <div className={styles.mainContent}>
                <div className={styles.videoSection}>
                    {exercise && exercise.videoUrl && (
                        <ReactPlayer
                            key={exercise.videoUrl}
                            width="100%"
                            height="auto"
                            className={styles.video}
                            url={exercise.videoUrl}
                            playing={false}
                            controls={true}
                            loop={false}
                            onDuration={handleDuration}
                            onEnded={handleEnded}
                        />
                    )}

                    <div className={styles.underVideo}>
                        <div className={styles.groupTab}>
                            <div className={styles.info_video}>
                                <p className={styles.video_description}>
                                    <p className={styles.video_title}>
                                        {exercise.exerciseName}
                                    </p>
                                    <p className={styles.video_date}>4 days ago | more</p>
                                    <p>{exercise.exerciseDescription}</p>
                                    <p>Tags:
                                        <Chip clickable className={styles.video_tags} label={exercise.target} />
                                        <Chip clickable className={styles.video_tags} label={exercise.bodyPart} />
                                        <Chip clickable className={styles.video_tags} label={exercise.equipment} />
                                    </p>
                                </p>
                            </div>

                            <div>
                                {/* Removed tab buttons - only show Instructions with Comments below */}

                                <div style={{ borderTop: '1px solid #00000021;' }}>
                                    <div>
                                        <Instructions exercise={exercise} />
                                        <Comments commentText={commentText} setCommentText={setCommentText}
                                            listComment={listComment} setListComment={setListComment}
                                            rating={rating} setRating={setRating}
                                            courseId={courseId}
                                            dayId={dayId}
                                            userData={userData}
                                            exercise={exercise}
                                            setLengthComment={setLengthComment}
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                {/* Move exercises list to right side of video */}
                <div className={styles.exercisesSection}>
                    <ListVideo videos={videos} onVideoSelect={onVideoSelect} />
                </div>
            </div>
        </div>
    )
}

export default VideoPlayer