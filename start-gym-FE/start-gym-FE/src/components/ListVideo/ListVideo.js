import React, { useState, useEffect } from 'react';
import styles from './ListVideo.module.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar, faCheck } from '@fortawesome/free-solid-svg-icons';

const ListVideo = ({ videos, onVideoSelect }) => {
    const [selectedVideo, setSelectedVideo] = useState(null);

    // Update selected video when videos array changes
    useEffect(() => {
        if (videos && videos.length > 0) {
            setSelectedVideo(videos[0]);
        } else {
            setSelectedVideo(null);
        }
    }, [videos]);

    const renderRating = () => {
        const stars = [];
        const duration = 5;

        for (let i = 1; i <= 5; i++) {
            if (i <= duration) {
                stars.push(
                    <span key={i}>
                        <FontAwesomeIcon icon={faStar} key={i} className={styles.star} />
                    </span>
                );
            } else {
                stars.push(
                    <span key={i}>
                        <FontAwesomeIcon icon={faStar} key={stars.length} className={`${styles.star} ${styles.emptyStar}`} />
                    </span>
                );
            }
        }

        return stars;
    };

    const handleVideoSelect = (video) => {
        setSelectedVideo(video);
        onVideoSelect(video);
    };

    const [searchTerm, setSearchTerm] = useState('');

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const filteredVideos = videos && videos.length > 0 
        ? videos.filter(video =>
            video.exerciseName.toLowerCase().includes(searchTerm.toLowerCase())
          )
        : [];

    if (!videos || videos.length === 0) {
        return (
            <div className={styles.videoContainer}>
                <div className={styles.noVideos}>
                    No exercises available for this day.
                </div>
            </div>
        );
    }

    return (
        <div className={styles.videoContainer}>
            <h3 className={styles.sectionTitle}>Exercises for this day</h3>
            <input
                type="text"
                placeholder="Search by exercise name"
                value={searchTerm}
                onChange={handleSearch}
                className={styles.searchBar}
            />
            <div className={styles.videoList}>
                {filteredVideos.length > 0 ? (
                    filteredVideos.map((video) => (
                        <div
                            key={video.exerciseId}
                            onClick={() => handleVideoSelect(video)}
                            className={`${styles.videoItem} ${selectedVideo && selectedVideo.exerciseId === video.exerciseId ? styles.videoItemSelected : ''}`}
                        >
                            <div className={styles.videoThumbnail}>
                                {video.status && (
                                    <div className={styles.completedBadge}>
                                        <FontAwesomeIcon icon={faCheck} />
                                    </div>
                                )}
                            </div>
                            <div className={styles.videoInfo}>
                                <h3 className={styles.title}>{video.exerciseName}</h3>
                                <p className={styles.target}>{video.target}</p>
                                <svg xmlns="http://www.w3.org/2000/svg" width="179" height="7" viewBox="0 0 179 7" fill="none">
                                    <rect x="0.549805" y="0.0119629" width="178" height="6" rx="3" fill="#DDEDFF" />
                                    <rect x="0.549805" y="0.0119629" width="118" height="6" rx="3" fill="#007AFF" />
                                </svg>
                                <div className={styles.rating}>{renderRating()}</div>
                            </div>
                        </div>
                    ))
                ) : (
                    <p className={styles.notFound}>No exercises found matching "{searchTerm}"</p>
                )}
            </div>
        </div>
    );
};

export default ListVideo;
