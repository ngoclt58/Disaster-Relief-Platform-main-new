import React, { useState } from 'react';
import { apiService } from '../services/api';
import { mediaService } from '../services/mediaService';
import { 
  Clock, 
  User, 
  MapPin, 
  Package, 
  CheckCircle, 
  XCircle, 
  AlertTriangle,
  Camera,
  FileText
} from 'lucide-react';

interface Task {
  id: string;
  status: 'new' | 'assigned' | 'picked_up' | 'delivered' | 'could_not_deliver' | 'cancelled';
  eta?: string;
  assignee?: {
    id: string;
    fullName: string;
  };
  request?: {
    id: string;
    type: string;
    severity: number;
    notes?: string;
  };
  hub?: {
    id: string;
    name: string;
  };
  plannedKitCode?: string;
  createdAt: string;
  updatedAt: string;
}

interface TaskStateCardProps {
  task: Task;
  onStatusChange: (taskId: string, newStatus: string) => void;
  onClaim: (taskId: string) => void;
  onDeliver: (taskId: string) => void;
  canManage: boolean;
}

const TaskStateCard: React.FC<TaskStateCardProps> = ({
  task,
  onStatusChange,
  onClaim,
  onDeliver,
  canManage
}) => {
  const [showDeliveryForm, setShowDeliveryForm] = useState(false);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'new': return 'bg-gray-100 text-gray-800';
      case 'assigned': return 'bg-blue-100 text-blue-800';
      case 'picked_up': return 'bg-yellow-100 text-yellow-800';
      case 'delivered': return 'bg-green-100 text-green-800';
      case 'could_not_deliver': return 'bg-red-100 text-red-800';
      case 'cancelled': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getSeverityColor = (severity: number) => {
    switch (severity) {
      case 5: return 'bg-red-100 text-red-800';
      case 4: return 'bg-orange-100 text-orange-800';
      case 3: return 'bg-yellow-100 text-yellow-800';
      case 2: return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getAvailableTransitions = (currentStatus: string) => {
    switch (currentStatus) {
      case 'new': return ['assigned'];
      case 'assigned': return ['picked_up', 'cancelled'];
      case 'picked_up': return ['delivered', 'could_not_deliver', 'cancelled'];
      default: return [];
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'new': return <Clock className="w-4 h-4" />;
      case 'assigned': return <User className="w-4 h-4" />;
      case 'picked_up': return <Package className="w-4 h-4" />;
      case 'delivered': return <CheckCircle className="w-4 h-4" />;
      case 'could_not_deliver': return <XCircle className="w-4 h-4" />;
      case 'cancelled': return <XCircle className="w-4 h-4" />;
      default: return <Clock className="w-4 h-4" />;
    }
  };

  const availableTransitions = getAvailableTransitions(task.status);

  return (
    <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          {getStatusIcon(task.status)}
          <h3 className="text-lg font-semibold text-gray-900">Task #{task.id.slice(-8)}</h3>
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(task.status)}`}>
            {task.status.replace('_', ' ').toUpperCase()}
          </span>
        </div>
        {task.eta && (
          <div className="flex items-center text-sm text-gray-500">
            <Clock className="w-4 h-4 mr-1" />
            ETA: {new Date(task.eta).toLocaleString()}
          </div>
        )}
      </div>

      {/* Task Details */}
      <div className="space-y-3 mb-4">
        {task.request && (
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-600">
              <strong>Request:</strong> {task.request.type}
            </span>
            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${getSeverityColor(task.request.severity)}`}>
              Severity {task.request.severity}
            </span>
          </div>
        )}

        {task.assignee && (
          <div className="flex items-center space-x-2">
            <User className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-600">
              <strong>Assigned to:</strong> {task.assignee.fullName}
            </span>
          </div>
        )}

        {task.hub && (
          <div className="flex items-center space-x-2">
            <MapPin className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-600">
              <strong>Hub:</strong> {task.hub.name}
            </span>
          </div>
        )}

        {task.plannedKitCode && (
          <div className="flex items-center space-x-2">
            <Package className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-600">
              <strong>Kit:</strong> {task.plannedKitCode}
            </span>
          </div>
        )}

        {task.request?.notes && (
          <div className="flex items-start space-x-2">
            <FileText className="w-4 h-4 text-gray-400 mt-0.5" />
            <span className="text-sm text-gray-600">
              <strong>Notes:</strong> {task.request.notes}
            </span>
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-2">
        {canManage && availableTransitions.length > 0 && (
          <>
            {availableTransitions.map(transition => (
              <button
                key={transition}
                onClick={() => onStatusChange(task.id, transition)}
                className={`px-3 py-1 text-sm rounded-md transition-colors ${
                  transition === 'delivered' || transition === 'could_not_deliver'
                    ? 'bg-green-600 text-white hover:bg-green-700'
                    : transition === 'cancelled'
                    ? 'bg-red-600 text-white hover:bg-red-700'
                    : 'bg-blue-600 text-white hover:bg-blue-700'
                }`}
              >
                {transition.replace('_', ' ').toUpperCase()}
              </button>
            ))}
          </>
        )}

        {task.status === 'new' && (
          <button
            onClick={() => onClaim(task.id)}
            className="px-3 py-1 text-sm bg-purple-600 text-white rounded-md hover:bg-purple-700"
          >
            CLAIM TASK
          </button>
        )}

        {task.status === 'picked_up' && (
          <button
            onClick={() => setShowDeliveryForm(true)}
            className="px-3 py-1 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 flex items-center"
          >
            <Camera className="w-4 h-4 mr-1" />
            DELIVER
          </button>
        )}
      </div>

      {/* Delivery Form Modal */}
      {showDeliveryForm && (
        <DeliveryForm
          taskId={task.id}
          onClose={() => setShowDeliveryForm(false)}
          onDeliver={onDeliver}
        />
      )}
    </div>
  );
};

