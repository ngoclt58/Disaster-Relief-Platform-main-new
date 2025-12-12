import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import {
  MapPin,
  Users,
  Phone,
  Clock,
  AlertCircle,
  CheckCircle,
  XCircle,
  Settings,
  Plus,
  Search,
  Filter,
  Edit,
  Trash2,
  Eye
} from 'lucide-react';

interface Shelter {
  id: string;
  name: string;
  description?: string;
  address: string;
  capacity: number;
  currentOccupancy: number;
  contactPhone?: string;
  contactEmail?: string;
  facilities?: string[];
  latitude?: number;
  longitude?: number;
  status: 'ACTIVE' | 'FULL' | 'CLOSED' | 'MAINTENANCE';
  isEmergencyShelter: boolean;
  accessibilityFeatures?: string[];
  operatingHours: string;
  managerName?: string;
  managerPhone?: string;
  notes?: string;
  availableCapacity: number;
  occupancyRate: number;
  isAvailable: boolean;
  isFull: boolean;
  createdAt: string;
  updatedAt: string;
}

const ShelterManagement: React.FC = () => {
  const navigate = useNavigate();
  const [shelters, setShelters] = useState<Shelter[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedShelter, setSelectedShelter] = useState<Shelter | null>(null);
  const [showDetails, setShowDetails] = useState(false);

  useEffect(() => {
    fetchShelters();
  }, []);

  const fetchShelters = async () => {
    try {
      setLoading(true);
      const response = await apiService.get<{content: Shelter[]}>('/shelters?size=100');
      setShelters(response.content || []);
    } catch (error) {
      console.error('Failed to fetch shelters:', error);
      setShelters([]);
    } finally {
      setLoading(false);
    }
  };





  const filteredShelters = shelters.filter(shelter => {
    const matchesSearch = shelter.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         shelter.address.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || shelter.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleDeleteShelter = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this shelter?')) return;
    
    try {
      await apiService.delete(`/shelters/${id}`);
      await fetchShelters();
    } catch (error) {
      console.error('Failed to delete shelter:', error);
      alert('Failed to delete shelter');
    }
  };

  const handleViewDetails = (shelter: Shelter) => {
    setSelectedShelter(shelter);
    setShowDetails(true);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Shelter Management</h1>
            <p className="text-gray-600 mt-1">Manage emergency shelters and their capacity</p>
          </div>
          <button
            onClick={() => setShowAddForm(true)}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
          >
            <Plus className="h-4 w-4" />
            <span>Add Shelter</span>
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search shelters by name or address..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <Filter className="h-4 w-4 text-gray-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="FULL">Full</option>
              <option value="CLOSED">Closed</option>
              <option value="MAINTENANCE">Maintenance</option>
            </select>
          </div>
        </div>
      </div>

      {/* Shelters Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredShelters.map((shelter) => (
          <div key={shelter.id} className="bg-white shadow rounded-lg overflow-hidden">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-lg font-semibold text-gray-900 truncate">{shelter.name}</h3>
                <div className="flex items-center space-x-1">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(shelter.status)}`}>
                    {getStatusIcon(shelter.status)}
                    <span className="ml-1">{shelter.status}</span>
                  </span>
                </div>
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center text-sm text-gray-600">
                  <MapPin className="h-4 w-4 mr-2" />
                  <span className="truncate">{shelter.address}</span>
                </div>
                
                <div className="flex items-center text-sm text-gray-600">
                  <Users className="h-4 w-4 mr-2" />
                  <span className={getOccupancyColor(shelter.occupancyRate)}>
                    {shelter.currentOccupancy}/{shelter.capacity} ({shelter.occupancyRate.toFixed(1)}%)
                  </span>
                </div>

                {shelter.contactPhone && (
                  <div className="flex items-center text-sm text-gray-600">
                    <Phone className="h-4 w-4 mr-2" />
                    <span>{shelter.contactPhone}</span>
                  </div>
                )}

                <div className="flex items-center text-sm text-gray-600">
                  <Clock className="h-4 w-4 mr-2" />
                  <span>{shelter.operatingHours}</span>
                </div>
              </div>

              {/* Facilities */}
              {shelter.facilities && shelter.facilities.length > 0 && (
                <div className="mb-4">
                  <div className="flex flex-wrap gap-1">
                    {shelter.facilities.slice(0, 3).map((facility, index) => (
                      <span key={index} className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-800">
                        {facility}
                      </span>
                    ))}
                    {shelter.facilities.length > 3 && (
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-gray-100 text-gray-600">
                        +{shelter.facilities.length - 3} more
                      </span>
                    )}
                  </div>
                </div>
              )}

              {/* Actions */}
              <div className="flex justify-between items-center pt-4 border-t border-gray-200">
                <button
                  onClick={() => handleViewDetails(shelter)}
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium flex items-center space-x-1"
                >
                  <Eye className="h-4 w-4" />
                  <span>View Details</span>
                </button>
                
                <div className="flex space-x-2">
                  <button
                    onClick={() => navigate(`/shelters/edit/${shelter.id}`)}
                    className="text-gray-600 hover:text-gray-800"
                  >
                    <Edit className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDeleteShelter(shelter.id)}
                    className="text-red-600 hover:text-red-800"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredShelters.length === 0 && (
        <div className="text-center py-12">
          <MapPin className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No shelters found</h3>
          <p className="mt-1 text-sm text-gray-500">
            {searchTerm || statusFilter !== 'all' 
              ? 'Try adjusting your search or filter criteria.'
              : 'Get started by adding a new shelter.'
            }
          </p>
          {!searchTerm && statusFilter === 'all' && (
            <div className="mt-6">
              <button
                onClick={() => setShowAddForm(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Shelter
              </button>
            </div>
          )}
        </div>
      )}

      {/* Shelter Details Modal */}
      {showDetails && selectedShelter && (
        <ShelterDetailsModal
          shelter={selectedShelter}
          onClose={() => {
            setShowDetails(false);
            setSelectedShelter(null);
          }}
        />
      )}

      {/* Add Shelter Form Modal */}
      {showAddForm && (
        <AddShelterModal
          onClose={() => setShowAddForm(false)}
          onSuccess={() => {
            setShowAddForm(false);
            fetchShelters();
          }}
        />
      )}
    </div>
  );
};

// Helper functions
const getStatusColor = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'bg-green-100 text-green-800';
    case 'FULL': return 'bg-red-100 text-red-800';
    case 'CLOSED': return 'bg-gray-100 text-gray-800';
    case 'MAINTENANCE': return 'bg-yellow-100 text-yellow-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'ACTIVE': return <CheckCircle className="h-4 w-4" />;
    case 'FULL': return <AlertCircle className="h-4 w-4" />;
    case 'CLOSED': return <XCircle className="h-4 w-4" />;
    case 'MAINTENANCE': return <Settings className="h-4 w-4" />;
    default: return <AlertCircle className="h-4 w-4" />;
  }
};

