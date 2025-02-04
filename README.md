# exceptionress.Java

The definition of the word exceptionress is: to be without exception. [exceptionress](https://exceptionress.io) provides
real-time error reporting for your Java apps. It organizes the gathered information into simple actionable data that
will help your app become exceptionress!

## Using exceptionress

Refer to the exceptionress documentation here: [exceptionress Docs](http://docs.exceptionress.io).

This project is available to install through Maven.
```
<!-- https://mvnrepository.com/artifact/com.exceptionress/exceptionress-client -->
<dependency>
    <groupId>com.exceptionress</groupId>
    <artifactId>exceptionress-client</artifactId>
    <version>0.1.0</version>
</dependency>
```
Check the latest version of the library [here](https://mvnrepository.com/artifact/com.exceptionress/exceptionress-client)

## Show me the code

```java
class ExampleApp {
    public static void main(String[] args) {
        private static final exceptionressClient client =
                exceptionressClient.from(
                        System.getenv("exceptionress_SAMPLE_APP_API_KEY"),
                        System.getenv("exceptionress_SAMPLE_APP_SERVER_URL"));

        // Submit different events using submitXXX methods
        client.submitLog("Test log");

        // Submit custom methods using our createXXX methods
        client.submitEvent(
                EventPluginContext.from(
                        client.createLog("test-log").referenceId("test-reference-id").build()));
    }
}
```

**Builder Pattern**

We love our builders!!! This project heavily utilized the use of builders instead of traditional object creation
using `new`. We do this with the help of Project Lombok's `@Builder` annotation. Read more about the
project [here](https://projectlombok.org/features/all). Read more about the
annotation [here](https://projectlombok.org/features/Builder). Read more about the builder
pattern [here](https://refactoring.guru/design-patterns/builder).

_Example: Customizing your Event Queue implementation_

```
EventQueueIF queue = //get your implementation
Configuration configuration = Configuration.builder()
        .serverUrl("http://your-server-url")
        .apiKey("your-api-key")
        .queue(queue)
        .build();
exceptionressClient client = exceptionressClient.builder().configuration(configuration).build();
```

In this library we have made sure that all the values which are not set by builders fallback to reasonable defaults. So
don't feel the pressure to supply values for all the fields. **Note:** Whenever customizing the client
using `Configuration` never forget to supply your `serverUrl` and `apiKey` using a `Configuration` object as
shown above.

## Getting Started (Development)

### Intellij
1. You will need to install:
    * [Intellij](https://www.jetbrains.com/idea/download/)
    * **Note:** In Intellij you can directly [download JDK 11 from the IDE](https://www.jetbrains.com/help/idea/sdk.html#jdk-from-ide). Intellij also comes with a bundled Maven installation and built-in Lombok support.
2. Clone the repo
3. Verify your setup using `mvn clean verify`

### VSCode
1. You will need to install:
    * [VSCode](https://code.visualstudio.com/download)
    * [JDK 11](https://www.oracle.com/ie/java/technologies/javase-downloads.html). Make sure that your `java --version` and `$JAVA_HOME` environment variable point to the correct jdk.
    * [VSCode Java extension pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
    * [VSCode Lombok extension](https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok)
2. Clone the repo
3. Verify your setup using `mvn clean verify`


## Spring Boot Users

You can observe `NoClassDefFoundError` in your Spring-boot apps because Spring-boot uses v3 of `OkHttpClient` while this
client uses v4. In that case you have to explicitly declare v4 of the library in you `pom.xml/build.gradle`.

```xml

<dependencies>
    <dependency>
        <groupId>com.exceptionress</groupId>
        <artifactId>exceptionress-client</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- EXPLICIT DECLARATION -->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.9.1</version>
    </dependency>
</dependencies>
```

## VSCode Users

We are using [Project Lombok](https://projectlombok.org/) to automatically generate a lot of code. Intellij can give you IntelliSense by default but for VSCode you may have to install [this extension](https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok).

## General Data Protection Regulation

By default the exceptionress Client will report all available metadata including potential PII data. You can fine tune
the collection of information via Data Exclusions or turning off collection completely.

Please visit
the [docs](https://exceptionress.com/docs/clients/javascript/client-configuration/#general-data-protection-regulation)
for detailed information on how to configure the client to meet your requirements.

## Support

If you need help, please contact us via in-app
support, [open an issue](https://github.com/exceptionress/exceptionress.Java/issues/new)
or [join our chat on Discord](https://discord.gg/6HxgFCx). We’re always here to help if you have any questions!

## Thanks

Thanks to all the people who have contributed!

[![contributors](https://contributors-img.web.app/image?repo=exceptionress/exceptionress.Java)](https://github.com/exceptionress/exceptionress.Java/graphs/contributors)
