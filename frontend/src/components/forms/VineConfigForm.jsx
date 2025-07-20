import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { vineConfigSchema } from '../../utils/validation';
import FormInput from './FormInput';
import FormSection from './FormSection';
import Button from '../ui/Button';
import { useVineConfig } from '../../hooks/useVineConfig';

const VineConfigForm = () => {
  const { vineConfig, updateVineConfig, isUpdating } = useVineConfig();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: yupResolver(vineConfigSchema),
  });

  useEffect(() => {
    if (vineConfig) {
      reset(vineConfig);
    }
  }, [vineConfig, reset]);

  const onSubmit = (data) => {
    updateVineConfig(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
      <FormSection title="File Paths" description="Configure VINE file locations">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <FormInput
            label="New Vine Data File Path"
            {...register('vineNewVineFilePath')}
            error={errors.vineNewVineFilePath?.message}
            required
          />
          <FormInput
            label="Vine File Name"
            {...register('vineInterFile')}
            error={errors.vineInterFile?.message}
            required
          />
        </div>
      </FormSection>

      <FormSection title="Section Headers" description="Configure data section headers">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <FormInput
            label="Charges Section Header"
            {...register('vineChargesFileHeader')}
            error={errors.vineChargesFileHeader?.message}
            required
          />
          <FormInput
            label="Prisoner Section Header"
            {...register('vinePrisonerFileHeader')}
            error={errors.vinePrisonerFileHeader?.message}
            required
          />
        </div>
      </FormSection>

      <div className="flex justify-end">
        <Button type="submit" loading={isUpdating} className="bg-blue-600 hover:bg-blue-700">
          Save Configuration
        </Button>
      </div>
    </form>
  );
};

export default VineConfigForm;