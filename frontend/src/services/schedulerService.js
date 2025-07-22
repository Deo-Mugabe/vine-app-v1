import { apiClient } from './api';

class SchedulerService {
  // Get current scheduler status
  async getSchedulerStatus() {
    try {
      return await apiClient.get('/api/v1/scheduler/status');
    } catch (error) {
      console.error('Error getting scheduler status:', error);
      throw error;
    }
  }

  // Start scheduler with interval
  async startScheduler(intervalMinutes = 30) {
    try {
      // Validate input
      if (!intervalMinutes || intervalMinutes < 1) {
        throw new Error('Interval minutes must be at least 1');
      }

      const url = `/api/v1/scheduler/start?intervalMinutes=${intervalMinutes}`;
      return await apiClient.post(url);
    } catch (error) {
      console.error('Error starting scheduler:', error);
      throw error;
    }
  }

  // Stop scheduler
  async stopScheduler() {
    try {
      return await apiClient.post('/api/v1/scheduler/stop');
    } catch (error) {
      console.error('Error stopping scheduler:', error);
      throw error;
    }
  }

  // Update scheduler configuration
  async updateSchedulerConfig(config) {
    try {
      // Validate config
      if (!config) {
        throw new Error('Configuration is required');
      }

      if (config.intervalMinutes && config.intervalMinutes < 1) {
        throw new Error('Interval minutes must be at least 1');
      }

      // Ensure proper structure
      const payload = {
        enabled: Boolean(config.enabled),
        intervalMinutes: parseInt(config.intervalMinutes) || 30,
        startFromTime: config.startFromTime || null
      };

      console.log('Updating scheduler config with payload:', payload);
      return await apiClient.put('/api/v1/scheduler/config', payload);
    } catch (error) {
      console.error('Error updating scheduler config:', error);
      throw error;
    }
  }

  // Get job history with pagination and filtering
  async getJobHistory(page = 0, size = 20, days = null) {
    try {
      // Validate pagination parameters
      if (page < 0) {
        throw new Error('Page must be non-negative');
      }
      if (size < 1 || size > 100) {
        throw new Error('Size must be between 1 and 100');
      }

      let url = `/api/v1/scheduler/history?page=${page}&size=${size}`;
      if (days !== null && days > 0) {
        url += `&days=${days}`;
      }

      return await apiClient.get(url);
    } catch (error) {
      console.error('Error getting job history:', error);
      throw error;
    }
  }

  // Get latest job executions (limited number)
  async getLatestExecutions(limit = 10) {
    try {
      if (limit < 1 || limit > 50) {
        throw new Error('Limit must be between 1 and 50');
      }

      return await apiClient.get(`/api/v1/scheduler/history/latest?limit=${limit}`);
    } catch (error) {
      console.error('Error getting latest executions:', error);
      throw error;
    }
  }

  // Trigger job manually
  async runJobNow() {
    try {
      return await apiClient.post('/api/v1/scheduler/run-now');
    } catch (error) {
      console.error('Error triggering job:', error);
      throw error;
    }
  }

  // Health check - verify API is accessible
  async healthCheck() {
    try {
      return await apiClient.get('/api/v1/scheduler/status');
    } catch (error) {
      console.error('Scheduler service health check failed:', error);
      throw error;
    }
  }
}

export const schedulerService = new SchedulerService();