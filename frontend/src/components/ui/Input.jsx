import React from 'react';

const Input = React.forwardRef(({ label, error, required, disabled, className = '', ...props }, ref) => {
    return (
        <div className="mb-2">
            <div className="flex items-center space-x-2">
                {label && (
                    <label className="text-xs font-medium text-gray-700 min-w-0 flex-shrink-0 w-32">
                        {label}
                        {required && <span className="text-red-500 ml-1">*</span>}
                        :
                    </label>
                )}
                <input
                    ref={ref}
                    {...props}
                    disabled={disabled}
                    className={`flex-1 px-2 py-1 text-sm border rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 ${
                        error ? 'border-red-500' : 'border-gray-300'
                    } ${
                        disabled
                            ? 'bg-gray-100 text-gray-600 cursor-not-allowed'
                            : 'bg-white text-gray-900'
                    } ${className}`}
                />
            </div>
            {error && <p className="text-red-500 text-xs mt-1 ml-34">{error}</p>}
        </div>
    );
});

Input.displayName = 'Input';

export default Input;