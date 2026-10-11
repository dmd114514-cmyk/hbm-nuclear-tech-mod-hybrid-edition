#!/usr/bin/env bash
# =============================================================================
# L3 in-world smoke (A1-SMOKE) - for the defects that only show up with a world loaded
#   (typically: a block whose TE is unregistered giving is missing a mapping during world tick, constructor NPEs and so on)
#
# Usage:
#   bash tools/verify/smoke_inworld.sh                        # start the server and run the default commands
#   bash tools/verify/smoke_inworld.sh --place "8 70 8 hbm:crate_iron"
#   bash tools/verify/smoke_inworld.sh --cmd "time set day" --cmd "save-all"
#   bash tools/verify/smoke_inworld.sh --keep-run             # keep run/ and the logs (the default keeps them anyway)
#
# What it does: turn rcon on temporarily, start runServer in the background (which loads run/world), wait for Done (,
# run commands over rcon (can place blocks), save-all (exercises the TE serialisation path), stop, print a summary,
# and restore server.properties. Judges: (1) Done ( appeared; (2) no new crash-report; (3) every rcon command replied.
# =============================================================================
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GIT_COMMON="$(git -C "$ROOT" rev-parse --git-common-dir 2>/dev/null || echo .git)"
case "$GIT_COMMON" in /*) ;; *) GIT_COMMON="$ROOT/$GIT_COMMON" ;; esac
BUILD="$(cd "$(dirname "$GIT_COMMON")/.." && pwd)/build.sh"
RUN="$ROOT/run"
LOG="$RUN/logs/latest.log"
PASS="a1smoke$$"
PORT=25575
CMDS=()
PLACES=()
WAIT_DONE="${WAIT_DONE:-150}"

while [ $# -gt 0 ]; do
  case "$1" in
    --place) PLACES+=("$2"); shift 2 ;;
    --cmd)   CMDS+=("$2");  shift 2 ;;
    *) echo "Unknown argument: $1"; exit 2 ;;
  esac
done

command -v python3 >/dev/null || { echo "Requires python3 (rcon client)"; exit 2; }
[ -f "$RUN/eula.txt" ] || echo "eula=true" > "$RUN/eula.txt"

# ---- back up server.properties and turn rcon on ----
SP="$RUN/server.properties"
cp -f "$SP" "$SP.a1smoke.bak" 2>/dev/null || : 
grep -q '^enable-rcon=' "$SP" && sed -i 's/^enable-rcon=.*/enable-rcon=true/' "$SP" || echo 'enable-rcon=true' >> "$SP"
grep -q '^rcon.port=' "$SP" && sed -i "s/^rcon.port=.*/rcon.port=$PORT/" "$SP" || echo "rcon.port=$PORT" >> "$SP"
grep -q '^rcon.password=' "$SP" && sed -i "s/^rcon.password=.*/rcon.password=$PASS/" "$SP" || echo "rcon.password=$PASS" >> "$SP"
restore() { [ -f "$SP.a1smoke.bak" ] && mv -f "$SP.a1smoke.bak" "$SP"; }
trap restore EXIT

CRASH_BEFORE="$(ls "$RUN/crash-reports" 2>/dev/null | wc -l)"

echo "================ A1-SMOKE (L3) ================"
echo "Starting the server (waiting for Done, at most ${WAIT_DONE}s)…"
( timeout $((WAIT_DONE + 90)) bash "$BUILD" "$ROOT" runServer >/tmp/a1_smoke_server.log 2>&1 ) &
SPID=$!

ok=0
for i in $(seq 1 "$WAIT_DONE"); do
  if grep -aq 'Done (' "$LOG" 2>/dev/null; then ok=1; break; fi
  sleep 1
done
if [ "$ok" != 1 ]; then
  echo "[1] server start: ✗ no 'Done (' seen within ${WAIT_DONE}s (check /tmp/a1_smoke_server.log and $LOG)"
else
  echo "[1] server start: ✓ $(grep -a 'Done (' "$LOG" | tail -1 | sed 's/.*DedicatedServer\]: //')"
fi

# ---- rcon ----
if [ "$ok" = 1 ]; then
  python3 - "$PORT" "$PASS" "${PLACES[@]:-}" -- "${CMDS[@]:-}" <<'PY'
import socket, struct, sys, time
port = int(sys.argv[1]); pw = sys.argv[2]
args = sys.argv[3:]
if '--' in args:
    cut = args.index('--'); places = [a for a in args[:cut] if a]; cmds = [a for a in args[cut+1:] if a]
else:
    places, cmds = [a for a in args if a], []
def pkt(i, t, b):
    body = b.encode('utf-8') + b'\x00\x00'
    return struct.pack('<ii', i, 0)[:0] + struct.pack('<ii', len(body)+8, i) + struct.pack('<i', t) + body
try:
    s = socket.create_connection(('127.0.0.1', port), timeout=10)
except Exception as e:
    print('[2] rcon: ✗ cannot reach %s' % e); sys.exit(0)
s.sendall(pkt(1, 3, pw)); s.recv(4096)
seq = [('setblock ' + p, ) for p in places] + [(c, ) for c in cmds] + [('save-all', )]
for (c, ) in seq:
    s.sendall(pkt(2, 2, c)); time.sleep(1.0)
    try:
        r = s.recv(8192)[12:160].decode('utf-8', 'ignore').strip('\x00')
    except Exception as e:
        r = '<no reply: %s>' % e
    print('[2] rcon %-34s -> %s' % (c, r))
s.sendall(pkt(3, 2, 'stop')); time.sleep(0.5)
s.close()
PY
fi

wait $SPID 2>/dev/null || true

CRASH_AFTER="$(ls "$RUN/crash-reports" 2>/dev/null | wc -l)"
echo "[3] crash-reports: before $CRASH_BEFORE → after $CRASH_AFTER $([ "$CRASH_AFTER" -gt "$CRASH_BEFORE" ] && echo '✗ new ones appeared' || echo '✓ none added')"
if [ "$CRASH_AFTER" -gt "$CRASH_BEFORE" ]; then
  ls -t "$RUN/crash-reports" | head -1 | sed 's/^/    latest: /'
  grep -a 'Caused by\|java.lang' "$RUN/crash-reports/$(ls -t "$RUN/crash-reports" | head -1)" 2>/dev/null | head -3 | sed 's/^/    /'
fi
echo "================ /A1-SMOKE (L3) ================"
