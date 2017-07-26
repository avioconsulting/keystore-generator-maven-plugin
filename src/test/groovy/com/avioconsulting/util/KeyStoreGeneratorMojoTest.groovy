package com.avioconsulting.util

import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class KeyStoreGeneratorMojoTest {
    @Test
    void propertyFileNotThereYet() {
        // arrange
        def tmpDir = new File('tmp')
        if (tmpDir.exists()) {
            assert tmpDir.deleteDir()
        }
        tmpDir.mkdirs()
        def mojo = new KeyStoreGeneratorMojo()
        def keystorePath = new File(tmpDir, 'keystore.jks')
        mojo.destinationKeyStorePath = keystorePath
        def propsPath = new File(tmpDir, 'stuff.properties')
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

        // act

        // assert
        fail 'write this'
    }

    @Test
    void propertyAlreadyThere() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
