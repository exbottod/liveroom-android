"""Publish the small update files for VenderLive.

Runs after the release APK is built: writes docs/unduh/build-info.json (the
current version) and a bsdiff difference between the previous release APK and
this one, so the app can offer a small download instead of the full APK.
"""
import hashlib
import json
import os
import re
import subprocess
import sys
import urllib.request

REPO = "exbottod/liveroom-android"
NEW_APK = "app/build/outputs/apk/release/app-universal-release.apk"
INFO_PATH = "docs/unduh/build-info.json"
PATCH_INFO_PATH = "docs/unduh/patch-info.json"
PATCH_DIR = "docs/unduh/patch"


def current_version():
    text = open("app/build.gradle.kts").read()
    match = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    return match.group(1) if match else ""


def previous_version():
    try:
        return json.load(open(INFO_PATH)).get("versi") or ""
    except Exception:
        return ""


def main():
    version = current_version()
    previous = previous_version()
    os.makedirs(PATCH_DIR, exist_ok=True)

    entry = None
    if previous and previous != version and os.path.exists(NEW_APK):
        old_apk = "prev-release.apk"
        url = f"https://github.com/{REPO}/releases/download/v{previous}/venderlive.apk"
        try:
            urllib.request.urlretrieve(url, old_apk)
            import bsdiff4  # noqa: F401  (installed by the workflow)
            import bsdiff4 as bd
            patch = bd.diff(open(old_apk, "rb").read(), open(NEW_APK, "rb").read())
            name = f"patch-{previous}-{version}.bsdiff"
            open(os.path.join(PATCH_DIR, name), "wb").write(patch)
            entry = {
                "dari": previous,
                "ke": version,
                "file": name,
                "ukuran": len(patch),
                "sha256": hashlib.sha256(patch).hexdigest(),
            }
            print("patch", name, len(patch), "bytes")
        except Exception as error:
            print("patch gagal:", error)

    json.dump({"patch": [entry] if entry else []}, open(PATCH_INFO_PATH, "w"), indent=2)
    note = os.environ.get("CI_NOTE", "").strip()
    json.dump({"versi": version, "catatan": note}, open(INFO_PATH, "w"), indent=2)
    print("build-info", version)


if __name__ == "__main__":
    main()
