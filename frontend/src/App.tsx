import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Login';
import Register from './Register';
import FileManager from './FileManager';
import ProtectedRoute from './ProtectedRoute';

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          path="/files"
          element={
            <ProtectedRoute>
              <FileManager />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
};

export default App;
