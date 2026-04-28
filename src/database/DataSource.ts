import { DataSource } from 'typeorm';
import { DatabaseConfig } from '../config/DatabaseConfig';

export const AppDataSource = new DataSource(DatabaseConfig.getConfig());

// Initialize data source
export async function initializeDataSource(): Promise<DataSource> {
  if (!AppDataSource.isInitialized) {
    await AppDataSource.initialize();
  }
  return AppDataSource;
}

export async function closeDataSource(): Promise<void> {
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }
}
