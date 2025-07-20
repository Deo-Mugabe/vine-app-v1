import React from 'react';

const Input = React.forwardRef(({ error, ...props }, ref) => {
  return (
    <div>
      <input
        ref={ref}
        {...props}
        className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-blue-300 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
      />
      {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
    </div>
  );
});

export default Input;