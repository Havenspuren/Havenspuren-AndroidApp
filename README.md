# Havenspuren Android App

Die Karten werden mit der Bibliothek [VTM](https://github.com/mapsforge/vtm) angezeigt

# Asset Bundles

Dadurch das die App durch mehrere Routen an größe zugenommen hat, wurde zu der Asset Bundle
Devlivery vom Play Store gewechselt.

Dies hat zur folge, dass man eine Einstellung in Android Studio ändern muss, damit die App weiterhin
einfach getestet werden kann.

Die app Launch Configuration muss geändert werden. Klick oben auf "app" links neben dem aktuell
angeschlossenen Geräts. Anschließend auf "Edit Configurations">"app">"Installation Options">"From
App Bundle".

Alternativ kann [hier](https://developer.android.com/guide/playcore/asset-delivery/test) nachgelesen
werden wie man manuell die apks generiert und installiert.

# Karte

## Karten aussehen.

Um das aussehen der Karte zu ändern, muss eine xml datei erstellt
werden. https://github.com/mapsforge/mapsforge/blob/master/docs/Rendertheme.md

## Kartendatei erstellen

2. OSM Rohdaten herrunterladen: z.B. [hier](https://extract.bbbike.org/) ( Mapforge als typ
   auswählen)
5. ".map" Datei in den asstets Ordner in einen Ordner mit dem Namen "maps" verschieben.

# Navigation

Ist im Ordner [routing](routing/README.md) dokumentiert.