<#
  start-all.ps1 - sobe a aplicacao inteira (backend + frontend) com um comando.

  Uso:
    .\start-all.ps1              # roda migrations, sobe os microservicos e o frontend
    .\start-all.ps1 -SkipMigrations
    .\start-all.ps1 -NoFrontend
    .\start-all.ps1 -SkipMigrations -NoFrontend

  Cada servico abre em sua propria janela do PowerShell.
  Cada .env do servico e carregado antes de rodar o mvnw.
#>
param(
  [switch]$SkipMigrations,
  [switch]$NoFrontend
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$backend = Join-Path $root 'backend'
$frontend = Join-Path $root 'frontend'
$powerShellExe = (Get-Command pwsh -ErrorAction SilentlyContinue).Source

if (-not $powerShellExe) {
  $powerShellExe = (Get-Command powershell -ErrorAction Stop).Source
}

# Comando que roda dentro de cada janela filha: carrega o .env do proprio servico e sobe o mvnw.
function New-BackendCommand([string]$dir) {
  return @"
Set-Location '$dir'
if (Test-Path '.env') {
  Get-Content '.env' | ForEach-Object {
    if (`$_ -match '^\s*([^#=]+?)\s*=\s*(.*?)\s*$') { Set-Item "Env:`$(`$matches[1])" `$matches[2] }
  }
}
Write-Host '>> Iniciando $dir' -ForegroundColor Cyan
& .\mvnw.cmd spring-boot:run
"@
}

# 1) Migrations (roda e sai). Espera terminar antes de subir o resto.
if (-not $SkipMigrations) {
  Write-Host '== Rodando db-migrations (Flyway)...' -ForegroundColor Yellow
  $mig = Join-Path $backend 'db-migrations'
  Start-Process $powerShellExe -WorkingDirectory $mig -Wait `
    -ArgumentList '-NoProfile','-Command',(New-BackendCommand $mig)
  Write-Host '== Migrations concluidas.' -ForegroundColor Green
}

# 2) Microservicos (cada um em sua janela). Ordem: dependencias primeiro, BFF por ultimo.
$servicos = @(
  'ms-usuarios',      # 8081
  'ms-mesas',         # 8082
  'ms-pedidos',       # 8083
  'ms-produtos',      # 8084
  'ms-notificacoes',  # 8085
  'ms-cozinha',       # 8086
  'ms-pagamentos',    # 8087
  'bff-restaurante'   # 8080
)

foreach ($svc in $servicos) {
  $dir = Join-Path $backend $svc
  Write-Host ">> Subindo $svc" -ForegroundColor Cyan
  Start-Process $powerShellExe -ArgumentList '-NoExit','-NoProfile','-Command',(New-BackendCommand $dir)
  Start-Sleep -Milliseconds 400
}

# 3) Frontend (Angular).
if (-not $NoFrontend) {
  Write-Host '>> Subindo frontend (ng serve)' -ForegroundColor Cyan
  $feCmd = "Set-Location '$frontend'; Write-Host '>> Frontend' -ForegroundColor Cyan; & npm.cmd run dev"
  Start-Process $powerShellExe -ArgumentList '-NoExit','-NoProfile','-Command',$feCmd
}

Write-Host ''
Write-Host 'Tudo iniciado. Portas:' -ForegroundColor Green
Write-Host '  BFF             http://localhost:8080'
Write-Host '  ms-usuarios     http://localhost:8081'
Write-Host '  ms-mesas        http://localhost:8082'
Write-Host '  ms-pedidos      http://localhost:8083'
Write-Host '  ms-produtos     http://localhost:8084'
Write-Host '  ms-notificacoes http://localhost:8085'
Write-Host '  ms-cozinha      http://localhost:8086'
Write-Host '  ms-pagamentos   http://localhost:8087'
Write-Host '  frontend        http://localhost:4200'
