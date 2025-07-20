import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { configService } from '../services/configService';
import { useNotification } from '../context/NotificationContext';

export const useFtpConfig = () => {
  const queryClient = useQueryClient();
  const { showNotification } = useNotification();

  const {
    data: ftpConfig,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['ftpConfig'],
    queryFn: configService.getFtpConfig,
    onError: () => {
      showNotification('Failed to load FTP configuration.', 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: configService.updateFtpConfig,
    onSuccess: () => {
      queryClient.invalidateQueries(['ftpConfig']);
      showNotification('FTP configuration updated successfully.', 'success');
    },
    onError: () => {
      showNotification('Failed to update FTP configuration.', 'error');
    },
  });

  return {
    ftpConfig,
    isLoading,
    error,
    refetch,
    updateFtpConfig: updateMutation.mutate,
    isUpdating: updateMutation.isLoading,
  };
};