package com.avioconsulting.util

import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class KeyStoreGeneratorMojoTest {
    private File tmpDir

    @Before
    void setup() {
        tmpDir = new File('tmp')
        if (this.tmpDir.exists()) {
            def tries = 0
            while (!this.tmpDir.deleteDir()) {
                tries++
                if (tries > 20) {
                    throw new Exception('ran out of tries!')
                }
                System.gc()
                Thread.yield()
            }
        }
        this.tmpDir.mkdirs()
    }

    @Test
    void propertyFileNotThereYet() {
        // arrange
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(this.tmpDir,
                                    'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsPath = new File(this.tmpDir,
                                 'stuff.properties')
        mojo.keystorePasswordPropertiesFilePath = propsPath
        mojo.keystorePasswordPropertyName = 'listener.keystore.password'

        // act
        mojo.execute()

        // assert
        assert propsPath.exists()
        def props = new Properties()
        props.load(propsPath.newInputStream())
        assertThat props['listener.keystore.password'],
                   is(notNullValue())
        def password = props['listener.keystore.password']
        assert keystorePath.exists()
        def text = "keytool -list -keystore ${keystorePath.absolutePath} -storepass ${password}".execute().text
        assertThat text,
                   is(not(containsString('Keystore was tampered with, or password was incorrect')))
        assertThat text,
                   is(containsString('Your keystore contains 1 entry'))
    }

    @Test
    void forcePassword() {
        // arrange
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(this.tmpDir,
                                    'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsPath = new File(this.tmpDir,
                                 'stuff.properties')
        mojo.keystorePasswordPropertiesFilePath = propsPath
        mojo.keystorePasswordPropertyName = 'listener.keystore.password'
        def password = mojo.forcedKeyStorePassword = 'forceIt'

        // act
        mojo.execute()

        // assert
        assert propsPath.exists()
        def props = new Properties()
        props.load(propsPath.newInputStream())
        assertThat props['listener.keystore.password'],
                   is(equalTo(password))
        assert keystorePath.exists()
        def text = "keytool -list -keystore ${keystorePath.absolutePath} -storepass ${password}".execute().text
        assertThat text,
                   is(not(containsString('Keystore was tampered with, or password was incorrect')))
        assertThat text,
                   is(containsString('Your keystore contains 1 entry'))
    }

    @Test
    void directoryNotExist() {
        // arrange
        def intermediate = new File(this.tmpDir,
                                    'somedir')
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(intermediate,
                                    'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsDir = new File(this.tmpDir,
                                'otherdir')
        def propsPath = new File(propsDir,
                                 'stuff.properties')
        mojo.keystorePasswordPropertiesFilePath = propsPath
        mojo.keystorePasswordPropertyName = 'listener.keystore.password'

        // act
        mojo.execute()

        // assert
        assert propsPath.exists()
        def props = new Properties()
        props.load(propsPath.newInputStream())
        assertThat props['listener.keystore.password'],
                   is(notNullValue())
        def password = props['listener.keystore.password']
        assert keystorePath.exists()
        def text = "keytool -list -keystore ${keystorePath.absolutePath} -storepass ${password}".execute().text
        assertThat text,
                   is(not(containsString('Keystore was tampered with, or password was incorrect')))
        assertThat text,
                   is(containsString('Your keystore contains 1 entry'))
    }

    @Test
    void propertyNotThereYet() {
        // arrange
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(this.tmpDir,
                                    'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsPath = new File(this.tmpDir,
                                 'stuff.properties')
        mojo.keystorePasswordPropertiesFilePath = propsPath
        mojo.keystorePasswordPropertyName = 'listener.keystore.password'
        def existingProps = new Properties()
        existingProps['otherSetting'] = '123'
        existingProps.store(propsPath.newOutputStream(),
                            '')
        propsPath.text = '# existing comments\n' + propsPath.text

        // act
        mojo.execute()

        // assert
        assert propsPath.exists()
        def props = new Properties()
        props.load(propsPath.newInputStream())
        assertThat props['listener.keystore.password'],
                   is(notNullValue())
        def password = props['listener.keystore.password']
        assertThat props['otherSetting'],
                   is(equalTo('123'))
        assert keystorePath.exists()
        def text = "keytool -list -keystore ${keystorePath.absolutePath} -storepass ${password}".execute().text
        assertThat text,
                   is(not(containsString('Keystore was tampered with, or password was incorrect')))
        assertThat text,
                   is(containsString('Your keystore contains 1 entry'))
        assertThat propsPath.text,
                   is(containsString('# existing comments'))
    }

    @Test
    void alreadyThere_overwrite() {
        // arrange
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(this.tmpDir,
                                    'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsPath = new File(this.tmpDir,
                                 'stuff.properties')
        mojo.keystorePasswordPropertiesFilePath = propsPath
        mojo.keystorePasswordPropertyName = 'listener.keystore.password'
        def existingProps = new Properties()
        existingProps['listener.keystore.password'] = 'foobar'
        existingProps.store(propsPath.newOutputStream(),
                            '')
        keystorePath.text = 'existing'

        // act
        mojo.execute()

        // assert
        assert propsPath.exists()
        def props = new Properties()
        props.load(propsPath.newInputStream())
        assertThat props['listener.keystore.password'],
                   is(notNullValue())
        def password = props['listener.keystore.password']
        assertThat password,
                   is(not(equalTo('foobar')))
        assert keystorePath.exists()
        def text = "keytool -list -keystore ${keystorePath.absolutePath} -storepass ${password}".execute().text
        assertThat text,
                   is(not(containsString('Keystore was tampered with, or password was incorrect')))
        assertThat text,
                   is(containsString('Your keystore contains 1 entry'))
    }
}
