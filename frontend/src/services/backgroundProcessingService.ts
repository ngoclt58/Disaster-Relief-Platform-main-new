interface BackgroundTask {
  id: string;
  type: string;
  data: any;
  progress: number;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  result?: any;
  error?: string;
  createdAt: number;
  startedAt?: number;
  completedAt?: number;
}

class BackgroundProcessingService {
  private tasks: Map<string, BackgroundTask> = new Map();
  private worker: Worker | null = null;
  private processingQueue: BackgroundTask[] = [];
  private isProcessing = false;

  constructor() {
    this.initWebWorker();
  }

  private initWebWorker() {
    if (typeof Worker !== 'undefined') {
      // Create a simple Web Worker for background processing
      const workerScript = `
        self.onmessage = function(e) {
          const { taskId, taskType, taskData } = e.data;
          
          switch (taskType) {
            case 'dataAggregation':
              // Aggregate data
              const aggregated = aggregateData(taskData);
              self.postMessage({ taskId, result: aggregated });
              break;
              
            case 'statisticalAnalysis':
              // Perform statistical analysis
              const analysis = performAnalysis(taskData);
              self.postMessage({ taskId, result: analysis });
              break;
              
            case 'reportGeneration':
              // Generate report
              const report = generateReport(taskData);
              self.postMessage({ taskId, result: report });
              break;
              
            case 'dataValidation':
              // Validate data
              const validation = validateData(taskData);
              self.postMessage({ taskId, result: validation });
              break;
              
            default:
              self.postMessage({ taskId, error: 'Unknown task type' });
          }
        };
        
        function aggregateData(data) {
          // Simple aggregation logic
          return {
            total: data.length,
            sum: data.reduce((sum, item) => sum + (item.value || 0), 0),
            average: data.length > 0 ? data.reduce((sum, item) => sum + (item.value || 0), 0) / data.length : 0,
            min: Math.min(...data.map(item => item.value || 0)),
            max: Math.max(...data.map(item => item.value || 0))
          };
        }
        
        function performAnalysis(data) {
          // Simple statistical analysis
          const values = data.map(item => item.value || 0);
          const mean = values.reduce((sum, val) => sum + val, 0) / values.length;
          const variance = values.reduce((sum, val) => sum + Math.pow(val - mean, 2), 0) / values.length;
          
          return {
            mean,
            standardDeviation: Math.sqrt(variance),
            variance,
            range: Math.max(...values) - Math.min(...values)
          };
        }
        
        function generateReport(data) {
          // Simple report generation
          return {
            title: data.title || 'Report',
            generatedAt: new Date().toISOString(),
            summary: {
              totalItems: data.items?.length || 0,
              categories: data.items?.reduce((acc, item) => {
                const category = item.category || 'Unknown';
                acc[category] = (acc[category] || 0) + 1;
                return acc;
              }, {})
            }
          };
        }
        
        function validateData(data) {
          // Simple data validation
          const errors = [];
          
          data.forEach((item, index) => {
            if (!item.id) errors.push({ index, field: 'id', message: 'Missing ID' });
            if (!item.value && item.value !== 0) errors.push({ index, field: 'value', message: 'Missing value' });
            if (item.value < 0) errors.push({ index, field: 'value', message: 'Negative value' });
          });
          
          return {
            valid: errors.length === 0,
            errors,
            validatedAt: new Date().toISOString()
          };
        }
      `;

      try {
        const blob = new Blob([workerScript], { type: 'application/javascript' });
        this.worker = new Worker(URL.createObjectURL(blob));
        
        this.worker.onmessage = (e) => {
          const { taskId, result, error } = e.data;
          const task = this.tasks.get(taskId);
          
          if (task) {
            task.status = error ? 'failed' : 'completed';
            task.result = result;
            task.error = error;
            task.completedAt = Date.now();
            task.progress = 100;
          }
          
          this.continueProcessing();
        };

        this.worker.onerror = (error) => {
          console.error('Worker error:', error);
          this.isProcessing = false;
        };
      } catch (error) {
        console.error('Failed to create Web Worker:', error);
      }
    }
  }

