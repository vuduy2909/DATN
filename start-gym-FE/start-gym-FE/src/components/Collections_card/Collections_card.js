import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import styles from "./Collections_card.module.css";
import Card from "react-bootstrap/Card";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { collectionService } from "../../service/collectionService";
import { faStar } from "@fortawesome/free-solid-svg-icons";
import Button from "@mui/material/Button";
import { CircularProgress } from "@mui/material";
import CourseJoinButton from "../Course/CourseJoinButton";

const CollectionCard = ({ id, title, image, duration, onJoinSuccess, isUserCourse = false, joinType = null, isRecommended = false }) => {
  const renderRating = () => {
    const stars = [];
    const ratingValue = parseInt(duration) || 3; // Default to 3 if can't parse
    
    for (let i = 1; i <= 5; i++) {
      if (i <= ratingValue) {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon icon={faStar} key={i} className={styles.star} />
          </span>
        );
      } else {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon
              icon={faStar}
              key={stars.length}
              className={styles.star}
            />
          </span>
        );
      }
    }

    return stars;
  };

  return (
    <Card key={id} className={styles.collection_card}>
      <div className={styles.card_header}>
        <Card.Img variant="top" src={image || 'https://via.placeholder.com/300x180?text=Course+Image'} className={styles.img} />
        {isUserCourse && joinType && (
          <div className={styles.course_badge}>
            <span className={`${styles.badge} ${isRecommended ? styles.recommended : styles.self_enrolled}`}>
              {joinType}
            </span>
          </div>
        )}
      </div>
      
      <Card.Body className={styles.card_body}>
        <Card.Title>{title}</Card.Title>
        <div className={styles.rating}>
          {renderRating()}
          <p style={{ marginLeft: "10px" }}>5 rating</p>
        </div>
        <svg
          className={styles.line}
          xmlns="http://www.w3.org/2000/svg"
          width="414"
          height="2"
          viewBox="0 0 414 2"
          fill="none"
        >
          <path d="M0 1H413.5" stroke="#6A6B6C" strokeWidth="0.5" />
        </svg>
        <div className={styles.footer}>
          <p className={styles.collection_duration}>Duration: {duration || 'Unknown'}</p>
          <div className={styles.course_actions}>
            <Link to={`/collections/${id}/days/1`} className={styles.link}>
              <button className={styles.btn}>
                {isUserCourse ? 'Tiếp tục học' : 'Xem chi tiết'}
              </button>
            </Link>
            {!isUserCourse && (
              <CourseJoinButton 
                courseId={id}
                courseName={title}
                onJoinSuccess={onJoinSuccess}
              />
            )}
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

const Collections_card = ({ selectedCategory, recommendedCourses, loading, onCourseJoinSuccess }) => {
  const [dataCourse, setDataCourse] = useState([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [error, setError] = useState(null);
  const [filteredCards, setFilteredCards] = useState([]);

  const getCardsFunction = async () => {
    try {
      setLoadingCourses(true);
      setError(null);
      console.log("Starting to fetch courses...");
      const cards = await collectionService.getCards();
      console.log("API Response:", cards);
      
      if (!cards) {
        console.error("No cards data received from API");
        setError("No courses data received");
        return;
      }
      
      if (!Array.isArray(cards)) {
        console.error("Received data is not an array:", cards);
        setError("Invalid courses data format");
        return;
      }

      if (cards.length === 0) {
        console.log("Received empty courses array");
        setError("No courses available");
        return;
      }

      console.log("Successfully loaded", cards.length, "courses");
      setDataCourse(cards);
    } catch (error) {
      console.error("Error details:", {
        message: error.message,
        stack: error.stack,
        response: error.response
      });
      setError(error.message || "Error loading courses");
      setDataCourse([]);
    } finally {
      setLoadingCourses(false);
    }
  };

  // Load courses when component mounts
  useEffect(() => {
    getCardsFunction();
  }, []);

  // Filter courses whenever the category changes or courses/recommended courses update
  useEffect(() => {
    if (!dataCourse || dataCourse.length === 0) return;
    
    console.log("Filtering with category:", selectedCategory);
    console.log("Available courses:", dataCourse);
    console.log("User courses:", recommendedCourses);
    
    let filtered;
    
    if (selectedCategory === "My Course") {
      // For My Course tab, show user's courses if any exist
      if (recommendedCourses && recommendedCourses.length > 0) {
        // Map user's courses to match the expected format
        filtered = recommendedCourses.map(userCourse => ({
          courseId: userCourse.courseId,
          courseName: userCourse.courseName,
          description: userCourse.description,
          duration: userCourse.duration,
          image: userCourse.image,
          courseType: userCourse.courseType,
          isUserCourse: true,
          joinType: userCourse.joinType || (userCourse.isRecommended ? 'Recommended' : 'Self-enrolled'),
          isRecommended: userCourse.isRecommended
        }));
        console.log("Showing user's courses in My Course tab:", filtered);
      } else {
        // No user courses, show empty state
        filtered = [];
        console.log("No user courses found for My Course tab");
      }
    } else if (selectedCategory === "All") {
      filtered = dataCourse;
    } else {
      filtered = dataCourse.filter(course => {
        const typeName = course.courseType?.courseTypeName;
        return typeName === selectedCategory;
      });
    }
    
    setFilteredCards(filtered || []);
  }, [selectedCategory, dataCourse, recommendedCourses]);

  if (loading || loadingCourses) {
    return (
      <div className={styles.loading_container}>
        <CircularProgress color="error" />
        <p>Đang tải khóa học...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.error_container}>
        <p className={styles.error_message}>{error}</p>
        <Button 
          variant="contained" 
          color="error" 
          onClick={getCardsFunction}
          style={{ marginTop: '10px' }}
        >
          Thử lại
        </Button>
      </div>
    );
  }

  const isMyCourseWithNoData = selectedCategory === "My Course" && filteredCards.length === 0;

  return (
    <div className={styles.collection}>
      {filteredCards.length > 0 ? (
        filteredCards.map((course) => (
          <CollectionCard
            key={course.courseId}
            id={course.courseId}
            title={course.courseName}
            description={course.description}
            duration={course.duration}
            image={course.image}
            isUserCourse={course.isUserCourse || false}
            joinType={course.joinType}
            isRecommended={course.isRecommended || false}
            onJoinSuccess={onCourseJoinSuccess}
          />
        ))
      ) : (
        <div className={styles.bn_left}>
          <div className={styles.bn_left_1}>
            <p className={styles.typo}>
              <span className={styles.typo_red}>
                {isMyCourseWithNoData ? "Chưa có" : "No"}
              </span> {isMyCourseWithNoData ? "khóa học nào" : "courses found"}
            </p>
            <p className={styles.typo_des}>
              {isMyCourseWithNoData ? (
                <>
                  <span>Bạn chưa tham gia khóa học nào. Hãy khám phá các khóa học khác!</span>
                  <Button variant="contained" className={styles.bn_left_btn}>
                    <Link to="/recommend" style={{ color: "#fff" }}>
                      Nhận gợi ý
                    </Link>
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="contained" className={styles.bn_left_btn}>
                    <Link to="/recommend" style={{ color: "#fff" }}>
                      Start now
                    </Link>
                  </Button>
                  <span>to receive personalized recommendations.</span>
                </>
              )}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Collections_card;
