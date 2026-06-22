# Nebula Common

Common utilities and base classes for Nebula Game Platform.

## Modules

- **scanner** — Class path scanner with `DynamicFind` template
- **worker** — WorkManager with user-level queue and dual thread pool
- **event** — Event engine with `@Listener` annotation and BEFORE interceptor
- **redis** — Redis utilities, queue, topic manager, distributed lock
- **thread** — Thread pool management
- **util** — Common utilities (Assert, Datetime, String)
- **exception** — Business exception and error codes

## Usage

```java
// WorkManager
WorkManager.getInstance().submit(new AynWork() {
    @Override
    public void run() {
        // async task
    }
});

// Event System
@Listener(eventTag = EventTag.ENGINE_ONLINE)
public class OnlineListener implements EventListener {
    @Override
    public Object notify(Object... objs) {
        // handle online event
        return null;
    }
}
