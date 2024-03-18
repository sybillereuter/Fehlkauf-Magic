package org.bille;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FehlkaufFileUtils {

    public static final String PC_FORMAT = "pc";
    public static final String READABLE = "re";
    public static final String PC_RECEIVERS = "pr";
    public static final String OVERVIEW = "ov";

    public static List<MemberData> readFrom(String fileName) throws IOException, CsvException {

        List<MemberData> members = new ArrayList<>();
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(fileName), UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build();
        List<String[]> lines = reader.readAll();
        for (String[] line : lines) {
            String name = line[0].replaceAll("\uFEFF", "");
            String address = line[1].replaceAll(", ", "\n");
            String cardsString = line[2];
            boolean isMax = "max".equalsIgnoreCase(cardsString);
            int cards = !isMax && !"".equalsIgnoreCase(cardsString) ? Integer.parseInt(cardsString) : 0;
            members.add(new MemberData(name, address, isMax, cards));
        }
        return members;
    }

    public static void write(FehlkaufRound matches, File file, String method) throws IOException {

        FileUtils.write(file,
                method.equals(PC_FORMAT) ? getPCMarkup(matches) : 
                        method.equals(PC_RECEIVERS) ? getReceiverList(matches) :
                        method.equals(OVERVIEW) ? getOverview(matches) :
                        getReadableOutput(matches),
                Charsets.UTF_8);
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
            for (String member : members) {
                builder.append(String.format("%" +
                        "s\n", member));
            }
        }
        return builder.toString();
    }

    public static String getPCMarkup(FehlkaufRound round) {

        TreeMap<MemberData, List<MemberData>> thisRound = round.getSenders();
        StringBuilder builder = new StringBuilder();
        for (MemberData user : thisRound.keySet()) {
            if (!user.hasNoCards()) {
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

    private static String getReceiverList(FehlkaufRound round) {

        StringBuilder builder = new StringBuilder();
        TreeMap<MemberData, List<MemberData>> receivers = round.getReceivers();
        for (MemberData member : receivers.keySet()) {
            if (!member.hasNoCards()) {
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
