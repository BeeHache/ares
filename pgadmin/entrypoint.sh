#!/bin/sh

# Generate pgpass file from environment variables
if [ -n "$POSTGRES_DB" ] && [ -n "$POSTGRES_USER" ] && [ -n "$POSTGRES_PASSWORD" ]; then
    # Write to /tmp which is writable
    echo "postgres:5432:${POSTGRES_DB}:${POSTGRES_USER}:${POSTGRES_PASSWORD}" > /tmp/pgpass
    chmod 600 /tmp/pgpass
    export PGPASSFILE=/tmp/pgpass
    echo "pgpass file generated at $PGPASSFILE"
else
    echo "Warning: POSTGRES_DB, POSTGRES_USER, or POSTGRES_PASSWORD not set. pgpass not generated."
fi

# Execute the original entrypoint
exec /entrypoint.sh "$@"
