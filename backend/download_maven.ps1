$mavenVersion = "3.9.6"
$mavenBaseDir = "$env:USERPROFILE\.m2\wrapper\dists"
$mavenDistDir = "$mavenBaseDir\apache-maven-$mavenVersion"
$mavenHome    = "$mavenDistDir\apache-maven-$mavenVersion"
$mavenZip     = "$env:TEMP\apache-maven-$mavenVersion-bin.zip"
$mavenUrl     = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"

if (Test-Path "$mavenHome\bin\mvn.cmd") {
    Write-Host "Maven $mavenVersion is already installed." -ForegroundColor Green
} else {
    Write-Host "Downloading Apache Maven $mavenVersion ..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $mavenDistDir | Out-Null
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
    Write-Host "Extracting ..." -ForegroundColor Cyan
    Expand-Archive -Path $mavenZip -DestinationPath $mavenDistDir -Force
    Remove-Item $mavenZip
    Write-Host "Done. Maven installed to: $mavenHome" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Maven path: $mavenHome\bin\mvn.cmd" -ForegroundColor Green
& "$mavenHome\bin\mvn.cmd" --version
