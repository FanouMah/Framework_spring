@echo off
setlocal

rem verifier si aucun argument n'est fourni
if "%~1" == "" (
    echo Veuillez spécifier le chemin vers le répertoire webapps de Tomcat.
    exit /b
)

rem recuperation du chemin de webapps
set webappFolder="%~1"

rem Extraire le nom du dossier root à partir du chemin du dossier source
for %%I in (".") do set "projet_name=%%~nxI"

rem declaration des variables
set temp=.\temp
set lib=.\Framework\lib
set src=.\Framework\src
set bin=.\Framework\bin
set script=.\Framework\script
set test=.\Test

rem création du dossier temp
if exist "%temp%" (
    rd /S /Q "%temp%"
    mkdir "%temp%"
) else (
    mkdir "%temp%"
)

rem Compilation des codes java vers le dossier bin
call %script%\compilateur.bat

rem Déplacement vers bin
cd %bin%

rem Compresser bin en un fichier jar
jar -cvf "..\lib\%projet_name%".jar *

rem Déplacement vers le projet
cd /D ..\..\

rem Copie des élements indispensables pour tomcat vers temp
xcopy "%test%\" "%temp%\WEB-INF\"
xcopy /E /I /Y "%lib%\" "%temp%\WEB-INF\lib"

rem Déplacement vers temp
cd /D "%temp%"

rem Compresser le projet en un fichier war
jar -cvf "..\%projet_name%".war *

rem Déplacement vers le projet
cd /D ..\

rem effacer temp
rd /S /Q "%temp%"

rem Copie du fichier war vers webapps de tomcat
copy /Y ".\%projet_name%.war" "%webappFolder%"

rem effacer le fichier war
del ".\%projet_name%.war"

echo Deployment success !!.

endlocal
