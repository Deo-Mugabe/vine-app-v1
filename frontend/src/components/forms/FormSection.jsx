import React from 'react';

const FormSection = ({ title, description, children }) => (
    <section className="mb-4">
        <h3 className="text-base font-semibold text-gray-800 mb-1">{title}</h3>
        {description && <p className="text-xs text-gray-500 mb-3">{description}</p>}
        <div className="space-y-1">{children}</div>
    </section>
);

export default FormSection;