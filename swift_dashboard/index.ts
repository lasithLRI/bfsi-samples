import { PluginInitializerContext as ServerPluginInitializerContext } from '.../../../src/core/server';
import { PluginInitializerContext as ClientPluginInitializerContext } from '.../../../src/core/public';
import { SwiftDashboardServerPlugin } from './server';
import { SwiftDashboardPlugin } from './public/plugin';

// Server plugin initialization
export function serverPlugin(initializerContext: ServerPluginInitializerContext) {
  return new SwiftDashboardServerPlugin(initializerContext);
}

// Client-side plugin initialization
export function plugin(initializerContext: ClientPluginInitializerContext) {
  return new SwiftDashboardPlugin(initializerContext);
}

// Export plugin interfaces
export { SwiftDashboardPluginSetup, SwiftDashboardPluginStart } from './public/types';
export { SwiftDashboardServerPluginSetup, SwiftDashboardServerPluginStart } from './server/types';