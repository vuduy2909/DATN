import { request } from '../utils/axiosInstance';
// GET FULL COURSE
export const getCards = async () => {
    const res = await request.get('/course');
    return res;
};

// GET DETAIL OF A COURSE -> JUST GET DAYS IN COMPONENT
export const getDays = async (days) => {
    try {
        const res = await request.get(`/course-day/courses/${days}`);
        return res;
    } catch (error) {
        console.error('Error fetching days:', error);
        throw error;
    }
};

// GET COURSE WITH COMPLETION STATUS FOR USER
export const getDaysWithCompletion = async (courseId, customerId = null) => {
    try {
        const url = customerId 
            ? `/course-day/courses/${courseId}/with-completion?customerId=${customerId}`
            : `/course-day/courses/${courseId}/with-completion`;
        const res = await request.get(url);
        return res;
    } catch (error) {
        console.error('Error fetching days with completion:', error);
        throw error;
    }
};

// GET FULL EXERCISES
export const getExercise = async () => {
    try {
        const res = await request.get(`/exercise`);
        return res;
    } catch (error) {
        console.error('Error fetching full exercises:', error);
        throw error;
    }
};

// GET EXERCISES OF DAYS IN A COURSE
export const getDetailDay = async (courseId, dayId) => {
    try {
        const res = await request.get(`/course/${courseId}/day/${dayId}/exercise`);
        return res;
    } catch (error) {
        console.error('Error fetching exercises:', error);
        throw error;
    }
};

// GET COMMENT OF EXERCISES IN A DAY
export const getComment = async (courseId, dayId) => {
    try {
        const res = await request.get(`/course/${courseId}/day/${dayId}/comment`);
        return res;
    } catch (error) {
        console.log('Error fetching comments', error);
        throw error;
    }
}

// POST COMMENT OF EXERCISES IN A DAY
export const postComment = async (data) => {
    try {
        const res = await request.post(`/comment/create`, data);
        return res;
    } catch (error) {
        console.log('Error posting comments', error);
        throw error;
    }
}

export const postRecommend = async (data) => {
    try {
        console.log('Sending recommend data:', data);
        const res = await request.post(`/track-data-ai/recommend-course`, data);
        console.log('Recommend response:', res);
        return res;
    } catch (error) {
        console.error('Error fetching recommend course:', error);
        if (error.response) {
            console.error('Server response:', error.response.data);
        }
        throw error; // Re-throw the error so it can be caught in the component
    }
}

export const putDoneExercise = async (day, exercise) => {
    try {
        const res = await request.put(`/exercise-day/updateStatusForDay/day/${day}/exercise/${exercise}?status=true`);
        return res;
    } catch (error) {
        console.log('Error updating exercise status', error);
    }
}

export const putReviewCourse = async (courseId, data) => {
    try {
        const res = await request.put(`/track-data-ai/update-effectiveness/${courseId}`, data);
        return res;
    } catch (error) {
        console.log('Error updating course review', error);
    }
}

// TOGGLE DAY COMPLETION STATUS
export const toggleDayCompletion = async (customerId, courseId, dayId) => {
    try {
        const res = await request.put(`/customer-progress/toggle?customerId=${customerId}&courseId=${courseId}&dayId=${dayId}`);
        return res;
    } catch (error) {
        console.error('Error toggling day completion:', error);
        throw error;
    }
}

// DELETE USER COURSE FROM customer_course TABLE
export const deleteUserCourse = async (courseId) => {
    try {
        const res = await request.remove(`/customer-course/${courseId}`);
        return res;
    } catch (error) {
        console.error('Error deleting user course:', error);
        throw error;
    }
}

export * as collectionService from './collectionService';
