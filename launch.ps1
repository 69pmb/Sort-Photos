# Set-Variable -Name PATH_TO_FX -Value "C:\Program Files\Java\javafx-sdk-11.0.2\lib"
if(!$env:PATH_TO_FX -Or -not (Test-Path -Path $env:PATH_TO_FX)) {
	Write-Host "Please install Java Fx SDK or define its path in 'PATH_TO_FX' env variable" -ForegroundColor Red
	pause
	exit
}
[xml]$pom = Get-Content pom.xml
$jar = "target\sort-photos-" + $pom.project.version + "-shaded.jar"
if (-not (Test-Path -Path $jar)) {
    Write-Host "Building jar.." -ForegroundColor Blue
	mvn clean package -"Dmaven.test.skip=true" -U -q
}
java --module-path $env:PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar $jar
pause
