import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { ftpConfigSchema } from '../../utils/validation';
import FormInput from './FormInput';
import FormSection from './FormSection';
import Button from '../ui/Button';
import { useFtpConfig } from '../../hooks/useFtpConfig';

const FtpConfigForm = () => {
  const { ftpConfig, updateFtpConfig, isUpdating } = useFtpConfig();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: yupResolver(ftpConfigSchema),
  });

  useEffect(() => {
    if (ftpConfig) {
      reset(ftpConfig);
    }
  }, [ftpConfig, reset]);

  const onSubmit = (data) => {
    updateFtpConfig(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
      <FormSection title="FTP/SFTP Settings" description="Configure remote file transfer">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <FormInput label="FTP Server" {...register('vineFtpServer')} error={errors.vineFtpServer?.message} required />
          <FormInput label="FTP Username" {...register('vineFtpUsername')} error={errors.vineFtpUsername?.message} required />
          <FormInput label="FTP Password" type="password" {...register('vineFtpPassword')} error={errors.vineFtpPassword?.message} required />
        </div>
      </FormSection>

      <div className="flex justify-end">
        <Button type="submit" loading={isUpdating} className="bg-blue-600 hover:bg-blue-700">
          Save FTP Configuration
        </Button>
      </div>
    </form>
  );
};

export default FtpConfigForm;