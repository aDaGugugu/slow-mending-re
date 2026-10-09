#!/usr/bin/env python3
"""Run the same plugin and API test JARs on pinned, disposable Paper servers."""

import argparse
import concurrent.futures
import hashlib
import json
import shutil
import subprocess
import sys
import urllib.request
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path


def download(url, destination, sha256):
    if not destination.is_file():
        request = urllib.request.Request(url, headers={
            "User-Agent": "SlowMendingCompatibility/2.2.3 (https://github.com/Junnstoy/slow-mending-re)"
        })
        with urllib.request.urlopen(request, timeout=60) as response, destination.open("wb") as output:
            shutil.copyfileobj(response, output)
    with destination.open("rb") as source:
        actual = hashlib.sha256(source.read()).hexdigest()
    if actual != sha256:
        raise ValueError(f"Checksum mismatch: {destination}")


def run_case(row, options, plugin, tests):
    version = row["version"]
    server = options.work_dir / version
    # Never reuse or delete a server's world/configuration during a test run.
    server.mkdir(parents=True, exist_ok=False)
    (server / "plugins").mkdir()
    (server / "cache").mkdir()
    jar = options.cache_dir / row["filename"]
    download(row["url"], jar, row["sha256"])
    with zipfile.ZipFile(jar) as archive:
        checksum, url, name = archive.read("META-INF/download-context").decode().strip().split("\t")
    vanilla = options.cache_dir / name
    download(url, vanilla, checksum)
    shutil.copy2(vanilla, server / "cache" / name)
    shutil.copy2(plugin, server / "plugins" / plugin.name)
    shutil.copy2(tests, server / "plugins" / tests.name)
    (server / "eula.txt").write_text("eula=true\n")
    flat = json.dumps({"biome": "minecraft:plains", "layers": [
        {"block": "minecraft:bedrock", "height": 1},
        {"block": "minecraft:grass_block", "height": 1}
    ]})
    (server / "server.properties").write_text(
        "server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nenforce-secure-profile=false\n"
        "view-distance=2\nsimulation-distance=2\nmax-players=1\nspawn-protection=0\n"
        "generate-structures=false\nlevel-type=minecraft:flat\nlevel-name=test-world\n"
        f"generator-settings={flat}\nmax-tick-time=120000\n"
    )
    java = getattr(options, f"java{row['java']}")
    command = [java, "-Xms256M", "-Xmx1024M", "-XX:ActiveProcessorCount=2",
               "-Dterminal.jline=false", "-Dterminal.ansi=false", "-jar", str(jar), "--nogui"]
    with (server / "console.log").open("w") as log:
        result = subprocess.run(command, cwd=server, stdout=log, stderr=subprocess.STDOUT,
                                stdin=subprocess.DEVNULL, timeout=options.timeout)
    report = server / "slow-mending-test-result.txt"
    message = report.read_text().strip() if report.exists() else "FAIL: no test result"
    return {"version": version, "build": row["build"], "java": row["java"],
            "exit_code": result.returncode, "result": message,
            "passed": result.returncode == 0 and message.startswith("PASS ")}


def main():
    root = Path(__file__).resolve().parent.parent
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java17", default="java")
    parser.add_argument("--java21", default="java")
    parser.add_argument("--java25", default="java")
    parser.add_argument("--work-dir", type=Path, required=True, help="New disposable directory")
    parser.add_argument("--cache-dir", type=Path, required=True)
    parser.add_argument("--versions", nargs="+")
    parser.add_argument("--workers", type=int, default=2)
    parser.add_argument("--timeout", type=int, default=180)
    parser.add_argument("--accept-eula", action="store_true",
                        help="Accept https://aka.ms/MinecraftEULA for these test servers")
    options = parser.parse_args()
    if not options.accept_eula:
        parser.error("Read the Minecraft EULA and pass --accept-eula to start test servers.")
    if not 1 <= options.workers <= 4:
        parser.error("--workers must be between 1 and 4")
    options.work_dir = options.work_dir.resolve()
    options.cache_dir = options.cache_dir.resolve()
    options.work_dir.mkdir(parents=True, exist_ok=True)
    options.cache_dir.mkdir(parents=True, exist_ok=True)
    release = ET.parse(root / "pom.xml").findtext("{http://maven.apache.org/POM/4.0.0}version")
    plugin = root / "target" / f"slow-mending-re-{release}.jar"
    tests = root / "integration-tests" / "target" / f"slow-mending-server-tests-{release}.jar"
    if not plugin.is_file() or not tests.is_file():
        parser.error("Build the main plugin and integration-tests plugin first.")
    matrix = json.loads((root / "integration-tests" / "matrix.json").read_text())
    if options.versions:
        known = {row["version"] for row in matrix}
        if set(options.versions) - known:
            parser.error("Unknown version(s): " + ", ".join(sorted(set(options.versions) - known)))
        matrix = [row for row in matrix if row["version"] in options.versions]
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=options.workers) as pool:
        futures = {pool.submit(run_case, row, options, plugin, tests): row for row in matrix}
        for future in concurrent.futures.as_completed(futures):
            row = futures[future]
            try:
                result = future.result()
            except Exception as error:
                result = {"version": row["version"], "build": row["build"], "java": row["java"],
                          "passed": False, "result": str(error)}
            results.append(result)
            print(json.dumps(result), flush=True)
    order = {row["version"]: i for i, row in enumerate(matrix)}
    results.sort(key=lambda result: order[result["version"]])
    report = {"plugin": plugin.name, "sha256": hashlib.sha256(plugin.read_bytes()).hexdigest(),
              "results": results}
    (options.work_dir / "results.json").write_text(json.dumps(report, indent=2) + "\n")
    return 0 if all(result["passed"] for result in results) else 1


if __name__ == "__main__":
    sys.exit(main())
