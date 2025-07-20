import * as yup from 'yup';

// Schema for VINE Configuration Form - matching the actual form field names used in the component
export const vineConfigSchema = yup.object().shape({
  vineChargesFileHeader: yup
      .string()
      .required('Charges file header is required'),

  vinePrisonerFileHeader: yup
      .string()
      .required('Prisoner file header is required'),

  vineJailIdNumber: yup
      .string()
      .required('Jail ID number is required'),

  vineNewMugShotDirectory: yup
      .string()
      .required('New mugshot directory is required'),

  vineMugShotDirectory: yup
      .string()
      .required('Mugshot directory is required'),

  vineNewVineFilePath: yup
      .string()
      .required('New VINE file path is required'),

  vineInterFile: yup
      .string()
      .required('VINE interfile name is required'),
});

// Schema for FTP Configuration Form - matching the form field names
export const ftpConfigSchema = yup.object().shape({
  ftpHost: yup
      .string()
      .required('FTP host is required'),

  ftpPort: yup
      .number()
      .positive('FTP port must be a positive number')
      .integer('FTP port must be an integer')
      .min(1, 'FTP port must be at least 1')
      .max(65535, 'FTP port must be less than 65536')
      .required('FTP port is required'),

  ftpUsername: yup
      .string()
      .required('FTP username is required'),

  ftpPassword: yup
      .string()
      .required('FTP password is required'),

  ftpRemotePath: yup
      .string()
      .required('FTP remote path is required'),

  useSftp: yup
      .boolean()
      .default(false),
});