#!/bin/bash

# Builds libclingo as a shared library. Run this from the root of a clingo checkout. Any additional argument is
# forwarded to the cmake configure step.

set -euo pipefail

if command -v nproc > /dev/null 2>&1; then
  parallel_jobs=$(nproc)
else
  parallel_jobs=$(sysctl -n hw.ncpu)
fi

cmake -S . -B build "$@" \
  -DCLINGO_BUILD_WITH_PYTHON=OFF \
  -DCLINGO_BUILD_WITH_LUA=OFF \
  -DCLINGO_BUILD_APPS=OFF \
  -DCLINGO_BUILD_EXAMPLES=OFF \
  -DCLINGO_BUILD_TESTS=OFF \
  -DCLINGO_MANAGE_RPATH=OFF \
  -DCLINGO_BUILD_SHARED=ON \
  -DCMAKE_BUILD_TYPE=Release

cmake --build build \
  --config Release \
  --target libclingo \
  -j "$parallel_jobs"
