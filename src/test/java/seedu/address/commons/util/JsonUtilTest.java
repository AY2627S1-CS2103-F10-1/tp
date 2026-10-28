package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void fromJsonString_duplicateProperties_rejectsAtAnyDepth() {
        for (String json : List.of("{\"persons\":[],\"persons\":[]}",
                "{\"persons\":[{\"email\":\"first@example.com\",\"email\":\"second@example.com\"}]}",
                "{\"unknown\":{\"name\":1,\"name\":2}}")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, Object.class));
        }
    }

    @Test
    public void fromJsonString_trailingContent_rejectsButAllowsWhitespace() throws Exception {
        for (String suffix : List.of(" {}", " []", " true", " null", " garbage", " {")) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{}" + suffix, Object.class));
        }
        assertEquals(Map.of(), JsonUtil.fromJsonString("{} \r\n\t ", Object.class));
    }

    @Test
    public void readJsonFile_malformedValue_doesNotLogCandidateContents() throws Exception {
        String privateValue = "AUDIT_PRIVATE_VALUE";
        FileUtil.writeToFile(SERIALIZATION_FILE, "{\"persons\":[" + privateValue + "]}");
        ByteArrayOutputStream records = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(records, new SimpleFormatter());
        handler.setEncoding(StandardCharsets.UTF_8.name());
        JsonUtil.fromJsonString("{}", Object.class);
        Logger logger = LogsCenter.getLogger(JsonUtil.class);
        logger.addHandler(handler);
        try {
            assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(SERIALIZATION_FILE, Object.class));
        } finally {
            handler.flush();
            logger.removeHandler(handler);
            handler.close();
        }

        String logged = records.toString(StandardCharsets.UTF_8);
        assertTrue(logged.contains("Could not read JSON file"));
        assertFalse(logged.contains(privateValue));
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
