#!/usr/bin/env python3
"""Extract the exact, signed original APK splits, Flutter assets and native libs."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
from zipfile import ZipFile

KNOWN_SHA256 = "aa92685372b3fac5327eddcd3314f37c884ad794f16f19d6c4e392f71dfe948a"
SPLITS = (
    "com.jpj.jpj_info.apk",
    "config.arm64_v8a.apk",
    "config.en.apk",
    "config.mdpi.apk",
    "config.zh.apk",
)


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def file_hash(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def destination(root: Path, name: str) -> Path:
    p = PurePosixPath(name)
    if p.is_absolute() or not p.parts or ".." in p.parts or ":" in p.parts[0]:
        raise ValueError(f"Unsafe archive path: {name!r}")
    target = root.joinpath(*p.parts)
    target.parent.mkdir(parents=True, exist_ok=True)
    return target


def write_verified(root: Path, name: str, content: bytes) -> None:
    path = destination(root, name)
    path.write_bytes(content)
    if file_hash(path) != sha256(content):
        raise IOError(f"Output failed integrity check: {path}")


def extract_original(xapk: Path, out: Path, allow_other_build: bool) -> dict:
    observed = file_hash(xapk)
    if observed != KNOWN_SHA256 and not allow_other_build:
        raise SystemExit(
            f"Not the verified original XAPK.\nObserved SHA-256: {observed}\n"
            f"Expected SHA-256: {KNOWN_SHA256}\n"
            "Use --allow-other-build to inspect a different archive intentionally."
        )
    report = {
        "xapk_file": xapk.name,
        "xapk_bytes": xapk.stat().st_size,
        "xapk_sha256": observed,
        "verified_original": observed == KNOWN_SHA256,
        "package_entries": [],
        "original_apks": [],
        "flutter_assets": [],
        "native_libraries": [],
    }
    with ZipFile(xapk) as source:
        report["manifest"] = json.loads(source.read("manifest.json"))
        for entry in source.infolist():
            if entry.is_dir():
                continue
            # Check paths even when not extracting them.
            destination(out / "_path_checks", entry.filename)
            data = source.read(entry)
            report["package_entries"].append({
                "path": entry.filename, "bytes": len(data), "sha256": sha256(data)
            })
            if entry.filename not in SPLITS:
                continue
            write_verified(out / "apks", entry.filename, data)
            report["original_apks"].append(entry.filename)
            with ZipFile(io.BytesIO(data)) as apk:
                for inner in apk.infolist():
                    if inner.is_dir():
                        continue
                    name = inner.filename
                    if name.startswith("assets/flutter_assets/"):
                        buf = apk.read(inner)
                        local_name = name[len("assets/flutter_assets/"):]
                        write_verified(out / "flutter_assets", local_name, buf)
                        report["flutter_assets"].append({
                            "apk": entry.filename, "path": local_name,
                            "bytes": len(buf), "sha256": sha256(buf)
                        })
                    elif name.startswith("lib/") and name.endswith(".so"):
                        buf = apk.read(inner)
                        write_verified(out / "native", name, buf)
                        report["native_libraries"].append({
                            "apk": entry.filename, "path": name,
                            "bytes": len(buf), "sha256": sha256(buf)
                        })
    analysis = out / "analysis"
    analysis.mkdir(parents=True, exist_ok=True)
    (analysis / "inventory.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    return report


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("xapk", type=Path, help="Original MyJPJ XAPK")
    parser.add_argument("--out", default=Path("workbench"), type=Path)
    parser.add_argument("--allow-other-build", action="store_true")
    args = parser.parse_args()
    input_file = args.xapk.expanduser().resolve()
    if not input_file.is_file():
        parser.error(f"File not found: {input_file}")
    report = extract_original(input_file, args.out.expanduser().resolve(), args.allow_other_build)
    print(f"Verified exact supplied XAPK: {report['verified_original']}")
    print(f"Original APK splits: {len(report['original_apks'])}")
    print(f"Original Flutter assets: {len(report['flutter_assets'])}")
    print(f"Original native libraries: {len(report['native_libraries'])}")
    print(f"Inventory: {args.out / 'analysis' / 'inventory.json'}")


if __name__ == "__main__":
    main()
