## the graddle setup
```groovy
plugins {
    id 'java'
    id 'application'
}


group = 'org.tinsae'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    maven {
        url "https://repo.eclipse.org/content/repositories/paho-snapshots/"
    }
}

//Creates a Mqtt.zip under build/distributions
tasks.register('controller', Zip) {
    from projectDir
    // include project specific dependencies
    include 'src/main/java/org/tinsae/controller/*'
    include 'src/main/java/org/tinsae/callback/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    // give the file name here
    archiveFileName = "Assignment-Controller.zip"
}

//Creates a Mqtt.zip under build/distributions
tasks.register('sensor', Zip) {
    from projectDir
    // include project specific dependencies
    include 'src/main/java/org/tinsae/feuchtigkeit/*'
    include 'src/main/java/org/tinsae/callback/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    // give the file name here
    archiveFileName = "Assignment-Feuchtigkeitssensor.zip"
}
//Creates a Mqtt.zip under build/distributions
tasks.register('sprinkler', Zip) {
    from projectDir
    // include project specific dependencies
    include 'src/main/java/org/tinsae/sprinkler/*'
    include 'src/main/java/org/tinsae/callback/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    // give the file name here
    archiveFileName = "Assignment-Bewässerungssystem.zip"
}
//Creates a Mqtt.zip under build/distributions
tasks.register('sirene', Zip) {
    from projectDir
    // include project specific dependencies
    include 'src/main/java/org/tinsae/sirene/*'
    include 'src/main/java/org/tinsae/callback/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    // give the file name here
    archiveFileName = "Assignment-Datenrecorder.zip"
}


dependencies {
    // mqtt dependencies
    implementation('org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0')
    implementation('org.eclipse.paho:org.eclipse.paho.android.service:1.1.1')
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}

test {
    useJUnitPlatform()
}
```

