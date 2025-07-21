import React from 'react';
import { Clock } from 'lucide-react';
import { useScheduler } from '../../hooks/useScheduler';
import SchedulerStatusCard from '../scheduler/SchedulerStatusCard';
import SchedulerConfigForm from '../scheduler/SchedulerConfigForm';
import JobHistoryTable from '../scheduler/JobHistoryTable';

const SchedulerPage = () => {
  const {
    schedulerStatus,
    statusLoading,
    startScheduler,
    stopScheduler,
    updateSchedulerConfig,
    runJobNow,
    isStarting,
    isStopping,
    isUpdatingConfig,
    isRunningJob,
  } = useScheduler();

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center space-x-3">
        <div className="p-2 bg-purple-600 rounded-lg">
          <Clock className="h-6 w-6 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Scheduler Management</h1>
          <p className="text-slate-600">Monitor and configure the job scheduler</p>
        </div>
      </div>

      {/* Status and Quick Actions */}
      <SchedulerStatusCard
        schedulerStatus={schedulerStatus}
        statusLoading={statusLoading}
        startScheduler={startScheduler}
        stopScheduler={stopScheduler}
        runJobNow={runJobNow}
        isStarting={isStarting}
        isStopping={isStopping}
        isRunningJob={isRunningJob}
      />

      {/* Configuration Form */}
      <SchedulerConfigForm
        schedulerStatus={schedulerStatus}
        updateSchedulerConfig={updateSchedulerConfig}
        isUpdatingConfig={isUpdatingConfig}
      />

      {/* Job History */}
      <JobHistoryTable />
    </div>
  );
};

export default SchedulerPage;