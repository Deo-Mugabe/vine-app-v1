import { useState, useEffect, useCallback } from 'react';
import { schedulerService } from '../services/schedulerService';

export const useScheduler = () => {
  const [schedulerStatus, setSchedulerStatus] = useState(null);
  const [schedulerHistory, setSchedulerHistory] = useState(null);
  const [isLoadingStatus, setIsLoadingStatus] = useState(false);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [isStopping, setIsStopping] = useState(false);
  const [isUpdatingConfig, setIsUpdatingConfig] = useState(false);
  const [isTriggeringJob, setIsTriggeringJob] = useState(false);
  const [error, setError] = useState(null);

  // Fetch scheduler status
  const fetchStatus = useCallback(async () => {
    setIsLoadingStatus(true);
    setError(null);
    try {
      const status = await schedulerService.getSchedulerStatus();
      setSchedulerStatus(status);
    } catch (err) {
      console.error('Error fetching scheduler status:', err);
      setError(err.response?.data?.message || 'Failed to fetch scheduler status');
    } finally {
      setIsLoadingStatus(false);
    }
  }, []);

  // Fetch scheduler history
  const fetchHistory = useCallback(async (page = 0, size = 20, days = null) => {
    setIsLoadingHistory(true);
    setError(null);
    try {
      const history = await schedulerService.getJobHistory(page, size, days);
      setSchedulerHistory(history);
    } catch (err) {
      console.error('Error fetching scheduler history:', err);
      setError(err.response?.data?.message || 'Failed to fetch scheduler history');
    } finally {
      setIsLoadingHistory(false);
    }
  }, []);

  // Start scheduler
  const startScheduler = useCallback(async (intervalMinutes = 30) => {
    setIsStarting(true);
    setError(null);
    try {
      const result = await schedulerService.startScheduler(intervalMinutes);
      setSchedulerStatus(result);
      // Refresh status after starting
      await fetchStatus();
    } catch (err) {
      console.error('Error starting scheduler:', err);
      setError(err.response?.data?.message || 'Failed to start scheduler');
    } finally {
      setIsStarting(false);
    }
  }, [fetchStatus]);

  // Stop scheduler
  const stopScheduler = useCallback(async () => {
    setIsStopping(true);
    setError(null);
    try {
      const result = await schedulerService.stopScheduler();
      setSchedulerStatus(result);
      // Refresh status after stopping
      await fetchStatus();
    } catch (err) {
      console.error('Error stopping scheduler:', err);
      setError(err.response?.data?.message || 'Failed to stop scheduler');
    } finally {
      setIsStopping(false);
    }
  }, [fetchStatus]);

  // Update scheduler configuration
  const updateConfig = useCallback(async (enabled, intervalMinutes, startFromTime) => {
    setIsUpdatingConfig(true);
    setError(null);
    try {
      const config = {
        enabled,
        intervalMinutes,
        startFromTime: startFromTime || null
      };
      const result = await schedulerService.updateSchedulerConfig(config);
      setSchedulerStatus(result);
      // Refresh status after updating config
      await fetchStatus();
    } catch (err) {
      console.error('Error updating scheduler config:', err);
      setError(err.response?.data?.message || 'Failed to update scheduler configuration');
    } finally {
      setIsUpdatingConfig(false);
    }
  }, [fetchStatus]);

  // Trigger job now
  const triggerJobNow = useCallback(async () => {
    setIsTriggeringJob(true);
    setError(null);
    try {
      await schedulerService.runJobNow();
      // Refresh status and history after triggering job
      await Promise.all([fetchStatus(), fetchHistory()]);
    } catch (err) {
      console.error('Error triggering job:', err);
      setError(err.response?.data?.message || 'Failed to trigger job');
    } finally {
      setIsTriggeringJob(false);
    }
  }, [fetchStatus, fetchHistory]);

  // Load initial data
  useEffect(() => {
    fetchStatus();
    fetchHistory();
  }, [fetchStatus, fetchHistory]);

  return {
    schedulerStatus,
    schedulerHistory,
    isLoadingStatus,
    isLoadingHistory,
    isStarting,
    isStopping,
    isUpdatingConfig,
    isTriggeringJob,
    error,
    startScheduler,
    stopScheduler,
    updateConfig,
    triggerJobNow,
    fetchStatus,
    fetchHistory
  };
};

export const useJobHistory = (page = 0, pageSize = 20, daysFilter = null) => {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await schedulerService.getJobHistory(page, pageSize, daysFilter);
      setData(result);
    } catch (err) {
      console.error('Error fetching job history:', err);
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, [page, pageSize, daysFilter]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    data,
    isLoading,
    error,
    refetch: fetchData
  };
};