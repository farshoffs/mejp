# Ghidra + JADX analysis — MyJPJ 5.1.2

Analysis date: 2026-10-29  
Input: user-supplied `MyJPJ_5.1.2_APKPure(1).xapk`  
XAPK SHA-256: `aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a`

This report records reproducible static-analysis results. It does **not** claim recovery of the original Dart source, JPJ backend code, private API contracts, credentials, signing keys, or official data.

## Tool runs

- Ghidra 12.1.2 headless, AARCH64 little-endian, default compiler spec.
- JADX 1.5.6, using JDK 21 against the base APK.
- r2flutter 0.5.1 with radare2 6.2.4.
- dae 0.1.14, app-only export with experimental Dart-like pseudocode.
- AOTopsy 1.6.0, full metadata/call-graph analysis and app-only Dart export.
- PyGhidra 3.1.0 applied the AOTopsy metadata to a separate Ghidra project.
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

## Dart-aware recovery pass

r2flutter matched snapshot hash `78da37fed6bf1489361a312568249f3f` exactly to Dart 3.11.5. Its parsed header reported 1,186 VM objects, 200,318 isolate objects, 938 isolate clusters, and 32,143 isolate instruction-table entries. This exact-hash result supersedes earlier family-level estimates, while still not identifying one exact Flutter framework patch release.

dae parsed the same binary without warnings and recovered the following app-side artifacts:

| Artifact | Count |
|---|---:|
| App-side libraries | 846 |
| App-side classes | 1,444 |
| Named app-side functions | 7,510 |
| Disassembled functions with IL | 6,936 |
| Recovered named fields | 937 |
| Object-pool entries | 42,442 |
| Direct call edges | 64,310 |
| Dart-like pseudocode blocks | 6,936 |
| `package:myjpj` Dart-like output files | 281 |

AOTopsy independently parsed 32,143 code entries and 5,892 class layouts. Its app-only exporter synthesized 11,823 methods across 1,340 classes into 846 Dart-like files. A separate PyGhidra project then created 24,693 functions, added 34,034 comments, and exported 497 selected decompilations; 71 selected functions failed decompilation.

The two tools use different version profiles and reconstruction strategies, so their class/method totals are not directly equivalent. Their outputs agree on the snapshot topology and recover many of the same application library paths. All generated `.dart` files are pseudocode containing machine-level constructs, synthetic locals, reconstructed control flow, and unresolved indirect calls. They are analysis aids, not compilable original source.

Recovered application-library paths describe modules including routing, login and registration, Keycloak/MyDigitalID flows, profile and digital licence views, vehicle licensing, summons and demerit enquiries, payments, inbox/notifications, directory services, e-Aduan, secure storage, Firebase Remote Config, OneSignal, Signet, and SarawakPay. This confirms client-side structure only; it does not recover server implementations or authorize reuse of private service contracts.

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

Blutter is appropriate for this ARM64 `libapp.so` and can recover annotated assembly and object-pool information. It still does not recover the original Dart project. The inspected Windows machine does not yet have the required CMake/Visual C++ toolchain, Capstone, and ICU build prerequisites, so Blutter was not executed. The r2flutter, dae, and AOTopsy passes now provide overlapping snapshot, object-pool, function, class, and pseudocode coverage without that heavyweight installation.

## Clean-room boundary

Safe reconstruction work in this repository is limited to independently written UI, local-only demo state, documented module names, and mock integration interfaces. Do not copy official assets, certificates, disassembly, credentials, private endpoints, or reconstructed payment/authentication logic into the public repository.


