this is the Gradle I used 
```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'org.example'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    maven {
        url "https://repo.eclipse.org/content/repositories/paho-snapshots/"
    }
}
// task for creating Assignment-Worker.zip
tasks.register('worker',Zip) {
    from projectDir
    // sources main worker and base MqttCallback class
    include 'src/main/java/org/exercise_four/worker/*'
    include 'src/main/java/org/exercise_four/mqqt/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    archiveFileName = "Assignment-Worker.zip"
}
// task to creates Assignment-Coordinator.zip
tasks.register('coordinator', Zip) {
    from projectDir
    // sources main coordinator and base MqttCallback class 
    include 'src/main/java/org/exercise_four/coordinator/*'
    include 'src/main/java/org/exercise_four/mqqt/*'
    include '*.gradle'
    include 'gradlew'
    include 'gradlew.bat'
    include 'gradle/**/*'
    exclude '.gradle'
    archiveFileName = "Assignment-Coordinator.zip"
}

dependencies {
    implementation('org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0')
    implementation('org.eclipse.paho:org.eclipse.paho.android.service:1.1.1')
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}

test {
    useJUnitPlatform()
}
```