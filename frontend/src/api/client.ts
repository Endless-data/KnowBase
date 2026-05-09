import axios from 'axios';
import type { ApiResponse } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

export async function request<T>(requestPromise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  try {
    const response = await requestPromise;
    const body = response.data;
    if (body.code !== 200) {
      throw new Error(body.message || 'Request failed');
    }
    return body.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const message = (error.response?.data as Partial<ApiResponse<unknown>> | undefined)?.message;
      throw new Error(message || error.message || 'Request failed');
    }
    throw error;
  }
}
