# Reverse engineering report — original MyJPJ 5.1.2

**Analysis date:** 2026-10-29  
**Source:** user-provided `MyJPJ_5.1.2_APKPure(1).xapk`  
**XAPK SHA-256:** `aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a`  
**Verification:** exactly matches the previously analyzed MyJPJ v5.1.2 XAPK.

## Actual decoded contents

| File/layer | Findings |
|---|---|
| Base APK / DEX | 10,531 defined classes; 58,754 method references; 51,848 method declarations |
| Compiled AndroidManifest.xml | Successfully decoded; 15 activities, including `com.jpj.jpj_info.MainActivity` |
| ARM64 libapp.so | 12,125,104 bytes; contains compiled Flutter/Dart AOT application |
| Original Dart path strings | 290 unique source paths recovered from AOT snapshot |
| Flutter assets | 423 original packaged files |
| Native ARM64 libraries | 12 original .so libraries |

The Android entry point `com.jpj.jpj_info.MainActivity` extends `io.flutter.embedding.android.FlutterFragmentActivity` and implements `sharepay.paylibrary.SarawakPayCallback`. Its compiled Android DEX bytecode contains Flutter method-channel references to `my_mimos.SignetClientHelper` and `com_sp.SarawakAPI`.

The user-specific decompile ZIP includes **decoded Android Manifest**, **class and method/field indices**, **method bytecode offsets**, **334 native Android/Flutter plugin Java signature listings**, **MainActivity actual Dalvik opcode listing**, and **all 290 Dart source file path hints**. It is available privately in this chat; binary/assets and third-party disassembly are deliberately not committed to this *public* GitHub repo.

## Recovered Dart module paths (counts)

| Module | Referenced source paths |
|---|---:|
| sub_menu | 95 |
| core | 63 |
| profile | 41 |
| supportService | 18 |
| login | 10 |
| main | 10 |
| register | 10 |
| settings | 9 |
| inbox | 6 |
| reset_password | 6 |
| directory | 5 |
| first_time_login | 4 |
| keycloak | 4 |
| convert_id | 3 |
| reset_email | 3 |
| splash | 2 |
| landing | 1 |

Examples of exact source-path breadcrumbs identified (path **does not** mean source code was recovered):

- `package:myjpj/main.dart`
- `package:myjpj/core/repository/app_client.dart`
- `package:myjpj/core/routes/app_routes.dart`
- `package:myjpj/core/services/keycloak/keycloak_auth_service.dart`
- `package:myjpj/screens/main/main_screen.dart`
- `package:myjpj/screens/login/login_screen.dart`
- `package:myjpj/screens/profile/profile_screen.dart`
- `package:myjpj/screens/sub_menu/enforcement/summons/summons.dart`
- `package:myjpj/screens/sub_menu/payment/payment.dart`
- `package:myjpj/screens/sub_menu/vehicle_licening/motor_vehicle_license_lkm_renewal/lkm_renewal_screen.dart`

## Reproduction using more specialized tools

1. Start from the original XAPK. Use `scripts/extract_original.py` from this repository to copy original signed APK splits unchanged.
2. Run [JADX](https://github.com/skylot/jadx) on the original **base APK** for full Android DEX → approximate Java decompilation:  
   `jadx --output-dir local-jadx workbench/apks/com.jpj.jpj_info.apk`
3. Read `workbench/native/lib/arm64-v8a/libapp.so` with compatible Flutter/Dart AOT reverse-engineering tooling such as [Blutter](https://github.com/worawit/blutter) or Ghidra, noting Dart version compatibility.
4. Validate inferred behavior against original app in authorized tests; never infer genuine personal licence, transaction or payment status from cached strings and resource names.

## Boundary

The original **Dart** source was not packaged in this release. A complete project with exact editable Dart code, original JPJ server/API source, or original signing keys cannot be generated from this XAPK. The Flutter web/PWA work in `web/` is an **independent prototype** and is not the decompiled Android application. Avoid presenting it as identical to MyJPJ.
