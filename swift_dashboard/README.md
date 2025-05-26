# Swift Dashboard Backend

## Overview
The Swift Dashboard Backend is an Express.js application that connects to OpenSearch to fetch and serve data for a dashboard. This project is designed to provide a seamless integration with a React frontend, allowing for efficient data retrieval and display.

## Project Structure
```
swift-dashboard-backend
├── src
│   ├── app.ts                     # Entry point of the application
│   ├── config                      # Configuration files
│   │   ├── index.ts               # Application configuration settings
│   │   └── opensearch.ts          # OpenSearch connection settings
│   ├── controllers                 # Request handlers
│   │   ├── dashboardController.ts  # Handles dashboard-related requests
│   │   └── index.ts               # Exports all controllers
│   ├── middleware                  # Middleware functions
│   │   ├── errorHandler.ts         # Error handling middleware
│   │   └── auth.ts                # Authentication middleware
│   ├── routes                      # API routes
│   │   ├── api.ts                 # API endpoints for dashboard data
│   │   └── index.ts               # Sets up all routes
│   ├── services                    # Business logic and data fetching
│   │   ├── opensearchService.ts    # Interacts with OpenSearch
│   │   └── index.ts               # Exports all services
│   ├── types                       # TypeScript types and interfaces
│   │   ├── dashboard.ts            # Types related to dashboard data
│   │   └── index.ts               # Exports all types
│   └── utils                       # Utility functions
│       ├── logger.ts               # Logger utility
│       └── helpers.ts             # Helper functions
├── .env.example                    # Example environment variables
├── .gitignore                      # Git ignore file
├── package.json                    # NPM configuration
├── tsconfig.json                   # TypeScript configuration
└── README.md                       # Project documentation
```

## Installation
1. Clone the repository:
   ```
   git clone <repository-url>
   cd swift-dashboard-backend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Set up environment variables:
   - Copy `.env.example` to `.env` and fill in the required values.

## Running the Application
To start the server, run:
```
npm start
```

The application will be available at `http://localhost:3000`.

## API Endpoints
- **GET /api/dashboard**: Fetches data for the dashboard from OpenSearch.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any enhancements or bug fixes.

## License
This project is licensed under the MIT License.