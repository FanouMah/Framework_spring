# Framework_sprint
 
 Technologies necessaires :
 - Java 17
 - Tomcat 10

 ## Déploiement

 1. Assurez-vous d'avoir les technologies nécessaires sur votre système.
 2. Clonez ce dépôt sur votre machine locale.
 3. Ouvire une invite de commande et naviguez jusqu'au répertoire racine du projet.
 4. Exécutez la commande suivante en remplaçant `CHEMIN_WEBAPPS` par le chemin absolu vers votre répertoire webapps de Tomcat :

    - **Pour Windows :**
      ```batch
      .\Framework\script\deployment.bat CHEMIN_WEBAPPS
      ```

Par exemple, si votre répertoire webapps et situé à `C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps`, la commande ressemblerait à ceci :
```batch
.\Framework\script\deployment.bat C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps
```