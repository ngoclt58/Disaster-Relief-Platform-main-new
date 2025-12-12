import React, { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { apiService } from '../services/api';
import { realtimeService, RealtimeEventType } from '../services/realtimeService';
import TaskStateCard from '../components/TaskStateCard';
import { 
  Filter, 
  Search, 
  Plus, 
  Clock, 
  CheckCircle, 
  AlertTriangle,
  RefreshCw
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

const TasksPage: React.FC = () => {
  const { user, token } = useAuthStore();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchTasks();
    
    // Set up real-time updates
    const unsubscribe = realtimeService.subscribe('task.updated' as RealtimeEventType, (event) => {
      console.log('Task updated:', event.data);
      fetchTasks(); // Refresh tasks when updated
    });
    
    const unsubscribeCreated = realtimeService.subscribe('task.created' as RealtimeEventType, (event) => {
      console.log('Task created:', event.data);
      fetchTasks(); // Refresh tasks when new one created
    });
    
    return () => {
      unsubscribe();
      unsubscribeCreated();
    };
  }, []);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyTasks();
      setTasks(data.content || []);
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (taskId: string, newStatus: string) => {
    try {
      await apiService.updateTaskStatus(taskId, newStatus);
      // Real-time update will handle the refresh
    } catch (error) {
      console.error('Failed to update task status:', error);
    }
  };

  const handleClaim = async (taskId: string) => {
    try {
      await apiService.claimTask(taskId);
      // Real-time update will handle the refresh
    } catch (error) {
      console.error('Failed to claim task:', error);
    }
  };

  const handleDeliver = async (taskId: string) => {
    // This would be handled by the delivery form
    await fetchTasks(); // Refresh tasks
  };

  const filteredTasks = tasks.filter(task => {
    const matchesFilter = filter === 'all' || task.status === filter;
    const matchesSearch = searchTerm === '' || 
      task.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      task.request?.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
      task.assignee?.fullName.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const getTaskStats = () => {
    return {
      total: tasks.length,
      new: tasks.filter(t => t.status === 'new').length,
      assigned: tasks.filter(t => t.status === 'assigned').length,
      pickedUp: tasks.filter(t => t.status === 'picked_up').length,
      delivered: tasks.filter(t => t.status === 'delivered').length,
      cancelled: tasks.filter(t => t.status === 'cancelled').length
    };
  };

  const stats = getTaskStats();

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
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Tasks</h1>
            <p className="text-gray-600 mt-1">Manage your assigned tasks and deliveries</p>
          </div>
          <button
            onClick={fetchTasks}
            className="flex items-center px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
          >
            <RefreshCw className="w-4 h-4 mr-2" />
            Refresh
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-6">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Clock className="h-6 w-6 text-gray-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.total}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">New</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.new}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Clock className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Assigned</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.assigned}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Clock className="h-6 w-6 text-orange-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Picked Up</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.pickedUp}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <CheckCircle className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Delivered</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.delivered}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-6 w-6 text-red-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Cancelled</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats.cancelled}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search tasks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          <div className="flex space-x-2">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="all">All Tasks</option>
              <option value="new">New</option>
              <option value="assigned">Assigned</option>
              <option value="picked_up">Picked Up</option>
              <option value="delivered">Delivered</option>
              <option value="could_not_deliver">Could Not Deliver</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Tasks Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {filteredTasks.length === 0 ? (
          <div className="col-span-2 text-center py-12">
            <Clock className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No tasks found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {filter === 'all' ? 'You have no tasks assigned.' : `No tasks with status "${filter}".`}
            </p>
          </div>
        ) : (
          filteredTasks.map(task => (
            <TaskStateCard
              key={task.id}
              task={task}
              onStatusChange={handleStatusChange}
              onClaim={handleClaim}
              onDeliver={handleDeliver}
              canManage={user?.role === 'DISPATCHER' || user?.role === 'ADMIN'}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default TasksPage;