  async processInBackground(
    type: string,
    data: any,
    onProgress?: (progress: number) => void
  ): Promise<BackgroundTask> {
    const taskId = `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    const task: BackgroundTask = {
      id: taskId,
      type,
      data,
      progress: 0,
      status: 'pending',
      createdAt: Date.now()
    };

    this.tasks.set(taskId, task);
    this.processingQueue.push(task);
    
    this.startProcessing();

    return task;
  }

  private startProcessing() {
    if (this.isProcessing) return;
    
    this.isProcessing = true;
    this.continueProcessing();
  }

  private continueProcessing() {
    if (this.processingQueue.length === 0) {
      this.isProcessing = false;
      return;
    }

    const task = this.processingQueue.shift();
    if (!task || task.status !== 'pending') {
      this.continueProcessing();
      return;
    }

    task.status = 'processing';
    task.startedAt = Date.now();

    // Use Web Worker if available, otherwise process in main thread
    if (this.worker) {
      this.worker.postMessage({
        taskId: task.id,
        taskType: task.type,
        taskData: task.data
      });
    } else {
      // Fallback to main thread processing
      this.processInMainThread(task);
    }
  }

  private processInMainThread(task: BackgroundTask) {
    try {
      let result: any;

      switch (task.type) {
        case 'dataAggregation':
          result = this.aggregateData(task.data);
          break;
          
        case 'statisticalAnalysis':
          result = this.performAnalysis(task.data);
          break;
          
        case 'reportGeneration':
          result = this.generateReport(task.data);
          break;
          
        case 'dataValidation':
          result = this.validateData(task.data);
          break;
          
        default:
          throw new Error('Unknown task type');
      }

      task.status = 'completed';
      task.result = result;
      task.completedAt = Date.now();
      task.progress = 100;
    } catch (error: any) {
      task.status = 'failed';
      task.error = error.message;
      task.completedAt = Date.now();
    }

    // Use setTimeout to yield to the main thread
    setTimeout(() => this.continueProcessing(), 0);
  }

  // Helper methods for different processing types
  aggregateData(data: any[]) {
    return {
      total: data.length,
      sum: data.reduce((sum, item) => sum + (item.value || 0), 0),
      average: data.length > 0 ? data.reduce((sum, item) => sum + (item.value || 0), 0) / data.length : 0,
      min: Math.min(...data.map(item => item.value || 0)),
      max: Math.max(...data.map(item => item.value || 0))
    };
  }

  performAnalysis(data: any[]) {
    const values = data.map(item => item.value || 0);
    const mean = values.reduce((sum, val) => sum + val, 0) / values.length;
    const variance = values.reduce((sum, val) => sum + Math.pow(val - mean, 2), 0) / values.length;

    return {
      mean,
      standardDeviation: Math.sqrt(variance),
      variance,
      range: Math.max(...values) - Math.min(...values)
    };
  }

  generateReport(data: any) {
    return {
      title: data.title || 'Report',
      generatedAt: new Date().toISOString(),
      summary: {
        totalItems: data.items?.length || 0,
        categories: data.items?.reduce((acc: any, item: any) => {
          const category = item.category || 'Unknown';
          acc[category] = (acc[category] || 0) + 1;
          return acc;
        }, {})
      }
    };
  }

  validateData(data: any[]) {
    const errors: any[] = [];

    data.forEach((item, index) => {
      if (!item.id) errors.push({ index, field: 'id', message: 'Missing ID' });
      if (!item.value && item.value !== 0) errors.push({ index, field: 'value', message: 'Missing value' });
      if (item.value < 0) errors.push({ index, field: 'value', message: 'Negative value' });
    });

    return {
      valid: errors.length === 0,
      errors,
      validatedAt: new Date().toISOString()
    };
  }

  getTask(taskId: string): BackgroundTask | undefined {
    return this.tasks.get(taskId);
  }

  getTasks(): BackgroundTask[] {
    return Array.from(this.tasks.values());
  }

  async cancelTask(taskId: string): Promise<void> {
    const task = this.tasks.get(taskId);
    if (task && task.status === 'pending') {
      this.processingQueue = this.processingQueue.filter(t => t.id !== taskId);
      task.status = 'failed';
      task.error = 'Cancelled by user';
    }
  }

  async cleanupCompletedTasks() {
    const now = Date.now();
    const MAX_AGE = 24 * 60 * 60 * 1000; // 24 hours

    for (const [taskId, task] of this.tasks.entries()) {
      if (task.status === 'completed' && task.completedAt && 
          (now - task.completedAt) > MAX_AGE) {
        this.tasks.delete(taskId);
      }
    }
  }
}

export const backgroundProcessingService = new BackgroundProcessingService();
export default backgroundProcessingService;

