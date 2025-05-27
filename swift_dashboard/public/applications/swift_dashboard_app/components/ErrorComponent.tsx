import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import './ErrorComponent.scss';
import './commonStyles.scss';
import apiService from '../../../services/apiService';

interface SummaryComponentProps {
  title: string;
  timeFilter: string;
  direction: string;
}

interface ErrorData {
  type: string;
  count: number;
  color: string;
  errorType: string; // Add this to store the API error type
}

interface ErrorResponse {
  fieldErrors: number;
  notSupportedErrors: number;
  invalidErrors: number;
  otherErrors: number;
  totalErrors: number;
}

const SummaryComponent: React.FC<SummaryComponentProps> = ({ 
  title, 
  timeFilter, 
  direction 
}) => {
  const history = useHistory();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [errorData, setErrorData] = useState<ErrorData[]>([]);
  const [totalErrors, setTotalErrors] = useState<number>(0);

  const allColor = '#6c6c6b'; // grey
  const inwardColor = '#52524f'; // dark grey
  const outwardColor = '#868686'; // light grey
  let generalColors: string;

  // Generate appropriate color scheme for the chart
  if (direction === 'All') {
    generalColors = allColor;
  } else if (direction === 'Inward') {
    generalColors = inwardColor;
  } else {
    generalColors = outwardColor;
  }

  useEffect(() => {
    const fetchErrorStatistics = async () => {
      try {
        setLoading(true);
        
        const response = await apiService.getErrorStatistics(timeFilter, direction);
        
        const { fieldErrors, notSupportedErrors, invalidErrors, otherErrors, totalErrors } = response;
        
        // Transform API data to the format needed for display
        const formattedData: ErrorData[] = [
          { 
            type: 'Mandatory field not present', 
            count: fieldErrors, 
            color: generalColors,
            errorType: 'fieldError' 
          },
          { 
            type: 'Message not supported', 
            count: notSupportedErrors, 
            color: generalColors,
            errorType: 'notSupportedError' 
          },
          { 
            type: 'Invalid message', 
            count: invalidErrors, 
            color: generalColors,
            errorType: 'invalidError' 
          },
          { 
            type: 'Other errors', 
            count: otherErrors, 
            color: generalColors,
            errorType: 'otherError' 
          }
        ];
        
        setErrorData(formattedData);
        setTotalErrors(totalErrors);
        setError(null);
      } catch (err) {
        console.error('Error fetching error statistics:', err);
        setError('Failed to load error statistics');
        // Use empty data if API fails
        setErrorData([]);
        setTotalErrors(0);
      } finally {
        setLoading(false);
      }
    };
    
    fetchErrorStatistics();
  }, [timeFilter, direction]);

  // Handle click on error bar to navigate to messages page with filters
  const handleErrorClick = (errorType: string) => {
    history.push('/messages', { 
      state: { 
        filters: {
          status: 'Failed',
          errorType: errorType,
          timeFilter: timeFilter,
          direction: direction !== 'All' ? direction : undefined
        }
      } 
    });
  };

  // Handle click on total errors to see all failed messages
  const handleTotalErrorsClick = () => {
    history.push('/messages', { 
      state: { 
        filters: {
          status: 'Failed',
          timeFilter: timeFilter,
          direction: direction !== 'All' ? direction : undefined
        }
      } 
    });
  };

  if (loading) {
    return (
      <div className="chart-component summary-component">
        <div className="chart-header">
          <h3>{title}</h3>
        </div>
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading error statistics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="chart-component summary-component">
        <div className="chart-header">
          <h3>{title}</h3>
        </div>
        <div className="error-message">
          <p>{error}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="chart-component summary-component">
      <div className="chart-header failure">
        <h3>{title}</h3>
        <div className="chart-info-icon failure" data-tooltip="Shows the breakdown of failures based on your selected time period and direction filters. Click on an error type to view all messages with that error.">
            <span className="info-icon">i</span>
        </div>
      </div>
      
      <div className="error-summary-content">
        {totalErrors === 0 ? (
          <div className="no-errors-message">
            <p>No errors found for the selected filters</p>
          </div>
        ) : (
          <>
            <div 
              className="total-errors"
              onClick={handleTotalErrorsClick}
              title="Click to see all failed messages"
            >
              <div className="total-count">{totalErrors}</div>
              <div className="total-label">Total Failures</div>
            </div>
            
            <div className="error-breakdown">
              {errorData.map((error, index) => (
                <div key={index} className="error-item" onClick={() => handleErrorClick(error.errorType)}>
                  <div className="error-bar-container">
                    <div 
                      className="error-bar" 
                      style={{
                        width: `${error.count > 0 ? Math.max(5, (error.count / totalErrors) * 100) : 0}%`,
                        backgroundColor: error.color
                      }}
                      title={`Click to view ${error.count} ${error.type} messages`}
                    />
                  </div>
                  
                  <div className="error-details">
                    <div className="error-count" style={{color: error.color}}>
                      {error.count}
                    </div>
                    <div className="error-type">{error.type}</div>
                    <div className="error-percentage">
                      {error.count > 0 ? Math.round((error.count / totalErrors) * 100) : 0}%
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default SummaryComponent;
