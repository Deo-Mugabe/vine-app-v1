import axios from 'axios';

class ConfigAPI {
  constructor() {
    this.client = axios.create({
      baseURL: process.env.REACT_APP_API_BASE_URL,
      timeout: 10000,
    });
  }

  async getVineConfig() {
    return this.client.get('/api/v1/system-config/file');
  }

  async updateVineConfig(config) {
    return this.client.put('/api/v1/system-config/file', config);
  }

  async getFtpConfig() {
    return this.client.get('/api/v1/system-config/ftp');
  }

  async updateFtpConfig(config) {
    return this.client.put('/api/v1/system-config/ftp', config);
  }
}

export const configApi = new ConfigAPI();