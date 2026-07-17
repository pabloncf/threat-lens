#!/bin/sh
# Synthetic attack traffic for the ThreatLens demo. Fired only at the bundled,
# intentionally-vulnerable /demo/** endpoints in this same Docker Compose stack - never at a
# third-party target. See claude.md §4.
set -eu

TARGET_URL="${TARGET_URL:-http://threatlens:8080}"

echo "[simulator] waiting for $TARGET_URL/actuator/health ..."
i=0
until curl -sf "$TARGET_URL/actuator/health" >/dev/null 2>&1; do
    i=$((i + 1))
    if [ "$i" -ge 30 ]; then
        echo "[simulator] ThreatLens never became healthy, giving up." >&2
        exit 1
    fi
    sleep 2
done
echo "[simulator] ThreatLens is healthy. Starting synthetic traffic."

echo "[simulator] benign traffic (for contrast) ..."
curl -s -o /dev/null -X POST "$TARGET_URL/demo/login" \
    --data-urlencode "username=alice" --data-urlencode "password=password123"
curl -s -o /dev/null -X POST "$TARGET_URL/demo/comments" \
    --data-urlencode "body=Great article, thanks for sharing!"

echo "[simulator] SQL injection (UNION-based credential exfiltration) against /demo/login ..."
curl -s -X POST "$TARGET_URL/demo/login" \
    --data-urlencode "username=nonexistent' UNION SELECT password FROM demo_users WHERE username='alice' -- " \
    --data-urlencode "password=x"
echo

echo "[simulator] stored XSS (cookie theft payload) against /demo/comments ..."
curl -s -o /dev/null -X POST "$TARGET_URL/demo/comments" \
    --data-urlencode "body=<script>document.cookie</script>"

echo "[simulator] brute force against /demo/login (8 rapid failed attempts) ..."
i=0
while [ "$i" -lt 8 ]; do
    curl -s -o /dev/null -X POST "$TARGET_URL/demo/login" \
        --data-urlencode "username=alice" --data-urlencode "password=wrong-$i"
    i=$((i + 1))
done

echo "[simulator] done. Check GET $TARGET_URL/api/incident-reports (HTTP Basic auth) for results."
