import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher

import javax.crypto.spec.SecretKeySpec


object GKVUtil {
  fun verschluesseln(byteArray: ByteArray): ByteArray {
    val skeyspec = SecretKeySpec("PASSWORT".toByteArray(), "Blowfish")
    val cipher = Cipher.getInstance("Blowfish")
    cipher.init(Cipher.ENCRYPT_MODE, skeyspec)
    return cipher.doFinal(byteArray)
  }

  fun gzip(nutzdaten: ByteArray): ByteArray {
    val byteStream = ByteArrayOutputStream(nutzdaten.size)
    val zipStream = GZIPOutputStream(byteStream)
    zipStream.write(nutzdaten)
    zipStream.close()
    byteStream.close()
    return byteStream.toByteArray()
  }

  fun base64(nutzdaten: ByteArray): ByteArray {
    return Base64.getEncoder().encode(nutzdaten)
  }

  fun xmlMinimize(xml: String): String {
     return xml.lines().joinToString("") { it.trim() }
  }
}