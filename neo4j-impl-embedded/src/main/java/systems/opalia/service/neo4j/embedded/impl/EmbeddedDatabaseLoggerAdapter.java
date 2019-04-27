package systems.opalia.service.neo4j.embedded.impl;

import java.util.function.Consumer;
import org.neo4j.logging.AbstractLog;
import org.neo4j.logging.Log;
import org.neo4j.logging.Logger;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseLogger;


public final class EmbeddedDatabaseLoggerAdapter
        extends AbstractLog {

    private final EmbeddedDatabaseLogger logger;

    private final Logger debugLogger;
    private final Logger infoLogger;
    private final Logger warnLogger;
    private final Logger errorLogger;

    public EmbeddedDatabaseLoggerAdapter(EmbeddedDatabaseLogger logger) {

        this.logger = logger;

        this.debugLogger = new Logger() {

            public void log(String message) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.debug(message);
                }
            }

            public void log(String message, Throwable throwable) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.debug(message, throwable);
                }
            }

            public void log(String format, Object... arguments) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    if (isDebugEnabled()) {

                        logger.debug(createMessage(format, arguments));
                    }
                }
            }

            public void bulk(Consumer<Logger> consumer) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    consumer.accept(this);
                }
            }
        };

        this.infoLogger = new Logger() {

            public void log(String message) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.info(message);
                }
            }

            public void log(String message, Throwable throwable) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.info(message, throwable);
                }
            }

            public void log(String format, Object... arguments) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    if (isInfoEnabled()) {

                        logger.info(createMessage(format, arguments));
                    }
                }
            }

            public void bulk(Consumer<Logger> consumer) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    consumer.accept(this);
                }
            }
        };

        this.warnLogger = new Logger() {

            public void log(String message) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.warning(message);
                }
            }

            public void log(String message, Throwable throwable) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.warning(message, throwable);
                }
            }

            public void log(String format, Object... arguments) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    if (isWarningEnabled()) {

                        logger.warning(createMessage(format, arguments));
                    }
                }
            }

            public void bulk(Consumer<Logger> consumer) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    consumer.accept(this);
                }
            }
        };

        this.errorLogger = new Logger() {

            public void log(String message) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.error(message);
                }
            }

            public void log(String message, Throwable throwable) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    logger.error(message, throwable);
                }
            }

            public void log(String format, Object... arguments) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    if (isErrorEnabled()) {

                        logger.error(createMessage(format, arguments));
                    }
                }
            }

            public void bulk(Consumer<Logger> consumer) {

                synchronized (EmbeddedDatabaseLoggerAdapter.this) {

                    consumer.accept(this);
                }
            }
        };
    }

    public boolean isDebugEnabled() {

        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {

        return logger.isInfoEnabled();
    }

    public boolean isWarningEnabled() {

        return logger.isWarningEnabled();
    }

    public boolean isErrorEnabled() {

        return logger.isErrorEnabled();
    }

    public Logger debugLogger() {

        return debugLogger;
    }

    public Logger infoLogger() {

        return infoLogger;
    }

    public Logger warnLogger() {

        return warnLogger;
    }

    public Logger errorLogger() {

        return errorLogger;
    }

    public void bulk(Consumer<Log> consumer) {

        synchronized (EmbeddedDatabaseLoggerAdapter.this) {

            consumer.accept(this);
        }
    }

    private String createMessage(String format, Object... arguments) {

        char[] array = format.toCharArray();
        StringBuilder builder = new StringBuilder();

        int i = 0, j = 0;

        while (i < array.length) {

            if (i + 1 < array.length && array[i] == '%' && array[i + 1] == 's') {

                if (j < arguments.length)
                    builder.append(arguments[j].toString());
                else
                    builder.append("NULL");

                i++;
                j++;

            } else {

                builder.append(array[i]);
            }

            i++;
        }

        return builder.toString();
    }
}
