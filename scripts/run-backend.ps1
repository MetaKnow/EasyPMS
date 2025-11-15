<#
  函数级注释：使用项目内置 JDK 启动后端，并在停止时清理残留 java 进程
  - 编译后端：mvn -DskipTests package
  - 启动后端：tools/jdk/bin/java.exe -jar backend/target/pms-*.jar
  - 退出时：根据命令行特征杀掉残留的 java 进程（包含项目 jar 路径）
#>

param(
  [switch]$SkipBuild
)

function Get-ProjectRoot {
  $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
  return Convert-Path (Join-Path $scriptDir '..')
}

function Find-LatestJar([string]$root) {
  $pattern = Join-Path $root 'backend/target/*.jar'
  $jars = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending
  if ($null -eq $jars -or $jars.Count -eq 0) { return $null }
  return $jars[0].FullName
}

function Kill-ProjectJavaProcesses([string]$root) {
  try {
    $projPath = (Convert-Path $root)
    $jarDir = Join-Path $projPath 'backend\target'
    # 根据命令行特征匹配本项目后端 jar 的 java 进程
    $procs = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
      Where-Object {
        $cmd = $_.CommandLine
        ($null -ne $cmd) -and (
          $cmd -like "*\backend\target\pms-*.jar*" -or
          $cmd -like "*\backend\target\*.jar*" -or
          $cmd -like "*com.missoft.pms.PmsApplication*"
        )
      }
    foreach ($p in $procs) {
      try { Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue } catch {}
    }
  } catch {}
}

try {
  $root = Get-ProjectRoot
  Write-Host "项目根目录: $root"

  if (-not $SkipBuild) {
    Write-Host "开始编译后端 (跳过测试)..."
    Push-Location (Join-Path $root 'backend')
    mvn -DskipTests package
    Pop-Location
  }

  $jar = Find-LatestJar -root $root
  if ($null -eq $jar) {
    Write-Error "未找到后端可运行的 Jar，请先构建后端。"
    exit 1
  }

  $java = Join-Path $root 'tools/jdk/bin/java.exe'
  if (-not (Test-Path $java)) {
    Write-Error "未找到项目内 JDK：$java"
    exit 1
  }

  Write-Host "使用项目内 JDK 启动后端：$java -jar $jar"
  $proc = Start-Process -FilePath $java -ArgumentList @('-jar', $jar) -WorkingDirectory (Split-Path $jar -Parent) -PassThru
  Write-Host "后端进程 PID: $($proc.Id)"

  # 等待进程退出
  Wait-Process -Id $proc.Id
} finally {
  Write-Host "后端进程已停止，清理残留 java 进程..."
  Kill-ProjectJavaProcesses -root $root
  Write-Host "清理完成。"
}