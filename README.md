# MEJP — original MyJPJ 5.1.2 inspection workspace

**This repository uses the actual original MyJPJ Android XAPK. It is not a UI recreation or clone.**

Verified source artifact: MyJPJ_5.1.2_APKPure.xapk, SHA-256 aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a.

## Run original extraction

    python scripts/extract_original.py /path/to/MyJPJ_5.1.2_APKPure.xapk --out ./workbench

Results are byte-identical original signed APK splits under workbench/apks, original bundled Flutter assets in workbench/flutter_assets, original libraries in workbench/native, and checksum inventory in workbench/analysis/inventory.json.

## Install the actual original app

Use Android platform-tools and an ARM64-compatible device. Extract first, then run:

    adb install-multiple workbench/apks/com.jpj.jpj_info.apk workbench/apks/config.arm64_v8a.apk workbench/apks/config.en.apk workbench/apks/config.mdpi.apk workbench/apks/config.zh.apk

This installs original signed binaries. Nothing in this workspace is repackaged or re-signed.

## Can we recover exact source?

**No.** The compiled Flutter/Dart release application lives in libapp.so. Its original Dart source code, comments, build configuration, signing keys, and JPJ backend are not present in the distributed XAPK. Static reverse engineering can recover artifacts, partial structure and pseudocode; any claim to have recovered the exact original Flutter source from this release would be false.

If your objective is to run the exact app, these original APKs are already the exact thing. If your objective is to edit and rebuild it, that necessarily involves reconstruction and cannot guarantee a bit-identical or fully functional rebuild.

## Scope

- Original: MyJPJ, Android package com.jpj.jpj_info
- Release: 5.1.2 (1174)
- Base APK: 74,927,192 bytes, 829 ZIP entries
- ARM64 split: 35,775,744 bytes, 12 native libraries
- Flutter assets: 423 files, including 361 image files
- Native Dart library: lib/arm64-v8a/libapp.so, 12,125,104 bytes

See docs/VERIFIED_FINDINGS.md for file hashes and methodology.

Only scripts and observations belong in this public repository. Do **not** commit third-party app binaries, packaged images, user identities, API tokens, private keys or certificates without proper permission.

Unofficial research workspace; not affiliated with JPJ.
