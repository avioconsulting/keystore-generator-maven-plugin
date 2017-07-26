package com.avioconsulting.util

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = 'generateKeystore')
class KeyStoreGeneratorMojo extends AbstractMojo {
    @Parameter(required = true, defaultValue = '${keystore.generated.path}')
    private String destinationKeyStorePath
    @Parameter(required = true, defaultValue = '${keystore.properties.file.path}')
    private File keystorePasswordPropertiesFilePath
    @Parameter(required = true, defaultValue = 'listener.keystore.password')
    private String keystorePasswordPropertyName

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {

    }
}