const getOccupancyColor = (rate: number) => {
  if (rate >= 90) return 'text-red-600';
  if (rate >= 70) return 'text-yellow-600';
  return 'text-green-600';
};

// Shelter Details Modal Component
const ShelterDetailsModal: React.FC<{
  shelter: Shelter;
  onClose: () => void;
}> = ({ shelter, onClose }) => {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-start mb-6">
            <h2 className="text-xl font-bold text-gray-900">{shelter.name}</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <XCircle className="h-6 w-6" />
            </button>
          </div>

          <div className="space-y-6">
            {/* Basic Info */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-3">Basic Information</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Address</label>
                  <p className="mt-1 text-sm text-gray-900">{shelter.address}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Status</label>
                  <span className={`mt-1 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(shelter.status)}`}>
                    {shelter.status}
                  </span>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Capacity</label>
                  <p className="mt-1 text-sm text-gray-900">{shelter.capacity} people</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Current Occupancy</label>
                  <p className="mt-1 text-sm text-gray-900">{shelter.currentOccupancy} people ({shelter.occupancyRate.toFixed(1)}%)</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Operating Hours</label>
                  <p className="mt-1 text-sm text-gray-900">{shelter.operatingHours}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Emergency Shelter</label>
                  <p className="mt-1 text-sm text-gray-900">{shelter.isEmergencyShelter ? 'Yes' : 'No'}</p>
                </div>
              </div>
            </div>

            {/* Contact Info */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-3">Contact Information</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {shelter.contactPhone && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Phone</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.contactPhone}</p>
                  </div>
                )}
                {shelter.contactEmail && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Email</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.contactEmail}</p>
                  </div>
                )}
                {shelter.managerName && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Manager</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.managerName}</p>
                  </div>
                )}
                {shelter.managerPhone && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Manager Phone</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.managerPhone}</p>
                  </div>
                )}
              </div>
            </div>

            {/* Facilities */}
            {shelter.facilities && shelter.facilities.length > 0 && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">Facilities</h3>
                <div className="flex flex-wrap gap-2">
                  {shelter.facilities.map((facility, index) => (
                    <span key={index} className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800">
                      {facility}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Accessibility Features */}
            {shelter.accessibilityFeatures && shelter.accessibilityFeatures.length > 0 && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">Accessibility Features</h3>
                <div className="flex flex-wrap gap-2">
                  {shelter.accessibilityFeatures.map((feature, index) => (
                    <span key={index} className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-green-100 text-green-800">
                      {feature}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Description & Notes */}
            {(shelter.description || shelter.notes) && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">Additional Information</h3>
                {shelter.description && (
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700">Description</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.description}</p>
                  </div>
                )}
                {shelter.notes && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Notes</label>
                    <p className="mt-1 text-sm text-gray-900">{shelter.notes}</p>
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="mt-6 flex justify-end">
            <button
              onClick={onClose}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

// Add Shelter Modal Component (simplified for now)
const AddShelterModal: React.FC<{
  onClose: () => void;
  onSuccess: () => void;
}> = ({ onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: '',
    capacity: '',
    contactPhone: '',
    contactEmail: '',
    operatingHours: '24/7',
    managerName: '',
    managerPhone: '',
    notes: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      await apiService.post('/shelters', {
        ...formData,
        capacity: parseInt(formData.capacity),
        currentOccupancy: 0,
        status: 'ACTIVE',
        isEmergencyShelter: true,
        facilities: ['wifi', 'kitchen'] // Default facilities
      });
      
      onSuccess();
    } catch (error) {
      console.error('Failed to create shelter:', error);
      alert('Failed to create shelter');
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-gray-900">Add New Shelter</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <XCircle className="h-6 w-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Address *</label>
              <textarea
                required
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                rows={2}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Capacity *</label>
              <input
                type="number"
                required
                min="1"
                value={formData.capacity}
                onChange={(e) => setFormData({...formData, capacity: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Contact Phone</label>
              <input
                type="tel"
                value={formData.contactPhone}
                onChange={(e) => setFormData({...formData, contactPhone: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Contact Email</label>
              <input
                type="email"
                value={formData.contactEmail}
                onChange={(e) => setFormData({...formData, contactEmail: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Manager Name</label>
              <input
                type="text"
                value={formData.managerName}
                onChange={(e) => setFormData({...formData, managerName: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Notes</label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({...formData, notes: e.target.value})}
                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                rows={3}
              />
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Create Shelter
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ShelterManagement;