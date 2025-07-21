import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { schedulerService } from '../services/schedulerService';
import { useNotification } from '../context/NotificationContext';

export const useScheduler = () => {
  const queryClient = useQueryClient();
  const { showNotification } = useNotification();

  // Get scheduler status
  const {
    data: schedulerStatus,
    isLoading: isLoadingStatus,
    error: statusError,
    refetch: refetchStatus,
  } = useQuery({
    queryKey: ['schedulerStatus'],
    queryFn: schedulerService.getSchedulerStatus,
    refetchInterval: 5000, // Refresh every 5 seconds for live updates
    onError: (error) => {
      console.error('Error fetching scheduler status:', error);
      showNotification('Failed to load scheduler status.', 'error');
    },
  });

  // Get scheduler history
  const {
    data: schedulerHistory,
    isLoading: isLoadingHistory,
    error: historyError,
    refetch: refetchHistory,
  } = useQuery({
    queryKey: ['schedulerHistory'],
    queryFn: () => schedulerService.getJobHistory(0, 20, null),
    staleTime: 30 * 1000, // 30 seconds
    onError: (error) => {
      console.error('Error fetching scheduler history:', error);
      showNotification('Failed to load job history.', 'error');
    },
  });

  // Start scheduler mutation
  const startMutation = useMutation({
    mutationFn: schedulerService.startScheduler,
    onSuccess: () => {
      queryClient.invalidateQueries(['schedulerStatus']);
      showNotification('Scheduler started successfully.', 'success');
    },
    onError: (error) => {
      console.error('Error starting scheduler:', error);
      showNotification('Failed to start scheduler.', 'error');
    },
  });

  // Stop scheduler mutation
  const stopMutation = useMutation({
    mutationFn: schedulerService.stopScheduler,
    onSuccess: () => {
      queryClient.invalidateQueries(['schedulerStatus']);
      showNotification('Scheduler stopped successfully.', 'success');
    },
    onError: (error) => {
      console.error('Error stopping scheduler:', error);
      showNotification('Failed to stop scheduler.', 'error');
    },
  });

  // Update config mutation
  const updateConfigMutation = useMutation({
    mutationFn: ({ enabled, intervalMinutes, startFromTime }) =>
      schedulerService.updateSchedulerConfig({ enabled, intervalMinutes, startFromTime }),
    onSuccess: () => {
      queryClient.invalidateQueries(['schedulerStatus']);
      showNotification('Scheduler configuration updated successfully.', 'success');
    },
    onError: (error) => {
      console.error('Error updating scheduler config:', error);
      showNotification('Failed to update scheduler configuration.', 'error');
    },
  });

  // Trigger job now mutation
  const triggerJobMutation = useMutation({
    mutationFn: schedulerService.runJobNow,
    onSuccess: () => {
      queryClient.invalidateQueries(['schedulerStatus']);
      queryClient.invalidateQueries(['schedulerHistory']);
      showNotification('Job triggered successfully.', 'success');
    },
    onError: (error) => {
      console.error('Error triggering job:', error);
      showNotification('Failed to trigger job manually.', 'error');
    },
  });

  // Fetch history with custom parameters
  const fetchHistory = async (page = 0, size = 20, days = null) => {
    try {
      const data = await schedulerService.getJobHistory(page, size, days);
      queryClient.setQueryData(['schedulerHistory'], data);
      return data;
    } catch (error) {
      console.error('Error fetching history:', error);
      showNotification('Failed to fetch job history.', 'error');
      throw error;
    }
  };

  return {
    // Data
    schedulerStatus,
    schedulerHistory,
    
    // Loading states
    isLoadingStatus,
    isLoadingHistory,
    
    // Errors
    statusError,
    historyError,
    
    // Actions
    startScheduler: startMutation.mutate,
    stopScheduler: stopMutation.mutate,
    updateConfig: updateConfigMutation.mutate,
    triggerJobNow: triggerJobMutation.mutate,
    fetchHistory,
    
    // Action loading states
    isStarting: startMutation.isLoading,
    isStopping: stopMutation.isLoading,
    isUpdatingConfig: updateConfigMutation.isLoading,
    isTriggeringJob: triggerJobMutation.isLoading,
    
    // Refetch functions
    refetchStatus,
    refetchHistory,
  };
};