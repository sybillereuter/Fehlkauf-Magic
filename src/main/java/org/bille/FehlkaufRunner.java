package org.bille;

import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FehlkaufRunner {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Bitte Arbeitsverzeichnis und Rundennummer als Parameter angeben!");
            System.err.println("Beispiel: java -jar FehlkaufRunner.jar /pfad/zum/verzeichnis 113");
            System.exit(1);
        }

        String workingDir = args[0];
        String round = args[1];

        try {
            new FehlkaufRunner().run(workingDir, round);
        } catch (Exception e) {
            System.err.println("Fehler beim Ausführen: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run(String workingDir, String round) throws IOException, CsvException {

        Path inputPath = Paths.get(workingDir, String.format("Fehlkauf%s.csv", round));
        Path absolutePath = inputPath.toAbsolutePath();
        if (!Files.exists(inputPath)) {
            throw new IOException("Datei nicht gefunden: " + absolutePath);
        }

        System.out.println("Lese Daten von: " + absolutePath);
        List<MemberData> members = FehlkaufFileUtils.readFrom(absolutePath.toString());
        System.out.println("read...");

        MemberMatcher matcher = new MemberMatcher(members);
        System.out.println("matcher...");
        FehlkaufRound fehlkaufRound = matcher.match();
        System.out.println("matched!");
        System.out.printf("Diese Runde werden %d Karten verschickt!%n", fehlkaufRound.getTotalCards());

        writeOutputFile(workingDir, fehlkaufRound, round, "Fehlkauf%s-liste.txt", FehlkaufFileUtils.READABLE);
        writeOutputFile(workingDir, fehlkaufRound, round, "Fehlkauf%s-forum.txt", FehlkaufFileUtils.PC_FORMAT);
        writeOutputFile(workingDir, fehlkaufRound, round, "Fehlkauf%s-forum-receivers.txt", FehlkaufFileUtils.PC_RECEIVERS);
        writeOutputFile(workingDir, fehlkaufRound, round, "Fehlkauf%s-übersicht.txt", FehlkaufFileUtils.OVERVIEW);
        writeOutputFile(workingDir, fehlkaufRound, round, "Fehlkauf%s-message-receivers.txt", FehlkaufFileUtils.MESSAGE_TO);

        System.out.println("Alle Ausgabedateien wurden erstellt in: " + workingDir);
    }

    private void writeOutputFile(String workingDir, FehlkaufRound fehlkaufRound, String round, String filenamePattern, String format)
            throws IOException {
        String filename = String.format(filenamePattern, round);
        File outputFile = new File(workingDir, filename);
        FehlkaufFileUtils.write(fehlkaufRound, outputFile, format);
        System.out.println("Erstellt: " + outputFile.getAbsolutePath());
    }
}