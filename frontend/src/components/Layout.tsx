import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { 
  MapIcon, 
  HomeIcon, 
  UserIcon, 
  CogIcon,
  LogOutIcon,
  BellIcon,
  PackageIcon,
  ClipboardListIcon,
  MountainIcon,
  SatelliteIcon,
  FlameIcon,
  ShieldIcon,
  BuildingIcon,
  DownloadIcon,
  ActivityIcon,
  MessageCircleIcon,
  DollarSignIcon,
  GraduationCapIcon,
  GlobeIcon,
  BarChart3Icon,
  BrainIcon,
  ZapIcon,
  MenuIcon,
  XIcon,
  ChevronDownIcon,
  WifiIcon,
  WifiOffIcon,
  SearchIcon,
  UserCircleIcon
} from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

interface MenuItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  description?: string;
  shortcut?: string;
}

interface MenuGroup {
  name: string;
  items: MenuItem[];
  icon?: React.ComponentType<{ className?: string }>;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [openDropdown, setOpenDropdown] = useState<string | null>(null);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Organize menu items into logical groups
  const menuGroups: MenuGroup[] = [
    {
      name: 'Core',
      items: [
        { name: 'Dashboard', href: '/dashboard', icon: HomeIcon, description: 'Overview and statistics', shortcut: 'D' },
        { name: 'Map', href: '/map', icon: MapIcon, description: 'Interactive map view', shortcut: 'M' },
        { name: 'Tasks', href: '/tasks', icon: ClipboardListIcon, description: 'Task management', shortcut: 'T' },
        { name: 'Inventory', href: '/inventory', icon: PackageIcon, description: 'Resource inventory', shortcut: 'I' },
      ]
    },
    {
      name: 'Maps & Location',
      items: [
        { name: 'Terrain', href: '/terrain', icon: MountainIcon, description: 'Terrain analysis' },
        { name: 'Satellite', href: '/satellite', icon: SatelliteIcon, description: 'Satellite imagery' },
        { name: 'Heatmap', href: '/heatmap', icon: FlameIcon, description: 'Heatmap visualization' },
        { name: 'Geofencing', href: '/geofencing', icon: ShieldIcon, description: 'Geofence management' },
        { name: 'Indoor', href: '/indoor', icon: BuildingIcon, description: 'Indoor navigation' },
        { name: 'Offline Maps', href: '/offline-maps', icon: DownloadIcon, description: 'Offline map cache' },
        { name: 'Location Analytics', href: '/location-analytics', icon: ActivityIcon, description: 'Location insights' },
      ]
    },
    {
      name: 'Analytics & Intelligence',
      items: [
        { name: 'Analytics', href: '/analytics', icon: BarChart3Icon, description: 'Data analytics' },
        { name: 'Real-time Intelligence', href: '/realtime', icon: ActivityIcon, description: 'Real-time insights' },
        { name: 'AI & ML', href: '/ai', icon: BrainIcon, description: 'AI-powered features' },
      ]
    },
    {
      name: 'Management',
      items: [
        { name: 'Communication', href: '/communication', icon: MessageCircleIcon, description: 'Communication hub' },
        { name: 'Financial', href: '/financial', icon: DollarSignIcon, description: 'Financial management' },
        { name: 'Training', href: '/training', icon: GraduationCapIcon, description: 'Training programs' },
        { name: 'Security', href: '/security', icon: ShieldIcon, description: 'Security settings' },
        { name: 'Optimization', href: '/optimization', icon: ZapIcon, description: 'System optimization' },
        { name: 'Integration', href: '/integration', icon: GlobeIcon, description: 'External integrations' },
      ]
    },
    {
      name: 'Account',
      items: [
        { name: 'Profile', href: '/profile', icon: UserIcon, description: 'User profile', shortcut: 'P' },
        ...(user?.role === 'ADMIN' ? [{ name: 'Admin', href: '/admin', icon: CogIcon, description: 'Admin panel', shortcut: 'A' }] : []),
      ]
    }
  ];

  // Flatten menu items for search
  const allMenuItems = menuGroups.flatMap(group => 
    group.items.map(item => ({ ...item, group: group.name }))
  );

  // Filter menu items based on search query
  const filteredGroups = searchQuery
    ? menuGroups.map(group => ({
        ...group,
        items: group.items.filter(item =>
          item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          item.description?.toLowerCase().includes(searchQuery.toLowerCase())
        )
      })).filter(group => group.items.length > 0)
    : menuGroups;

  const isActive = (path: string) => location.pathname === path;

  const handleDropdownToggle = (groupName: string) => {
    setOpenDropdown(openDropdown === groupName ? null : groupName);
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.metaKey) return;
      
      const key = e.key.toUpperCase();
      const item = allMenuItems.find(item => item.shortcut === key);
      if (item) {
        window.location.href = item.href;
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, []);

