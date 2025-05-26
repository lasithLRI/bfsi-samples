import { 
  AppMountParameters, 
  CoreSetup, 
  CoreStart, 
  Plugin,
  PluginInitializerContext 
} from '../../../src/core/public';
import { 
  SwiftDashboardPluginSetup, 
  SwiftDashboardPluginStart,
  AppPluginStartDependencies
} from './types';
 
import { renderApp } from './applications/swift_dashboard_app/swiftDashboard'
export class SwiftDashboardPlugin implements Plugin<SwiftDashboardPluginSetup, SwiftDashboardPluginStart> {
  private readonly initializerContext: PluginInitializerContext;

  constructor(initializerContext: PluginInitializerContext) {
    this.initializerContext = initializerContext;
  }

  public setup(core: CoreSetup): SwiftDashboardPluginSetup {
    // Register your application with OpenSearch Dashboards
    core.application.register({
      id: 'swiftDashboard',
      title: 'SWIFT Dashboard',
      category: {
        id: 'swiftPlugin',
        label: 'SWIFT Analytics',
        order: 1000,
      },
      order: 1000,
      appRoute: '/app/swiftDashboard',
      async mount(params: AppMountParameters) {
        // Create a root element with your plugin's namespace
        const { element } = params;
        element.classList.add('swift-dashboard-plugin');
        
        // Safer dynamic import
        try {
            console.log('Attempting to import applications...');
            // const applicationsModule = await import('./applications/index.ts.bkp');
            // console.log('Applications module imported:', applicationsModule);
            
            if (!renderApp) {
              throw new Error('renderApp function not found in applications module');
            }
            
            console.log('Getting start services...');
            const [coreStart, depsStart] = await core.getStartServices();
            console.log('Services obtained, rendering app...');
            
            return renderApp(params, coreStart, depsStart as AppPluginStartDependencies);
          } catch (error) {
            console.error('Failed to load Swift Dashboard application:', error);
            if (error instanceof Error) {
              console.error('Error name:', error.name);
              console.error('Error message:', error.message);
              console.error('Error stack:', error.stack);
            }
            element.innerHTML = `<div class="euiText euiText--medium">
              <p>Failed to load SWIFT Dashboard application. Error: ${error instanceof Error ? error.message : String(error)}</p>
              <p>Please check browser console for details.</p>
            </div>`;
            return () => {};
          }
      },
    });

    console.log("Something went wrong!");
    return {};
  }

  public start(core: CoreStart): SwiftDashboardPluginStart {
    return {};
  }

  public stop() {}
}