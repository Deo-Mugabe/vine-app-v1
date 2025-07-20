import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { ftpConfigSchema } from '../../utils/validation';
import FormInput from './FormInput';
import FormSection from './FormSection';
import Button from '../ui/Button';
import { useFtpConfig } from '../../hooks/useFtpConfig';
import { Edit, Save, X, AlertTriangle, Server, Shield } from 'lucide-react';

const FtpConfigForm = () => {
  const { ftpConfig, updateFtpConfig, isUpdating, isLoading } = useFtpConfig();
  const [isEditing, setIsEditing] = useState(false);
  const [showSaveConfirmation, setShowSaveConfirmation] = useState(false);
  const [pendingData, setPendingData] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    reset,
    watch,
  } = useForm({
    resolver: yupResolver(ftpConfigSchema),
    defaultValues: {
      ftpHost: '',
      ftpPort: 21,
      ftpUsername: '',
      ftpPassword: '',
      ftpRemotePath: '',
      useSftp: false,
    }
  });

  const useSftp = watch('useSftp');
  const ftpPort = watch('ftpPort');

  useEffect(() => {
    if (ftpConfig) {
      console.log('Backend FTP Config:', ftpConfig);

      // Map backend field names to frontend field names
      const formData = {
        ftpHost: ftpConfig.vinePrimaryFtpServerName || '',
        ftpPort: parseInt(ftpConfig.vineFtpFirewallOutPort || (ftpConfig.vineUseSftp ? 22 : 21)),
        ftpUsername: ftpConfig.vineFtpUserName || '',
        ftpPassword: ftpConfig.vineFtpPassword || '',
        ftpRemotePath: ftpConfig.vineFtpDatFolderName || '/',
        useSftp: ftpConfig.vineUseSftp || false,
      };

      console.log('Mapped Form Data:', formData);
      reset(formData);
    }
  }, [ftpConfig, reset]);

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    if (ftpConfig) {
      const formData = {
        ftpHost: ftpConfig.vinePrimaryFtpServerName || '',
        ftpPort: parseInt(ftpConfig.vineFtpFirewallOutPort || (ftpConfig.vineUseSftp ? 22 : 21)),
        ftpUsername: ftpConfig.vineFtpUserName || '',
        ftpPassword: ftpConfig.vineFtpPassword || '',
        ftpRemotePath: ftpConfig.vineFtpDatFolderName || '/',
        useSftp: ftpConfig.vineUseSftp || false,
      };
      reset(formData);
    }
  };

  const onSubmit = (data) => {
    console.log('Form submission data:', data);
    setPendingData(data);
    setShowSaveConfirmation(true);
  };

  const confirmSave = () => {
    if (pendingData) {
      // Map frontend field names back to backend field names
      const backendData = {
        vineFtpUserName: pendingData.ftpUsername,
        vineFtpPassword: pendingData.ftpPassword,
        vinePrimaryFtpServerName: pendingData.ftpHost,
        vineFtpDatFolderName: pendingData.ftpRemotePath,
        vineFtpFirewallOutPort: pendingData.ftpPort.toString(),
        vineFtpMugshotFolderName: ftpConfig?.vineFtpMugshotFolderName || 'mugshots',
        vineUseSftp: pendingData.useSftp,
      };

      console.log('Backend submission data:', backendData);
      updateFtpConfig(backendData);
      setShowSaveConfirmation(false);
      setIsEditing(false);
      setPendingData(null);
    }
  };

  const cancelSave = () => {
    setShowSaveConfirmation(false);
    setPendingData(null);
  };

  if (isLoading) {
    return (
        <div className="flex justify-center items-center py-6">
          <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-blue-500"></div>
          <span className="ml-2 text-sm text-gray-600">Loading FTP configuration...</span>
        </div>
    );
  }

  return (
      <>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-800">FTP/SFTP Configuration</h2>
              <p className="text-sm text-gray-600">Configure remote file transfer connection</p>
            </div>
            {!isEditing ? (
                <Button
                    type="button"
                    onClick={handleEdit}
                    className="bg-gray-600 hover:bg-gray-700 flex items-center space-x-2"
                >
                  <Edit size={16} />
                  <span>Edit</span>
                </Button>
            ) : (
                <div className="flex space-x-2">
                  <Button
                      type="button"
                      onClick={handleCancel}
                      className="bg-gray-600 hover:bg-gray-700 flex items-center space-x-2"
                  >
                    <X size={16} />
                    <span>Cancel</span>
                  </Button>
                  <Button
                      type="submit"
                      loading={isUpdating}
                      disabled={!isDirty}
                      className="bg-blue-600 hover:bg-blue-700 flex items-center space-x-2"
                  >
                    <Save size={16} />
                    <span>Save Changes</span>
                  </Button>
                </div>
            )}
          </div>

          <FormSection title="Connection Settings" description="Server connection and authentication details">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
              <FormInput
                  label="FTP Host"
                  {...register('ftpHost')}
                  error={errors.ftpHost?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="FTP Port"
                  type="number"
                  {...register('ftpPort', { valueAsNumber: true })}
                  error={errors.ftpPort?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="FTP Username"
                  {...register('ftpUsername')}
                  error={errors.ftpUsername?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="FTP Password"
                  type="password"
                  {...register('ftpPassword')}
                  error={errors.ftpPassword?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="Remote Path"
                  {...register('ftpRemotePath')}
                  error={errors.ftpRemotePath?.message}
                  required
                  disabled={!isEditing}
              />
              <div className="flex items-center space-x-2 mt-2">
                <input
                    type="checkbox"
                    id="useSftp"
                    {...register('useSftp')}
                    className="h-3 w-3 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    disabled={!isEditing}
                />
                <label htmlFor="useSftp" className="text-xs font-medium text-gray-700 flex items-center space-x-1">
                  <Shield size={12} />
                  <span>Use SFTP (Secure FTP)</span>
                </label>
              </div>
            </div>

            {/* Connection Status Information */}
            <div className="mt-3 p-3 bg-gray-50 border border-gray-200 rounded">
              <div className="flex items-start space-x-2">
                <Server className="h-4 w-4 text-gray-600 mt-0.5 flex-shrink-0" />
                <div className="text-sm text-gray-700">
                  <p className="font-medium">
                    Current Protocol: {useSftp ? 'SFTP (Secure)' : 'FTP (Standard)'}
                  </p>
                  <p className="text-xs mt-1">
                    {useSftp
                        ? 'Using encrypted file transfer over SSH. Default port: 22'
                        : 'Using standard file transfer protocol. Default port: 21'
                    }
                  </p>
                  {useSftp && ftpPort === 21 && (
                      <p className="text-xs text-amber-700 mt-1">
                        💡 Consider using port 22 for SFTP connections
                      </p>
                  )}
                </div>
              </div>
            </div>

            {isEditing && (
                <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded">
                  <div className="flex items-start space-x-2">
                    <AlertTriangle className="h-4 w-4 text-yellow-600 mt-0.5 flex-shrink-0" />
                    <div className="text-sm text-yellow-800">
                      <p className="font-medium">FTP Configuration Edit Mode</p>
                      <p>You are editing connection settings that affect file transfer operations.</p>
                    </div>
                  </div>
                </div>
            )}
          </FormSection>
        </form>

        {/* Save Confirmation Modal */}
        {showSaveConfirmation && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0">
                    <AlertTriangle className="h-6 w-6 text-orange-600" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                      Confirm FTP Configuration Changes
                    </h3>
                    <div className="text-sm text-gray-600 space-y-2">
                      <p><strong>Important:</strong> These changes will affect file transfer operations.</p>
                      <div className="bg-gray-50 p-3 rounded text-xs">
                        <p><strong>Potential Effects:</strong></p>
                        <ul className="list-disc list-inside mt-1 space-y-1">
                          <li>File uploads and downloads will use new settings</li>
                          <li>Connection protocol will be updated ({pendingData?.useSftp ? 'SFTP' : 'FTP'})</li>
                          <li>Remote server path will change to: {pendingData?.ftpRemotePath}</li>
                          <li>Port will be updated to: {pendingData?.ftpPort}</li>
                          <li>Existing connections may be interrupted briefly</li>
                        </ul>
                      </div>
                      <p><strong>Test the connection after saving to ensure it works correctly.</strong></p>
                    </div>
                  </div>
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <Button
                      type="button"
                      onClick={cancelSave}
                      className="bg-gray-600 hover:bg-gray-700"
                  >
                    Cancel
                  </Button>
                  <Button
                      type="button"
                      onClick={confirmSave}
                      loading={isUpdating}
                      className="bg-orange-600 hover:bg-orange-700"
                  >
                    Yes, Save Changes
                  </Button>
                </div>
              </div>
            </div>
        )}
      </>
  );
};

export default FtpConfigForm;