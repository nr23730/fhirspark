package fhirspark;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.ByteStreams;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

public class ConfigurationLoader {
    private final ObjectMapper objectMapper;
    private final StringSubstitutor stringSubstitutor;

    ConfigurationLoader() {
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
    }

    <T> T loadConfiguration(InputStream config, Class<T> cls) {
        try {
            String contents = this.stringSubstitutor.replace(new String(ByteStreams.toByteArray(config), StandardCharsets.UTF_8));

            return this.objectMapper.readValue(contents, cls);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}