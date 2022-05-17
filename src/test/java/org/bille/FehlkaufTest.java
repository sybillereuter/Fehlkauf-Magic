package org.bille;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testMemberMatcher() throws IOException, CsvException {

        List<MemberData> members = readWithFehlKaufReader("fehlkauf77.csv");

        MemberMatcher matcher = new MemberMatcher(members);
        FehlkaufRound fehlkaufRound = matcher.match();
        int reduction = 0;
        while (fehlkaufRound == null) {  // handle max!!
            matcher = new MemberMatcher(members, reduction+=1);
            fehlkaufRound = matcher.match();

        }

        assertFalse(fehlkaufRound.getReceivers().isEmpty());
        assertFalse(fehlkaufRound.getSenders().isEmpty());
        assertEquals(matcher.getInitialMax(), 20);
        assertEquals(matcher.getCurrentMax(), 25);
        assertTrue(fehlkaufRound.check());
    }

    private List<MemberData> readWithFehlKaufReader(String testFile) throws IOException, CsvException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(testFile)).getFile());
        return FehlkaufFileUtils.readFrom(
                file.getAbsolutePath().replaceAll("%20", " "));
    }
}
