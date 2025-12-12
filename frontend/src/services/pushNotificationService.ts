interface PushNotificationOptions {
  title: string;
  body: string;
  icon?: string;
  badge?: string;
  image?: string;
  tag?: string;
  requireInteraction?: boolean;
  data?: any;
  actions?: NotificationAction[];
  vibrate?: number[];
  sound?: string;
  dir?: 'auto' | 'ltr' | 'rtl';
  lang?: string;
  renotify?: boolean;
  silent?: boolean;
  timestamp?: number;
}

interface NotificationAction {
  action: string;
  title: string;
  icon?: string;
}

interface NotificationSubscription {
  endpoint: string;
  keys: {
    p256dh: string;
    auth: string;
  };
}

class PushNotificationService {
  private registration: ServiceWorkerRegistration | null = null;
  private subscription: PushSubscription | null = null;
  private supported = false;

  constructor() {
    this.checkSupport();
  }

  private checkSupport() {
    this.supported = 'Notification' in window && 
                     'serviceWorker' in navigator &&
                     'PushManager' in window;
  }

  async requestPermission(): Promise<NotificationPermission> {
    if (!this.supported) {
      throw new Error('Push notifications are not supported on this device');
    }

    return await Notification.requestPermission();
  }

  async registerServiceWorker(): Promise<void> {
    if (!this.supported) {
      throw new Error('Service workers are not supported');
    }

    try {
      this.registration = await navigator.serviceWorker.register('/sw.js');
      console.log('Service worker registered');
    } catch (error) {
      console.error('Service worker registration failed:', error);
      throw error;
    }
  }

  async subscribeToPush(): Promise<PushSubscription> {
    if (!this.registration) {
      await this.registerServiceWorker();
    }

    try {
      const applicationServerKey = this.urlBase64ToUint8Array(
        'BEl62iUYgUivxIkv69yViEuiBIa40HI--XVx3kXsufSkV3b9g9vZFE8wTF5XQeXNYlRhXr4q-VS9-n5n3Ut0aKA' // Example key
      );

      this.subscription = await this.registration!.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: applicationServerKey
      });

      // Send subscription to server
      await this.sendSubscriptionToServer(this.subscription);

