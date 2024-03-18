package org.bille;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FehlkaufTest {

    @Test
    public void testFehlkaufReader() throws IOException, CsvException {

        List<MemberData> data = readWithFehlKaufReader("members.csv");
        MemberData first = data.get(0);
        Assertions.assertEquals(first.getUserName(), "miau123");
        Assertions.assertEquals(first.getAddress(), "Mieze Katz\nMiau Str. 12\n12345 Berlin");
        Assertions.assertFalse(first.isMax());
        Assertions.assertEquals(first.getCards(), 3);
    }

    @Test
    public void run() throws IOException, CsvException {

        List<MemberData> members = readWithFehlKaufReader("Fehlkauf100.csv");
        System.out.println("read...");
        MemberMatcher matcher = new MemberMatcher(members);
        System.out.println("matcher...");
        FehlkaufRound fehlkaufRound = matcher.match();
        System.out.println("matched!");
        int reduction = 0;
        while (fehlkaufRound == null) {  // handle max!!
            matcher = new MemberMatcher(members, reduction+=1);
            fehlkaufRound = matcher.match();
        }
        System.out.printf("Diese Runde werden %d Karten verschickt!", fehlkaufRound.getTotalCards());
        FehlkaufFileUtils.write(
                fehlkaufRound,
                new File("Fehlkauf100-liste.txt"), FehlkaufFileUtils.READABLE);
        FehlkaufFileUtils.write(
                fehlkaufRound,
                new File("Fehlkauf100-forum.txt"),FehlkaufFileUtils.PC_FORMAT);
        FehlkaufFileUtils.write(fehlkaufRound,
                new File("Fehlkauf100-forum-receivers.txt"), FehlkaufFileUtils.PC_RECEIVERS);
        FehlkaufFileUtils.write(fehlkaufRound,
                new File("Fehlkauf100-übersicht.txt"), FehlkaufFileUtils.OVERVIEW);

    }

    @Test
    public void testOverview() throws IOException, CsvException {

        List<MemberData> members = readWithFehlKaufReader("members.csv");
        MemberMatcher matcher = new MemberMatcher(members);
        FehlkaufRound fehlkaufRound = matcher.match();
        int reduction = 0;
        while (fehlkaufRound == null) {  // handle max!!
            matcher = new MemberMatcher(members, reduction+=1);
            fehlkaufRound = matcher.match();
        }
        System.out.println(FehlkaufFileUtils.getOverview(fehlkaufRound));
    }


    private List<MemberData> readWithFehlKaufReader(String testFile) throws IOException, CsvException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(testFile)).getFile());
        return FehlkaufFileUtils.readFrom(
                file.getAbsolutePath().replaceAll("%20", " "));
    }
}
