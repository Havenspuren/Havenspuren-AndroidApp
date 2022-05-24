# Navigation


## Navigationsdateien erstellen
1. Graphhopper repo clonen ``git clone https://github.com/graphhopper/graphhopper.git``
2. Version eins auschecken ``git checkout 1.0``
3. `config.yml` aus diesem Ordner in den geklonten Graphhopper Ordner kopieren und somit die vorhandene config ersetzen.
4. Öffne den Graphhopper Ordner in einer GitBash, oder auf Linux.
5. Es muss Java und Maven installiert sein
6. $JAVA_HOME und $MAVEN_HOME definieren (pfad ohne bin) ``export JAVA_HOME="<PATH to JAVA JDK>"``, ``export MAVEN_HOME="<PATH to MAVEN>"``
7. ``./graphhopper.sh -a import -i <PATH to PBF file>``
   optional (wird dem Gerät entpackt, kann also auch direkt entpackt auf das Gerät kopiert werden ):
8. Alle erstellten Dateien im Ordner als zip archiv einpacken. (ACHTUNG: Erstelle Dateien dürfen im Archiv nicht in einem unter ordner liegen)
9. Zip archiv dateiendung zu ".ghz" ändern


## Profile

Die aktuelle Konfiguration unterstützt `by bike` und `by foot` Routen. Eine genaue Dokumentation der Profile kann [hier](https://github.com/graphhopper/graphhopper/blob/1.0/docs/core/profiles.md) gefunden werden.

## Pitfalls
### Konfiguration
Alle Konifguration die sich auf Profile beziehen müssen im Java Quellcode und in der config.yml übereinstimmen (auch die *Reihenfolge*).

### Profile
Alle Profile die in der `config.yml` angegeben sind, werden mit exportiert!
