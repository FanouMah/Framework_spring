@echo off
setlocal EnableDelayedExpansion

set lib=.\Framework\lib
set src=.\Framework\src
set bin=.\Framework\bin

rem Initialiser la liste des fichiers Java à compiler
set "javaFiles="

rem Récupérer la liste de tous les fichiers Java dans les sous-dossiers de %src%
for /r "%src%" %%G in (*.java) do (
    rem Extraire la structure des packages à partir du chemin complet du fichier source
    set "javaFile=%%~fG"
    set "packagePath=!javaFile:%src%=!"
    set "packagePath=!packagePath:~0,-\%%~nG%%~xG!"

    rem Créer les répertoires de sortie si nécessaire
    if not exist "%bin%!packagePath!" (
        mkdir "%bin%!packagePath!" >nul
    )

    rem Ajouter le fichier Java à la liste des fichiers à compiler
    set "javaFiles=!javaFiles! "%%G""
)

rem Construire le chemin de classe pour toutes les bibliothèques dans le dossier "lib"
set "classpath="
for %%I in ("%lib%\*.jar") do (
    set "classpath=!classpath!;"%%I""
)

rem Compiler tous les fichiers Java en une seule commande avec les bibliothèques nécessaires
javac -cp "%classpath%" -parameters -d "%bin%" !javaFiles!

endlocal
