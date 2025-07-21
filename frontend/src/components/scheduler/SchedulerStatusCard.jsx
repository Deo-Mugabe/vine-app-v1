import React from 'react';
import { Play, Square, Clock, RefreshCw } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import StatusMessage from '../ui/StatusMessage';
import LoadingSpinner from '../ui/LoadingSpinner';

const SchedulerStatusCard = ({
  schedulerStatus,
  statusLoading,
  startScheduler,
  stopScheduler,
  runJobNow,
  isStarting,
  isStopping,
  isRunningJob,
}) => {
  if (statusLoading) {
    return (
      <Card>
        <div className="flex items-center justify-center py-8">
          <LoadingSpinner />
        </div>
      </Card>
    );
  }

  const isRunning = schedulerStatus?.running;
  const lastExecution = schedulerStatus?.lastExecution;
  const nextExecution = schedulerStatus?.nextExecution;
  const intervalMinutes = schedulerStatus?.intervalMinutes;

  return (
    <Card>
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center space-x-3">
          <div className={`p-2 rounded-lg ${isRunning ? 'bg-green-100' : 'bg-gray-100'}`}>
            <Clock className={`h-6 w-6 ${isRunning ? 'text-green-600' : 'text-gray-600'}`} />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Scheduler Status</h3>
            <p className="text-sm text-gray-500">
              {isRunning ? `Running every ${intervalMinutes} minutes` : 'Stopped'}
            </p>
          </div>
        </div>
        
        <div className={`px-3 py-1 rounded-full text-sm font-medium ${
          isRunning 
            ? 'bg-green-100 text-green-800' 
            : 'bg-gray-100 text-gray-800'
        }`}>
          {isRunning ? 'Active' : 'Inactive'}
        </div>
      </div>

      {/* Execution Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div className="bg-gray-50 p-3 rounded-lg">
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider">Last Execution</p>
          <p className="text-sm text-gray-900 mt-1">
            {lastExecution ? new Date(lastExecution).toLocaleString() : 'Never'}
          </p>
        </div>
        
        <div className="bg-gray-50 p-3 rounded-lg">
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider">Next Execution</p>
          <p className="text-sm text-gray-900 mt-1">
            {nextExecution && isRunning ? new Date(nextExecution).toLocaleString() : 'N/A'}
          </p>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-3">
        {isRunning ? (
          <Button
            onClick={() => stopScheduler()}
            loading={isStopping}
            className="bg-red-600 hover:bg-red-700"
          >
            <Square className="h-4 w-4 mr-2" />
            Stop Scheduler
          </Button>
        ) : (
          <Button
            onClick={() => startScheduler(30)}
            loading={isStarting}
            className="bg-green-600 hover:bg-green-700"
          >
            <Play className="h-4 w-4 mr-2" />
            Start Scheduler
          </Button>
        )}
        
        <Button
          onClick={() => runJobNow()}
          loading={isRunningJob}
          className="bg-blue-600 hover:bg-blue-700"
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Run Now
        </Button>
      </div>

      {schedulerStatus?.lastExecutionStatus && (
        <div className="mt-4">
          <StatusMessage
            message={`Last execution: ${schedulerStatus.lastExecutionStatus}`}
            type={schedulerStatus.lastExecutionStatus === 'SUCCESS' ? 'success' : 'error'}
          />
        </div>
      )}
    </Card>
  );
};

export default SchedulerStatusCard;