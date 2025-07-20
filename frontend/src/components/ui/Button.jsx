import React from 'react';

const Button = ({ children, className = '', loading, ...props }) => {
  return (
    <button
      className={`px-4 py-2 rounded text-white font-semibold transition-all ${className} ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
      disabled={loading}
      {...props}
    >
      {loading ? 'Loading...' : children}
    </button>
  );
};

export default Button;