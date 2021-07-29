# Keystore Generator Maven Plugin
This maven plugin helps to generate a Self-Signed SSL keystore for using with HTTPS Listener configurations. 
It does follow steps when executed - 
1. Create a Self-Signed SSL Keystore using provided password or auto-generated one.
2. Encrypt the Password with the provided encryption key and write it to properties file.

## Usage

**Step0:** Configure AVIO Github Package Registry

In your POM, add following plugin repository in `pluginRepositories` tag (add if doesn't exist) -

```xml
    <pluginRepository>
        <id>github-avio-pkg</id>
        <name>AVIO Github Package Repository</name>
        <url>https://maven.pkg.github.com/avioconsulting/public-packages/</url>
        <layout>default</layout>
    </pluginRepository>
```

In your `~/.m2/settings.xml`, add credentials for server id `github-avio-pkg`, like below -
```xml
    <server>
        <id>github</id>
        <username>YOUR_GIT_USER</username>
        <password>YOUR_GIT_PERSONAL_TOKEN</password>
    </server>
```
See [working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token) for more details on Github Package Authentication.

**Step1:** To use this plugin, add following entry to maven pom.xml -
```xml
<plugin>
    <groupId>com.avioconsulting.util</groupId>
    <artifactId>keystore-generator-maven-plugin</artifactId>
    <version>1.0.2</version>
    <executions>
        <execution>
            <id>generate-keystore</id>
            <goals>
                <goal>generateKeystore</goal>
            </goals>
            <configuration>
                <destinationKeyStorePath>src/main/resources/keystores/listener_keystore_${env}.jks</destinationKeyStorePath>
                <keystorePasswordPropertiesFilePath>src/main/resources/${env}.api.properties</keystorePasswordPropertiesFilePath>
            </configuration>
        </execution>
    </executions>
</plugin>
```
NOTE: `keystorePasswordPropertiesFilePath` must be a valid property file path. 

**Step2:** Run following command for each target environment -

``` 
mvn keystore-generator:generateKeystore@generate-keystore \
 -Denv=dev \
 -Dkeystore.generator.crypto.key={Secret key used for secure properties}
```
NOTE: `keystore.generator.force.password` defaults to `generatedAutomatically`. 
If you need to provide your keystore password, you can add `-Dkeystore.generator.force.password={your password}`

NOTE: crypto key must be minimum 16 characters long.

**Step3:** Verify keystore is generated in `src/main/resources/keystores` and relevant property file is updated to 
insert encrypted keystore password.


When using it in Mule CloudHub targetted applications,
it can create keystores required for deploying HTTPS apps to CloudHub. 
Then, you can configure HTTP listener config like below -

```xml
<http:listener-config name="cloudhub-https-listener"
						  doc:name="CloudHub Worker Listener">
    <http:listener-connection host="0.0.0.0"
                              protocol="HTTPS"
                              port="${https.port}">
        <tls:context>
            <tls:key-store type="jks"
                           path="keystores/listener_keystore_${env}.jks"
                           alias="selfsigned"
                           keyPassword="${secure::listener.keystore.password}"
                           password="${secure::listener.keystore.password}"/>
        </tls:context>
    </http:listener-connection>
</http:listener-config>
```