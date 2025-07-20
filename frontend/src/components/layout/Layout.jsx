import React from 'react';
import Header from './Header';
import Container from './Container';

const Layout = ({ children }) => (
  <div className="min-h-screen bg-gray-50">
    <Header />
    <Container>{children}</Container>
  </div>
);

export default Layout;