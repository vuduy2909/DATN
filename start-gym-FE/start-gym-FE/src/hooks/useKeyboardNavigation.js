import { useEffect } from 'react';

const useKeyboardNavigation = (handleNext, handlePrevious, disabled = false, currentStep = null, totalSteps = 7) => {
    useEffect(() => {
        const handleKeyDown = (event) => {
            // Chỉ xử lý phím tắt khi không đang focus vào input/textarea/select
            if (event.target.tagName === 'INPUT' || 
                event.target.tagName === 'TEXTAREA' || 
                event.target.tagName === 'SELECT' ||
                event.target.contentEditable === 'true' ||
                disabled) {
                return;
            }

            // Prevent default behavior for arrow keys to avoid page scrolling
            if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
                event.preventDefault();
            }

            if (event.key === 'ArrowLeft' && handlePrevious) {
                // Không cho phép quay lại từ step 1
                if (currentStep && currentStep <= 1) {
                    return;
                }
                handlePrevious();
            } else if (event.key === 'ArrowRight' && handleNext) {
                // Không cho phép tiến tới từ step cuối
                if (currentStep && currentStep >= totalSteps) {
                    return;
                }
                handleNext();
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [handleNext, handlePrevious, disabled, currentStep, totalSteps]);
};

export default useKeyboardNavigation; 