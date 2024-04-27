import ru.CryptoPro.JCP.JCP
import ru.CryptoPro.JCSP.JCSP
import ru.CryptoPro.reprov.RevCheck
import ru.CryptoPro.ssl.Provider
import java.io.FileInputStream
import java.net.URL
import java.security.KeyStore
import java.security.Security
import java.util.Properties
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

fun main() {
	// Инициализируем провайдеры и настройки
	initialize()
	val path = "gost-tls-client.properties"
	val properties = Properties()
	properties.load(object {}.javaClass.getResourceAsStream(path))

	// Создаем ГОСТ TLS контекст
	val sslContext = createSSLContext(
		pfxStorePath = properties.getProperty("pfx-store-path"),
		clientKeyStorePassword = properties.getProperty("client-key-store-password"),
	)
	// Подключаемся к УЦ используя ранее созданный TLS контекст
	val connection = createGostConnection(
		url = URL(properties.getProperty("url")),
		sslContext = sslContext
	).apply(HttpsURLConnection::connect)

	// Выводим результат подключения в консоль
	val result = connection.inputStream.readAllBytes()
	println(String(result))
}

fun initialize() {
	System.setProperty("com.sun.security.enableCRLDP", "true")
	System.setProperty("com.ibm.security.enableCRLDP", "true")

	// Добавляем нужные crypto провайдеры
	Security.addProvider(JCSP())
	Security.addProvider(JCP())
	Security.addProvider(RevCheck())
	Security.addProvider(Provider()) // JTLS
}

fun createSSLContext(
	pfxStorePath: String,
	clientKeyStorePassword: String,
): SSLContext {
	// Достаем сертификаты из вкладки CSP -
	// доверенные корневые центры сертификации
	val rootKeyStore = KeyStore.getInstance(JCSP.CA_STORE_NAME)
	rootKeyStore.load(null, null)

	// Достаем ключи в формате PFX с цепочкой сертификатов
	// CSP > Сертификаты > Личное > {Нужный сертификат} > Экспортировать ключи
	val clientKeyStore = KeyStore.getInstance(JCSP.PFX_STORE_NAME)
	clientKeyStore.load(FileInputStream(pfxStorePath), clientKeyStorePassword.toCharArray())

	val kmf = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG)
	kmf.init(clientKeyStore, clientKeyStorePassword.toCharArray())

	val tmf = TrustManagerFactory.getInstance(Provider.TRUSTMANGER_ALG)
	tmf.init(rootKeyStore)

	// Создаем SSL контекст с провайдером - ГОСТ TLS 1.2
	val sslContext = SSLContext.getInstance(Provider.ALGORITHM_12)
	sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

	return sslContext
}

fun createGostConnection(url: URL, sslContext: SSLContext) = (url.openConnection() as HttpsURLConnection).apply {
	// Создали соединение и теперь подменяем SSL фабрику на ГОСТ'овскую
	sslSocketFactory = sslContext.socketFactory
}
