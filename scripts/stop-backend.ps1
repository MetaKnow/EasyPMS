<#
  函数级注释：手动清理本项目相关的残留 java 进程
  - 通过 Win32_Process 检索命令行，匹配包含 backend/target/*.jar 或 PmsApplication 的进程并强制终止
#>

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Convert-Path (Join-Path $scriptDir '..')

Write-Host "扫描并清理残留 java 进程 (项目根: $root) ..."

try {
  $procs = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object {
      $cmd = $_.CommandLine
      ($null -ne $cmd) -and (
        $cmd -like "*\backend\target\pms-*.jar*" -or
        $cmd -like "*\backend\target\*.jar*" -or
        $cmd -like "*com.missoft.pms.PmsApplication*"
      )
    }
  if ($procs) {
    foreach ($p in $procs) {
      Write-Host "终止 PID=$($p.ProcessId): $($p.CommandLine)"
      try { Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue } catch {}
    }
  } else {
    Write-Host "未找到可清理的残留 java 进程。"
  }
} catch {
  Write-Error "清理过程中发生错误：$_"
}