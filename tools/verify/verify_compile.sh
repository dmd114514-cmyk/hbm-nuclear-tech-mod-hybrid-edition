#!/usr/bin/env bash
# Side-channel compile check: without waiting for the skeleton, verify your own code against a real 1.12.2 jar plus throwaway stubs.
# Usage: tools/verify/verify_compile.sh <java file or dir>...
# Requires JAVA_HOME pointing at JDK 8 and the 1.12.2 mapped jar already in the gradle cache from a build.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
: "${JAVA_HOME:=/usr/lib/jvm/zulu-8-amd64}"

JAR_SRC="$HOME/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.12.2-14.23.5.2864_mapped_snapshot_20171003-1.12/forge-1.12.2-14.23.5.2864_mapped_snapshot_20171003-1.12.jar"
# Private copy: ForgeGradle deletes then rewrites that jar, and anyone else running gradle at the same time makes this check fail at random.
# So it copies the jar to /tmp and uses the copy, fully decoupled from concurrent builds.
JAR="/tmp/a3-jar/forge.jar"
mkdir -p "$(dirname "$JAR")"
if [ ! -s "$JAR" ] || [ "$JAR_SRC" -nt "$JAR" ]; then
  if [ -f "$JAR_SRC" ]; then
    cp "$JAR_SRC" "$JAR"
  elif [ ! -s "$JAR" ]; then
    echo "Mapped jar missing and no copy to reuse ($JAR_SRC). Wait for the other gradle run to finish and retry." >&2
    exit 2
  fi
fi

GSON="$HOME/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.2.4/a60a5e993c98c864010053cb901b7eab25306568/gson-2.2.4.jar"
GUAVA="$HOME/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/26.0-jre/6a806eff209f36f635f943e16d97491f00f6bfab/guava-26.0-jre.jar"
# LWJGL2: needed by client code like Keyboard/GL11, same version as 1.12.2
LWJGL="$(find "$HOME/.gradle/caches" -name 'lwjgl-2.9.4-nightly-20150209.jar' 2>/dev/null | head -1)"
LWJGL_UTIL="$(find "$HOME/.gradle/caches" -name 'lwjgl_util-2.9.4-nightly-20150209.jar' 2>/dev/null | head -1)"
# Netty: IBufPacketReceiver's ByteBuf needs it; 1.12.2 ships 4.1.9
NETTY="$(find "$HOME/.gradle/caches" -name 'netty-all-4.1.9.Final.jar' 2>/dev/null | head -1)"
# Apache Commons: a few utility classes use it
COMMONS_LANG="$(find "$HOME/.gradle/caches" -name 'commons-lang3-*.jar' 2>/dev/null | grep -v sources | head -1)"
# log4j: MainRegistry's logging needs it; 1.12.2 ships 2.8.1
LOG4J="$(find "$HOME/.gradle/caches" -name 'log4j-api-2.8.1.jar' 2>/dev/null | head -1)"

for f in "$JAR" "$GSON" "$GUAVA"; do
  [ -f "$f" ] || { echo "missing dependency: $f" >&2; exit 2; }
done

CP="$JAR:$GSON:$GUAVA"
[ -n "$LWJGL" ] && CP="$CP:$LWJGL"
[ -n "$LWJGL_UTIL" ] && CP="$CP:$LWJGL_UTIL"
[ -n "$NETTY" ] && CP="$CP:$NETTY"
[ -n "$COMMONS_LANG" ] && CP="$CP:$COMMONS_LANG"
[ -n "$LOG4J" ] && CP="$CP:$LOG4J"

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

# Collect the files to compile: recurse for a directory, use the file directly otherwise
SRCS=()
for arg in "$@"; do
  if [ -d "$arg" ]; then
    while IFS= read -r f; do SRCS+=("$f"); done < <(find "$arg" -name '*.java')
  else
    SRCS+=("$arg")
  fi
done

"$JAVA_HOME/bin/javac" -encoding UTF-8 -nowarn \
  -cp "$CP" -d "$OUT" \
  $(find "$HERE/stubs" -name '*.java') "${SRCS[@]}"

echo "PASS: compiled, $(find "$OUT" -name '*.class' | wc -l) classes"
