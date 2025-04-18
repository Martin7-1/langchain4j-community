package dev.langchain4j.community.model.zhipu.chat;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Function {

    private String name;
    private String description;
    private Parameters parameters;

    private Function(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.parameters = builder.parameters;
    }

    public static Builder builder() {
        return new Builder();
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

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Parameters parameters() {
        return this.parameters;
    }

    public static final class Builder {
        private String name;
        private String description;
        private Parameters parameters;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameters(Parameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder addParameter(String name, Map<String, JsonSchemaElement> properties) {
            this.addOptionalParameter(name, properties);
            this.parameters.getRequired().add(name);
            return this;
        }

        public Builder addOptionalParameter(String name, Map<String, JsonSchemaElement> properties) {
            if (this.parameters == null) {
                this.parameters = Parameters.builder().build();
            }

            Map<String, Object> jsonSchemaPropertiesMap = new HashMap<>();

            for (Map.Entry<String, JsonSchemaElement> entry : properties.entrySet()) {
                jsonSchemaPropertiesMap.put(entry.getKey(), entry.getValue());
            }

            this.parameters.getProperties().put(name, jsonSchemaPropertiesMap);
            return this;
        }

        public Function build() {
            return new Function(this);
        }
    }
}
