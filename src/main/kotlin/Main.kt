enum class RequestType { Extra, MTOM }

enum class XmlMinimize { On, Off }

enum class NutzdatenKomprimierung { NONE, GZIP }

enum class HttpKomprimierung { NONE, GZIP }


val vorlage_extra = RequestType::class.java.getResource("/vorlage_extra.xml").readText(Charsets.UTF_8)
val vorlage_mtom = RequestType::class.java.getResource("/vorlage_mtom.xml").readText(Charsets.UTF_8)


fun main() {
  log(" Vergleich der verschiedenen Übertragungsformate zu den Kommunikations-Servern")
  log("-------------------------------------------------------------------------------")
  log()
  log("Hinweise: Die Verschlüsselung und Signierung entspricht nicht den Anforderungen")
  log("          der GG. Hier wurde stattdessen eine einfache Passwort-Verschlüsselung")
  log("          genutzt, die die Resultate aber nicht entscheidend beeinflussen sollte.")
  log()
  log("          Größe des Transport XML   (normal / minified)")
  log("                   eXTra: " + "%,10d bytes / %,10d bytes ".format(
      vorlage_extra.toByteArray(Charsets.ISO_8859_1).size,
      GKVUtil.xmlMinimize(vorlage_extra).toByteArray(Charsets.ISO_8859_1).size
  ))
  log("                    MTOM: " + "%,10d bytes / %,10d bytes ".format(
      vorlage_mtom.toByteArray(Charsets.ISO_8859_1).size,
      GKVUtil.xmlMinimize(vorlage_mtom).toByteArray(Charsets.ISO_8859_1).size
  ))



  buildTable("vsnrabfrage.txt", "Eine einzelne Versicherungsnummern-Abfrage")
  buildTable("dua-klein.txt", "10x 10er Anmeldung")
  buildTable("dua-mittel.txt", "1000x 10er Anmeldung")
  buildTable("dua-gross.txt", "100000x 10er Anmeldung")
  buildTable("a1.pdf", "Normales A1 Bewilligungs-PDFf")
  
  buildTable("dua-echt.txt", "Echt DUA-Meldung mit 889 Meldungen")
  buildTable("a1-echt.xml", "Echtes A1-Versandpaket mit 7x Bewilligung")
}


private fun buildTable(nutzdaten: String, desc: String) {
  val content = RequestType::class.java.getResource("/$nutzdaten")?.readBytes()
  if (content == null) {
    log("WARNUNG: Datei '$nutzdaten' existiert nicht")
    return
  }

  log();log();log()
  log("====================================================================================================================================================================================")
  log("===== Nutzdaten: $nutzdaten ($desc)")
  log("  Größe: %,10d bytes".format(content.size))
  log()
  log("+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------+")
  log("| Type  | ND-Komprimierung | XML minify | HTTP-Komprimierung ||| Reine Nutzdaten (ggf. komprimiert) | Nutzdaten im Endformat | Vollst. XML inkl. ND | Vollst. HTTP Request |")
  log("+-------+------------------+------------+--------------------+++------------------------------------+------------------------+----------------------+----------------------+")
  buildXml(content, RequestType.Extra, XmlMinimize.Off, NutzdatenKomprimierung.NONE, HttpKomprimierung.NONE)
  buildXml(content, RequestType.MTOM, XmlMinimize.Off, NutzdatenKomprimierung.NONE, HttpKomprimierung.NONE)
  log("|       |                  |            |                    |||                                    |                        |                      |                      |")
  buildXml(content, RequestType.Extra, XmlMinimize.On, NutzdatenKomprimierung.NONE, HttpKomprimierung.NONE)
  buildXml(content, RequestType.MTOM, XmlMinimize.On, NutzdatenKomprimierung.NONE, HttpKomprimierung.NONE)
  log("|       |                  |            |                    |||                                    |                        |                      |                      |")
  buildXml(content, RequestType.Extra, XmlMinimize.On, NutzdatenKomprimierung.GZIP, HttpKomprimierung.NONE)
  buildXml(content, RequestType.MTOM, XmlMinimize.On, NutzdatenKomprimierung.GZIP, HttpKomprimierung.NONE)
  log("|       |                  |            |                    |||                                    |                        |                      |                      |")
  buildXml(content, RequestType.Extra, XmlMinimize.On, NutzdatenKomprimierung.NONE, HttpKomprimierung.GZIP)
  buildXml(content, RequestType.MTOM, XmlMinimize.On, NutzdatenKomprimierung.NONE, HttpKomprimierung.GZIP)
  log("|       |                  |            |                    |||                                    |                        |                      |                      |")
  buildXml(content, RequestType.Extra, XmlMinimize.On, NutzdatenKomprimierung.GZIP, HttpKomprimierung.GZIP)
  buildXml(content, RequestType.MTOM, XmlMinimize.On, NutzdatenKomprimierung.GZIP, HttpKomprimierung.GZIP)
  log("+-------+------------------+------------+--------------------+++------------------------------------+------------------------+----------------------+----------------------+")
}


fun buildXml(nutzdaten: ByteArray, type: RequestType, xmlMinimize: XmlMinimize, nutzdatenKomprimierung: NutzdatenKomprimierung, httpKomprimierung: HttpKomprimierung) {

  // ***** Transport XML (eXTra + MTOM) *****
  val xmlOhneNutzdaten = when (type) {
    RequestType.Extra -> vorlage_extra
    RequestType.MTOM -> vorlage_mtom
  }

  val xmlOhneNutzdatenGgfMinimizedBytes = when (xmlMinimize) {
    XmlMinimize.On -> GKVUtil.xmlMinimize(xmlOhneNutzdaten)
    XmlMinimize.Off -> xmlOhneNutzdaten
  }.toByteArray(Charsets.ISO_8859_1)


  // ***** Nutzdaten *****
  val nutzdatenVerschluesseltBytes = GKVUtil.verschluesseln(nutzdaten)

  val nutzdatenKomprimiertBytes = when (nutzdatenKomprimierung) {
    NutzdatenKomprimierung.GZIP -> GKVUtil.gzip(nutzdaten)
    NutzdatenKomprimierung.NONE -> nutzdatenVerschluesseltBytes
  }

  val nutzdatenImEndformatBytes = when (type) {
    RequestType.Extra -> GKVUtil.base64(nutzdatenKomprimiertBytes)
    RequestType.MTOM -> nutzdatenKomprimiertBytes
  }


  // ***** Vollständiger Request *****
  // wir führen nur zusammen, das Resultat ist naturlich inhaltlich nicht korrekt, besitzt 
  // aber die korrekte Größe
  val xmlMitNutzdatenBytes = xmlOhneNutzdatenGgfMinimizedBytes + nutzdatenImEndformatBytes

  val httpRequest = when (httpKomprimierung) {
    HttpKomprimierung.GZIP -> GKVUtil.gzip(xmlMitNutzdatenBytes)
    HttpKomprimierung.NONE -> xmlMitNutzdatenBytes
  }

  // ***** OUTPUT *****
  log(("| %-5s |       %-4s       |     %-3s    |          %-4s      |||                   %,10d bytes |       %,10d bytes |     %,10d bytes |     %,10d bytes |")
      .format(type, nutzdatenKomprimierung, xmlMinimize, httpKomprimierung,
          nutzdatenKomprimiertBytes.size, nutzdatenImEndformatBytes.size, xmlMitNutzdatenBytes.size, httpRequest.size))
}