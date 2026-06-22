# Changelog

All notable changes to Nebula Game Platform will be documented in this file.

## [0.1.0] - 2026-06-22

### Added
- Initial public release
- `nebula-common` with scanner, worker, event, redis, lock modules
- `nebula-storage` with MySQL + ShardingSphere + MongoDB
- `nebula-gateway` with Spring Cloud Gateway, token auth, custom router
- `nebula-connect` with WebSocket, user queue, message routing
- `nebula-logic` with room management, handler routing, remote calls
- `nebula-starter` with auto-configuration
- `chat-demo` example application
- AGPL 3.0 license
- Full documentation

### Architecture
- Three-layer architecture (Gateway → Connect → Logic)
- User-level queue for ordered message processing
- Event-driven with `@Listener` annotation
- Dual storage (MySQL + MongoDB)
- Infinite horizontal scalability
