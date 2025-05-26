import { NavigationPublicPluginStart } from '../../../src/plugins/navigation/public';
import { DataPublicPluginStart } from '../../../src/plugins/data/public';
import { SavedObjectsStart } from '../../../src/plugins/saved_objects/public';

// Setup is when the plugin is initialized
export interface SwiftDashboardPluginSetup {
  // Define any setup contracts the plugin requires here
}

// Start is when all plugins are ready and the application loads
export interface SwiftDashboardPluginStart {
  // Define any start contracts the plugin requires here
}

// Dependencies from other plugins that the plugin needs during startup
export interface AppPluginStartDependencies {
  navigation: NavigationPublicPluginStart;
  data: DataPublicPluginStart;
  savedObjects: SavedObjectsStart;
}