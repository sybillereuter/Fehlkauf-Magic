package org.bille;

import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Frontend extends JPanel implements ActionListener {

    static private final String newline = "\n";
    final JButton openButton, saveButton;
    final JTextArea log;
    final JFileChooser csvFileChooser;
    final JFileChooser directoryChooser;

    FehlkaufRound matches = null;
    String filename = "";

    public Frontend() {
        super(new BorderLayout());

        log = new JTextArea(10,70);
        log.setMargin(new Insets(5,5,5,5));
        log.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        log.setEditable(false);
        log.setBackground(new Color(124, 146, 168));

        log.append(("Wähle eine Datei mit den Teilnehmenden aus! Die Datei " +
                    "muss eine .csv-Datei sein, mit den Spalten: Username;Addresse;Kartenzahl \n\n" +
                    "Wähle dafür csv-Export bei Excel oder LibreOffice, mit den Einstellungen: \n" +
                    "Encoding UTF-8, Zeilentrenner \";\". Wenn du die Datei mit einem Texteditor öffnest, " +
                    "sollte ein Eintag etwa so aussehen:\n\n" +
                    "mieo4;\"Frau Miep\n" +
                    "Miep-von-Miep-Str. 87\n" +
                    "87654 München\";3\n"));
        JScrollPane logScrollPane = new JScrollPane(log);

        File workingDirectory = new File(System.getProperty("user.dir"));
        csvFileChooser = new JFileChooser();
        csvFileChooser.setCurrentDirectory(workingDirectory);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        csvFileChooser.setFileFilter(filter);

        directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(workingDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        openButton = new JButton("Öffnen");
        openButton.addActionListener(this);
        saveButton = new JButton("Export...");
        saveButton.addActionListener(this);
        saveButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.setBackground(new Color(95, 117, 140));

        JPanel saveButtonPanel = new JPanel();
        saveButtonPanel.add(saveButton);
        saveButtonPanel.setBackground(new Color(95, 117, 140));

        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        add(saveButtonPanel, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == openButton) {
            int returnVal = csvFileChooser.showOpenDialog(Frontend.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = csvFileChooser.getSelectedFile();
                filename = file.getName().replaceAll("\\.csv", "");
                List<MemberData> data = null;
                try {
                    data = FehlkaufFileUtils.readFrom(file.getAbsolutePath());
                } catch (IOException | CsvException | ArrayIndexOutOfBoundsException ex) {
                    log.append("ACHTUNG: Die Eingabedatei konnte nicht gelesen werden!\n\n");
                    log.append(ex.toString());
                }
            MemberMatcher matcher = new MemberMatcher(data);
            int reduction = 0;
            matches = matcher.match();
            while (matches == null) {  // handle max!!
                assert data != null;
                matcher = new MemberMatcher(data, reduction+=1);
                matches = matcher.match();
            }
                if (matcher.getInitialMax() > matcher.getCurrentMax() || !matches.check()) {
                    log.append("ACHTUNG! Es gab Probleme bei der Zuordnung!" + newline);
                    log.append("Nicht alle gewünschten Karten konnten vergeben werden!\n\n");
                }
                log.append("Fehlkauf Wizard hat die nächste Runde berechnet!" + newline);
                log.append("Wähle das Verzeichnis, in das die Listen gespeichert werden sollen!" + newline);
                saveButton.setEnabled(true);

            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

            //Handle save button action.
        } else if (e.getSource() == saveButton) {
            int returnVal = directoryChooser.showSaveDialog(Frontend.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = directoryChooser.getSelectedFile();
                try {
                    FehlkaufFileUtils.write(matches,
                            new File(file, filename + "-liste.txt"),
                            FehlkaufFileUtils.READABLE);
                } catch (IOException ex) {
                    log.append(ex.getMessage());
                }
                File forum = new File(file, filename + "-forum.txt");
                try {
                    FehlkaufFileUtils.write(matches, forum, FehlkaufFileUtils.PC_FORMAT);
                } catch (IOException ex) {
                    log.append(ex.getMessage());
                }
                File receiverList = new File(file, filename + "-forum-empfänger.txt");
                try {
                    FehlkaufFileUtils.write(matches, receiverList, FehlkaufFileUtils.PC_RECEIVERS);
                } catch (IOException ex) {
                    log.append(ex.getMessage());
                }
                File overview = new File(file, filename + "-übersicht.txt");
                try {
                    FehlkaufFileUtils.write(matches, overview, FehlkaufFileUtils.OVERVIEW);
                } catch (IOException ex) {
                    log.append(ex.getMessage());
                }
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
            JOptionPane.showMessageDialog(null, "Fertig!");
            System.exit(0);

        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Fehlkauf Wizard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Frontend());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            createAndShowGUI();
        });
    }
}
