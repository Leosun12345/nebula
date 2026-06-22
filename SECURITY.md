# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.1.x   | ✅        |
| < 0.1   | ❌        |

## Reporting a Vulnerability

**Please DO NOT report security vulnerabilities through public GitHub issues.**

Instead, please send an email to **leo@nebula-game.io** with:

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will respond within 48 hours and coordinate a fix.

## Security Best Practices

When deploying Nebula in production:

1. **Change default passwords** — Always override `nebula.storage.mysql.password` and `nebula.storage.mongodb.password` via environment variables
2. **Enable HTTPS** — Use SSL/TLS for all production endpoints
3. **Configure rate limiting** — Set proper `nebula.gateway.rate-limit` values
4. **Enable authentication** — Never disable token authentication in production
5. **Regular updates** — Keep Nebula and all dependencies up to date
6. **Monitor logs** — Watch for suspicious patterns in the logs
7. **Network security** — Run behind a firewall, limit access to Redis and MySQL
