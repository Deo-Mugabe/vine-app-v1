import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { configService } from '../services/configService';
import { useNotification } from '../context/NotificationContext';

export const useVineConfig = () => {
  const queryClient = useQueryClient();
  const { showNotification } = useNotification();

  const {
    data: vineConfig,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['vineConfig'],
    queryFn: configService.getFileConfig,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
    retry: 2,
    onError: (error) => {
      console.error('Error fetching VINE config:', error);
      showNotification('Failed to load VINE configuration.', 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: configService.updateFileConfig,
    onSuccess: (data) => {
      queryClient.setQueryData(['vineConfig'], data);
      queryClient.invalidateQueries(['vineConfig']);
      showNotification('VINE configuration updated successfully.', 'success');
    },
    onError: (error) => {
      console.error('Error updating VINE config:', error);
      showNotification('Failed to update VINE configuration.', 'error');
    },
  });

  return {
    vineConfig,
    isLoading,
    error,
    refetch,
    updateVineConfig: updateMutation.mutate,
    isUpdating: updateMutation.isLoading,
    updateError: updateMutation.error,
    updateSuccess: updateMutation.isSuccess,
  };
};