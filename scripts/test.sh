#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/target/self-test-classes"
rm -rf "$OUT"
mkdir -p "$OUT"
find "$ROOT/src/main/java" "$ROOT/src/test/java" -name '*.java' -print0 \
  | xargs -0 javac --release 17 -encoding UTF-8 -d "$OUT"
java -ea -cp "$OUT" ru.urfu.practicalwork2.AllTests
