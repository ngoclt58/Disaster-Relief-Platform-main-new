import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { apiService } from '../services/api';
import { Eye, EyeOff, MapPin, Users, Shield, Heart } from 'lucide-react';

const LoginPage: React.FC = () => {
  const [formData, setFormData] = useState({
    emailOrPhone: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const { login } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const data = await apiService.login(formData.emailOrPhone, formData.password);
      login(data.user, data.accessToken);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Invalid credentials. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="min-h-screen flex">
      {/* Left side - Login Form */}
      <div className="flex-1 flex flex-col justify-center py-12 px-4 sm:px-6 lg:flex-none lg:px-20 xl:px-24">
        <div className="mx-auto w-full max-w-sm lg:w-96">
          <div>
            <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
              Sign in to Relief Platform
            </h2>
            <p className="mt-2 text-sm text-gray-600">
              Coordinate disaster relief efforts efficiently
            </p>
          </div>

          <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
            )}

            <div>
              <label htmlFor="emailOrPhone" className="block text-sm font-medium text-gray-700">
                Email or Phone
              </label>
              <div className="mt-1">
                <input
                  id="emailOrPhone"
                  name="emailOrPhone"
                  type="text"
                  required
                  value={formData.emailOrPhone}
                  onChange={handleChange}
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="Enter your email or phone number"
                />
              </div>
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={formData.password}
                  onChange={handleChange}
                  className="appearance-none block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading}
                className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? 'Signing in...' : 'Sign in'}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Right side - Features */}
      <div className="hidden lg:block relative w-0 flex-1">
        <div className="absolute inset-0 bg-gradient-to-br from-blue-600 to-blue-800">
          <div className="flex items-center justify-center h-full p-12">
            <div className="text-center text-white">
              <h1 className="text-4xl font-bold mb-8">Disaster Relief Platform</h1>
              <p className="text-xl mb-12 text-blue-100">
                Modern, reliable platform to coordinate relief during disasters
              </p>
              
              <div className="grid grid-cols-1 gap-8 max-w-md mx-auto">
                <div className="flex items-center space-x-4">
                  <MapPin className="w-8 h-8 text-blue-200" />
                  <div className="text-left">
                    <h3 className="text-lg font-semibold">Real-time Mapping</h3>
                    <p className="text-blue-100">Live visualization of needs and resources</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-4">
                  <Users className="w-8 h-8 text-blue-200" />
                  <div className="text-left">
                    <h3 className="text-lg font-semibold">Multi-role Support</h3>
                    <p className="text-blue-100">Residents, helpers, dispatchers, and admins</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-4">
                  <Shield className="w-8 h-8 text-blue-200" />
                  <div className="text-left">
                    <h3 className="text-lg font-semibold">Offline Capable</h3>
                    <p className="text-blue-100">Works in low-connectivity areas</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-4">
                  <Heart className="w-8 h-8 text-blue-200" />
                  <div className="text-left">
                    <h3 className="text-lg font-semibold">Privacy First</h3>
                    <p className="text-blue-100">Data protection and consent controls</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
