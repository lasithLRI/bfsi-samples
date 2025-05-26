import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { CoreStart } from '../../../../../../src/core/public'; // Fix incorrect path (remove leading dot)
import { AppPluginStartDependencies } from '../../../types'; // Fix incorrect path
import Header from './Header';
import OverviewPage from './OverviewPage';
import MessagesPage from './MessagesPage';

interface AppProps {
  coreStart: CoreStart;
  plugins: AppPluginStartDependencies;
}

const App: React.FC<AppProps> = ({ coreStart, plugins }) => {
  // You can now access OpenSearch Dashboards services like:
  // coreStart.http - for API requests
  // coreStart.notifications - for showing toasts
  // plugins.data - for data queries

  return (
    <div className="swift-dashboard-plugin">
      <Header />
      <div className="app-container">
        <Switch>
          <Route path="/messages" component={MessagesPage} />
          <Route path="/" component={OverviewPage} />
        </Switch>
      </div>
    </div>
  );
};

export default App;