  // Close dropdown when clicking outside or pressing ESC
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      // Close if clicking outside the dropdown menu and button
      // Check for both the button container and the dropdown menu
      const isClickInsideDropdown = target.closest('[data-dropdown-container]') || 
                                    target.closest('[data-dropdown-menu]');
      if (!isClickInsideDropdown) {
        setOpenDropdown(null);
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && openDropdown) {
        setOpenDropdown(null);
      }
    };

    if (openDropdown) {
      // Use a small delay to avoid closing immediately when opening
      const timeoutId = setTimeout(() => {
        document.addEventListener('mousedown', handleClickOutside);
        document.addEventListener('keydown', handleEscape);
      }, 100);
      
      return () => {
        clearTimeout(timeoutId);
        document.removeEventListener('mousedown', handleClickOutside);
        document.removeEventListener('keydown', handleEscape);
      };
    }
  }, [openDropdown]);

  const getInitials = (fullName: string | undefined) => {
    if (!fullName) return 'UN';
    const names = fullName.split(' ');
    if (names.length > 1) {
      return (names[0][0] + names[names.length - 1][0]).toUpperCase();
    }
    return names[0][0].toUpperCase();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navigation */}
      <nav className="bg-white shadow-sm border-b sticky top-0 z-50 overflow-visible">
        <div className="max-w-full mx-auto px-2 sm:px-4 lg:px-6 overflow-visible">
          <div className="flex justify-between items-center h-14 overflow-visible">
            {/* Logo and Main Menu */}
            <div className="flex items-center flex-1 min-w-0 overflow-visible">
              <div className="flex-shrink-0 flex items-center">
                <Link to="/dashboard" className="text-lg sm:text-xl font-bold text-blue-600 hover:text-blue-700 whitespace-nowrap">
                  Relief Platform
                </Link>
              </div>
              
              {/* Desktop Menu */}
              <div className="hidden lg:flex lg:ml-6 lg:space-x-0.5 flex-1 min-w-0 overflow-visible">
                {filteredGroups.map((group) => {
                  if (group.items.length === 0) return null;
                  
                  // Single item groups - show as direct link (no dropdown needed)
                  // Groups that should always show as dropdowns even with 1 item are excluded
                  const shouldShowAsDropdown = ['Maps & Location', 'Analytics & Intelligence', 'Management'].includes(group.name);
                  
                  if (group.items.length === 1 && !shouldShowAsDropdown) {
                    const item = group.items[0];
                    const Icon = item.icon;
                    return (
                      <Link
                        key={item.name}
                        to={item.href}
                        className={`inline-flex items-center px-2.5 py-2 rounded-md text-sm font-medium transition-colors whitespace-nowrap ${
                          isActive(item.href)
                            ? 'bg-blue-50 text-blue-600 border-b-2 border-blue-500'
                            : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                        }`}
                        title={item.description}
                      >
                        <Icon className="w-4 h-4 mr-1.5 flex-shrink-0" />
                        <span className="hidden xl:inline">{item.name}</span>
                        {item.shortcut && (
                          <span className="ml-1.5 text-xs text-gray-400 hidden 2xl:inline">({item.shortcut})</span>
                        )}
                      </Link>
                    );
                  }
                  
                  // Multi-item groups - show as dropdown
                  return (
                    <div key={group.name} className="relative group flex-shrink-0" data-dropdown-container>
                      <button
                        onClick={() => handleDropdownToggle(group.name)}
                        className={`inline-flex items-center px-2.5 py-2 rounded-md text-sm font-medium transition-colors whitespace-nowrap ${
                          group.items.some(item => isActive(item.href))
                            ? 'bg-blue-50 text-blue-600'
                            : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                        }`}
                      >
                        <span>{group.name}</span>
                        <ChevronDownIcon className={`w-3.5 h-3.5 ml-1 transition-transform flex-shrink-0 ${openDropdown === group.name ? 'rotate-180' : ''}`} />
                      </button>
                      
                      {/* Dropdown Menu */}
                      {openDropdown === group.name && (
                        <div className="absolute left-0 top-full mt-2 w-64 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-[100]" data-dropdown-menu>
                          <div className="py-1">
                            {group.items.map((item) => {
                              const Icon = item.icon;
                              return (
                                <Link
                                  key={item.name}
                                  to={item.href}
                                  onClick={() => setOpenDropdown(null)}
                                  className={`flex items-center px-4 py-2 text-sm transition-colors ${
                                    isActive(item.href)
                                      ? 'bg-blue-50 text-blue-600'
                                      : 'text-gray-700 hover:bg-gray-100'
                                  }`}
                                >
                                  <Icon className="w-4 h-4 mr-3 text-gray-400" />
                                  <div className="flex-1">
                                    <div className="font-medium">{item.name}</div>
                                    {item.description && (
                                      <div className="text-xs text-gray-500">{item.description}</div>
                                    )}
                                  </div>
                                  {item.shortcut && (
                                    <span className="text-xs text-gray-400 ml-2">({item.shortcut})</span>
                                  )}
                                </Link>
                              );
                            })}
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Right Side: Search, Status, Notifications, User */}
            <div className="flex items-center space-x-2 sm:space-x-3 flex-shrink-0">
              {/* Search (Desktop) */}
              <div className="hidden lg:flex items-center">
                <div className="relative">
                  <SearchIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                  <input
                    type="text"
                    placeholder="Search..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10 pr-8 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent w-40 min-w-[160px]"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 z-10"
                    >
                      <XIcon className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>

              {/* Online/Offline Status */}
              <div 
                className={`hidden md:flex flex-col items-start px-2.5 py-1 rounded-md min-w-[90px] ${
                  isOnline ? 'bg-green-50' : 'bg-red-50'
                }`}
                title={isOnline ? 'All changes synced' : 'Offline mode - changes will sync when online'}
              >
                <div className={`flex items-center space-x-1.5 font-medium text-xs ${
                  isOnline ? 'text-green-700' : 'text-red-700'
                }`}>
                  {isOnline ? (
                    <>
                      <WifiIcon className="w-3.5 h-3.5 flex-shrink-0" />
                      <span className="whitespace-nowrap">Online</span>
                    </>
                  ) : (
                    <>
                      <WifiOffIcon className="w-3.5 h-3.5 flex-shrink-0" />
                      <span className="whitespace-nowrap">Offline</span>
                    </>
                  )}
                </div>
                {isOnline && (
                  <div className="text-gray-500 text-[10px] leading-tight mt-0.5 whitespace-nowrap">
                    All changes synced
                  </div>
                )}
              </div>

              {/* Notifications */}
              <button 
                className="p-2 text-gray-400 hover:text-gray-500 relative flex-shrink-0"
                title="Notifications"
              >
                <BellIcon className="w-5 h-5" />
                <span className="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full border border-white"></span>
              </button>

              {/* User Menu & Logout */}
              <div className="flex items-center space-x-2 flex-shrink-0">
                {/* User Avatar/Initials */}
                <div className="hidden sm:flex items-center justify-center w-7 h-7 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold flex-shrink-0">
                  {user?.fullName ? getInitials(user.fullName) : <UserCircleIcon className="w-4 h-4" />}
                </div>
                <div className="hidden xl:block">
                  <span className="block text-sm font-medium text-gray-700 truncate max-w-[120px]">{user?.fullName}</span>
                  <span className="block text-xs text-gray-500 truncate max-w-[120px]">{user?.role}</span>
                </div>
                {/* Logout Button */}
                <button
                  onClick={logout}
                  className="flex items-center px-3 py-1.5 bg-red-50 text-red-700 rounded-md text-sm font-medium hover:bg-red-100 transition-colors whitespace-nowrap"
                  title="Logout"
                >
                  <LogOutIcon className="w-4 h-4 mr-1.5" />
                  <span className="hidden sm:inline">Logout</span>
                </button>
              </div>

              {/* Mobile Menu Button */}
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="lg:hidden p-2 text-gray-400 hover:text-gray-500"
              >
                {mobileMenuOpen ? <XIcon className="w-6 h-6" /> : <MenuIcon className="w-6 h-6" />}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="lg:hidden border-t bg-white">
            <div className="px-4 pt-2 pb-3 space-y-1">
              {/* Mobile Search */}
              <div className="mb-4">
                <div className="relative">
                  <SearchIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search menu..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              {/* Mobile Menu Items */}
              {filteredGroups.map((group) => (
                <div key={group.name} className="mb-4">
                  <div className="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    {group.name}
                  </div>
                  {group.items.map((item) => {
                    const Icon = item.icon;
                    return (
                      <Link
                        key={item.name}
                        to={item.href}
                        onClick={() => setMobileMenuOpen(false)}
                        className={`flex items-center px-3 py-2 rounded-md text-base font-medium ${
                          isActive(item.href)
                            ? 'bg-blue-50 text-blue-600'
                            : 'text-gray-700 hover:bg-gray-100'
                        }`}
                      >
                        <Icon className="w-5 h-5 mr-3" />
                        <div className="flex-1">
                          <div>{item.name}</div>
                          {item.description && (
                            <div className="text-xs text-gray-500">{item.description}</div>
                          )}
                        </div>
                        {item.shortcut && (
                          <span className="text-xs text-gray-400">({item.shortcut})</span>
                        )}
                      </Link>
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        )}
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {children}
      </main>
    </div>
  );
};

export default Layout;
