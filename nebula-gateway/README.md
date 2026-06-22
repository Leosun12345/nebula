
### 10.3 nebula-gateway/README.md

```markdown
# Nebula Gateway

API Gateway with authentication, routing and load balancing.

## Features

- Spring Cloud Gateway
- Token authentication with user info propagation
- Custom load balancing (`RouterSelector`)
- HTTP/WebSocket dual protocol routing
- Nacos service discovery

## Usage

```yaml
gateway:
  no-token-interceptions:
    - /login
    - /register
    - /health
