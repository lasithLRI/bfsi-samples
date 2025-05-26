import { PluginInitializerContext } from '../../../src/core/public';
import { SwiftDashboardPlugin } from './plugin';

// This exports the client-side plugin initialization function
export function plugin(initializerContext: PluginInitializerContext) {
  return new SwiftDashboardPlugin(initializerContext);
}

// Export plugin types for consumers
export {
  SwiftDashboardPluginSetup,
  SwiftDashboardPluginStart,
  AppPluginStartDependencies
} from './types';