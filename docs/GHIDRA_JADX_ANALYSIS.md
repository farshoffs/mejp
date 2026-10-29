# Ghidra + JADX analysis — MyJPJ 5.1.2

Analysis date: 2026-10-29  
Input: user-supplied `MyJPJ_5.1.2_APKPure(1).xapk`  
XAPK SHA-256: `aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a`

This report records reproducible static-analysis results. It does **not** claim recovery of the original Dart source, JPJ backend code, private API contracts, credentials, signing keys, or official data.

## Tool runs

- Ghidra 12.1.2 headless, AARCH64 little-endian, default compiler spec.
- JADX 1.5.6, using JDK 21 against the base APK.
- Proprietary APKs, native libraries, bundled certificates, decoded assets, raw symbol dumps, and JADX output remain local and are excluded from Git.

JADX processed 6,313 classes and completed with 59 decompilation errors. Decompiled Java is therefore approximate and should be checked against bytecode before relying on edge-case control flow.

## `libapp.so`: Flutter/Dart AOT application

| Property | Result |
|---|---|
| Size | 12,125,104 bytes |
| SHA-256 | `1479f6e1532530474068d183b14a821652ec41451eac86bd9a6a927da6f58658` |
| Format | ELF64, ARM64/AArch64, little-endian |
| Ghidra image base | `0x00100000` |
| Ghidra memory blocks | 12 |
| Conventional functions recovered by stock Ghidra | 0 |
| Imported/global/primary symbols in compact report | 16 |
| Defined strings from stock analyzers | 57 |

The absence of conventional functions is expected for this Dart AOT snapshot layout. Stock Ghidra identifies the snapshot containers but does not reconstruct Dart classes and methods by itself.

### Snapshot boundaries

| Symbol | Address |
|---|---:|
| `_kDartSnapshotBuildId` | `0x001001c8` |
| `_kDartVmSnapshotData` | `0x00100340` |
| `_kDartIsolateSnapshotData` | `0x00104b40` |
| `_kDartVmSnapshotInstructions` | `0x00590000` |
| `_kDartIsolateSnapshotInstructions` | `0x005a6a40` |

The executable `.text` snapshot region spans `0x00590000`–`0x00c8e7df` (7,333,856 bytes). The read-only snapshot data region spans `0x00100340`–`0x00582e1f` (4,729,568 bytes).

The snapshot feature string is:

`product no-code_comments no-dwarf_stack_traces_mode dedup_instructions no-asan no-msan no-tsan no-shared_data arm64 android compressed-pointers`

Snapshot format hash: `78da37fed6bf1489361a312568249f3f`. Current community version tables place this hash in the Dart 3.11.x snapshot family (r2flutter currently labels it Dart 3.11.5). The hash identifies snapshot compatibility; it is not a hash of this APK and should not be used alone to assert one exact Flutter patch release.

Stock string analysis also recovered application breadcrumbs such as:

- `package:myjpj/core/services/notifications/onesignal_service.dart`
- `tacCode`
- `PrivacyPolicy`

These are names/paths embedded in compiled data, not recovered source files.

## `libflutter.so`: Flutter engine + Dart runtime

| Property | Result |
|---|---|
| Size | 11,317,712 bytes |
| SHA-256 | `50d733719c7c9eb23c5aa8ea62ef06755e20a8a83e9fcbb0434ebde643f6f0ac` |
| Format | ELF64, ARM64/AArch64, little-endian |
| Ghidra memory blocks | 30 |
| Functions identified | 44,469 |
| Symbols in compact report | 246,138 |
| Defined strings | 16,319 |
| Unresolved Android/native imports | 477 |

The engine has normal native-code structure: `.text`, PLT/GOT, relocation tables, C++ exception metadata, BoringSSL strings, Skia/Impeller paths, JNI/Flutter embedding strings, and Dart runtime APIs. It is useful for engine/runtime understanding but is not the primary container for MyJPJ business logic.

The 477 unresolved imports are expected in an isolated analysis project because Android platform libraries (`libc.so`, `libandroid.so`, `libEGL.so`, `libGLESv2.so`, and others) were not imported.

## Android manifest and platform bridge

- Package: `com.jpj.jpj_info`
- Version: `5.1.2` (`versionCode=1174`)
- Minimum SDK: 24
- Target/compile SDK: 36
- Application class: `com.dynatrace.android.app.Application`
- Main activity: `com.jpj.jpj_info.MainActivity`
- Main activity base: `FlutterFragmentActivity`
- OAuth redirect: `my.gov.jpj.myjpj://login-callback`
- Cleartext traffic: disabled both on the application and in `network_security_config.xml`
- Manifest-level network security config only disables cleartext; it does not declare custom trust anchors or certificate pins.

Declared capabilities include internet, notifications, biometric/fingerprint, location, camera, wake lock, foreground service, and media-audio access. Their presence does not prove every permission is exercised in every flow.

### Custom method channels recovered from `MainActivity`

Channel `my_mimos.SignetClientHelper`:

- `isSignetInstalled`
- `isSignetRegistered` (`appCert`)
- `getTokenOne` (`appCert`)
- `getTokenThree` (`data`, `appCert`)
- native-to-Dart callback: `handleActivityResult`

Channel `com_sp.SarawakAPI`:

- `isSpayInstalled`
- `openSarawakPay`
- `sendPayment` (`data`)
- `spayCreateOrder` (`encData`)
- `spayCreateOrder2` (`orderInfo`)
- native-to-Dart callback: `spayPaymentResult`

The bridge confirms integration boundaries, not the official server-side payment/order protocol. Reimplementations should use mocks or documented partner SDKs only.

### Registered Flutter plugins

The generated registrant includes Android Intent, app settings, connectivity, Dynatrace, Firebase Analytics/Core/Crashlytics/Remote Config, AppAuth, in-app webview, PDF view, root/jailbreak checking, secure storage, geocoding/geolocation, image picker, local auth, maps launcher, mobile scanner, OneSignal, file opening, package info, path provider, permission handler, sharing, shared preferences, URL launcher, video player, and video thumbnail plugins.

## Static review notes

- `SarawakPayResultService` is declared exported. This is not by itself proof of a vulnerability; its intent handling and SDK trust boundary should be reviewed in an authorized assessment.
- The OAuth redirect receiver is exported as required for browser callbacks. Redirect-state, PKCE, issuer, and claimed-HTTPS/app-link validation are runtime concerns not established by the manifest alone.
- Root/jailbreak checking and secure storage plugins are present. Presence does not prove correct configuration or complete enforcement.
- No attempt was made to bypass authentication, certificate validation, integrity controls, or access official JPJ data.

## Blutter suitability

Blutter is appropriate for this ARM64 `libapp.so` and can recover annotated assembly and object-pool information. It still does not recover the original Dart project. The inspected Windows machine did not have the required CMake/Visual C++ toolchain, WSL, Docker, Capstone, and ICU build prerequisites, so Blutter was not executed during this pass. Installing that toolchain would be a separate, heavyweight environment change.

## Clean-room boundary

Safe reconstruction work in this repository is limited to independently written UI, local-only demo state, documented module names, and mock integration interfaces. Do not copy official assets, certificates, disassembly, credentials, private endpoints, or reconstructed payment/authentication logic into the public repository.


