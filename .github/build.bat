@echo off

rem Builds libclingo as a shared library. Run this from the root of a clingo checkout. Any additional argument is
rem forwarded to the cmake configure step. The generator is deliberately left unset so that cmake picks the newest
rem Visual Studio installed on the runner.

cmake -S . -B build %* ^
  -DCLINGO_BUILD_WITH_PYTHON=OFF ^
  -DCLINGO_BUILD_WITH_LUA=OFF ^
  -DCLINGO_BUILD_APPS=OFF ^
  -DCLINGO_BUILD_EXAMPLES=OFF ^
  -DCLINGO_BUILD_TESTS=OFF ^
  -DCLINGO_MANAGE_RPATH=OFF ^
  -DCLINGO_BUILD_SHARED=ON ^
  -DCMAKE_BUILD_TYPE=Release

if errorlevel 1 exit /b %ERRORLEVEL%

cmake --build build ^
  --config Release ^
  --target libclingo ^
  -j %NUMBER_OF_PROCESSORS%

if errorlevel 1 exit /b %ERRORLEVEL%
