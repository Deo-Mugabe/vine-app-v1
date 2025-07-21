import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { Settings } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import FormInput from '../forms/FormInput';
import FormSection from '../forms/FormSection';

const configSchema = yup.object().shape({
  enabled: yup.boolean().default(false),
  intervalMinutes: yup
    .number()
    .positive('Interval must be a positive number')
    .integer('Interval must be an integer')
    .min(1, 'Interval must be at least 1 minute')
    .max(1440, 'Interval cannot exceed 1440 minutes (24 hours)')
    .required('Interval is required'),
  startFromTime: yup
    .string()
    .matches(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Start time must be in HH:MM format')
    .nullable(),
});

const SchedulerConfigForm = ({
  schedulerStatus,
  updateSchedulerConfig,
  isUpdatingConfig,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    resolver: yupResolver(configSchema),
    defaultValues: {
      enabled: false,
      intervalMinutes: 30,
      startFromTime: '',
    },
  });

  const enabled = watch('enabled');

  // Reset form when scheduler status changes
  useEffect(() => {
    if (schedulerStatus) {
      reset({
        enabled: schedulerStatus.running || false,
        intervalMinutes: schedulerStatus.intervalMinutes || 30,
        startFromTime: schedulerStatus.startFromTime || '',
      });
    }
  }, [schedulerStatus, reset]);

  const onSubmit = (data) => {
    updateSchedulerConfig({
      enabled: data.enabled,
      intervalMinutes: parseInt(data.intervalMinutes),
      startFromTime: data.startFromTime || null,
    });
  };

  return (
    <Card>
      <div className="flex items-center space-x-3 mb-6">
        <div className="p-2 bg-blue-600 rounded-lg">
          <Settings className="h-5 w-5 text-white" />
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Scheduler Configuration</h3>
          <p className="text-sm text-gray-500">Configure scheduler settings and timing</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <FormSection
          title="Basic Settings"
          description="Enable or disable the scheduler and set execution interval"
        >
          <div className="flex items-center space-x-3">
            <input
              type="checkbox"
              id="enabled"
              {...register('enabled')}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="enabled" className="text-sm font-medium text-gray-700">
              Enable Scheduler
            </label>
          </div>

          <FormInput
            label="Interval (minutes)"
            type="number"
            placeholder="30"
            disabled={!enabled}
            {...register('intervalMinutes')}
            error={errors.intervalMinutes?.message}
            required
          />
        </FormSection>

        <FormSection
          title="Advanced Settings"
          description="Optional settings for scheduler behavior"
        >
          <FormInput
            label="Start From Time"
            type="time"
            placeholder="09:00"
            disabled={!enabled}
            {...register('startFromTime')}
            error={errors.startFromTime?.message}
          />
          
          <div className="text-xs text-gray-500 mt-2">
            Leave empty to start immediately when enabled. Format: HH:MM (24-hour)
          </div>
        </FormSection>

        <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
          <Button
            type="button"
            onClick={() => reset()}
            className="bg-gray-500 hover:bg-gray-600"
          >
            Reset
          </Button>
          
          <Button
            type="submit"
            loading={isUpdatingConfig}
            className="bg-blue-600 hover:bg-blue-700"
          >
            Update Configuration
          </Button>
        </div>
      </form>
    </Card>
  );
};

export default SchedulerConfigForm;