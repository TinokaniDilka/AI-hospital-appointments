# SmartCare AI Full-Stack System Launcher Script

Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host "  SmartCare - AI-Powered Hospital Appointment and Queue System  " -ForegroundColor Cyan
Write-Host "==========================================================================" -ForegroundColor Cyan

$projectRoot = Get-Location

# Auto-detect Python Executable
$pythonExe = "C:\Users\USER\anaconda_envs\tf_intel\python.exe"
if (-not (Test-Path $pythonExe)) {
    $pythonCmd = Get-Command python, py -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Path
    if ($pythonCmd) { $pythonExe = $pythonCmd } else { $pythonExe = "python" }
}

Write-Host ""
Write-Host "[1/3] Starting Python FastAPI AI ML Wait-Time Service (Port 8000)..." -ForegroundColor Yellow
Start-Process -FilePath $pythonExe -ArgumentList "main.py" -WorkingDirectory "$projectRoot\smartcare_ai"

Write-Host "[2/3] Starting React Web Portal (Vite Dev Server)..." -ForegroundColor Yellow
Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory "$projectRoot\smartcare_web"

Write-Host "[3/3] System Ready!" -ForegroundColor Green
Write-Host "--------------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "  * Python AI Engine:  http://localhost:8000 (Predict Wait API and Docs)" -ForegroundColor Green
Write-Host "  * React Web Portal:  http://localhost:5173 (Doctors and Admins)" -ForegroundColor Green
Write-Host "  * Spring Boot API:   http://localhost:8080 (REST Controllers)" -ForegroundColor Green
Write-Host "==========================================================================" -ForegroundColor Cyan
