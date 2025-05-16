import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Header from "../components/Header/Header";
import Footer from "../components/Footer/Footer";
import MainVideoPlayer from "../components/MainVideoPlayer/MainVideoPlayer"
import Related from "../components/Related/Related";
import List_Day from "../components/List_Day/List_Day";
import { collectionService } from "../service/collectionService";

const Workout = () => {
    const { courseId, dayId } = useParams();

    const [selectedVideo, setSelectedVideo] = useState(null);
    const [videos, setVideos] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const getExercisesOfCourse = async () => {
            try {
                setLoading(true);
                console.log(`Fetching exercises for course ${courseId}, day ${dayId}`);
                const exercises = await collectionService.getDetailDay(courseId, dayId);
                console.log("Exercises fetched:", exercises);
                
                if (Array.isArray(exercises) && exercises.length > 0) {
                    setVideos(exercises);
                    setSelectedVideo(exercises[0]);
                } else {
                    console.log("No exercises found or invalid response format");
                    setVideos([]);
                    setSelectedVideo(null);
                }
            } catch (err) {
                console.error("Error fetching exercises:", err);
                setVideos([]);
                setSelectedVideo(null);
            } finally {
                setLoading(false);
            }
        }
        
        if (courseId && dayId) {
            getExercisesOfCourse();
        }
    }, [courseId, dayId]);

    return (
        <div>
            <Header />
            <List_Day />
            {loading ? (
                <div style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    height: '300px',
                    marginTop: '100px' 
                }}>
                    Loading exercises...
                </div>
            ) : (
                videos.length > 0 && selectedVideo ? (
                    <MainVideoPlayer 
                        videos={videos} 
                        setVideos={setVideos} 
                        selectedVideo={selectedVideo} 
                        setSelectedVideo={setSelectedVideo} 
                    />
                ) : (
                    <div style={{ 
                        display: 'flex', 
                        justifyContent: 'center', 
                        alignItems: 'center', 
                        height: '300px',
                        marginTop: '100px' 
                    }}>
                        No exercises found for this day.
                    </div>
                )
            )}
            <Footer />
        </div>
    )
}

export default Workout