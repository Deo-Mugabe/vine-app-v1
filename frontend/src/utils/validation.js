import * as yup from 'yup';

export const vineConfigSchema = yup.object().shape({
  newVineDataFilePath: yup
    .string()
    .required('Data file path is required')
    .matches(/^[a-zA-Z]:\\/, 'Path must be a valid Windows path'),
  
  vineFileName: yup
    .string()
    .required('Vine file name is required')
    .matches(/\.(dat|txt)$/, 'File must have .dat or .txt extension'),
  
  vineNewIntermFileName: yup
    .string()
    .required('Intermediate file name is required')
    .matches(/\.(dat|txt)$/, 'File must have .dat or .txt extension'),
  
  timeBetweenRunningVine: yup
    .number()
    .required('Time between runs is required')
    .min(1, 'Must be at least 1 minute')
    .max(1440, 'Cannot exceed 1440 minutes (24 hours)'),
  
  maxSizeOfLogFile: yup
    .number()
    .required('Max log file size is required')
    .min(1000, 'Must be at least 1000 bytes')
    .max(1000000000, 'Cannot exceed 1GB'),
  
  jailIdentificationNumber: yup
    .string()
    .required('Jail ID is required')
    .matches(/^\d+$/, 'Must contain only numbers'),
  
  courtSectionHeader: yup.string().required('Court section header is required'),
  chargesSectionHeader: yup.string().required('Charges section header is required'),
  prisonerSectionHeader: yup.string().required('Prisoner section header is required'),
  victimSectionHeader: yup.string().required('Victim section header is required'),
  demographicSectionHeader: yup.string().required('Demographic section header is required'),
  
  whereToFindMugshotFiles: yup
    .string()
    .required('Mugshot source path is required'),
  
  whereToPutMugshotFilesToTransfer: yup
    .string()
    .required('Mugshot destination path is required'),
});

export const ftpConfigSchema = yup.object().shape({
  primaryFtpServerName: yup
    .string()
    .required('Server name is required')
    .matches(/^[a-zA-Z0-9.-]+$/, 'Invalid server name format'),
  
  vineFtpUserName: yup
    .string()
    .required('Username is required')
    .min(3, 'Username must be at least 3 characters'),
  
  vineFtpPassword: yup
    .string()
    .required('Password is required')
    .min(6, 'Password must be at least 6 characters'),
  
  vineFtpFirewallOutPortSetting: yup
    .number()
    .required('Port is required')
    .min(1, 'Port must be between 1 and 65535')
    .max(65535, 'Port must be between 1 and 65535'),
  
  ftpRemoteDataFolder: yup
    .string()
    .required('Remote data folder is required'),
  
  ftpRemoteMugshotFolder: yup
    .string()
    .required('Remote mugshot folder is required'),
});