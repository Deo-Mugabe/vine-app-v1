import axios from 'axios';

class ConfigAPI {
  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: 10000,
    });

    // Add response interceptor to extract data
    this.client.interceptors.response.use(
        (response) => response.data,
        (error) => {
          console.error('API Error:', error);
          throw error;
        }
    );
  }

  async get(url) {
    return this.client.get(url);
  }

  async post(url, data) {
    return this.client.post(url, data);
  }

  async put(url, data) {
    return this.client.put(url, data);
  }

  async delete(url) {
    return this.client.delete(url);
  }
}

export const apiClient = new ConfigAPI();
