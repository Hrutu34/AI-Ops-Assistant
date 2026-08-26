#!/usr/bin/env bash

set -eu

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$APP_DIR/target/ai-ops-assistant.pid"
LOG_FILE="$APP_DIR/target/ai-ops-assistant.log"

is_running() {
    [ -f "$PID_FILE" ] || return 1
    pid="$(cat "$PID_FILE")"
    kill -0 "$pid" 2>/dev/null
}

start() {
    if is_running; then
        echo "AI Ops Assistant is already running (PID $(cat "$PID_FILE"))."
        return 0
    fi

    mkdir -p "$APP_DIR/target"
    rm -f "$PID_FILE"
    cd "$APP_DIR"
    nohup mvn spring-boot:run >"$LOG_FILE" 2>&1 &
    echo $! >"$PID_FILE"
    echo "AI Ops Assistant started (PID $(cat "$PID_FILE"))."
    echo "Dashboard: http://localhost:8080"
    echo "Log: $LOG_FILE"
}

stop() {
    if ! is_running; then
        rm -f "$PID_FILE"
        echo "AI Ops Assistant is not running."
        return 0
    fi

    pid="$(cat "$PID_FILE")"
    kill "$pid"
    rm -f "$PID_FILE"
    echo "AI Ops Assistant stopped (PID $pid)."
}

status() {
    if is_running; then
        echo "AI Ops Assistant is running (PID $(cat "$PID_FILE"))."
        echo "Dashboard: http://localhost:8080"
    else
        rm -f "$PID_FILE"
        echo "AI Ops Assistant is not running."
    fi
}

case "${1:-}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    status)
        status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
