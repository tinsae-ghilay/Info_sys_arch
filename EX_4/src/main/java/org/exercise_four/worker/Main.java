package org.exercise_four.worker;

import org.exercise_four.coordinator.Coordinator;
import org.exercise_four.mqqt.MyMqttCallBack;
import org.exercise_four.worker.Worker;

/*
Grob umrissen könnte ein mögliches Kommunikationsmuster so ausschauen:

    Als ersters wird der Coordinator-Prozess gestartet, der sich mit dem MQTT Broker verbindet und die entsprechenden Topics abonniert.

    Die Worker melden sich beim Coordinator an, indem sie eine entsprechende MQTT Nachricht an den Coordinator schicken.

    Der Coordinator teilt jedem Worker in einer MQTT Nachricht mit, wieviele Darts er verschiessen darf.

    Nachdem ein Worker die virtuellen Darts verschossen hat, teilt der das Ergebnis dem Coordinator über MQTT mit und fragt nach einer weiteren Zuteilung von Darts.

    Der Coordinator teilt dem Worker weitere virtuelle Darts zu oder teilt ihm mit, dass es keine weiteren Darts gibt und er sich beenden kann.

 */
public class Main {
    public static void main(String[] args) {

        Worker worker = new Worker("worker");
        worker.connect();
        worker.init();
        worker.disconnect();
    }
}