      return this.subscription;
    } catch (error) {
      console.error('Push subscription failed:', error);
      throw error;
    }
  }

  async unsubscribeFromPush(): Promise<boolean> {
    if (!this.subscription) {
      return false;
    }

    try {
      const unsubscribed = await this.subscription.unsubscribe();
      if (unsubscribed) {
        this.subscription = null;
        await this.sendUnsubscribeToServer();
      }
      return unsubscribed;
    } catch (error) {
      console.error('Unsubscribe failed:', error);
      return false;
    }
  }

  async showLocalNotification(options: PushNotificationOptions): Promise<void> {
    if (!this.supported) {
      throw new Error('Notifications are not supported');
    }

    const permission = await this.requestPermission();
    if (permission !== 'granted') {
      throw new Error('Notification permission denied');
    }

    if (this.registration) {
      await this.registration.showNotification(options.title, {
        body: options.body,
        icon: options.icon || '/icon-192x192.png',
        badge: options.badge || '/badge-72x72.png',
        image: options.image,
        tag: options.tag,
        requireInteraction: options.requireInteraction || false,
        data: options.data,
        actions: options.actions || [],
        vibrate: options.vibrate || [200, 100, 200],
        dir: options.dir || 'auto',
        lang: options.lang || 'en',
        renotify: options.renotify || false,
        silent: options.silent || false,
        timestamp: options.timestamp || Date.now()
      });
    } else {
      // Fallback to basic notification
      new Notification(options.title, {
        body: options.body,
        icon: options.icon
      });
    }
  }

  async showTaskNotification(task: any): Promise<void> {
    const actions: NotificationAction[] = [
      {
        action: 'accept',
        title: 'Accept Task',
        icon: '/check-icon.png'
      },
      {
        action: 'view',
        title: 'View Details',
        icon: '/view-icon.png'
      },
      {
        action: 'dismiss',
        title: 'Dismiss',
        icon: '/dismiss-icon.png'
      }
    ];

    await this.showLocalNotification({
      title: 'New Task Assigned',
      body: `${task.title} - ${task.description}`,
      tag: `task-${task.id}`,
      requireInteraction: true,
      data: { task, type: 'task' },
      actions,
      icon: '/task-icon.png'
    });
  }

  async showNeedNotification(need: any): Promise<void> {
    const actions: NotificationAction[] = [
      {
        action: 'respond',
        title: 'Respond',
        icon: '/respond-icon.png'
      },
      {
        action: 'view',
        title: 'View Details'
      }
    ];

    await this.showLocalNotification({
      title: 'New Need Reported',
      body: `${need.item} - Priority: ${need.priority}`,
      tag: `need-${need.id}`,
      requireInteraction: false,
      data: { need, type: 'need' },
      actions,
      icon: '/need-icon.png'
    });
  }

  async showEmergencyNotification(emergency: any): Promise<void> {
    const actions: NotificationAction[] = [
      {
        action: 'respond',
        title: 'Respond Now',
        icon: '/emergency-icon.png'
      }
    ];

    await this.showLocalNotification({
      title: '🚨 Emergency Alert',
      body: emergency.message || 'Emergency situation requires immediate attention',
      tag: 'emergency',
      requireInteraction: true,
      data: { emergency, type: 'emergency' },
      actions,
      vibrate: [500, 200, 500, 200, 500],
      icon: '/emergency-icon.png'
    });
  }

  setupNotificationHandlers() {
    if (!this.registration) {
      return;
    }

    // Handle notification clicks
    this.registration.addEventListener('notificationclick', (event: Event) => {
      // Type assertion for NotificationEvent (service worker types)
      // NotificationEvent is not in standard types, use any with proper structure
      const notificationEvent = event as any;
      notificationEvent.notification.close();

      const data = notificationEvent.notification.data;
      const action = notificationEvent.action;

      if (action) {
        this.handleNotificationAction(action, data);
      } else {
        // Open the app
        window.focus();
      }
    });

    // Handle push events
    navigator.serviceWorker.addEventListener('message', (event) => {
      if (event.data && event.data.type === 'PUSH') {
        this.handlePushEvent(event.data);
      }
    });
  }

  private handleNotificationAction(action: string, data: any) {
    switch (action) {
      case 'accept':
        if (data.type === 'task') {
          window.dispatchEvent(new CustomEvent('notification-accept-task', { detail: data.task }));
        }
        break;

      case 'respond':
        if (data.type === 'need') {
          window.dispatchEvent(new CustomEvent('notification-respond-need', { detail: data.need }));
        } else if (data.type === 'emergency') {
          window.dispatchEvent(new CustomEvent('notification-respond-emergency', { detail: data.emergency }));
        }
        break;

      case 'view':
        window.dispatchEvent(new CustomEvent('notification-view', { detail: data }));
        break;

      case 'dismiss':
        // Do nothing
        break;
    }
  }

  private handlePushEvent(data: any) {
    // Show notification from push
    this.showLocalNotification({
      title: data.title,
      body: data.body,
      icon: data.icon,
      data: data.data
    });
  }

  private async sendSubscriptionToServer(subscription: PushSubscription) {
    // Send subscription to server
    const subscriptionJson = subscription.toJSON();
    try {
      const { apiService } = await import('./api');
      await apiService.subscribePushNotifications(subscriptionJson);
    } catch (error) {
      console.error('Failed to send subscription to server:', error);
    }
  }

  private async sendUnsubscribeToServer() {
    try {
      const { apiService } = await import('./api');
      await apiService.unsubscribePushNotifications();
    } catch (error) {
      console.error('Failed to send unsubscribe to server:', error);
    }
  }

  private urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
      .replace(/\-/g, '+')
      .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }

  getIsSupported(): boolean {
    return this.supported;
  }

  getPermission(): NotificationPermission {
    return Notification.permission;
  }

  async getSubscription(): Promise<PushSubscription | null> {
    return this.subscription;
  }
}

export const pushNotificationService = new PushNotificationService();
export default pushNotificationService;

