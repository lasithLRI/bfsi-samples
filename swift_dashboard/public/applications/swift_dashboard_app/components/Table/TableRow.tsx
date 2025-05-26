import React from 'react';
import './Table.scss';

interface TableRowProps {
  data: {
    id: string;
    mtMessageType: string;
    mxMessageType: string;
    currency: string;
    date: string;
    direction: string;
    amount: string;
    status: string;
    originalMessage: string;
    translatedMessage: string;
  };
  onClick: () => void;
}

const TableRow: React.FC<TableRowProps> = ({ data, onClick }) => {
  return (
    <tr className='tableRow' onClick={onClick}>
      <td>{data.id}</td>
      <td>{data.mtMessageType}</td>
      <td>{data.mxMessageType}</td>
      <td>{data.direction}</td>
      <td>{data.amount}</td>
      <td>{data.currency}</td>
      <td>{data.date}</td>
      <td>{data.status}</td>
    </tr>
  );
};

export default TableRow;