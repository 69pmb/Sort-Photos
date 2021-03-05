package pmb.sort.photos.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.RunMyTests;
import pmb.sort.photos.TestUtils;

@RunMyTests
class PictureTest {

    @Nested
    class picture_constructor {

        private Path filePath;
        private BasicFileAttributes attr;

        @BeforeEach
        void init() throws IOException {
            filePath = TestUtils.WITH_EXIF.toPath();
        }

        private void mockFileAttributes() throws IOException {
            attr = spy(Files.readAttributes(filePath, BasicFileAttributes.class));
            doReturn(FileTime.fromMillis(1603990860000L)).when(attr).creationTime();
            doReturn(FileTime.fromMillis(1603990860000L)).when(attr).lastModifiedTime();
            doReturn(93184L).when(attr).size();
        }

        @Test
        void with_exif_datas() throws ParseException, IOException {
            mockFileAttributes();
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                File withExif = TestUtils.WITH_EXIF;
                files.when(() -> Files.readAttributes(withExif.toPath(), BasicFileAttributes.class)).thenReturn(attr);

                Picture picture = new Picture(withExif);

                assertAll(() -> TestUtils.assertDate("2020-10-29T18:01", picture.getCreation()), () -> assertEquals("jpg", picture.getExtension()),
                        () -> assertEquals(Optional.of("FinePixS1Pro"), picture.getModel()),
                        () -> TestUtils.assertDate("2020-10-29T18:01", picture.getModified()), () -> assertEquals("test1.jpg", picture.getName()),
                        () -> assertEquals(withExif.getAbsolutePath(), picture.getPath()), () -> assertEquals("91 KB", picture.getSize()),
                        () -> TestUtils.assertDate("2008-02-07T11:33", picture.getTaken().orElseThrow()));

                files.verify(() -> Files.readAttributes(withExif.toPath(), BasicFileAttributes.class));
            }
        }

        @Test
        void without_exif_datas() throws ParseException, IOException {
            mockFileAttributes();
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                File withoutExif = TestUtils.WITHOUT_EXIF;
                files.when(() -> Files.readAttributes(withoutExif.toPath(), BasicFileAttributes.class)).thenReturn(attr);

                Picture picture = new Picture(withoutExif);

                assertAll(() -> TestUtils.assertDate("2020-10-29T18:01", picture.getCreation()), () -> assertEquals("jpg", picture.getExtension()),
                        () -> assertEquals(Optional.empty(), picture.getModel()),
                        () -> TestUtils.assertDate("2020-10-29T18:01", picture.getModified()), () -> assertEquals("test2.jpg", picture.getName()),
                        () -> assertEquals(withoutExif.getAbsolutePath(), picture.getPath()), () -> assertEquals("91 KB", picture.getSize()),
                        () -> assertEquals(Optional.empty(), picture.getTaken()));

                files.verify(() -> Files.readAttributes(withoutExif.toPath(), BasicFileAttributes.class));
            }
        }

        @Test
        void fails_reading_file_attributes() {
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                File withoutExif = TestUtils.WITHOUT_EXIF;
                files.when(() -> Files.readAttributes(withoutExif.toPath(), BasicFileAttributes.class)).thenThrow(IOException.class);

                assertThrows(MinorException.class, () -> new Picture(withoutExif));

                files.verify(() -> Files.readAttributes(withoutExif.toPath(), BasicFileAttributes.class));
            }
        }

    }

    @Nested
    class pretty_print {

        @Test
        void with_exif_datas() {
            Picture picture = new Picture(TestUtils.WITH_EXIF);
            picture.setCreation(Date.from(Instant.ofEpochMilli(1603990860000L)));
            picture.setModified(Date.from(Instant.ofEpochMilli(1603990860000L)));
            String prettyPrint = picture.prettyPrint(TestUtils.BUNDLE);

            String[] actual = StringUtils.split(prettyPrint, MyConstant.NEW_LINE);
            assertEquals(6, actual.length);
            assertPretty("test1.jpg", actual[0]);
            assertPretty("29/10/2020 18:01", actual[1]);
            assertPretty("29/10/2020 18:01", actual[2]);
            assertPretty("07/02/2008 11:33", actual[3]);
            assertPretty("FinePixS1Pro", actual[4]);
            assertPretty("84 KB", actual[5]);
        }

        @Test
        void without_exif_datas() {
            Picture picture = new Picture(TestUtils.WITHOUT_EXIF);
            picture.setCreation(Date.from(Instant.ofEpochMilli(1603990860000L)));
            picture.setModified(Date.from(Instant.ofEpochMilli(1603990860000L)));

            String prettyPrint = picture.prettyPrint(TestUtils.BUNDLE);

            String[] actual = StringUtils.split(prettyPrint, MyConstant.NEW_LINE);
            assertEquals(6, actual.length);
            assertPretty("test2.jpg", actual[0]);
            assertPretty("29/10/2020 18:01", actual[1]);
            assertPretty("29/10/2020 18:01", actual[2]);
            assertPretty("Pas trouvé", actual[3]);
            assertPretty("Inconnu", actual[4]);
            assertPretty("62 KB", actual[5]);
        }

        private void assertPretty(String expected, String actual) {
            assertEquals(expected, StringUtils.substringAfter(actual, ": "));
        }

    }

}
