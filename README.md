# HTL-SWP-Normal
Libraries:
JavaFX, MySQL Connector 8

Main und Testingsuit ausführen um alle .txt Dateien zu erhalten. In die Ticker.txt werden jene Ticker(TLSA, AMZN, ...) geschrieben, von diesen werden dann alle Daten aus der
API geholt und in die Datenbank geschrieben. 
In Userdates.txt werden Port, Nutzername der DB, Passwort der DB und API Key von Alphaadvantage jeweils in eine Zeile eingetragen.
In testingSuite.txt werden jene Ticker eingetragen von denen man schon Daten (durch Main) hat und berechnen möchte welche Investment Methode am besten Funktioniert bzw. gibt die Prozent und absoluten Wert an den man am Ende dadurch erhält, der Rest sollte im Programm erklärt sein.

Die Main erstellt außerdem eine Graphik in der alle Close-werte Splitcorrected und der 200er-Durchschnitt aufgetragen sind.

