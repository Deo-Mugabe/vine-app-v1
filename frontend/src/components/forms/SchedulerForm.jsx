import React, { useState, useEffect } from 'react';
import { Play, Square, RotateCcw, Clock, History, Settings, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { useScheduler } from '../hooks/useScheduler';
import Button from '../ui/Button';
import FormInput from './FormInput';
import FormSection from './FormSection';
import LoadingSpinner from '../ui/LoadingSpinner';
import StatusMessage from '../ui/StatusMessage';

const SchedulerForm = () => {
  const {
    schedulerStatus,
    schedulerHistory,
    isLoadingStatus,
    isLoadingHistory,
    startScheduler,
    stopScheduler,
    updateConfig,
    triggerJobNow,
    fetchHistory,
    isStarting,
    isStopping,
    isUpdatingConfig,
    isTriggeringJob
  } = useScheduler();

  const [configForm, setConfigForm] = useState({
    enabled: false,
    intervalMinutes: 30,
    startFromTime: ''
  });

  const [historyFilters, setHistoryFilters] = useState({
    page: 0,
    size: 20,
    days: null
  });

  // Update form when status is loaded
  useEffect(() => {
    if (schedulerStatus) {
      setConfigForm({
        enabled: schedulerStatus.running || false,
        intervalMinutes: schedulerStatus.intervalMinutes || 30,
        startFromTime: schedulerStatus.startFromTime || ''
      });
    }
  }, [schedulerStatus]);

  const handleStart = () => {
    startScheduler(configForm.intervalMinutes);
  };

  const handleStop = () => {
    stopScheduler();
  };

  const handleUpdateConfig = (e) => {
    e.preventDefault();
    updateConfig(configForm.enabled, configForm.intervalMinutes, configForm.startFromTime);
  };

  const handleTriggerNow = () => {
    triggerJobNow();
  };

  const handleHistoryRefresh = () => {
    fetchHistory(historyFilters.page, historyFilters.size, historyFilters.days);
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'completed':
      case 'success':
        return <CheckCircle className="h-4 w-4 text-green-600" />;
      case 'failed':
      case 'error':
        return <XCircle className="h-4 w-4 text-red-600" />;
      case 'running':
        return <Clock className="h-4 w-4 text-blue-600 animate-pulse" />;
      default:
        return <AlertCircle className="h-4 w-4 text-yellow-600" />;
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const formatDuration = (startTime, endTime) => {
    if (!startTime || !endTime) return 'N/A';
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end - start;
    const diffSecs = Math.floor(diffMs / 1000);
    const mins = Math.floor(diffSecs / 60);
    const secs = diffSecs % 60;
    return `${mins}m ${secs}s`;
  };

  if (isLoadingStatus) {
    return (
      <div className="flex justify-center items-center py-8">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Current Status Section */}
      <FormSection
        title="Scheduler Status"
        description="Current status and controls for the scheduled job"
      >
        <div className="bg-gray-50 p-4 rounded-lg">
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="space-y-2">
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium">Status:</span>
                <span className={`px-2 py-1 rounded text-xs font-medium ${
                  schedulerStatus?.running 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                }`}>
                  {schedulerStatus?.running ? 'Running' : 'Stopped'}
                </span>
              </div>
              
              <div className="text-sm">
                <span className="font-medium">Interval:</span> {schedulerStatus?.intervalMinutes || 'N/A'} minutes
              </div>
              
              <div className="text-sm">
                <span className="font-medium">Next Run:</span> {
                  schedulerStatus?.nextRunTime ? formatDateTime(schedulerStatus.nextRunTime) : 'N/A'
                }
              </div>
            </div>
            
            <div className="space-y-2">
              <div className="text-sm">
                <span className="font-medium">Last Run:</span> {
                  schedulerStatus?.lastRunTime ? formatDateTime(schedulerStatus.lastRunTime) : 'N/A'
                }
              </div>
              
              <div className="text-sm">
                <span className="font-medium">Last Status:</span> {schedulerStatus?.lastRunStatus || 'N/A'}
              </div>
              
              <div className="text-sm">
                <span className="font-medium">Total Runs:</span> {schedulerStatus?.totalRuns || 0}
              </div>
            </div>
          </div>
          
          {/* Control Buttons */}
          <div className="flex space-x-3">
            <Button
              onClick={handleStart}
              loading={isStarting}
              disabled={schedulerStatus?.running}
              className="bg-green-600 hover:bg-green-700 disabled:bg-gray-400"
            >
              <Play className="h-4 w-4 mr-1" />
              Start
            </Button>
            
            <Button
              onClick={handleStop}
              loading={isStopping}
              disabled={!schedulerStatus?.running}
              className="bg-red-600 hover:bg-red-700 disabled:bg-gray-400"
            >
              <Square className="h-4 w-4 mr-1" />
              Stop
            </Button>
            
            <Button
              onClick={handleTriggerNow}
              loading={isTriggeringJob}
              className="bg-blue-600 hover:bg-blue-700"
            >
              <RotateCcw className="h-4 w-4 mr-1" />
              Run Now
            </Button>
          </div>
        </div>
      </FormSection>

      {/* Configuration Section */}
      <FormSection
        title="Scheduler Configuration"
        description="Configure scheduler settings and timing"
      >
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4">
            <FormInput
              label="Interval (minutes)"
              type="number"
              min="1"
              max="1440"
              value={configForm.intervalMinutes}
              onChange={(e) => setConfigForm(prev => ({
                ...prev,
                intervalMinutes: parseInt(e.target.value) || 30
              }))}
              required
            />
            
            <FormInput
              label="Start From Time"
              type="time"
              value={configForm.startFromTime}
              onChange={(e) => setConfigForm(prev => ({
                ...prev,
                startFromTime: e.target.value
              }))}
            />
            
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="enabled"
                checked={configForm.enabled}
                onChange={(e) => setConfigForm(prev => ({
                  ...prev,
                  enabled: e.target.checked
                }))}
                className="rounded focus:ring-blue-500"
              />
              <label htmlFor="enabled" className="text-sm font-medium">
                Enable Scheduler
              </label>
            </div>
          </div>
          
          <Button
            onClick={handleUpdateConfig}
            loading={isUpdatingConfig}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Settings className="h-4 w-4 mr-1" />
            Update Configuration
          </Button>
        </div>
      </FormSection>

      {/* Job History Section */}
      <FormSection
        title="Job Execution History"
        description="Recent job execution history and results"
      >
        <div className="space-y-4">
          {/* History Controls */}
          <div className="flex justify-between items-center">
            <div className="flex space-x-3">
              <select
                value={historyFilters.days || ''}
                onChange={(e) => {
                  const days = e.target.value ? parseInt(e.target.value) : null;
                  setHistoryFilters(prev => ({ ...prev, days }));
                }}
                className="px-3 py-1 border rounded focus:ring-blue-500 focus:border-blue-500 text-sm"
              >
                <option value="">All time</option>
                <option value="1">Last 24 hours</option>
                <option value="7">Last 7 days</option>
                <option value="30">Last 30 days</option>
              </select>
              
              <select
                value={historyFilters.size}
                onChange={(e) => setHistoryFilters(prev => ({
                  ...prev,
                  size: parseInt(e.target.value)
                }))}
                className="px-3 py-1 border rounded focus:ring-blue-500 focus:border-blue-500 text-sm"
              >
                <option value="10">10 per page</option>
                <option value="20">20 per page</option>
                <option value="50">50 per page</option>
              </select>
            </div>
            
            <Button
              onClick={handleHistoryRefresh}
              loading={isLoadingHistory}
              className="bg-gray-600 hover:bg-gray-700"
            >
              <History className="h-4 w-4 mr-1" />
              Refresh
            </Button>
          </div>

          {/* History Table */}
          {isLoadingHistory ? (
            <LoadingSpinner />
          ) : schedulerHistory?.executions?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Start Time</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">End Time</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Message</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {schedulerHistory.executions.map((execution, index) => (
                    <tr key={execution.id || index} className="hover:bg-gray-50">
                      <td className="px-4 py-2 whitespace-nowrap">
                        <div className="flex items-center space-x-2">
                          {getStatusIcon(execution.status)}
                          <span className="text-sm">{execution.status}</span>
                        </div>
                      </td>
                      <td className="px-4 py-2 whitespace-nowrap text-sm">
                        {formatDateTime(execution.startTime)}
                      </td>
                      <td className="px-4 py-2 whitespace-nowrap text-sm">
                        {formatDateTime(execution.endTime)}
                      </td>
                      <td className="px-4 py-2 whitespace-nowrap text-sm">
                        {formatDuration(execution.startTime, execution.endTime)}
                      </td>
                      <td className="px-4 py-2 text-sm">
                        <div className="max-w-xs truncate" title={execution.message}>
                          {execution.message || 'No message'}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              
              {/* Pagination Info */}
              {schedulerHistory.totalElements && (
                <div className="mt-4 text-sm text-gray-600">
                  Showing {schedulerHistory.executions.length} of {schedulerHistory.totalElements} executions
                  {schedulerHistory.totalPages > 1 && (
                    <span> (Page {historyFilters.page + 1} of {schedulerHistory.totalPages})</span>
                  )}
                </div>
              )}
            </div>
          ) : (
            <StatusMessage message="No job execution history available" type="info" />
          )}
        </div>
      </FormSection>
    </div>
  );
};

export default SchedulerForm;