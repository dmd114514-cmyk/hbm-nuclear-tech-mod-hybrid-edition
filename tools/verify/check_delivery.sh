#!/usr/bin/env bash
# =============================================================================
#
# Usage (from the project root or anywhere):
#   bash tools/verify/check_delivery.sh                 # report on HEAD (includes a build)
#   bash tools/verify/check_delivery.sh --with-g1       # also actually run runClient (the L2 gate)
#   bash tools/verify/check_delivery.sh --no-build      # skip the build (other numbers only)
#
#   [1] G0  compile: gradle_exit / error count
#   [2] Old API leftovers: hits among added lines (comment lines excluded)
#   [3] Closure: how many added files are referenced elsewhere / how many are orphans
#   [4] Hot file erosion: new fields in ModBlocks/ModItems/ModEntities (should be 0)
#   [5] Registry: block/item/entity/potion counts from the last runClient log, plus that log's timestamp
#   [6] dev-title: whether the last runClient log has a dev-title line (the G1 receipt)
#   [7] crash-reports: timestamps of the three newest files (did this round add one)
# =============================================================================
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"   # <root>/Forge1122MDK
REPO="$ROOT"
GIT_COMMON="$(git -C "$ROOT" rev-parse --git-common-dir 2>/dev/null || echo .git)"
case "$GIT_COMMON" in /*) ;; *) GIT_COMMON="$ROOT/$GIT_COMMON" ;; esac
BUILD="$(cd "$(dirname "$GIT_COMMON")/.." && pwd)/build.sh"
REF="HEAD"
WITH_G1=0
DO_BUILD=1
for a in "$@"; do
  case "$a" in
    --with-g1) WITH_G1=1 ;;
    --no-build) DO_BUILD=0 ;;
    -*) echo "Unknown argument: $a"; exit 2 ;;
    *) REF="$a" ;;
  esac
done

cd "$REPO" || exit 2
HOTFILES="src/main/java/com/hbm/blocks/ModBlocks.java src/main/java/com/hbm/items/ModItems.java src/main/java/com/hbm/entity/ModEntities.java"
LEGACY='cpw\.mods\.fml|ForgeDirection|worldObj\.|\.xCoord|\.yCoord|\.zCoord|IIcon|MovingObjectPosition|net\.minecraft\.util\.AxisAlignedBB|com\.hbm\.packet|com\.hbm\.handler\.threading'

# Baseline = the merge-base of main and that ref (normally the branch point);
# if the merge-base is the ref itself (its commits were merged or swallowed by a revert) it falls back to versus the previous commit and says so.
BASE="$(git merge-base main "$REF" 2>/dev/null || echo HEAD~1)"
if [ "$(git rev-parse "$REF")" = "$BASE" ]; then
  echo "!! note: $REF has no net change against main (its commits are already in main's history, possibly merged or rolled back) ⇒ the numbers below are given as "relative to the previous commit""
  BASE="$REF~1"
fi

echo "================ A1-GATE ================"
printf "ref=%s  head=%s  base=%s\n" "$REF" "$(git rev-parse --short "$REF")" "$(git rev-parse --short "$BASE")"
echo

# ---------- [1] G0 ----------
if [ "$DO_BUILD" = 1 ]; then
  OUT="$(bash "$BUILD" "$REPO" compileJava 2>&1 || true)"
  EXIT="$(printf '%s' "$OUT" | grep -o 'gradle_exit=[0-9]*' | tail -1 | cut -d= -f2)"
  ERRS="$(printf '%s' "$OUT" | grep -c 'error:' || true)"
  [ -z "$EXIT" ] && EXIT="?"
  echo "[1] G0 compile: gradle_exit=$EXIT  errors=$ERRS"
else
  echo "[1] G0 compile: (skipped --no-build)"
fi

# ---------- [2] old API leftovers ----------
HITS="$(git diff "$BASE" "$REF" -- 'src/main/java/**' 2>/dev/null \
        | grep -E '^\+' | grep -vE '^\+\+\+' \
        | grep -vE '^\+\s*(\*|//|/\*)' \
        | grep -cE "$LEGACY" || true)"
echo "[2] old-API residue (added lines, comments excluded): $HITS"

# ---------- [3] closure: does anything reference the added classes ----------
NEWFILES="$(git diff --name-only --diff-filter=A "$BASE...$REF" -- 'src/main/java/**/*.java' 2>/dev/null || true)"
TOTAL=0; ORPHAN=0
if [ -n "$NEWFILES" ]; then
  while IFS= read -r f; do
    [ -z "$f" ] && continue
    cls="$(basename "$f" .java)"
    TOTAL=$((TOTAL+1))
    n="$(grep -rlE "\\b${cls}\\b" src/main/java --include='*.java' 2>/dev/null | grep -v "^${f}$" | wc -l)"
    [ "$n" -eq 0 ] && ORPHAN=$((ORPHAN+1))
  done <<< "$NEWFILES"
fi
echo "[3] closure: $TOTAL new classes, of which $ORPHAN have zero references tree-wide (orphans > 0 means "classes landed but never wired" = half-done)"

# ---------- [4] hot file erosion ----------
echo -n "[4] new fields in hot files: "
for hf in $HOTFILES; do
  n="$(git diff "$BASE" "$REF" -- "$hf" 2>/dev/null | grep -E '^\+' | grep -vE '^\+\+\+' \
        | grep -cE '^\+\s*(public|protected|private)??\s*static\s|HbmRegistry\.(block|blockWithItem|item|tile|entity|sound)\(' || true)"
  echo -n "$(basename "$hf" .java)=$n  "
done
echo "(>0 means new fields were stuffed into hot files ⇒ switch to your own Mod<family> registrar)"

# ---------- [5][6] runtime side (reads the last runClient log) ----------
LOG="run/logs/latest.log"
if [ -f "$LOG" ]; then
  LMT="$(date -r "$LOG" '+%m-%d %H:%M' 2>/dev/null || echo '?')"
  REG_LINE="$(grep -a 'registered blocks' "$LOG" | tail -1 | sed 's/.*\[hbm\] //;s/^/ /')"
  echo "[5] registration face (from $LMT's $LOG):${REG_LINE:- none}"
  if grep -aq 'dev-title' "$LOG"; then
    echo "[6] dev-title: present -> $(grep -a 'dev-title' "$LOG" | tail -1 | sed 's/.*dev-title/dev-title/')"
  else
    echo "[6] dev-title: absent (this log is not G1 evidence)"
  fi
else
  echo "[5] registration face: no run/logs/latest.log"
  echo "[6] dev-title: no log"
fi

# ---------- [7] crash-reports ----------
if [ -d run/crash-reports ]; then
  echo "[7] latest 3 crash-reports: $(ls -t run/crash-reports 2>/dev/null | head -3 | tr '\n' ' ')"
else
  echo "[7] crash-reports: directory absent (clean)"
fi

# ---------- optional L2: actually run runClient ----------
if [ "$WITH_G1" = 1 ]; then
  echo
  echo "---- running the L2 gate (runClient, up to 300s) ----"
  timeout 300 bash "$BUILD" "$REPO" runClient -PdevTitle="[A1-GATE] L2 smoke" >/tmp/a1_gate_run.log 2>&1 || true
  grep -a 'dev-title\|registered blocks\|registered items\|registered entities' "$LOG" 2>/dev/null | tail -4 | sed 's/^/  /'
  echo "  latest crash-reports: $(ls -t run/crash-reports 2>/dev/null | head -2 | tr '\n' ' ')"
fi
echo "================ /A1-GATE ================"
