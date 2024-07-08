import ru.CryptoPro.AdES.Options
import ru.CryptoPro.CAdES.CAdESSignature
import ru.CryptoPro.CAdES.CAdESType
import ru.CryptoPro.JCP.tools.AlgorithmUtility
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

data class CertRequest(
	val rawRequest: List<Byte>,
	val userId: String?,
	val authorityName: String
)

fun sign(keyStore: KeyStore, password: String, data: List<Byte>): List<Byte> {
	val first = keyStore.aliases().nextElement()
	val certificate = keyStore.getCertificate(first) as X509Certificate
	val privateKey = keyStore.getKey(first, password.toCharArray()) as PrivateKey

	val digestOid = AlgorithmUtility.keyAlgToDigestOid(privateKey.algorithm)
	val keyOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(privateKey.algorithm)

	val cAdESSignature = CAdESSignature(false)
	cAdESSignature.setOptions(Options().disableCertificateValidation())
	cAdESSignature.addSigner(
		keyStore.provider.name,
		digestOid,
		keyOid,
		privateKey,
		listOf(certificate),
		CAdESType.CAdES_BES, // signature type
		null, // tsp address (could be null)
		false, // countersign
		null, // signed attributes
		null, // unsigned attributes
		emptySet(), // set of CRL
		true // add chain
	)

	val signature = ByteArrayOutputStream()
	cAdESSignature.open(signature)
	cAdESSignature.update(data.toByteArray())
	cAdESSignature.close()

	return signature.toByteArray().toList()
}

/**
 * Функция для создания запроса на сертификат,
 * она принимает CSR в формате PEM и превращает
 * его в формат DER, затем подписывает два раза
 * 
 * Нужно отправить POST запрос /api/ra/certRequests в
 * формате JSON, в ответ приходит UUID для отслеживания
 */
fun createCertRequest(
	csr: String,
	userId: String,
	authorityName: String,
	keyStore: KeyStore,
	password: String
): CertRequest {
	// Переводим из формата PEM в DER
	val der = csr.lines().drop(1).dropLast(1).joinToString("\n")

	// Формируем запрос на выпуск сертификата
	return CertRequest(
		rawRequest = sign(
			keyStore = keyStore,
			password = password,
			data = sign(
				keyStore = keyStore,
				password = password,
				data = Base64.getMimeDecoder().decode(der).toList()
			)
		),
		userId = userId,
		authorityName = authorityName
	)
}