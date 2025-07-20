import React from 'react';

const FormSection = ({ title, description, children }) => (
  <section>
    <h3 className="text-lg font-semibold text-gray-800 mb-1">{title}</h3>
    {description && <p className="text-sm text-gray-500 mb-4">{description}</p>}
    <div>{children}</div>
  </section>
);

export default FormSection;