# Logging conventions

Adapted from the [SE-EDU Java logging conventions](https://se-education.org/guides/conventions/java/logging.html), checked on 28 October 2026. See [LICENSE](LICENSE) for upstream attribution.

Use `java.util.logging`. In HRvest, obtain loggers through `LogsCenter` so application configuration controls their handlers and levels.

| Level | When to use it |
| --- | --- |
| `SEVERE` | A critical failure threatens continued application operation. |
| `WARNING` | Work can continue, but a problem needs attention or caution. |
| `INFO` | A noteworthy application action or outcome helps explain normal operation. |
| `FINE` | Additional diagnostic context is useful when investigating a problem. |

Project security rules apply at every level. Record command types, outcomes, displayed indexes, counts, or safe error categories. Keep candidate names, contact details, notes, full commands, and feedback containing candidate details out of logs. Exception messages from JSON parsers may quote source data; log a safe error category instead of assuming the message is safe.

Log meaningful operation and recovery boundaries. Avoid repetitive messages for trivial getters or every loop iteration, and distinguish a completed operation from an attempted operation that was subsequently rolled back.
