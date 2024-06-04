@echo off
setlocal

rem Extraire le nom du dossier root à partir du chemin du dossier source
for %%I in (".") do set "projet_name=%%~nxI"

rem declaration des variables
set temp=.\temp
set lib=.\Framework\lib
set src=.\Framework\src
set bin=.\Framework\bin
set script=.\Framework\script
set test=.\Test
set views=.\Test\views
set webappFolder=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps

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
xcopy /E /I /Y "%views%\" "%temp%\views"
xcopy /E /I /Y "%lib%\" "%temp%\WEB-INF\lib"
xcopy /E /I /Y "%bin%\" "%temp%\WEB-INF\classes"

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
