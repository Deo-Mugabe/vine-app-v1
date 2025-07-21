import React, { useState } from 'react';
import { Settings, Server, FolderOpen, Clock } from 'lucide-react';
import TabNavigation from '../ui/TabNavigation';
import VineConfigForm from '../forms/VineConfigForm';
import FtpConfigForm from '../forms/FtpConfigForm';
import SchedulerForm from '../forms/SchedulerForm';
import Card from '../ui/Card';

const SystemConfigPage = () => {
  const [activeTab, setActiveTab] = useState('vine');

  const tabs = [
    { id: 'vine', label: 'VINE Setup', icon: FolderOpen },
    { id: 'ftp', label: 'FTP Setup', icon: Server },
    { id: 'scheduler', label: 'Job Scheduler', icon: Clock },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <div className="p-2 bg-blue-600 rounded-lg">
          <Settings className="h-6 w-6 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-800">System Configuration</h1>
          <p className="text-slate-600">Manage VINE, FTP, and Scheduler settings</p>
        </div>
      </div>

      <TabNavigation
        tabs={tabs}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      <Card>
        {activeTab === 'vine' && <VineConfigForm />}
        {activeTab === 'ftp' && <FtpConfigForm />}
        {activeTab === 'scheduler' && <SchedulerForm />}
      </Card>
    </div>
  );
};

export default SystemConfigPage;