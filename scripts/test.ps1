$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$Out = Join-Path $Root 'target/self-test-classes'
if (Test-Path $Out) { Remove-Item $Out -Recurse -Force }
New-Item -ItemType Directory -Path $Out -Force | Out-Null
$Sources = Get-ChildItem (Join-Path $Root 'src/main/java'), (Join-Path $Root 'src/test/java') -Filter '*.java' -Recurse | ForEach-Object { $_.FullName }
& javac --release 17 -encoding UTF-8 -d $Out $Sources
if ($LASTEXITCODE -ne 0) { throw 'Компиляция тестов завершилась с ошибкой.' }
& java -ea -cp $Out ru.urfu.practicalwork2.AllTests
if ($LASTEXITCODE -ne 0) { throw 'Тесты завершились с ошибкой.' }
