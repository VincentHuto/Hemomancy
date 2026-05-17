@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "REPO_ROOT=%SCRIPT_DIR%..\.."

pushd "%REPO_ROOT%" >nul

if "%~1"=="" (
  echo Drag one or more Model.java files onto this file to export BBModel files.
  echo.
  echo Output:
  echo   Each .bbmodel is written beside the dropped .java file.
  echo.
  echo Advanced command-line use:
  echo   node tools/model_export/java_model_to_bbmodel.mjs --source path/to/Model.java --out path/to/output/folder
  echo.
  pause
  popd >nul
  exit /b 1
)

set "FAILED=0"

:next_file
if "%~1"=="" goto done

echo.
echo Exporting "%~1"
if not exist "%~1" (
  set "FAILED=1"
  echo File does not exist: "%~1"
  shift
  goto next_file
)

set "OUT_DIR=%~dp1"
if "%OUT_DIR:~-1%"=="\" set "OUT_DIR=%OUT_DIR:~0,-1%"
echo Output folder: "%OUT_DIR%"
node tools/model_export/java_model_to_bbmodel.mjs --source "%~1" --out "%OUT_DIR%"
if errorlevel 1 (
  set "FAILED=1"
  echo Failed to export "%~1"
)

shift
goto next_file

:done
echo.
if "%FAILED%"=="0" (
  echo Finished exporting dragged Java model files.
) else (
  echo Finished with one or more export errors.
)
echo.
pause
popd >nul
exit /b %FAILED%
