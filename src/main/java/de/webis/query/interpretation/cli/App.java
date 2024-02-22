package de.webis.query.interpretation.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.webis.query.interpretation.QueryInterpreter;
import de.webis.query.interpretation.datastructures.Entity;
import de.webis.query.interpretation.datastructures.Interpretation;
import de.webis.query.interpretation.datastructures.Query;
import de.webis.query.interpretation.ExplicitEntityLinker;
import de.webis.query.interpretation.strategies.AllNGrams;
import de.webis.query.segmentation.utils.NgramHelper;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author marcel.gohsen@uni-weimar.de
 */
@CommandLine.Command(name = "interpret", mixinStandardHelpOptions = true)
public class App implements Callable<Void> {
    @CommandLine.Option(names = "--input", required = true)
    private Path inputDir;

    @CommandLine.Option(names = "--output", required = true)
    private Path outputDir;

    @CommandLine.Option(names = "--cache", required = true)
    private Path cacheDir;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<ObjectNode> parseQueryFile(){
        final List<ObjectNode> queries = new LinkedList<>();
        final File file = Paths.get(inputDir.toString(), "queries.jsonl").toFile();

        try(final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = reader.readLine()) != null){
                queries.add(OBJECT_MAPPER.readValue(line, ObjectNode.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return queries;
    }

    @Override
    public Void call() throws Exception {
        NgramHelper.setNgramCountsCache(parseNetspeakCache(cacheDir));

        final ExplicitEntityLinker explicitEntityLinker = ExplicitEntityLinker.getInstance(new AllNGrams());
        final QueryInterpreter queryInterpreter = new QueryInterpreter();
        final List<ObjectNode> queries = parseQueryFile();

        for(final ObjectNode jsonNode: queries){
            final Query query = new Query(jsonNode.get("query").asText().toLowerCase());
            final Set<Interpretation> interpretations = queryInterpreter.annotate(
                    query,
                    explicitEntityLinker.annotate(query)
            );
            jsonNode.putPOJO("interpretations", interpretations);
        }

        writeOutput(queries);
        persistCache(cacheDir, NgramHelper.getNgramCountsCache());
        return null;
    }

    public void writeOutput(final List<ObjectNode> queries){
        final File file = Paths.get(outputDir.toString(), "queries.jsonl").toFile();
        System.out.println(file);

        try(final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
            for(final ObjectNode query: queries){
                bufferedWriter.write(OBJECT_MAPPER.writeValueAsString(query));
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Map<String, Long> parseNetspeakCache(Path inputDirectory) {
        Path cacheFile = findFileInDirectory(inputDirectory, "netspeak-cache.json");
        if (cacheFile == null) {
            return new LinkedHashMap<>();
        }

        try {
            Map<String, String>  ret = new ObjectMapper().readValue(cacheFile.toFile(), Map.class);
            return ret.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(), i -> Long.valueOf(i.getValue())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Path findFileInDirectory(Path directory, String fileName) {
        if (Files.exists(directory.resolve(fileName))) {
            return directory.resolve(fileName);
        }

        if (directory == null || directory.toFile() == null || directory.toFile().list() == null) {
            return null;
        }

        for (String p: directory.toFile().list()) {
            Path ret = findFileInDirectory(directory.resolve(p).toAbsolutePath(), fileName);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }

    static void persistCache(Path outputDirectory, Map<String, Long> cache) {
        Map<String, String> toPersist = cache.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(), i -> "" + i.getValue()));
        try {
            new ObjectMapper().writeValue(outputDirectory.resolve("netspeak-cache.json").toFile(), toPersist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) {
        new CommandLine(new App()).execute(args);
    }
}
