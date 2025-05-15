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
    const displayDayId = parseInt(dayId) || 1;
    // We'll always use day 1 for API calls until the backend is fixed
    const apiDayId = 1;

    const [selectedVideo, setSelectedVideo] = useState(null);
    const [videos, setVideos] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const getExercisesOfCourse = async () => {
            try {
                setLoading(true);
                console.log(`Fetching exercises for course ${courseId}, display day ${displayDayId}, API day ${apiDayId}`);
                
                // Use apiDayId (1) for all requests until backend is fixed
                const exercises = await collectionService.getDetailDay(courseId, apiDayId);
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
    }, [courseId, dayId, apiDayId, displayDayId]);

    return (
        <div>
            <Header />
            <List_Day />
            <div style={{ marginTop: '20px', textAlign: 'center', padding: '10px', backgroundColor: '#f8d7da', color: '#721c24', borderRadius: '5px', maxWidth: '800px', margin: '80px auto 0' }}>
                <strong>Note:</strong> This is a temporary display. The backend needs to be updated to provide unique day IDs and content.
            </div>
            {loading ? (
                <div style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    height: '300px',
                    marginTop: '20px' 
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
                        marginTop: '20px' 
                    }}>
                        No exercises found for this day.
                    </div>
                )
            )}
            {/* <Related /> */}
            <Footer />
        </div>
    )
}

export default Workout