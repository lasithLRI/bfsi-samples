export interface MessageData {

    id: string,
    mtMessageType: string,
    mxMessageType: string,
    direction: string,
    amount: string,
    currency: string,
    date: string,
    status: string,
    originalMessage: string,
    translatedMessage: string,
    fieldError: string,
    notSupportedError: string,
    invalidError: string,
    otherError: string
  
  }

// Message chart data interface
export interface MessageChartData {
    date: string;
    displayDate?: string;
    inward: {
      success: number;
      fail: number;
    };
    outward: {
      success: number;
      fail: number;
    };
  }

// Direction stats interface
export interface DirectionStats {
  inward: {
    successful: number;
    failed: number;
    pending: number;
    total: number;
  };
  outward: {
    successful: number;
    failed: number;
    pending: number;
    total: number;
  };
  total: number;
}

export interface TimeSpecificData {
    successCount: number;
    failCount: number;
    inwardCount: number;
    outwardCount: number;
    totalCount: number;
    successPercentage: number;
    failPercentage: number;
    period: string;
    date?: string; // For daily
    startDate?: string; // For weekly/monthly
    endDate?: string; // For weekly/monthly
  }
  
  // Add interface for MessageCounts props
  export interface MessageCountsProps {
    successCount: number;
    failCount: number;
    timeSpecificData: TimeSpecificData | null;
    direction: string;
  }


  