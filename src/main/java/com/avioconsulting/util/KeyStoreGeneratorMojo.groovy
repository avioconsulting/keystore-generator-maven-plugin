package com.avioconsulting.util

import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.*

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate

@Mojo(name = 'generateKeystore')
class KeyStoreGeneratorMojo extends AbstractMojo {
    @Parameter(required = true, defaultValue = '${keystore.generated.path}')
    private File destinationKeyStorePath
    @Parameter(required = true, defaultValue = '${keystore.properties.file.path}')
    private File keystorePasswordPropertiesFilePath
    @Parameter(required = true, defaultValue = 'listener.keystore.password')
    private String keystorePasswordPropertyName
    @Parameter(property = 'keystore.generator.force.password')
    private String forcedKeyStorePassword
    @Parameter(required = true,
            property = 'keystore.generator.crypto.key')
    private String cryptoKey

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        String usePassword = forcedKeyStorePassword
        if (!usePassword) {
            def random = new SecureRandom()
            usePassword = new BigInteger(130,
                                         random).toString(32)

        }
        def keystoreParentDir = destinationKeyStorePath.parentFile
        if (!keystoreParentDir.exists()) {
            keystoreParentDir.mkdirs()
        }
        generateKeystore(destinationKeyStorePath,
                         usePassword)
        def propsParentDir = keystorePasswordPropertiesFilePath.parentFile
        if (!propsParentDir.exists()) {
            propsParentDir.mkdirs()
        }
        def props = new PropertiesConfiguration(keystorePasswordPropertiesFilePath)
        def encryptedPassword = encryptForSecurePropertyPlaceholder(usePassword)
        props.setProperty(keystorePasswordPropertyName,
                          '![' + encryptedPassword + ']')
        props.save(keystorePasswordPropertiesFilePath.newWriter())
    }

    private static File join(File parent, String... parts) {
        def separator = System.getProperty 'file.separator'
        new File(parent,
                 parts.join(separator))
    }

    static void generateKeystore(File keystoreFile,
                                 String keystorePassword) {
        def keystore = KeyStore.getInstance('jks')
        def keystorePasswordCharArr = keystorePassword.toCharArray()
        keystore.load(null,
                      keystorePasswordCharArr)
        def keyGen = new CertAndKeyGen('RSA',
                                       'SHA1WithRSA',
                                       null)
        keyGen.generate(1024)
        CertificateExtensions certificateExtensions = getCertExtensions()
        def cert = keyGen.getSelfCertificate(new X500Name('CN=ROOT'),
                                             new Date(),
                                             // 20 years
                                             (long) 20 * 365 * 24 * 3600,
                                             certificateExtensions)
        Certificate[] chain = [cert]
        keystore.setKeyEntry('selfsigned',
                             keyGen.privateKey,
                             keystorePasswordCharArr,
                             chain)
        def stream = keystoreFile.newOutputStream()
        keystore.store(stream,
                       keystorePasswordCharArr)
        stream.close()
    }

    // Chrome no longer accepts certs without a subject name
    private static CertificateExtensions getCertExtensions() {
        def certificateExtensions = new CertificateExtensions()
        def names = new GeneralNames()
        names.add(new GeneralName(new DNSName('localhost')))
        def extension = new SubjectAlternativeNameExtension(names)
        certificateExtensions.set(SubjectAlternativeNameExtension.NAME,
                                  extension)
        certificateExtensions
    }

    private String encryptForSecurePropertyPlaceholder(String password) {
        def keyBytes = cryptoKey.bytes
        // Mule uses the key as the IV
        def ivspec = new IvParameterSpec(keyBytes)
        def secretKey = new SecretKeySpec(keyBytes,
                                          'AES')
        def cipher = Cipher.getInstance('AES/CBC/PKCS5Padding')
        cipher.init(Cipher.ENCRYPT_MODE,
                    secretKey,
                    ivspec)
        Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes("UTF-8")))
    }
}
