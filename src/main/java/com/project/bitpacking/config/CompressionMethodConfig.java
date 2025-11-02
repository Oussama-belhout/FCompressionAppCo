package com.project.bitpacking.config;

import java.util.Objects;

/**
 * Configuration metadata for a compression method.
 * Used for dependency injection and extensibility.
 */
public class CompressionMethodConfig {
    private String name;
    private String displayName;
    private String className;
    private String description;

    public CompressionMethodConfig() {
    }

    public CompressionMethodConfig(String name, String displayName, String className, String description) {
        this.name = name;
        this.displayName = displayName;
        this.className = className;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompressionMethodConfig that = (CompressionMethodConfig) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CompressionMethodConfig{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", className='" + className + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

