package com.avioconsulting.util

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = 'generateKeystore')
class KeyStoreGeneratorMojo extends AbstractMojo {
    @Override
    void execute() throws MojoExecutionException, MojoFailureException {

    }
}
