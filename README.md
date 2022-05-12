# Havenspuren Android App

Die Karten werden mit der Bibliothek [VTM](https://github.com/mapsforge/vtm) angezeigt


#Karte
## Karten aussehen.

Um das aussehen der Karte zu  ändern, muss eine xml datei erstellt werden. https://github.com/mapsforge/mapsforge/blob/master/docs/Rendertheme.md


## Kartendatei erstellen
1. [Osmosis](https://github.com/openstreetmap/osmosis/releases) installieren
2. OSM Rohdaten herrunterladen: z.B. [hier](https://extract.bbbike.org/)
3. [Mapsforge plugin](https://github.com/mapsforge/mapsforge/blob/master/docs/Getting-Started-Map-Writer.md) herunterladen und installieren
  3.1. Plugin Datei in den "lib/default" Ordner kopieren
4. Folgenenden Command ausführen `osmosis.bat --rb file="input.osm.pbf" --mapfile-writer file="output.map"`
5. ".map" Datei in den asstets Ordner in einen Ordner mit dem Namen "maps" verschieben.


# Navigation
# Navigationsdateien erstellen
1. Graphhopper repo clonen ``git clone https://github.com/graphhopper/graphhopper.git``
2. Version eins auschecken ``git checkout 1.0``
3. Öffne den Graphhopper Ordner in einerl GitBash, oder auf Linux.
4. Es muss Java und Maven installiert sein
5. $JAVA_HOME und $MAVEN_HOME definieren ``export JAVA_HOME="<PATH to JAVA JDK>"``, ``export MAVEN_HOME="<PATH to MAVEN>"``
6. ``./graphhopper.sh -a import -i <PATH to PBF file>``
optional:
7. Alle erstellten Dateien im Ordner als zip archiv einpacken. (ACHTUNG: Erstelle Dateien dürfen im Archiv nicht in einem unter ordner liegen)
8. Zip archiv dateiendung zu ".ghz" ändern