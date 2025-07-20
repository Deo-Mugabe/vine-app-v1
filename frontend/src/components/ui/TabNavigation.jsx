import React from 'react';

const TabNavigation = ({ tabs, activeTab, onTabChange }) => {
  return (
    <div className="flex border-b space-x-4">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onTabChange(tab.id)}
          className={`pb-2 px-4 font-medium transition-colors duration-200 border-b-2 ${
            activeTab === tab.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-600'
          }`}
        >
          <tab.icon className="inline-block mr-1 w-4 h-4" /> {tab.label}
        </button>
      ))}
    </div>
  );
};

export default TabNavigation;