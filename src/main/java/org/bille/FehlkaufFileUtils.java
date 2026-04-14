package org.bille;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FehlkaufFileUtils {

    public static final String PC_FORMAT = "pc";
    public static final String READABLE = "re";
    public static final String PC_RECEIVERS = "pr";
    public static final String OVERVIEW = "ov";
    public static final String MESSAGE_TO = "ms";

    private static final String[][] NAME_RANGES = {
            {"a-f", "^[a-f].*"},
            {"g-r", "^[g-r].*"},
            {"s-z", "^[s-z].*"}
    };

    public static List<MemberData> readFrom(String fileName) throws IOException, CsvException {

        List<MemberData> members = new ArrayList<>();
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(Files.newInputStream(Paths.get(fileName)), UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build();
        List<String[]> lines = reader.readAll();
        for (String[] line : lines) {
            String name = line[0].replaceAll("\uFEFF", "");
            String address = line[1];
            String cardsString = line[2];
            boolean isMax = "max".equalsIgnoreCase(cardsString)
                    || (!"".equalsIgnoreCase(cardsString) && Integer.parseInt(cardsString) > 50); // autocorrect "fun" wishes
            int cards = !isMax && !"".equalsIgnoreCase(cardsString) ? Integer.parseInt(cardsString) : 0;
            members.add(new MemberData(name, address, isMax, cards));
        }
        return members;
    }

    public static void write(FehlkaufRound matches, File file, String method) throws IOException {

        if (method.equals(PC_FORMAT) || method.equals(PC_RECEIVERS)) {
            writeMultipleFiles(matches, file, method);
        } else {
            String content = method.equals(OVERVIEW) ? getOverview(matches)
                    : method.equals(MESSAGE_TO) ? getMessageReceivers(matches)
                    : getReadableOutput(matches);
            FileUtils.write(file, content, Charsets.UTF_8);
        }
    }

    private static void writeMultipleFiles(FehlkaufRound matches, File baseFile, String method) throws IOException {

        String baseName = baseFile.getName();
        String basePath = baseFile.getParent() != null ? baseFile.getParent() : ".";
        // Strip extension if present, e.g. "output.txt" -> "output" + ".txt"
        int dotIndex = baseName.lastIndexOf('.');
        String nameWithoutExt = dotIndex >= 0 ? baseName.substring(0, dotIndex) : baseName;
        String ext = dotIndex >= 0 ? baseName.substring(dotIndex) : "";

        for (String[] range : NAME_RANGES) {
            String rangeLabel = range[0];
            String regex = range[1];

            String content = method.equals(PC_FORMAT)
                    ? getPCMarkupFiltered(matches, regex)
                    : getReceiverListFiltered(matches, regex);

            File rangeFile = new File(basePath, nameWithoutExt + "_" + rangeLabel + ext);
            FileUtils.write(rangeFile, content, Charsets.UTF_8);
        }
    }

    private static String getPCMarkupFiltered(FehlkaufRound round, String regex) {

        TreeMap<MemberData, List<MemberData>> thisRound = round.getSenders();
        StringBuilder builder = new StringBuilder();
        for (MemberData user : thisRound.keySet()) {
            if (!user.hasNoCards() && user.getUserName().toLowerCase().matches(regex)) {
                builder.append(String.format("**%s (%d Karten)**\n[details=\"Summary\"]\n", user.getUserName(), user.getCards()));
                for (MemberData match : thisRound.get(user)) {
                    builder.append(String.format("@%s\n%s\n\n", match.getUserName(), match.getAddress()));
                }
                builder.append("[/details]\n");
            }
        }
        return builder.toString();
    }

    public static String getReadableOutput(FehlkaufRound round) {

        StringBuilder builder = new StringBuilder();
        TreeMap<MemberData, List<MemberData>> receivers = round.getReceivers();
        for (MemberData member : receivers.keySet()) {
            if (!member.hasNoCards()) {
                builder.append(String.format("%s bekommt %d Karten von: \n", member.getUserName(), member.getCards()));
                for (MemberData sender : receivers.get(member)) {
                    builder.append(String.format("%s\n", sender.getUserName()));
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public static String getOverview(FehlkaufRound round) {

        TreeMap<Integer, HashSet<String>> overview = round.getOverview();
        StringBuilder builder  = new StringBuilder();
        builder.append("Die Karten in dieser Runde:\n");
        List<Integer> sortedNumbers = overview.keySet().stream()
                .sorted((x,y) -> Integer.compare(y,x))
                .collect(Collectors.toList());
        for (Integer cards : sortedNumbers) {
            builder.append(
                    Objects.equals(cards, round.getMax()) ? String.format("\n**Maximale Kartenanzahl (%s)**\n\n", cards)
                            : String.format("\n**%s Karten**\n\n", cards));
            HashSet<String> strings = overview.get(cards);
            List<String> members = strings.stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());
            members.stream().map(member -> String.format(String.format("%%s\n"), member)).forEach(builder::append);
        }
        return builder.toString();
    }

    private static String getMessageReceivers(FehlkaufRound matches) {
        List<String> usernames = matches.getUsernames();

        return "a-f:\n" + filterAndJoin(usernames, "^[a-f].*") + "\n" +
                "g-r:\n" + filterAndJoin(usernames, "^[g-r].*") + "\n" +
                "s-z:\n" + filterAndJoin(usernames, "^[s-z].*") + "\n";
    }

    private static String filterAndJoin(List<String> usernames, String regex) {
        return usernames.stream()
                .filter(name -> name.toLowerCase().matches(regex))
                .collect(Collectors.joining(" "));
    }

    private static String getReceiverListFiltered(FehlkaufRound round, String regex) {

        StringBuilder builder = new StringBuilder();
        TreeMap<MemberData, List<MemberData>> receivers = round.getReceivers();
        for (MemberData member : receivers.keySet()) {
            if (!member.hasNoCards() && member.getUserName().toLowerCase().matches(regex)) {
                builder.append(String.format("**%s bekommt %d Karten von:**\n[details=\"Summary\"]\n", member.getUserName(), member.getCards()));
                for (MemberData sender : receivers.get(member)) {
                    builder.append(String.format("%s\n", sender.getUserName()));
                }
                builder.append("[/details]\n");
            }
        }
        return builder.toString();
    }
}