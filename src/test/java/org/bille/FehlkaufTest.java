package org.bille;

import com.opencsv.exceptions.CsvException;
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
        assertEquals(first.getUserName(), "miau123");
        assertEquals(first.getAddress(), "Mieze Katz, Miau Str. 12, 12345 Berlin");
        assertFalse(first.isMax());
        assertEquals(first.getCards(), 3);
    }

    @Test
    public void testOverview() throws IOException, CsvException {

        List<MemberData> members = readWithFehlKaufReader("members.csv");
        MemberMatcher matcher = new MemberMatcher(members);
        FehlkaufRound fehlkaufRound = matcher.match();
        assertFalse(FehlkaufFileUtils.getOverview(fehlkaufRound).isEmpty());
    }


    private List<MemberData> readWithFehlKaufReader(String testFile) throws IOException, CsvException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(testFile)).getFile());
        return FehlkaufFileUtils.readFrom(
                file.getAbsolutePath().replaceAll("%20", " "));
    }
}
