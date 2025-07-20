import React, { forwardRef } from 'react';
import Input from '../ui/Input';

const FormInput = forwardRef(({ label, error, ...props }, ref) => (
    <Input label={label} error={error} ref={ref} {...props} />
));

export default FormInput;