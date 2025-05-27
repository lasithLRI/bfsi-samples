import React, { useState, useEffect, ReactElement } from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend } from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';
import apiService from '../../../services';

import './commonStyles.scss';
import './ChartComponent.scss';
import { MessageData, MessageChartData, TimeSpecificData } from './types';
import { getISOWeek} from './utils/dateUtils';
import MessageCounts from './MessageCounts';

// Register Chart.js components
ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend);

// Types
interface ChartComponentProps {
  type: string;
  direction: string;
  period: string;
}

/**
 * Chart Component - Displays various chart visualizations for message data
 */
const ChartComponent = ({ type, period, direction }: ChartComponentProps): ReactElement => {
  // State
  const [chartData, setChartData] = useState<MessageChartData[]>([]);
  const [timeSpecificData, setTimeSpecificData] = useState<TimeSpecificData | null>(null);
  const [loading, setLoading] = useState(true);
  const [weeksToShow, setWeeksToShow] = useState<number>(8);
  const [error, setError] = useState<string | null>(null);

  // Handlers
  const handleWeeksChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(event.target.value, 10);
    if (!isNaN(value) && value >= 1 && value <= 52) {
      setWeeksToShow(value);
    }
  };

  // Helper functions
  /**
   * Converts UI filter to API format
   */
  const getApiTimeframe = (filter: string): 'daily' | 'weekly' | 'monthly' => {
    switch (filter.toLowerCase()) {
      case 'daily': return 'daily';
      case 'weekly': return 'weekly';
      default: return 'monthly';
    }
  };

  /**
   * Converts direction filter to API format
   */
  const getApiDirection = (filter: string): 'inward' | 'outward' | undefined => {
    if (filter.toLowerCase() === 'inward') return 'inward';
    if (filter.toLowerCase() === 'outward') return 'outward';
    return undefined; // For 'All'
  };

  /**
   * Aggregates data for chart rendering based on current filters
   */
  const aggregateData = () => {
    if (!chartData.length) {
      return {
        successCount: 0,
        failCount: 0,
        timeLabels: [],
        inwardData: [],
        outwardData: []
      };
    }
  
    // Format labels based on time period
    const timeLabels = chartData.map(item => {
      if (item.displayDate) {
        return item.displayDate;
      }
      
      if (period === 'Daily') {
        const date = new Date(item.date);
        return date.toLocaleDateString('en-US', { weekday: 'short' });
      } else if (period === 'Weekly') {
        if (item.date.includes('W')) {
          const [_, weekPart] = item.date.split('-W');
          return `W${weekPart}`;
        } else {
          const date = new Date(item.date);
          const weekNum = getISOWeek(date);
          return `W${weekNum}`;
        }
      } else { // Monthly
        if (item.date.includes('-')) {
          const [year, monthStr] = item.date.split('-');
          const month = parseInt(monthStr, 10) - 1;
          const date = new Date(parseInt(year, 10), month, 1);
          return date.toLocaleDateString('en-US', { month: 'short' });
        } else {
          return item.date;
        }
      }
    });
  
    // Calculate message counts
    let successCount = 0;
    let failCount = 0;
    
    const inwardData = chartData.map(item => {
      const total = item.inward.success + item.inward.fail;
      if (direction !== 'Outward') {
        successCount += item.inward.success;
        failCount += item.inward.fail;
      }
      return total;
    });
    
    const outwardData = chartData.map(item => {
      const total = item.outward.success + item.outward.fail;
      if (direction !== 'Inward') {
        successCount += item.outward.success;
        failCount += item.outward.fail;
      }
      return total;
    });
  
    return {
      successCount,
      failCount,
      timeLabels,
      inwardData: direction !== 'Outward' ? inwardData : new Array(timeLabels.length).fill(0),
      outwardData: direction !== 'Inward' ? outwardData : new Array(timeLabels.length).fill(0)
    };
  };

  /**
   * Generate chart data for bar chart
   */
  const generateBarChartData = () => {
    const data = aggregateData();
    const datasets: {
      label: string;
      data: number[];
      borderRadius: number;
      backgroundColor: string;
    }[] = [];
    
    if (direction === 'All' || direction === 'Inward') {
      datasets.push({
        label: 'Inward',
        data: data.inwardData,
        borderRadius: 3,
        backgroundColor: '#52524f',
      });
    }
    
    if (direction === 'All' || direction === 'Outward') {
      datasets.push({
        label: 'Outward',
        data: data.outwardData,
        borderRadius: 3,
        backgroundColor: '#868686',
      });
    }
    
    return {
      labels: data.timeLabels,
      datasets: datasets
    };
  };

  /**
   * Generate chart data for completion status pie chart
   */
  const generateCompletionPieChartData = () => {
    return {
      labels: ['Successful', 'Failure'],
      datasets: [
        {
          label: 'Messages',
          data: [timeSpecificData?.successCount || 0, timeSpecificData?.failCount || 0],
          backgroundColor: ['#00d157', '#ff513d'],
          cutout: '70%',
          radius: '95%',
        },
      ],
    };
  };

  /**
   * Generate chart data for direction pie chart
   */
  const generateDirectionPieChartData = () => {
    return {
      labels: ['Inward', 'Outward'],
      datasets: [
        {
          label: 'Messages',
          data: [timeSpecificData?.inwardCount || 0, timeSpecificData?.outwardCount || 0],
          backgroundColor: ['#52524f', '#868686'],
          cutout: '80%',
        },
      ],
    };
  };

  const pieOptionsNoLegend = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
    },
  };

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        border: {
          display: false,
        },
        title: {
          display: true,
          text: 'Count',
          color: '#000000'
        }
      },
      y: {
        grid: {
          display: false,
        },
        border: {
          display: false,
        },
        title: {
          display: true,
          text: 'Message Type',
          color: '#000000'
        }
      },
    },
  };

  // Data fetching
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const apiTimeframe = getApiTimeframe(period);
        const apiDirection = getApiDirection(direction);
        
        // Use apiService to fetch data
        const [chartResponse, timeSpecificResponse] = await Promise.all([
            apiService.getChartData(apiTimeframe, apiDirection),
            apiService.getTimeSpecificData(apiTimeframe, apiDirection)
          ]);
        
        // Process chart data
        let filteredData = [...chartResponse];
        
        // Filter weeks if needed
        if (period === 'Weekly' && weeksToShow !== 52) {
          filteredData = filteredData.slice(-weeksToShow);
        }
        
        // Sort data chronologically
        filteredData.sort((a, b) => a.date.localeCompare(b.date));
        
        setChartData(filteredData);
        
        // Process the time-specific response
        const messages = timeSpecificResponse.messages as MessageData[];
  
        let successCount = 0;
        let failCount = 0;
        let inwardCount = 0;
        let outwardCount = 0;
  
        // Count message statistics
        messages.forEach((msg: MessageData) => {
          if (msg.status.toLowerCase() === 'successful') {
            successCount++;
          } else if (msg.status.toLowerCase() === 'failed') {
            failCount++;
          }
  
          if (msg.direction.toLowerCase() === 'inward') {
            inwardCount++;
          } else if (msg.direction.toLowerCase() === 'outward') {
            outwardCount++;
          }
        });
        
        const totalCount = successCount + failCount;
        const successPercentage = totalCount > 0 ? Math.round((successCount / totalCount) * 100) : 0;
        const failPercentage = totalCount > 0 ? Math.round((failCount / totalCount) * 100) : 0;
        
        // Set the time-specific data
        setTimeSpecificData({
          successCount,
          failCount,
          inwardCount,
          outwardCount,
          totalCount,
          successPercentage,
          failPercentage,
          period: timeSpecificResponse.period,
          date: timeSpecificResponse.date,
          startDate: timeSpecificResponse.startDate,
          endDate: timeSpecificResponse.endDate
        });
        
        setError(null);
      } catch (err) {
        console.error('Error fetching chart data:', err);
        setError('Failed to load chart data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
  
    fetchData();
  }, [period, direction, weeksToShow]);

  // UI Components
  const renderPieChart = () => {
    console.log('Rendering pie chart with data:', direction);
    return (
      <div className="chart-component">
        <div className="chart-header">
          <h3>Messages By Completion</h3>
          <div className="chart-info-icon pie" data-tooltip="Shows the distribution of messages by status (Success/Failure) based on your selected time period and direction filters">
            <span className="info-icon">i</span>
          </div>
        </div>
        <div>
          <div className="donut-chart-layout">
          {direction === 'All' ? (
            <div className="donut-chart-container">  
              <Doughnut 
                className="doughnut" 
                data={generateDirectionPieChartData()} 
                options={pieOptionsNoLegend} 
                width={200} 
                height={200}
              />
              <div style={{ position: 'absolute', top: 26, right: 0, left: 0}}>
                <Doughnut 
                  data={generateCompletionPieChartData()} 
                  options={pieOptionsNoLegend}
                />
              </div></div>) : ( 
                <div className="donut-chart-container">
                <Doughnut 
                data={generateCompletionPieChartData()} 
                options={pieOptionsNoLegend}
                width={200} 
                height={200}
              /></div> 
            )}
            <MessageCounts 
              successCount={aggregateData().successCount}
              failCount={aggregateData().failCount}
              timeSpecificData={timeSpecificData}
              direction = {direction}
            />
          </div>
          {direction === 'All' ? (
          <div className="single-line-legend">
            <div className="legend-item">
              <div className="legend-color outer inward"></div>
              <div className="legend-label">Inward</div>
            </div>
            <div className="legend-item">
              <div className="legend-color outer outward"></div>
              <div className="legend-label">Outward</div>
            </div>
            <div className="legend-item">
              <div className="legend-color inner success"></div>
              <div className="legend-label">Successful</div>
            </div>
            <div className="legend-item">
              <div className="legend-color inner failure"></div>
              <div className="legend-label">Failure</div>
            </div>
          </div>) : (
            <div className="single-line-legend">
            <div className="legend-item">
              <div className="legend-color inner success"></div>
              <div className="legend-label">Successful</div>
            </div>
            <div className="legend-item">
              <div className="legend-color inner failure"></div>
              <div className="legend-label">Failure</div>
            </div>
          </div>
          )}
        </div>
      </div>
    );
  };

  const renderBarChart = () => {
    return (
      <div className="chart-component">
        <div className="chart-header">
          <h3>Messages Trend</h3>
          <div className="chart-info-icon bar" data-tooltip="Shows the volume of messages over time, segmented by direction (Inward/Outward) and period (Monthly/Weekly/Daily). For weekly views, you can adjust the number of weeks shown using the input below.">
            <span className="info-icon">i</span>
          </div>
        </div>
        <div>
          <div className="chart-container">
            <Bar data={generateBarChartData()} options={barOptions} />
          </div>
          <div className="toggle-footer-bar">
            {period === 'Weekly' && (
              <div className="weeks-input-container">
                <label htmlFor="weeks-to-show">Number of weeks: </label>
                <input
                  id="weeks-to-show"
                  type="number"
                  min="1"
                  max="52"
                  value={weeksToShow}
                  onChange={handleWeeksChange}
                  className="weeks-input"
                />
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Loading and error states
  if (loading) {
    return (
      <div className="chart-component">
        <div className='loading-indicator-chart'>
          <div className='spinner-chart'></div>
          <span>Loading chart data...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="chart-component">
        <div className='error-message-chart'>
          <div>{error}</div>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </div>
    );
  }

  // Render appropriate chart based on type
  return type === 'pie' ? renderPieChart() : renderBarChart();
};

export default ChartComponent;
