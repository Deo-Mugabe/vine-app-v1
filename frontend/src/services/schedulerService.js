import { apiClient } from './api';

class SchedulerService {
  // Get current scheduler status
  async getSchedulerStatus() {
    return await apiClient.get('/api/v1/scheduler/status');
  }

  // Start scheduler with interval
  async startScheduler(intervalMinutes = 30) {
    return await apiClient.post(`/api/v1/scheduler/start?intervalMinutes=${intervalMinutes}`);
  }

  // Stop scheduler
  async stopScheduler() {
    return await apiClient.post('/api/v1/scheduler/stop');
  }

  // Update scheduler configuration
  async updateSchedulerConfig(config) {
    const payload = {
      enabled: config.enabled,
      intervalMinutes: config.intervalMinutes,
      startFromTime: config.startFromTime
    };
    return await apiClient.put('/api/v1/scheduler/config', payload);
  }

  // Get job history with pagination and filtering
  async getJobHistory(page = 0, size = 20, days = null) {
    let url = `/api/v1/scheduler/history?page=${page}&size=${size}`;
    if (days !== null) {
      url += `&days=${days}`;
    }
    return await apiClient.get(url);
  }

  // Get latest job executions (limited number)
  async getLatestExecutions(limit = 10) {
    return await apiClient.get(`/api/v1/scheduler/history/latest?limit=${limit}`);
  }

  // Trigger job manually
  async runJobNow() {
    return await apiClient.post('/api/v1/scheduler/run-now');
  }
}

export const schedulerService = new SchedulerService();