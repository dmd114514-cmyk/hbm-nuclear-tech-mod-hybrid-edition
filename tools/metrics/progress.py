#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HBM port project progress metrics.
- collects: code size / coverage (same name and path) / package distribution / registration counts from the last runClient
- history: appended to docs/metrics/history.jsonl (deduplicated per minute)
- chart: redraws docs/progress.svg from the history
No third-party dependencies, python3 stdlib only.

Usage:
  python3 tools/metrics/progress.py                 # collect once and redraw
  python3 tools/metrics/progress.py --snapshot-only # collect only, no chart
"""
import argparse
import datetime as dt
import json
import os
import re
import subprocess
import sys
from collections import OrderedDict

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))          # Forge1122MDK
SRC = os.path.join(REPO, "src", "main", "java")
DOCS = os.path.join(REPO, "docs")
METRICS = os.path.join(DOCS, "metrics")
HISTORY = os.path.join(METRICS, "history.jsonl")
OUT_SVG = os.path.join(DOCS, "progress.svg")
BASELINE = os.environ.get(
    "HBM_SRC_HANG",
    os.path.abspath(os.path.join(REPO, "..", "mod source", "HBM Space 1.7.10 latest source", "src", "main", "java")),
)
ORIGINAL = os.environ.get(
    "HBM_SRC_VANILLA",
    os.path.abspath(os.path.join(REPO, "..", "mod source", "HBM 1.7.10 vanilla latest source", "src", "main", "java")),
)


def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout


def java_files(root):
    out = sh(f"find '{root}' -name '*.java' 2>/dev/null")
    return [l for l in out.splitlines() if l.strip()]


def rel_set(root):
    """Relative path set (for same-name same-path coverage)"""
    files = java_files(root)
    return {os.path.relpath(f, root) for f in files}


def count_by_pkg(base):
    """{subpackage: file count}"""
    d = {}
    for name in sorted(os.listdir(base)):
        p = os.path.join(base, name)
        if os.path.isdir(p):
            d[name] = len(java_files(p))
    return d


def latest_registrations():
    """Pulls registration counts from the latest runClient log (registered blocks: N ...)"""
    logs_dir = os.path.join(REPO, "run", "logs")
    if not os.path.isdir(logs_dir):
        return {}
    logs = sorted(
        (os.path.join(logs_dir, f) for f in os.listdir(logs_dir) if f.endswith(".log")),
        key=lambda p: os.path.getmtime(p), reverse=True)
    if not logs:
        return {}
    text = ""
    try:
        with open(logs[0], "r", encoding="utf-8", errors="ignore") as fh:
            text = fh.read()
    except OSError:
        return {}
    out = {}
    for cn, key in (("blocks", "blocks"), ("items", "items"), ("entities", "entities"),
                    ("potions", "potions"), ("sounds", "sounds")):
        m = re.findall(cn + r" (\d+)", text)
        if m:
            out[key] = int(m[-1])
    m = re.findall(r"dev-title:\s*(.+)", text)
    if m:
        out["dev_title"] = m[-1].strip()
    return out


def snapshot():
    comhbm = os.path.join(SRC, "com", "hbm")
    api = os.path.join(SRC, "api")
    cov_b = len(rel_set(SRC) & rel_set(BASELINE)) if os.path.isdir(BASELINE) else 0
    cov_o = len(rel_set(SRC) & rel_set(ORIGINAL)) if os.path.isdir(ORIGINAL) else 0
    base_total = len(rel_set(BASELINE)) if os.path.isdir(BASELINE) else 0
    orig_total = len(rel_set(ORIGINAL)) if os.path.isdir(ORIGINAL) else 0
    snap = OrderedDict()
    snap["ts"] = dt.datetime.now().strftime("%Y-%m-%d %H:%M")
    snap["java_total"] = len(java_files(SRC))
    snap["java_comhbm"] = len(java_files(comhbm))
    snap["java_api"] = len(java_files(api))
    snap["cov_baseline"] = cov_b          # same name and path as the space-tree baseline
    snap["baseline_total"] = base_total
    snap["cov_original"] = cov_o          # same name and path as the upstream baseline
    snap["original_total"] = orig_total
    snap["pkgs"] = count_by_pkg(comhbm)
    snap["reg"] = latest_registrations()
    return snap


def append_history(snap, force=False):
    os.makedirs(METRICS, exist_ok=True)
    rows = []
    if os.path.exists(HISTORY):
        with open(HISTORY, "r", encoding="utf-8") as fh:
            rows = [json.loads(l) for l in fh if l.strip()]
    key = snap["ts"][:16]  # down to the minute
    if rows and rows[-1]["ts"][:16] == key and not force:
        rows[-1] = snap
    else:
        rows.append(snap)
    # Sort, stable by time
    rows.sort(key=lambda r: r["ts"])
    with open(HISTORY, "w", encoding="utf-8") as fh:
        for r in rows:
            fh.write(json.dumps(r, ensure_ascii=False) + "\n")
    return rows


# ---------------- drawing ----------------
def fmt(n):
    return f"{n:,}"


def line_chart(rows, key, title, ymax, color, unit="", target=None, w=560, h=210):
    """Returns an SVG snippet (line chart)"""
    x0, y0, pw, ph = 60, 46, w - 90, h - 76
    pts = [(r["ts"], r.get(key)) for r in rows if isinstance(r.get(key), int)]
    if not pts:
        return f'<text x="{x0}" y="{y0}" font-size="12" fill="#888">no data</text>'
    vmax = max(ymax, max(v for _, v in pts))
    vmax = vmax or 1

    def xy(i, v):
        n = max(len(pts) - 1, 1)
        x = x0 + (pw * i / n)
        y = y0 + ph - (ph * (v / vmax))
        return x, y

    s = [f'<text x="{x0-52}" y="{y0-14}" font-size="13" font-weight="bold" fill="#12202e">{title}</text>']
    # axes
    s.append(f'<line x1="{x0}" y1="{y0+ph}" x2="{x0+pw}" y2="{y0+ph}" stroke="#c9d2dd"/>')
    s.append(f'<line x1="{x0}" y1="{y0}" x2="{x0}" y2="{y0+ph}" stroke="#c9d2dd"/>')
    s.append(f'<text x="{x0-8}" y="{y0+8}" font-size="10" fill="#8a97a6" text-anchor="end">{fmt(vmax)}</text>')
    s.append(f'<text x="{x0-8}" y="{y0+ph}" font-size="10" fill="#8a97a6" text-anchor="end">0</text>')
    if target:
        ty = y0 + ph - (ph * (target / vmax))
        s.append(f'<line x1="{x0}" y1="{ty:.1f}" x2="{x0+pw}" y2="{ty:.1f}" stroke="#c0392b" stroke-dasharray="4 3" opacity="0.6"/>')
        s.append(f'<text x="{x0+pw-4}" y="{ty-4:.1f}" font-size="10" fill="#c0392b" text-anchor="end">target {fmt(target)}</text>')
    d = " ".join(("M" if i == 0 else "L") + f"{x:.1f},{y:.1f}" for i, (_, v) in enumerate(pts) for x, y in [xy(i, v)])
    s.append(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="2.4"/>')
    for i, (_, v) in enumerate(pts):
        x, y = xy(i, v)
        s.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="3" fill="{color}"/>')
    lx, ly = xy(len(pts) - 1, pts[-1][1])
    s.append(f'<text x="{lx-4:.1f}" y="{ly-8:.1f}" font-size="11" font-weight="bold" fill="{color}" text-anchor="end">{fmt(pts[-1][1])}{unit}</text>')
    # first and last timestamp on the x axis
    s.append(f'<text x="{x0}" y="{y0+ph+14}" font-size="10" fill="#8a97a6">{pts[0][0][5:]}</text>')
    s.append(f'<text x="{x0+pw}" y="{y0+ph+14}" font-size="10" fill="#8a97a6" text-anchor="end">{pts[-1][0][5:]}</text>')
    # start and end values
    s.append(f'<text x="{x0+4}" y="{y0+12}" font-size="10" fill="#8a97a6">start {fmt(pts[0][1])}{unit}</text>')
    return "\n".join(s)


def group_chart(rows, keys, title, colors, w=560, h=210):
    x0, y0, pw, ph = 60, 46, w - 90, h - 76
    series = OrderedDict()
    for k in keys:
        pts = [(r["ts"], (r.get("reg") or {}).get(k)) for r in rows
               if isinstance((r.get("reg") or {}).get(k), int)]
        if pts:
            series[k] = pts
    s = [f'<text x="{x0-52}" y="{y0-14}" font-size="13" font-weight="bold" fill="#12202e">{title}</text>']
    s.append(f'<line x1="{x0}" y1="{y0+ph}" x2="{x0+pw}" y2="{y0+ph}" stroke="#c9d2dd"/>')
    s.append(f'<line x1="{x0}" y1="{y0}" x2="{x0}" y2="{y0+ph}" stroke="#c9d2dd"/>')
    if not series:
        s.append(f'<text x="{x0}" y="{y0+20}" font-size="12" fill="#888">no data</text>')
        return "\n".join(s)
    vmax = max(v for pts in series.values() for _, v in pts) or 1
    vmax = int(vmax * 1.15) + 1
    n = max(len(next(iter(series.values()))) - 1, 1)
    s.append(f'<text x="{x0-8}" y="{y0+8}" font-size="10" fill="#8a97a6" text-anchor="end">{fmt(vmax)}</text>')
    s.append(f'<text x="{x0-8}" y="{y0+ph}" font-size="10" fill="#8a97a6" text-anchor="end">0</text>')
    lx = x0 + pw - sum(20 + 9 * len(k) + 14 for k in series)
    for i, (k, pts) in enumerate(series.items()):
        c = colors[i % len(colors)]
        def xy(j, v):
            return x0 + (pw * j / n), y0 + ph - (ph * (v / vmax))
        d = " ".join(("M" if j == 0 else "L") + f"{x:.1f},{y:.1f}" for j, (_, v) in enumerate(pts) for x, y in [xy(j, v)])
        s.append(f'<path d="{d}" fill="none" stroke="{c}" stroke-width="2.2"/>')
        for j, (_, v) in enumerate(pts):
            x, y = xy(j, v)
            s.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="2.6" fill="{c}"/>')
        ex, ey = xy(len(pts) - 1, pts[-1][1])
        s.append(f'<text x="{ex-3:.1f}" y="{ey-7:.1f}" font-size="11" font-weight="bold" fill="{c}" text-anchor="end">{fmt(pts[-1][1])}</text>')
        s.append(f'<rect x="{lx}" y="{y0-14}" width="10" height="10" rx="2" fill="{c}"/>')
        s.append(f'<text x="{lx+14}" y="{y0-5}" font-size="11" fill="#333">{k}</text>')
        lx += 20 + 8 * len(k) + 16
    first = next(iter(series.values()))[0][0][5:]
    last = next(iter(series.values()))[-1][0][5:]
    s.append(f'<text x="{x0}" y="{y0+ph+14}" font-size="10" fill="#8a97a6">{first}</text>')
    s.append(f'<text x="{x0+pw}" y="{y0+ph+14}" font-size="10" fill="#8a97a6" text-anchor="end">{last}</text>')
    return "\n".join(s)


def render_svg(rows, cur):
    W, H = 1220, 780
    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" font-family="Noto Sans SC, Noto Sans CJK SC, sans-serif">',
             f'<rect width="{W}" height="{H}" fill="#ffffff"/>']
    parts.append('<text x="36" y="40" font-size="24" font-weight="bold" fill="#12202e">HBM Hybrid Edition - project progress</text>')
    parts.append(f'<text x="36" y="66" font-size="13" fill="#6b7a8c">snapshot {cur["ts"]} · {len(rows)} data points · source: measured on main + runClient logs</text>')

    # summary card
    reg = cur.get("reg") or {}
    cards = [("java classes", fmt(cur["java_total"])),
             ("Name coverage (baseline)※", f'{cur["cov_baseline"]}/{cur["baseline_total"]}'),
             ("registered blocks", fmt(reg.get("blocks", 0))),
             ("registered items", fmt(reg.get("items", 0))),
             ("registered entities", fmt(reg.get("entities", 0)))]
    cx = 36
    for name, val in cards:
        parts.append(f'<rect x="{cx}" y="84" width="200" height="64" rx="10" fill="#f6f8fb" stroke="#d5dde7"/>')
        parts.append(f'<text x="{cx+14}" y="108" font-size="12" fill="#6b7a8c">{name}</text>')
        parts.append(f'<text x="{cx+14}" y="134" font-size="21" font-weight="bold" fill="#12202e">{val}</text>')
        cx += 212

    parts.append('<g transform="translate(30,170)">' + line_chart(rows, "java_total", "Code size (total .java)", 0, "#1f6396", target=cur.get("baseline_total")) + '</g>')
    parts.append('<g transform="translate(620,170)">' + group_chart(rows, ["blocks", "items", "entities"], "Registered counts (measured via runClient)", ["#e2a13a", "#357a45", "#6b3fa0"]) + '</g>')

    # package distribution (horizontal bars)
    pkgs = sorted((cur.get("pkgs") or {}).items(), key=lambda kv: -kv[1])[:12]
    bx, by = 36, 420
    parts.append(f'<text x="{bx}" y="{by-8}" font-size="13" font-weight="bold" fill="#12202e">package distribution (com.hbm.*, top 12)</text>')
    maxv = max([v for _, v in pkgs] or [1])
    for i, (name, v) in enumerate(pkgs):
        y = by + 10 + i * 20
        parts.append(f'<text x="{bx+112}" y="{y+11}" font-size="11.5" fill="#334" text-anchor="end">{name}</text>')
        w = int(300 * v / maxv)
        parts.append(f'<rect x="{bx+120}" y="{y}" width="{w}" height="13" rx="3" fill="#79c3cf"/>')
        parts.append(f'<text x="{bx+126+w}" y="{y+11}" font-size="11" fill="#4f7f88">{v}</text>')

    # coverage bar
    parts.append(f'<text x="640" y="412" font-size="13" font-weight="bold" fill="#12202e">name coverage (same name and path)</text>')
    for i, (label, got, tot, col) in enumerate([
            ("vs space baseline", cur["cov_baseline"], cur["baseline_total"], "#357a45"),
            ("vs vanilla", cur["cov_original"], cur["original_total"], "#1f6396")]):
        y = 432 + i * 56
        parts.append(f'<text x="640" y="{y+13}" font-size="12" fill="#334">{label}</text>')
        bwd = 330
        parts.append(f'<rect x="750" y="{y}" width="{bwd}" height="18" rx="4" fill="#eef1f6"/>')
        if tot:
            parts.append(f'<rect x="750" y="{y}" width="{int(bwd*got/tot)}" height="18" rx="4" fill="{col}"/>')
            parts.append(f'<text x="750" y="{y+34}" font-size="11.5" fill="#6b7a8c">{got} / {tot}　（{100*got/tot:.1f}%）</text>')

    parts.append(f'<text x="36" y="{H-14}" font-size="11" fill="#8a97a6">generated by tools/metrics/progress.py · history in docs/metrics/history.jsonl</text>')
    parts.append('<text x="36" y="768" font-size="11" fill="#8b97a6">note: name coverage matches com/hbm class names AND package paths against the 1.7.10 space tree; renamed/moved classes (~29) and the not-porting surfaces (packet 26, NEI 79) never count, so the ceiling is ~93%. The remainder sits in four big packages: blocks 598 / render 526 / inventory 422 / tileentity 399 (67% of it), which land family by family - hence the staircase instead of a straight line.</text>')
    parts.append('</svg>')
    return "\n".join(parts)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--snapshot-only", action="store_true")
    ap.add_argument("--force", action="store_true", help="append even within the same minute")
    args = ap.parse_args()
    snap = snapshot()
    rows = append_history(snap, force=args.force)
    print(f"[metrics] {snap['ts']} java={snap['java_total']} "
          f"cov(base)={snap['cov_baseline']}/{snap['baseline_total']} reg={snap.get('reg')}")
    if not args.snapshot_only:
        svg = render_svg(rows, snap)
        with open(OUT_SVG, "w", encoding="utf-8") as fh:
            fh.write(svg)
        print(f"[metrics] redrew {OUT_SVG}")


if __name__ == "__main__":
    main()
