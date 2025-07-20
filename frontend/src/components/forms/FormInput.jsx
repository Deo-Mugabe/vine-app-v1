import React from 'react';
import Input from '../ui/Input';

const FormInput = ({ label, error, ...props }) => (
  <Input label={label} error={error} {...props} />
);

export default FormInput;