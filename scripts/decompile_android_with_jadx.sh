#!/usr/bin/env bash
# Run JADX on the exact original base APK after private extraction.
# Requires jadx CLI on PATH. Third-party binary content is kept out of Git.
set -euo pipefail
XAPK="${1:-}"
if [[ -z "$XAPK" ]]; then
 echo "Usage: bash scripts/decompile_android_with_jadx.sh /path/to/MyJPJ_5.1.2_APKPure.xapk" >&2
 exit 2
fi
if ! command -v jadx >/dev/null 2>&1; then
 echo "Install jadx first: https://github.com/skylot/jadx/releases" >&2
 exit 1
fi
python3 scripts/extract_original.py "$XAPK" --out ./workbench
mkdir -p ./workbench/jadx-output
jadx --output-dir ./workbench/jadx-output ./workbench/apks/com.jpj.jpj_info.apk
echo "Android Java decompilation: ./workbench/jadx-output"
echo "Flutter application: ./workbench/native/lib/arm64-v8a/libapp.so (AOT compiled, not Java/Dart source)"
