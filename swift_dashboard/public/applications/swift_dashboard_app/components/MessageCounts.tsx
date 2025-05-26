import React from 'react';
import { MessageCountsProps } from './types';
import './commonStyles.scss';

const MessageCounts: React.FC<MessageCountsProps> = ({ successCount, failCount, timeSpecificData, direction }) => {
  // Use timeSpecificData if provided, otherwise use the passed-in counts
  const total = timeSpecificData ? timeSpecificData.totalCount : (successCount + failCount);
  const success = timeSpecificData ? timeSpecificData.successCount : successCount;
  const fail = timeSpecificData ? timeSpecificData.failCount : failCount;
  const inward = timeSpecificData ? timeSpecificData.inwardCount : 0;
  const outward = timeSpecificData ? timeSpecificData.outwardCount : 0;
  
  const successPercentage = total > 0 ? Math.round((success / total) * 100) : 0;
  const failPercentage = total > 0 ? Math.round((fail / total) * 100) : 0;
  const inwardPercentage = total > 0 ? Math.round((inward / total) * 100) : 0;
  const outwardPercentage = total > 0 ? Math.round((outward / total) * 100) : 0;
  
  return (
    <div className="message-counts">
      {/* Total count display */}
      <div className={`count-item total ${direction}`}>
        <div className="count-header">
          <span className="count-name total">Total</span>
        </div>
        <div className="count-value total">{total}</div>
      </div>
      
      {/* Direction breakdown (Only shown when direction is 'All') */}
      {direction === 'All' && (
        <div className="counts-row direction">
          <div className="count-item inward">
            <div className="count-header">
              <div className="count-indicator inward"></div>
              <span className="count-name">Inward</span>
            </div>
            <div className="count-value">{inward}</div>
            <div className="count-percentage">{inwardPercentage}%</div>
          </div>
          <div className="count-item outward">
            <div className="count-header">
              <div className="count-indicator outward"></div>
              <span className="count-name">Outward</span>
            </div>
            <div className="count-value">{outward}</div>
            <div className="count-percentage">{outwardPercentage}%</div>
          </div>
        </div>
      )}
      
      {/* Success/Failure breakdown */}
      <div className="counts-row">
        <div className="count-item success">
          <div className="count-header">
            <div className="count-indicator success"></div>
            <span className="count-name">Successful</span>
          </div>
          <div className="count-value">{success}</div>
          <div className="count-percentage">{successPercentage}%</div>
        </div>
        <div className="count-item fail">
          <div className="count-header">
            <div className="count-indicator fail"></div>
            <span className="count-name">Failure</span>
          </div>
          <div className="count-value">{fail}</div>
          <div className="count-percentage">{failPercentage}%</div>
        </div>
      </div>
    </div>
  );
};

export default MessageCounts;