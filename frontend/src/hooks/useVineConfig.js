import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { configService } from '../services/configService';

export const useVineConfig = () => {
  const queryClient = useQueryClient();

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
    },
  });

  const updateMutation = useMutation({
    mutationFn: configService.updateFileConfig,
    onSuccess: (data) => {
      queryClient.setQueryData(['vineConfig'], data);
      queryClient.invalidateQueries(['vineConfig']);
    },
    onError: (error) => {
      console.error('Error updating VINE config:', error);
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

export const useFtpConfig = () => {
  const queryClient = useQueryClient();

  const {
    data: ftpConfig,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['ftpConfig'],
    queryFn: configService.getFtpConfig,
    staleTime: 5 * 60 * 1000,
    cacheTime: 10 * 60 * 1000,
    retry: 2,
    onError: (error) => {
      console.error('Error fetching FTP config:', error);
    },
  });

  const updateMutation = useMutation({
    mutationFn: configService.updateFtpConfig,
    onSuccess: (data) => {
      queryClient.setQueryData(['ftpConfig'], data);
      queryClient.invalidateQueries(['ftpConfig']);
    },
    onError: (error) => {
      console.error('Error updating FTP config:', error);
    },
  });

  return {
    ftpConfig,
    isLoading,
    error,
    refetch,
    updateFtpConfig: updateMutation.mutate,
    isUpdating: updateMutation.isLoading,
    updateError: updateMutation.error,
    updateSuccess: updateMutation.isSuccess,
  };
};