#!/usr/bin/env python3
"""Copy bundled stroke-order SVG assets for the configured kanji list."""

from __future__ import annotations

import argparse
import json
import shutil
from pathlib import Path


def load_characters(path: Path) -> list[str]:
    with path.open(encoding="utf-8") as handle:
        data = json.load(handle)

    if not isinstance(data, list) or not all(isinstance(item, str) for item in data):
        raise ValueError(f"Expected a JSON string array in {path}")

    return data


def codepoint_hex(character: str) -> str:
    return format(ord(character), "x").zfill(5)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--kanjivg-dir",
        type=Path,
        default=Path("/tmp/kanjivg/kanji"),
        help="Path to the KanjiVG kanji/ directory",
    )
    parser.add_argument(
        "--list-file",
        type=Path,
        default=Path("scripts/joyo_kanji.json"),
        help="JSON array of characters to bundle",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("app/src/main/assets/strokes"),
        help="Destination directory for copied SVG assets",
    )
    args = parser.parse_args()

    args.output_dir.mkdir(parents=True, exist_ok=True)
    characters = load_characters(args.list_file)
    expected_filenames = {f"{codepoint_hex(character)}.svg" for character in characters}

    removed = 0
    for existing_file in args.output_dir.glob("*.svg"):
        if existing_file.name not in expected_filenames:
            existing_file.unlink()
            removed += 1

    copied = 0
    missing: list[str] = []

    for character in characters:
        source = args.kanjivg_dir / f"{codepoint_hex(character)}.svg"
        destination = args.output_dir / source.name
        if not source.exists():
            missing.append(character)
            continue
        shutil.copy2(source, destination)
        copied += 1

    print(f"Copied {copied} stroke SVG files to {args.output_dir}")
    if removed:
        print(f"Removed {removed} stale stroke SVG files from {args.output_dir}")
    if missing:
        preview = "".join(missing[:20])
        print(f"Missing {len(missing)} characters: {preview}")


if __name__ == "__main__":
    main()
