package rocks.inspectit.ocelot.core.config.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jonas Kunz
 */
public class PropertyUtils {

    /**
     * Reads the given YAML resources into a Properties Object according to Spring rules.
     *
     * @param resources the resources to load
     * @return the generated Properties object
     */
    public static Properties readYamlFiles(Resource... resources) {
        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setSingleton(false);
        properties.setResources(resources);
        return properties.getObject();
    }

    /**
     * Reads the given .properties resources into a Properties Object according to Spring rules.
     *
     * @param resources the resources to load
     * @return the generated Properties object
     */
    public static Properties readPropertyFiles(Resource... resources) throws IOException {
        PropertiesFactoryBean properties = new PropertiesFactoryBean();
        properties.setSingleton(false);
        properties.setLocations(resources);
        return properties.getObject();
    }

    public static Properties readJsonFile(Resource jsonFile) throws JsonParseException, IOException {
        try (InputStream is = jsonFile.getInputStream()) {
            return readJsonFromStream(is);
        }
    }

    public static Properties readJson(String json) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            return readJsonFromStream(is);
        }
    }

    public static Properties readYaml(String yaml) {
        ByteArrayResource resource = new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8));
        return readYamlFiles(resource);
    }

    private static Properties readJsonFromStream(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            Map<String, Object> mapFormat = mapper.readValue(is, new TypeReference<Map<String, Object>>() {
            });
            Properties result = new Properties();
            result.putAll(MapListTreeFlattener.flatten(mapFormat));
            return result;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e); //should not occur as we are mapping to plain maps
        }
    }
}


