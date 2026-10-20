package site.cdc

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.testing.ApplicationTestBuilder
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

// ponytail: conteneur partagé par tout le run, jamais nettoyé — TRUNCATE dans un @BeforeTest si deux tests se marchent dessus.
internal val postgres = PostgreSQLContainer("postgres:17-alpine").apply { start() }

internal const val TEST_PROJECT = "test-project"
private const val KEY_ID = "test-key"

private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.genKeyPair()
private val publicKey = keyPair.public as RSAPublicKey

private val jwksUrl = createTempFile("jwks", ".json").apply {
    writeText("""{"keys":[{"kty":"RSA","alg":"RS256","use":"sig","kid":"$KEY_ID","n":"${publicKey.modulus.base64Url()}","e":"${publicKey.publicExponent.base64Url()}"}]}""")
}.toUri().toString()

private fun BigInteger.base64Url(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray().dropWhile { it == 0.toByte() }.toByteArray())

internal fun tokenFor(uid: String, project: String = TEST_PROJECT): String = JWT.create()
    .withKeyId(KEY_ID)
    .withIssuer("https://securetoken.google.com/$project")
    .withAudience(project)
    .withSubject(uid)
    .withExpiresAt(Instant.now().plusSeconds(3600))
    .sign(Algorithm.RSA256(publicKey, keyPair.private as RSAPrivateKey))

internal fun ApplicationTestBuilder.configureTestApp() = configure(overrides = {
    put("db.url", postgres.jdbcUrl)
    put("db.user", postgres.username)
    put("db.password", postgres.password)
    put("firebase.projectId", TEST_PROJECT)
    put("firebase.jwksUrl", jwksUrl)
})
