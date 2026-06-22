
### 10.4 nebula-connect/README.md

```markdown
# Nebula Connect

WebSocket connection management, message routing and user queue.

## Features

- WebSocket server with `@ServerEndpoint`
- Online user management (`CacheOnline`)
- Annotation-driven routing (`@IREQ`)
- User-level queue (`WorkManager`)
- Event-driven with `@Listener`
- Heartbeat detection

## Usage

```java
@IREQ(codes = {Code.BUSINESS})
public class BusinessProcess extends QueueProcess<BusinessRequest> {
    @Override
    public void process(WebClient client, BusinessRequest req, String code, String subCode, int seq) {
        // handle business logic
    }
}
