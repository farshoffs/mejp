# Verified facts — user-supplied MyJPJ v5.1.2 XAPK

Inspected 2026-10-29. Source XAPK hash verified locally.

| Source / element | Bytes | SHA-256 |
|---|---:|---|
| XAPK itself | 110,926,028 | aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a |
| com.jpj.jpj_info.apk | 74,927,192 | 3db0ce3fbf32d2c1a0b495fb94779ca1a8dbe7eb8628018eb072a6ad17018e06 |
| config.arm64_v8a.apk | 35,775,744 | 90a041ba6f69212bbf557d5b839f90f8cec74bdc0eb74d4b85e9fe17033b39db |
| config.en.apk | 49,561 | 94443fc81eae010b9cfb85d34515afec11020b65bc02d693e1f38fc1f9684632 |
| config.mdpi.apk | 54,582 | 1aecda65fe388ec441d4c05952d85ff3244dbd667fae56ddf07b625e78156a8f |
| config.zh.apk | 33,177 | a5330b5438e0b6654c48daae2d7445062419b80ee84aa5d0950c028057fbcecf |
| libapp.so (ARM64) | 12,125,104 | 1479f6e1532530474068d183b14a821652ec41451eac86bd9a6a927da6f58658 |
| libflutter.so (ARM64) | 11,317,712 | 50d733719c7c9eb23c5aa8ea62ef06755e20a8a83e9fcbb0434ebde643f6f0ac |

Manifest metadata: MyJPJ, version 5.1.2, versionCode 1174, package com.jpj.jpj_info, min SDK 24, target SDK 36. Four split APKs accompany the base. Android declares internet, location, biometric, camera and notification permissions, among others.

Base APK contains 829 ZIP entries including classes.dex (9,107,536 bytes) and 423 Flutter assets. Exactly 361 entries under assets/flutter_assets/assets/images are images. ARM64 split contains libapp.so (Dart AOT), libflutter.so and 10 additional native libraries for barcode/PDF/other integration. Bundled certificate files are present but deliberately not published.

## Technical reality

- Exact binaries and all assets can be extracted and SHA-256 validated.
- Android-side code may be analyzed with JADX/apktool.
- The app's compiled Dart binary can be examined with Ghidra, AOTopsy, flutterdec or Blutter, subject to snapshot compatibility.
- Original Dart source and backend software are not embedded in the APK, so exact source recovery or exact source rebuild is impossible from this archive alone.
- The extracted APKs preserve the original app's signing; modifying the APK invalidates that signature.

References: Android ADB install-multiple documentation (https://developer.android.com/tools/adb); flutterdec (https://github.com/caverav/flutterdec), AOTopsy (https://github.com/BroNils/aotopsy) and Blutter (https://github.com/worawit/blutter).
