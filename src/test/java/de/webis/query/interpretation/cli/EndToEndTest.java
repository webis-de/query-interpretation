package de.webis.query.interpretation.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.webis.query.interpretation.datastructures.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
public class EndToEndTest {
    @Test
    public void testCLI(){
        try {
            runQueryInterpretation("src/test/resources/cw09-input", "src/test/resources/cw09-cache");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void runQueryInterpretation(final String input, final String cache) throws IOException {
        final String tmpDir = Files.createTempDirectory("tmp").toFile().getAbsolutePath();

        App.main("--input", input,
                "--output", tmpDir,
                "--cache", cache);

        final Path outFile = Paths.get(tmpDir, "queries.jsonl");
        final ObjectMapper objectMapper = new ObjectMapper();

        for(final String line: Files.readAllLines(outFile)){
            final ObjectNode query = objectMapper.readValue(line, ObjectNode.class);

            Assert.assertTrue(query.has("interpretations"));

//            if(query.get("query").asText().equals("president of the united states")){
//                boolean found = false;
//                final Entity desired = new Entity(0, 30, "president of the united states", "https://en.wikipedia.org/wiki/President_of_the_United_States");
//
//                for(final JsonNode entity: query.get("entities")){
//                    final Entity entityObj = objectMapper.readValue(entity.toString(), Entity.class);
//
//                    if(entityObj.equals(desired)){
//                        found = true;
//                    }
//                }
//
//                Assert.assertTrue(found);
//            }
        }
    }

}
