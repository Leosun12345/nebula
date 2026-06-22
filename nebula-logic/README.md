
### 10.5 nebula-logic/README.md

```markdown
# Nebula Logic

Game logic layer with room management, handler routing and remote calls.

## Features

- Room management (`Room` / `CacheRoom`)
- Handler routing (`@ISubCode`)
- Remote calls via Redis queue
- Event-driven game logic

## Usage

```java
@ISubCode(subCodes = {SubCode.REQ_ROOM_INFO})
public class ReqRoomInfoHandler extends Handler {
    @Override
    public void handler(Room room, SimpleUser user, int seq, String params) {
        // handle room info request
    }
}
