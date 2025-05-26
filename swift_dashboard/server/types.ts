import { 
  CoreSetup, 
  CoreStart, 
  Plugin, 
  Logger, 
  PluginInitializerContext 
} from '../../../src/core/server';

// Setup is when the plugin is initialized
export interface SwiftDashboardServerPluginSetup {
  // Define setup contracts your plugin provides to others
}

// Start is when all plugins are ready and the server is running
export interface SwiftDashboardServerPluginStart {
  // Define start contracts your plugin provides to others
}

// Dependencies from other plugins that your server-side plugin needs during setup
export interface SwiftDashboardServerPluginSetupDependencies {
  // Add any plugin dependencies needed during setup phase
}

// Dependencies from other plugins that your server-side plugin needs during start
export interface SwiftDashboardServerPluginStartDependencies {
  // Add any plugin dependencies needed during start phase
}

// Common context that can be used throughout your plugin
export interface PluginContext {
  logger: Logger;
  config?: {
    enabled: boolean;
    index: string;
    cacheTTL?: number;
    [key: string]: any; // For any additional config properties
  };
}

// Type for message documents from OpenSearch
export interface MessageDocument {
  id: string;
  mtMessageType: string;
  mxMessageType: string;
  direction: string;
  amount: string;
  currency: string;
  date: string;
  status: string;
  originalMessage: string;
  translatedMessage: string;
  fieldError: string;
  notSupportedError: string;
  invalidError: string;
  otherError: string;
}

// For services that use the OpenSearch client
export interface OpenSearchServiceOptions {
  index?: string;
  cacheTTL?: number;
}