import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { vineConfigSchema } from '../../utils/validation';
import FormInput from './FormInput';
import FormSection from './FormSection';
import Button from '../ui/Button';
import { useVineConfig } from '../../hooks/useVineConfig';
import { Edit, Save, X, AlertTriangle } from 'lucide-react';

const VineConfigForm = () => {
  const { vineConfig, updateVineConfig, isUpdating, isLoading } = useVineConfig();
  const [isEditing, setIsEditing] = useState(false);
  const [showSaveConfirmation, setShowSaveConfirmation] = useState(false);
  const [pendingData, setPendingData] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    reset,
    getValues,
  } = useForm({
    resolver: yupResolver(vineConfigSchema),
    defaultValues: {
      vineChargesFileHeader: '',
      vinePrisonerFileHeader: '',
      vineJailIdNumber: '',
      vineNewMugShotDirectory: '',
      vineMugShotDirectory: '',
      vineNewVineFilePath: '',
      vineInterFile: '',
    }
  });

  useEffect(() => {
    if (vineConfig) {
      reset({
        vineChargesFileHeader: vineConfig.vineChargesFileHeader || '',
        vinePrisonerFileHeader: vineConfig.vinePrisonerFileHeader || '',
        vineJailIdNumber: vineConfig.vineJailIdNumber || '',
        vineNewMugShotDirectory: vineConfig.vineNewMugShotDirectory || '',
        vineMugShotDirectory: vineConfig.vineMugShotDirectory || '',
        vineNewVineFilePath: vineConfig.vineNewVineFilePath || '',
        vineInterFile: vineConfig.vineInterFile || '',
      });
    }
  }, [vineConfig, reset]);

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    if (vineConfig) {
      reset({
        vineChargesFileHeader: vineConfig.vineChargesFileHeader || '',
        vinePrisonerFileHeader: vineConfig.vinePrisonerFileHeader || '',
        vineJailIdNumber: vineConfig.vineJailIdNumber || '',
        vineNewMugShotDirectory: vineConfig.vineNewMugShotDirectory || '',
        vineMugShotDirectory: vineConfig.vineMugShotDirectory || '',
        vineNewVineFilePath: vineConfig.vineNewVineFilePath || '',
        vineInterFile: vineConfig.vineInterFile || '',
      });
    }
  };

  const onSubmit = (data) => {
    setPendingData(data);
    setShowSaveConfirmation(true);
  };

  const confirmSave = () => {
    if (pendingData) {
      updateVineConfig(pendingData);
      setShowSaveConfirmation(false);
      setIsEditing(false);
      setPendingData(null);
    }
  };

  const cancelSave = () => {
    setShowSaveConfirmation(false);
    setPendingData(null);
  };

  if (isLoading || !vineConfig) {
    return (
        <div className="flex justify-center items-center py-6">
          <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-blue-500"></div>
          <span className="ml-2 text-sm text-gray-600">Loading configuration...</span>
        </div>
    );
  }

  return (
      <>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-800">VINE System Configuration</h2>
              <p className="text-sm text-gray-600">Configure headers, file paths, and IDs</p>
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

          <FormSection title="File Headers and Paths" description="Configure VINE system file headers and directory paths">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
              <FormInput
                  label="Charges File Header"
                  {...register('vineChargesFileHeader')}
                  error={errors.vineChargesFileHeader?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="Prisoner File Header"
                  {...register('vinePrisonerFileHeader')}
                  error={errors.vinePrisonerFileHeader?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="Jail ID Number"
                  {...register('vineJailIdNumber')}
                  error={errors.vineJailIdNumber?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="New Mugshot Directory"
                  {...register('vineNewMugShotDirectory')}
                  error={errors.vineNewMugShotDirectory?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="Mugshot Directory"
                  {...register('vineMugShotDirectory')}
                  error={errors.vineMugShotDirectory?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="New VINE File Path"
                  {...register('vineNewVineFilePath')}
                  error={errors.vineNewVineFilePath?.message}
                  required
                  disabled={!isEditing}
              />
              <FormInput
                  label="VINE Interfile Name"
                  {...register('vineInterFile')}
                  error={errors.vineInterFile?.message}
                  required
                  disabled={!isEditing}
              />
            </div>
          </FormSection>

          {isEditing && (
              <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded">
                <div className="flex items-start space-x-2">
                  <AlertTriangle className="h-4 w-4 text-yellow-600 mt-0.5 flex-shrink-0" />
                  <div className="text-sm text-yellow-800">
                    <p className="font-medium">Configuration Edit Mode</p>
                    <p>You are currently editing VINE system configuration. Changes will affect how the system processes files and data.</p>
                  </div>
                </div>
              </div>
          )}
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
                      Confirm Configuration Changes
                    </h3>
                    <div className="text-sm text-gray-600 space-y-2">
                      <p><strong>Important:</strong> These changes will affect how the VINE system processes data and files.</p>
                      <div className="bg-gray-50 p-3 rounded text-xs">
                        <p><strong>Potential Effects:</strong></p>
                        <ul className="list-disc list-inside mt-1 space-y-1">
                          <li>File processing paths will be updated immediately</li>
                          <li>New data files will use the updated headers</li>
                          <li>Existing integrations may need to be verified</li>
                          <li>Mugshot directories will be updated for new uploads</li>
                        </ul>
                      </div>
                      <p><strong>Are you sure you want to proceed with these changes?</strong></p>
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

export default VineConfigForm;