#!/bin/bash
#
# Start script for resourceprocessortest. Intended to be run by an upstart job.

[[ "$USER" == "resourceprocessortest" ]] || exec su -l -s /bin/sh -c "exec $0" resourceprocessortest

JAVA_OPTS=()
if [[ -n "$IS24_PROXY_HOST" ]] && [[ -n "$IS24_PROXY_PORT" ]] && [[ -n "$IS24_NON_PROXY_HOSTS" ]]; then
    JAVA_OPTS=( "${JAVA_OPTS[@]}"
    -Dhttp.proxyHost="${IS24_PROXY_HOST}"
    -Dhttp.proxyPort="${IS24_PROXY_PORT}"
    -Dhttps.proxyHost="${IS24_PROXY_HOST}"
    -Dhttps.proxyPort="${IS24_PROXY_PORT}"
    -Dhttp.nonProxyHosts="${IS24_NON_PROXY_HOSTS}"
    )
fi

exec &> >(exec logger -s -t "resourceprocessortest")

trap 'kill -TERM $(jobs -p)' TERM
set -x

LOGGING_CONFIG=/etc/resourceprocessortest/default-logback.xml

shopt -s nullglob
for configfile in /etc/resourceprocessortest/*.sh ; do
    source "$configfile"
done

java -server "${JAVA_OPTS[@]}" -Dlogging.config="$LOGGING_CONFIG" -jar /opt/resourceprocessortest/resourceprocessortest.jar &

wait
