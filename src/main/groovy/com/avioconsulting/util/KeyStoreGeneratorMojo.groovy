package com.avioconsulting.util

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.CertificateExtensions
import sun.security.x509.DNSName
import sun.security.x509.GeneralName
import sun.security.x509.GeneralNames
import sun.security.x509.SubjectAlternativeNameExtension
import sun.security.x509.X500Name

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

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        def random = new SecureRandom()
        def randomPassword = new BigInteger(130, random).toString(32)
        generateKeystore(destinationKeyStorePath, randomPassword)
        def props = new Properties()
        if (keystorePasswordPropertiesFilePath.exists()) {
            props.load(keystorePasswordPropertiesFilePath.newInputStream())
        }
        props[keystorePasswordPropertyName] = randomPassword
        props.store(keystorePasswordPropertiesFilePath.newOutputStream(), '')
    }

    private static File join(File parent, String... parts) {
        def separator = System.getProperty 'file.separator'
        new File(parent, parts.join(separator))
    }

    static void generateKeystore(File keystoreFile,
                                 String keystorePassword) {
        def keystore = KeyStore.getInstance('jks')
        def keystorePasswordCharArr = keystorePassword.toCharArray()
        keystore.load(null, keystorePasswordCharArr)
        def keyGen = new CertAndKeyGen('RSA', 'SHA1WithRSA', null)
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
        keystore.store(stream, keystorePasswordCharArr)
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
}