// Delivery Form Component
const DeliveryForm: React.FC<{
  taskId: string;
  onClose: () => void;
  onDeliver: (taskId: string) => void;
}> = ({ taskId, onClose, onDeliver }) => {
  const [formData, setFormData] = useState({
    recipientName: '',
    recipientPhone: '',
    notes: '',
    proofPhoto: null as File | null
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      let proofMediaId = null;
      
      // Upload photo if provided
      if (formData.proofPhoto) {
        const uploadResult = await mediaService.uploadImage(formData.proofPhoto, {
          maxWidth: 1920,
          maxHeight: 1080,
          quality: 0.8
        });
        proofMediaId = uploadResult.id;
      }
      
      // Create delivery record
      await apiService.createDelivery({
        taskId,
        recipientName: formData.recipientName,
        recipientPhone: formData.recipientPhone,
        notes: formData.notes,
        proofMediaId
      });
      
      onDeliver(taskId);
      onClose();
    } catch (error) {
      console.error('Failed to create delivery:', error);
      alert('Failed to create delivery. Please try again.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 className="text-lg font-semibold mb-4">Delivery Confirmation</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Recipient Name *
            </label>
            <input
              type="text"
              required
              value={formData.recipientName}
              onChange={(e) => setFormData({...formData, recipientName: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Recipient Phone
            </label>
            <input
              type="tel"
              value={formData.recipientPhone}
              onChange={(e) => setFormData({...formData, recipientPhone: e.target.value})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Delivery Notes
            </label>
            <textarea
              value={formData.notes}
              onChange={(e) => setFormData({...formData, notes: e.target.value})}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Proof Photo
            </label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setFormData({...formData, proofPhoto: e.target.files?.[0] || null})}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
            />
          </div>

          <div className="flex justify-end space-x-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-sm bg-green-600 text-white rounded-md hover:bg-green-700"
            >
              Confirm Delivery
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskStateCard;
