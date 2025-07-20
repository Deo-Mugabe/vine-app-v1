import { apiClient } from './api';

class ConfigService {
  // FTP Configuration - matches your SystemConfigController
  async getFtpConfig() {
    return await apiClient.get('/api/v1/system-config/ftp');
  }

  async updateFtpConfig(config) {
    return await apiClient.put('/api/v1/system-config/ftp', config);
  }

  // File Configuration (VINE) - matches your SystemConfigController
  async getFileConfig() {
    return await apiClient.get('/api/v1/system-config/file');
  }

  async updateFileConfig(config) {
    return await apiClient.put('/api/v1/system-config/file', config);
  }

  // Generic SystemConfig operations
  async getAllConfigs() {
    return await apiClient.get('/api/v1/system-config');
  }

  async getConfigById(id) {
    return await apiClient.get(`/api/v1/system-config/${id}`);
  }

  async createConfig(config) {
    return await apiClient.post('/api/v1/system-config', config);
  }

  async updateConfig(id, config) {
    return await apiClient.put(`/api/v1/system-config/${id}`, config);
  }

  async deleteConfig(id) {
    return await apiClient.delete(`/api/v1/system-config/${id}`);
  }
}

export const configService = new ConfigService();