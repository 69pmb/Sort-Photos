cd C:\workspace\Perso\Sort-Photos
Set-Variable -Name PATH_TO_FX -Value "C:\Program Files\Java\javafx-sdk-11.0.2\lib"
if (-not (Test-Path -Path target\sort-photos-0.0.1-SNAPSHOT-shaded.jar)) {
    mvn clean package -"Dmaven.test.skip=true" -U -q
}
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target\sort-photos-0.0.1-SNAPSHOT-shaded.jar
pause
