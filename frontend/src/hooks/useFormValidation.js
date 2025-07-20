import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

export const useFormValidation = (schema, defaultValues = {}) => {
  return useForm({
    resolver: yupResolver(schema),
    defaultValues,
    mode: 'onSubmit',
  });
};
