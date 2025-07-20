import { apiClient } from './api';

class ConfigService {
  // FTP Configuration - matches your SystemConfigController
  async getFtpConfig() {
    return await apiClient.get('/api/v1/vine-app/ftp');
  }

  async updateFtpConfig(config) {
    return await apiClient.put('/api/v1/vine-app/ftp', config);
  }

  // File Configuration (VINE) - matches your SystemConfigController
  async getFileConfig() {
    return await apiClient.get('/api/v1/vine-app/file');
  }

  async updateFileConfig(config) {
    return await apiClient.put('/api/v1/vine-app/file', config);
  }

  // Generic SystemConfig operations
  async getAllConfigs() {
    return await apiClient.get('/api/v1/vine-app');
  }

  async getConfigById(id) {
    return await apiClient.get(`/api/v1/vine-app/${id}`);
  }

  async createConfig(config) {
    return await apiClient.post('/api/v1/vine-app', config);
  }

  async updateConfig(id, config) {
    return await apiClient.put(`/api/v1/vine-app/${id}`, config);
  }

  async deleteConfig(id) {
    return await apiClient.delete(`/api/v1/vine-app/${id}`);
  }
}

export const configService = new ConfigService();