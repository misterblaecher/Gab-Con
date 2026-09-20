#!/usr/bin/env python3
import json
import subprocess
import sys
from pathlib import Path

MANIFEST = Path("tools/processed-upload-commits.json")
SUBJECT_PREFIX = "Add files via upload"

if not MANIFEST.exists():
    print(f"::error::{MANIFEST} is missing")
    sys.exit(1)

data = json.loads(MANIFEST.read_text(encoding="utf-8"))
processed = {entry["sha"] for entry in data.get("processed", [])}

output = subprocess.check_output(
    ["git", "log", "--all", "--format=%H%x09%s"],
    text=True,
    encoding="utf-8",
)
uploads = []
for line in output.splitlines():
    sha, _, subject = line.partition("\t")
    if subject.startswith(SUBJECT_PREFIX):
        uploads.append((sha, subject))

missing = [(sha, subject) for sha, subject in uploads if sha not in processed]
unknown = sorted(processed - {sha for sha, _ in uploads})

print(f"Upload commits found: {len(uploads)}")
print(f"Upload commits marked processed: {len(processed)}")

if unknown:
    for sha in unknown:
        print(f"::warning::Processed SHA not found in current git history: {sha}")

if missing:
    print("Unprocessed 'Add files via upload' commits:")
    for sha, subject in missing:
        print(f"::error::{sha} - {subject}")
    print("Add each SHA to tools/processed-upload-commits.json after its assets are integrated.")
    sys.exit(1)

print("All 'Add files via upload' commits are marked as processed.")
