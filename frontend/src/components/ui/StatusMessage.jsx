import React from 'react';

const StatusMessage = ({ message, type = 'info' }) => {
  const color = {
    success: 'text-green-600',
    error: 'text-red-600',
    info: 'text-blue-600',
  }[type];

  return <div className={`p-2 font-medium ${color}`}>{message}</div>;
};

export default StatusMessage;