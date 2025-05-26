// src/components/ToggleBar.tsx
import React, { useState, useCallback, useEffect } from 'react';
import { debounce } from 'lodash'; // Make sure to install lodash if not already there
import './commonStyles.scss';

interface ToggleBarProps {
  options: string[];
  activeOption: string;
  onToggle: (option: string) => void;
  debounceTime?: number; // Optional debounce time in ms
}

const ToggleBar: React.FC<ToggleBarProps> = ({ 
  options, 
  activeOption, 
  onToggle, 
  debounceTime = 300 // Default debounce of 300ms
}) => {
  // Local state to track selected option for immediate UI feedback
  const [localActiveOption, setLocalActiveOption] = useState(activeOption);

  // Update local state when props change
  useEffect(() => {
    setLocalActiveOption(activeOption);
  }, [activeOption]);

  // Create a debounced version of the onToggle function
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const debouncedOnToggle = useCallback(
    debounce((option: string) => {
      onToggle(option);
    }, debounceTime),
    [onToggle, debounceTime]
  );

  // Handle button click
  const handleToggle = (option: string) => {
    // Update local state immediately for responsive UI
    setLocalActiveOption(option);
    
    // Call the debounced function to trigger API calls
    debouncedOnToggle(option);
  };

  return (
    <div className="toggle-bar">
      {options.map(option => (
        <button
          key={option}
          className={`toggle-button ${localActiveOption === option ? 'active' : ''}`}
          onClick={() => handleToggle(option)}
          // Add disabled state during debounce period to prevent rapid clicking
          disabled={localActiveOption !== activeOption && localActiveOption === option}
        >
          {option}
        </button>
      ))}
    </div>
  );
};

export default ToggleBar;