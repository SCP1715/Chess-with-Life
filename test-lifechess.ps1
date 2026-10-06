param([string]$PythonExecutable, [string]$EngineExecutable = 'stockfish.exe')

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$engineSource = Join-Path $repoRoot 'third_party/fairy-stockfish/src'
$engineFile = [IO.Path]::GetFileName($EngineExecutable)
if (-not $engineFile -or $engineFile -ne $EngineExecutable) {
    throw 'EngineExecutable must be a file name placed in the Fairy-Stockfish src directory.'
}
$savedPath = $env:PATH
$savedEngine = $env:LIFECHESS_ENGINE_BIN
$savedPythonEngine = $env:LIFECHESS_ENGINE
$resolvedEnginePath = Join-Path $engineSource $engineFile

try {
    $env:PATH = "C:\msys64\usr\bin;C:\msys64\mingw64\bin;$savedPath"
    Push-Location $engineSource

    if ($engineFile -eq 'stockfish.exe') {
        & make ARCH=x86-64 COMP=mingw build
    }
    else {
        & make ARCH=x86-64 COMP=mingw "EXE=$engineFile" build
    }
    if ($LASTEXITCODE -ne 0) { throw "Fairy-Stockfish build failed ($LASTEXITCODE)" }

    $env:LIFECHESS_ENGINE_BIN = "./$engineFile"
    & expect ../tests/lifechess-core.tcl
    if ($LASTEXITCODE -ne 0) { throw "LifeChess core tests failed ($LASTEXITCODE)" }

    & expect ../tests/lifechess-node-budget.tcl
    if ($LASTEXITCODE -ne 0) { throw "LifeChess node-budget tests failed ($LASTEXITCODE)" }

    & expect ../tests/lifechess-draw.tcl
    if ($LASTEXITCODE -ne 0) { throw "LifeChess draw-policy tests failed ($LASTEXITCODE)" }

    Pop-Location
    $engineSource = $null

    if (-not $PythonExecutable) {
        foreach ($candidate in @('python3', 'python')) {
            $command = Get-Command $candidate -ErrorAction SilentlyContinue
            if ($command) {
                $PythonExecutable = $command.Source
                break
            }
        }
    }

    if ($PythonExecutable) {
        $env:LIFECHESS_ENGINE = $resolvedEnginePath
        Push-Location $repoRoot
        try {
            $pythonCode = "import sys, unittest; sys.path.insert(0, r'$repoRoot'); " +
                    "unittest.main(module='tools.test_lifechess_console', verbosity=2)"
            & $PythonExecutable -c $pythonCode
            if ($LASTEXITCODE -ne 0) { throw "LifeChess console adapter tests failed ($LASTEXITCODE)" }
        }
        finally {
            Pop-Location
        }
    }
    else {
        Write-Warning 'Python 3 not found; console-adapter tests were skipped.'
    }
}
finally {
    if ($engineSource -and (Get-Location).Path -eq $engineSource) { Pop-Location }
    $env:PATH = $savedPath
    $env:LIFECHESS_ENGINE_BIN = $savedEngine
    $env:LIFECHESS_ENGINE = $savedPythonEngine
}
