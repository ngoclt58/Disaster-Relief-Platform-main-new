import React from 'react';
import { ResidentProfileForm } from '../components/ResidentProfileForm';

export const ResidentProfilePage: React.FC = () => {
  return (
    <div className="px-4 py-6">
      <h1 className="text-2xl font-semibold mb-2">My Profile</h1>
      <p className="text-sm text-gray-600 mb-6">Keep your information up to date so responders can reach you quickly.</p>
      <ResidentProfileForm />
    </div>
  );
};

export default ResidentProfilePage;





