# Vergleich zwischen reiner eXTra Übertragung vs. eXTra im MTOM

Mit diesem Projekt soll eine Grundlage für die Diskussion geschaffen werden,
ob durch die Verwendung von MTOM wirklich geringere Datenmengen erzeugt werden.

Hierzu wurden verschiedene, SV-übliche Daten als Testdaten genutzt:
- einzelne VSNR-Abfrage
- 10 / 1000 / 10.000 DEUEV-Meldungen*
- A1 Bewilligungs-PDF
- echtes DUA-Versandpaket mit 889 Meldungen **
- echtes A1-Versandpaket mit 7x Bewilligungs-PDF **

Betrachtet wurden verschiedene Kombinationen der folgenden Einstellungen: 
- Nutzdaten ohne Komprimierung / Komprimiert mit GZIP
- normales eXTra / eXTra eingebetet in MTOM
- lesbares XML / minimized XML (auf eXTra-Ebene)
- HTTP-Request ohne / mit GZIP-Komprimierung

Die verschiedenen Konstellationen wurden mit folgenden Dateien durchgeführt:

\* _Die DEUEV-Meldungen sind nicht optimal gewählt. Da es sich um händisch generierte Daten handelt, 
wiederholen sich diese sehr häufig, sodass GZIP hier deutlich stärker greift, als echten
SV-Daten._  

\** Die Dateien sind aus Datenschutzgründen selbstverständlich nicht mitgeliefert.

Die dedizierten Resultate können durch Starten der `main()` in `Main.kt` abgerufen werden. Zusätzlich 
wurde der Output auch in `result.txt` gespeichert.

## Ergebnisse MTOM

- Die  Nutzdaten innerhalb der Transportstruktur sind mit MTOM grundsätzlich
~25% kleiner (unabhängig von der Nutzdaten-Komprimierung).

- Das Speicher-Gewinn für das fertige Transport-XML inkl. der Nutzdaten ist abhängig von der Nutzdatengröße.
 
  - Bei sehr kleinen Meldungen (bis ca. 3k) macht es größentechnisch keinen Unterschied bzw. hier ist
  MTOM sogar größer als reines eXTra.
  
  - Bereits bei mittelgroßen Meldungen (ab ~50k) sind die Transport-XMLs bei MTOM wieder ~25% kleiner.

- Wird der vollständige HTTP-Request aber GZIP komprimiert _(wie es ja eigentlich
Standard sein sollte)_, sieht das Bild komplett anders aus. Mit eingeschaltem
"HTTP-GZIP" haben die Request mit und ohne MTOM nahezu exakt die gleiche Größe (Abweichungen )

## Erkenntnisse / Fazit
Es sollte auf die korrekte Komprimierung der Daten Wert gelegt werden. Insbesondere bei nicht-binären Daten
kann dies problemlos einen **Faktor von 50** ausmachen. Dies bedeutet Komprimierung auf Nutzdaten-Ebene und
auf HTTP-Ebene/Nutzung von MTOM.
 
**MTOM bringt speichertechnisch keine Vorteile gegenüber GZIP auf HTTP-Ebene.**
 
## Abgrenzung
 
Es wurde in diesem Prozess explizit **nicht bewertet**:
 
- Sind die genutzten Standards aktuell und werden sie empfohlen?
- Sind sie in der Branche verbreitet (bspw. bei anderen elektr. Meldungen des öffentlichen Lebens) und wer aus der
Payroll-Branche unterstützt diese Verfahren bereits?
- Gibt es ausreichend technischen Support der einzelnen Verfahren (Bibliotheken zum Senden/Empfangen) und sind
diese öffentlich oder proprietär?
- Wie werden andere Resourcen beansprucht (CPU, RAM)?
- Gäbe es nicht andere, deutlich kompaktere Wege Meldungen zu senden, die zeitkonformer wären.
 
 
 ### Kontakt
 Für Fragen, Hinweise und Kritik bitte melden per Kontakt-Formular auf www.quick-lohn.de oder direkt über Github.   
 