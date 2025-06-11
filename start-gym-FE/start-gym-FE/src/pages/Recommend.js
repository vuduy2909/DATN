import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Question from "../components/Question/Question";
import { useAuth } from "../AuthContext";

const Recommend = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in
    if (!user) {
      // Redirect to login page with a return URL to come back after login
      navigate('/login', { state: { returnUrl: '/recommend' } });
    }
  }, [user, navigate]);

  // Don't render content if not logged in
  if (!user) {
    return null;
  }

  return (
    <div>
      <Question />
    </div>
  );
};

export default Recommend;
