package com.nusinfineon.storage;

import static com.nusinfineon.util.Directories.SAVE_FILE_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nusinfineon.core.Core;

public class JsonParser {

    private String filepath = "./" + SAVE_FILE_NAME;

    /**
     * Constructor that checks if the file exists and creates a new json formatted file if it.
     * @throws IOException in the case of a missing file
     */
    public JsonParser() throws IOException {
        File file = new File(filepath);
        if (file.createNewFile()) {
            FileWriter fileWriter = new FileWriter(filepath);
            fileWriter.write("{}");
            fileWriter.close();
        }
    }

    /**
     * Method to store data in the Json format.
     * @throws IOException in the case of a missing file
     */
    public void storeData(Core core) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File(filepath),core);
    }

    /**
     * Method to load data from the Json formatted file.
     * @return returns the core class with all the saved data loaded
     * @throws IOException in the case of missing file
     */
    public Core loadData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        Core core = mapper.readValue(new FileInputStream(filepath), Core.class);
        return core;
    }
}
