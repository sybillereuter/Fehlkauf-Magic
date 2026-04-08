package org.bille;

import com.formdev.flatlaf.FlatLightLaf;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Frontend extends JPanel {

    private static final String NEWLINE = "\n";

    private final JButton openButton;
    private final JButton exportButton;
    private final JTextArea log;
    private final JScrollPane logScroll;
    private final JFileChooser csvFileChooser;
    private final JFileChooser directoryChooser;
    private final JLabel statusLabel;
    private final JPanel stepCircle1;
    private final JPanel stepCircle2;
    private final JLabel stepLabel1;
    private final JLabel stepLabel2;
    private final JSeparator stepConnector;
    private final JPanel howtoBody;
    private final JLabel howtoChevron;
    private boolean howtoOpen = false;

    private FehlkaufRound matches = null;
    private String filename = "";

    private static final Color BG_PRIMARY    = new Color(255, 255, 255);
    private static final Color BG_SECONDARY  = new Color(246, 247, 248);
    private static final Color BG_INFO       = new Color(230, 241, 251);
    private static final Color BG_SUCCESS    = new Color(234, 243, 222);
    private static final Color BG_WARNING    = new Color(250, 238, 218);
    private static final Color BORDER        = new Color(200, 200, 200);
    private static final Color TEXT_PRIMARY  = new Color(30,  30,  30);
    private static final Color TEXT_MUTED    = new Color(100, 100, 100);
    private static final Color TEXT_HINT     = new Color(150, 150, 150);
    private static final Color TEXT_INFO     = new Color(12,  68, 124);
    private static final Color TEXT_SUCCESS  = new Color(39,  80,  10);
    private static final Color TEXT_WARNING  = new Color(133, 79,  11);
    private static final Color ACCENT_INFO   = new Color(55, 138, 221);
    private static final Color ACCENT_SUCCESS= new Color(99, 153,  34);

    public Frontend() {
        super(new BorderLayout());
        setBackground(BG_PRIMARY);

        File workingDir = new File(System.getProperty("user.dir"));
        csvFileChooser = new JFileChooser(workingDir);
        csvFileChooser.setFileFilter(new FileNameExtensionFilter("CSV-Dateien", "csv"));
        directoryChooser = new JFileChooser(workingDir);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // --- Schritt-Indikatoren ---
        stepCircle1  = makeStepCircle("1", true);
        stepCircle2  = makeStepCircle("2", false);
        stepLabel1   = makeStepLabel("Datei wählen", true);
        stepLabel2   = makeStepLabel("Exportieren", false);
        stepConnector = new JSeparator(JSeparator.HORIZONTAL);
        stepConnector.setForeground(BORDER);

        JPanel stepsRow = new JPanel(new GridBagLayout());
        stepsRow.setBackground(BG_PRIMARY);
        stepsRow.setBorder(new EmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        stepsRow.add(makeStepColumn(stepCircle1, stepLabel1), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        stepsRow.add(stepConnector, gc);
        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        stepsRow.add(makeStepColumn(stepCircle2, stepLabel2), gc);

        // --- Drop-Zone (klickbar) ---
        JPanel dropZone = new JPanel(new GridLayout(2, 1, 0, 4));
        dropZone.setBackground(BG_SECONDARY);
        dropZone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(150, 150, 150), 4, 4, 1, false),
                new EmptyBorder(22, 20, 22, 20)
        ));
        dropZone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel dropMain = new JLabel("CSV-Datei auswählen", SwingConstants.CENTER);
        dropMain.setFont(dropMain.getFont().deriveFont(14f));
        dropMain.setForeground(TEXT_MUTED);
        JLabel dropSub = new JLabel("Format:  Username ; Addresse ; Kartenzahl", SwingConstants.CENTER);
        dropSub.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        dropSub.setForeground(TEXT_HINT);
        dropZone.add(dropMain);
        dropZone.add(dropSub);
        dropZone.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { doOpen(); }
            public void mouseEntered(MouseEvent e) {
                dropZone.setBackground(BG_INFO);
                dropZone.repaint();
            }
            public void mouseExited(MouseEvent e) {
                dropZone.setBackground(BG_SECONDARY);
                dropZone.repaint();
            }
        });

        // --- Howto ---
        howtoChevron = new JLabel("▼");
        howtoChevron.setFont(howtoChevron.getFont().deriveFont(10f));
        howtoChevron.setForeground(TEXT_HINT);

        JLabel howtoTitle = new JLabel("  Wie erstelle ich die CSV-Datei?");
        howtoTitle.setFont(howtoTitle.getFont().deriveFont(Font.BOLD, 13f));
        howtoTitle.setForeground(TEXT_MUTED);

        JPanel howtoHeader = new JPanel(new BorderLayout());
        howtoHeader.setBackground(BG_SECONDARY);
        howtoHeader.setBorder(new EmptyBorder(9, 10, 9, 10));
        howtoHeader.add(howtoTitle, BorderLayout.WEST);
        howtoHeader.add(howtoChevron, BorderLayout.EAST);
        howtoHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        howtoHeader.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { toggleHowto(); }
        });

        howtoBody = new JPanel();
        howtoBody.setLayout(new BoxLayout(howtoBody, BoxLayout.Y_AXIS));
        howtoBody.setBackground(BG_PRIMARY);
        howtoBody.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));
        howtoBody.add(makeHowtoStep("1",
                "Öffne deine Tabelle in Excel oder LibreOffice Calc. " +
                        "Spaltenreihenfolge: Username — Addresse — Kartenzahl"));
        howtoBody.add(Box.createVerticalStrut(8));
        howtoBody.add(makeHowtoStep("2",
                "Klicke auf Datei → Speichern unter und wähle als Dateityp CSV."));
        howtoBody.add(Box.createVerticalStrut(8));
        howtoBody.add(makeHowtoStep("3", "Im Export-Dialog unbedingt einstellen:"));
        howtoBody.add(Box.createVerticalStrut(6));
        howtoBody.add(makeWarningRow());
        howtoBody.add(Box.createVerticalStrut(8));
        howtoBody.add(makeHowtoStep("4",
                "Zur Kontrolle: im Texteditor sollte ein Eintrag so aussehen:"));
        howtoBody.add(Box.createVerticalStrut(6));
        howtoBody.add(makeCodeBlock("mieo4;\"Frau Miep\nMiep-von-Miep-Str. 87\n87654 München\";3"));
        howtoBody.setVisible(false);

        JPanel howtoPanel = new JPanel(new BorderLayout());
        howtoPanel.setBackground(BG_PRIMARY);
        howtoPanel.setBorder(BorderFactory.createLineBorder(BORDER));
        howtoPanel.add(howtoHeader, BorderLayout.NORTH);
        howtoPanel.add(howtoBody,   BorderLayout.CENTER);

        // --- Log ---
        log = new JTextArea(6, 60);
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setBackground(BG_SECONDARY);
        log.setForeground(TEXT_MUTED);
        log.setBorder(new EmptyBorder(10, 12, 10, 12));
        logScroll = new JScrollPane(log);
        logScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        logScroll.setVisible(false);

        // --- Content (scrollbar) ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_PRIMARY);
        content.setBorder(new EmptyBorder(12, 20, 12, 20));
        dropZone.setAlignmentX(Component.LEFT_ALIGNMENT);
        howtoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(dropZone);
        content.add(Box.createVerticalStrut(10));
        content.add(howtoPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(logScroll);

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.setBorder(null);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);

        // --- Buttons ---
        openButton = new JButton("Öffnen…");
        openButton.setFocusPainted(false);
        openButton.addActionListener(e -> doOpen());

        exportButton = new JButton("Exportverzeichnis wählen & exportieren…");
        exportButton.setFocusPainted(false);
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> doExport());

        JPanel btnRow = new JPanel(new BorderLayout(8, 0));
        btnRow.setBackground(BG_PRIMARY);
        btnRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 20, 12, 20)
        ));
        btnRow.add(openButton,   BorderLayout.WEST);
        btnRow.add(exportButton, BorderLayout.CENTER);

        // --- Statusleiste ---
        statusLabel = new JLabel("Keine Datei geladen");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        statusLabel.setForeground(TEXT_HINT);
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusBar.setBackground(BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        statusBar.add(statusLabel);

        // --- Zusammensetzen ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_PRIMARY);
        top.add(stepsRow,      BorderLayout.NORTH);
        top.add(contentScroll, BorderLayout.CENTER);

        add(top,       BorderLayout.CENTER);
        add(btnRow,    BorderLayout.SOUTH);

        // Statusbar in eigenes Panel ganz unten
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(btnRow,    BorderLayout.NORTH);
        southPanel.add(statusBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    // --- Aktionen ---

    private void doOpen() {
        int ret = csvFileChooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            appendMuted("Öffnen abgebrochen.");
            return;
        }
        File file = csvFileChooser.getSelectedFile();
        filename = file.getName().replaceAll("\\.csv$", "");

        List<MemberData> data;
        try {
            data = FehlkaufFileUtils.readFrom(file.getAbsolutePath());
        } catch (IOException | CsvException | ArrayIndexOutOfBoundsException ex) {
            appendError("Datei konnte nicht gelesen werden:\n" + ex);
            logScroll.setVisible(true);
            packParentFrame();
            statusLabel.setText("Fehler beim Lesen");
            return;
        }

        MemberMatcher matcher = new MemberMatcher(data);
        matches = matcher.match();

        logScroll.setVisible(true);

        if (matcher.getInitialMax() > matcher.getCurrentMax() || !matches.check()) {
            appendWarning("Nicht alle gewünschten Karten konnten vergeben werden!");
        }
        appendOk("Datei eingelesen: " + file.getName());
        appendOk("Zuordnung berechnet – bereit zum Exportieren.");
        scrollToBottom();

        setStepDone(stepCircle1, stepLabel1);
        setStepActive(stepCircle2, stepLabel2);
        stepConnector.setForeground(ACCENT_INFO);

        statusLabel.setText("Geladen: " + file.getName());
        exportButton.setEnabled(true);

        packParentFrame();
    }

    private void doExport() {
        int ret = directoryChooser.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            appendMuted("Export abgebrochen.");
            scrollToBottom();
            return;
        }
        File dir = directoryChooser.getSelectedFile();
        exportFile(dir, filename + "-liste.txt",             FehlkaufFileUtils.READABLE);
        exportFile(dir, filename + "-forum.txt",             FehlkaufFileUtils.PC_FORMAT);
        exportFile(dir, filename + "-forum-empfänger.txt",   FehlkaufFileUtils.PC_RECEIVERS);
        exportFile(dir, filename + "-übersicht.txt",         FehlkaufFileUtils.OVERVIEW);
        exportFile(dir, filename + "-message-receivers.txt", FehlkaufFileUtils.MESSAGE_TO);
        scrollToBottom();

        JOptionPane.showMessageDialog(this,
                "Alle Dateien wurden exportiert!", "Fertig",
                JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void exportFile(File dir, String name, String format) {
        try {
            FehlkaufFileUtils.write(matches, new File(dir, name), format);
            appendMuted("  Geschrieben: " + name);
        } catch (IOException ex) {
            appendError("Fehler bei " + name + ": " + ex.getMessage());
        }
    }

    // --- Hilfsmethoden ---

    private void toggleHowto() {
        howtoOpen = !howtoOpen;
        howtoBody.setVisible(howtoOpen);
        howtoChevron.setText(howtoOpen ? "▲" : "▼");
        packParentFrame();
    }

    /** Lässt den JFrame seine Größe an den Inhalt anpassen. */
    private void packParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JFrame) {
            ((JFrame) w).pack();
        }
    }

    private void appendOk(String msg)      { log.append("✔ " + msg + NEWLINE); }
    private void appendWarning(String msg) { log.append("⚠ " + msg + NEWLINE); }
    private void appendError(String msg)   { log.append("✖ " + msg + NEWLINE); }
    private void appendMuted(String msg)   { log.append("  " + msg + NEWLINE); }
    private void scrollToBottom()          { log.setCaretPosition(log.getDocument().getLength()); }

    private void setStepDone(JPanel circle, JLabel label) {
        circle.setBackground(BG_SUCCESS);
        ((JLabel) circle.getComponent(0)).setText("✓");
        ((JLabel) circle.getComponent(0)).setForeground(TEXT_SUCCESS);
        label.setForeground(TEXT_SUCCESS);
    }

    private void setStepActive(JPanel circle, JLabel label) {
        circle.setBackground(BG_INFO);
        ((JLabel) circle.getComponent(0)).setForeground(TEXT_INFO);
        label.setForeground(TEXT_INFO);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
    }

    // --- Builder ---

    private JPanel makeStepCircle(String text, boolean active) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(26, 26));
        p.setMaximumSize(new Dimension(26, 26));
        p.setBackground(active ? BG_INFO : BG_PRIMARY);
        p.setBorder(BorderFactory.createLineBorder(active ? ACCENT_INFO : BORDER, active ? 2 : 1));
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(active ? TEXT_INFO : TEXT_HINT);
        p.add(lbl);
        return p;
    }

    private JLabel makeStepLabel(String text, boolean active) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 11f));
        l.setForeground(active ? TEXT_INFO : TEXT_HINT);
        return l;
    }

    private JPanel makeStepColumn(JPanel circle, JLabel label) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(BG_PRIMARY);
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(circle);
        col.add(Box.createVerticalStrut(5));
        col.add(label);
        return col;
    }

    private JPanel makeHowtoStep(String num, String text) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_PRIMARY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));

        JLabel numLbl = new JLabel(num, SwingConstants.CENTER);
        numLbl.setFont(numLbl.getFont().deriveFont(Font.BOLD, 11f));
        numLbl.setForeground(TEXT_INFO);
        numLbl.setBackground(BG_INFO);
        numLbl.setOpaque(true);
        numLbl.setPreferredSize(new Dimension(20, 20));
        numLbl.setBorder(BorderFactory.createLineBorder(ACCENT_INFO));

        JLabel textLbl = new JLabel(
                "<html><body style='width:340px;font-size:13px'>" + text + "</body></html>");
        textLbl.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        left.setBackground(BG_PRIMARY);
        left.add(numLbl);

        row.add(left,    BorderLayout.WEST);
        row.add(textLbl, BorderLayout.CENTER);
        return row;
    }

    private JPanel makeWarningRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(BG_PRIMARY);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(makePill("  Zeichensatz: UTF-8  ", BG_WARNING, TEXT_WARNING, new Color(200, 140, 50)));
        row.add(makePill("  Trennzeichen: ;  ",    BG_SECONDARY, TEXT_MUTED, BORDER));
        return row;
    }

    private JLabel makePill(String text, Color bg, Color fg, Color border) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(2, 4, 2, 4)
        ));
        return l;
    }

    private JTextArea makeCodeBlock(String text) {
        JTextArea ta = new JTextArea(text);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setForeground(TEXT_MUTED);
        ta.setBackground(BG_SECONDARY);
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return ta;
    }

    // --- Einstiegspunkt ---

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Fehlkauf Wizard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(480, 300));
        frame.add(new Frontend());
        frame.setResizable(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }
        SwingUtilities.invokeLater(Frontend::createAndShowGUI);
    }
}