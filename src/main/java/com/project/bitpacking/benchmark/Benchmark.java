package com.project.bitpacking.benchmark;

import java.util.List;
import java.util.Map;

/**
 * Represents a benchmark configuration with metadata and generator parameters.
 */
public class Benchmark {
    private String name;
    private String description;
    private String generatorClass;
    private String dataFile;
    private List<String> parameters;
    private Map<String, String> metadata;

    public Benchmark() {
    }

    public Benchmark(String name, String description, String generatorClass, String dataFile, 
                     List<String> parameters, Map<String, String> metadata) {
        this.name = name;
        this.description = description;
        this.generatorClass = generatorClass;
        this.dataFile = dataFile;
        this.parameters = parameters;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(String generatorClass) {
        this.generatorClass = generatorClass;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Benchmark{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", generatorClass='" + generatorClass + '\'' +
                ", dataFile='" + dataFile + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}


