package tw.boris4110799.composing.library.common.util

import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * The utility of SSL.
 */
object SslUtil {
    private fun getKeyStore(
        inputStream: InputStream,
        password: String
    ): KeyStore {
        val keyStorePassword = password.toCharArray()
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        keyStore.load(inputStream, keyStorePassword)
        return keyStore
    }

    private fun getTrustManagerFactory(
        inputStream: InputStream,
        password: String
    ): TrustManagerFactory {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())

        trustManagerFactory.init(getKeyStore(inputStream, password))
        return trustManagerFactory
    }

    /**
     * Get SSL context.
     * @param inputStream The input stream of the certificate. Recommends of [java.io.FileInputStream]
     */
    fun getSslContext(
        inputStream: InputStream,
        password: String
    ): SSLContext {
        val sslContext = SSLContext.getInstance("SSL")

        sslContext.init(
            null, getTrustManagerFactory(inputStream, password).trustManagers, SecureRandom()
        )
        return sslContext
    }

    /**
     * Get trust manager.
     * @param inputStream The input stream of the certificate. Recommends of [java.io.FileInputStream]
     */
    fun getTrustManager(
        inputStream: InputStream,
        password: String
    ): X509TrustManager {
        val trustManagers = getTrustManagerFactory(inputStream, password).trustManagers

        return trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }
}
