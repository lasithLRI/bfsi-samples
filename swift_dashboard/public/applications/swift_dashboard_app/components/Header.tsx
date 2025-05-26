import React from 'react';
import { Link } from 'react-router-dom';

const Header: React.FC = () => {
  return (
    <header className="header">
      <div className="logo">SWIFT Message Analytics Dashboard</div>
      <nav>
        <Link to="/">Overview</Link>
        <Link to="/messages">Messages</Link>
      </nav>
    </header>
  );
};

export default Header;