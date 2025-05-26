import React, { useState } from 'react';
import ChartComponent from './ChartComponent';
import SummaryComponent from './SummaryComponent';
import MessageTypeDistribution from './MessageTypeDistribution';
import RecentActivity from './RecentActivity';
import useWindowSize from './hooks/useWindowSize';
import ToggleBar from './ToggleBar';
import './commonStyles.scss';

// Define proper types for your filter options
type PeriodType = 'Daily' | 'Weekly' | 'Monthly';
type DirectionType = 'All' | 'Inward' | 'Outward';

const OverviewPage: React.FC = () => {
  const { width, height } = useWindowSize();
  const [timeFilter, setTimeFilter] = useState<PeriodType>('Monthly');
  const [directionFilter, setDirectionFilter] = useState<DirectionType>('All');

  const messageCompletion = ChartComponent({ type: "pie", direction: directionFilter, period: timeFilter });
  const messageTrend = ChartComponent({ type: "bar", direction: directionFilter, period: timeFilter });

  // Handle period toggle with proper typing
  const handlePeriodToggle = (option: PeriodType) => {
    setTimeFilter(option);
  };

  // Handle direction toggle with proper typing
  const handleDirectionToggle = (option: DirectionType) => {
    setDirectionFilter(option);
  };

  return (
    <div className="overview-page">
    <div className="toggle-footer-pie">
    <ToggleBar
            options={['Monthly', 'Weekly', 'Daily']}
            activeOption={timeFilter}
            onToggle={handlePeriodToggle as (option: string) => void}
            debounceTime={400}
          />
          <ToggleBar
            options={['All', 'Inward', 'Outward']}
            activeOption={directionFilter}
            onToggle={handleDirectionToggle as (option: string) => void}
            debounceTime={400}
          />
        </div>
      <div className="dashboard-row">
        <div className="dashboard-column completion">
          {messageCompletion}
        </div>
        <div className="dashboard-column trend">
          {messageTrend}
        </div>
      </div>
      
      <div className="dashboard-row">
        <div className="dashboard-column distribution">
          <MessageTypeDistribution title="MT Message Type Distribution" period={timeFilter} direction={directionFilter} />
        </div>
        <div className="dashboard-column activity">
          <RecentActivity title="Recent Activity" direction={directionFilter} period={timeFilter}/>
        </div>
        <div className="dashboard-column errors">
            <SummaryComponent title="Translation Failures" timeFilter={timeFilter} direction={directionFilter}/>
        </div>
      </div>
    </div>
  );
};

export default OverviewPage;