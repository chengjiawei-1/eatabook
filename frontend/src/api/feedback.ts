import http from '../utils/http';

export const submitFeedback = (feedbackData: { content: string }) => {
  return http.post('/api/feedback', feedbackData);
};