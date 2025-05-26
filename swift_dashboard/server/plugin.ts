import {
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
  PluginInitializerContext,
} from '../../../src/core/server';
import {registerRoutes}  from './routes';
import { PluginContext, SwiftDashboardServerPluginSetup, SwiftDashboardServerPluginStart } from './types';

export class SwiftDashboardServerPlugin implements Plugin<SwiftDashboardServerPluginSetup, SwiftDashboardServerPluginStart> {
  private readonly logger: Logger;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();
  }

  public setup(core: CoreSetup, plugins: object): SwiftDashboardServerPluginSetup {
    this.logger.debug('Swift Dashboard: Setup');
    
    // Register server routes
    const router = core.http.createRouter();
    
    // Create the plugin context with logger and configuration
    const pluginContext: PluginContext = {
      logger: this.logger,
      config: {
        enabled: true,
        index: 'translated_log', // Default index name
        cacheTTL: 5 * 60 * 1000 // 5 minutes in milliseconds
      }
    };
    
    // Pass the plugin context to the routes
    registerRoutes(router, pluginContext);
    
    return {};
  }

  public start(core: CoreStart): SwiftDashboardServerPluginStart {
    this.logger.debug('Swift Dashboard: Started');
    return {};
  }

  public stop(): void {
    this.logger.debug('Swift Dashboard: Stopped');
  }
}