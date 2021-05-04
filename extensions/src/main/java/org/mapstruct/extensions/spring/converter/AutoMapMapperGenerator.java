package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.*;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;

public class AutoMapMapperGenerator {

    private final Clock clock;

    public AutoMapMapperGenerator(final Clock clock) {
        this.clock = clock;
    }


    public void write(AutoMapMapperDescriptor descriptor, Writer out) {
        try {
            JavaFile.builder(
                    descriptor.sourcePackageName(),
                    createTypeSpec(descriptor))
                    .build()
                    .writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TypeSpec createTypeSpec(AutoMapMapperDescriptor descriptor) {

        ParameterizedTypeName converterName = ParameterizedTypeName.get(
                ClassName.get("org.mapstruct.extensions.spring","BaseAutoMapper"),
                descriptor.getSourceClassName(),
                descriptor.getTargetClassName());

        return TypeSpec.interfaceBuilder(descriptor.mapperName())
                .addSuperinterface(converterName)
                .addModifiers(PUBLIC)
                .addAnnotation(buildGeneratedMapperConfigAnnotationSpec(descriptor))
                .addMethod(buildMappingMethods(descriptor))
                .addMethod(buildMapTargetMethods(descriptor))
                .build();
    }

    private AnnotationSpec buildGeneratedMapperConfigAnnotationSpec(AutoMapMapperDescriptor descriptor) {
        ClassName springConfig = ClassName.get("org.mapstruct.extensions.spring",
                "AutoMapSpringConfig");

        CodeBlock config = CodeBlock.builder().add("$T.class",springConfig).build();

        List<ClassName> useClassNameList = Optional.ofNullable(descriptor.getUsesClassNameList())
                .orElse(new ArrayList<>());


        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                .addMember("config", config);


        if (useClassNameList.size()>0){
            CodeBlock.Builder codeBuilder = CodeBlock.builder()
                    .add("{");
            useClassNameList.forEach(c->{
                codeBuilder.add("$T.class",c);
            });
            CodeBlock uses =codeBuilder.add("}").build();
            builder.addMember("uses", uses);
        }


        return builder.build();
    }

    private static AnnotationSpec buildMethodMappingAnnotationSpec(AutoMapMapperDescriptor.AutoMapFieldDescriptor descriptor) {


        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapping"))
                .addMember("target", "$S", descriptor.getTarget());


        Map<String, CodeBlock> map = mappingAnnotationMap(descriptor);
        for (Map.Entry<String, CodeBlock> item :
                map.entrySet()) {
            builder.addMember(item.getKey(), item.getValue());
        }

        return builder.build();
    }


    private static Map<String, CodeBlock> mappingAnnotationMap(AutoMapMapperDescriptor.AutoMapFieldDescriptor descriptor) {

        Map<String, CodeBlock> result = new HashMap<>();
        result.put("source", buildCodeBlock(descriptor.getSource()));
        result.put("dateFormat", buildCodeBlock(descriptor.getDateFormat()));
        result.put("numberFormat", buildCodeBlock(descriptor.getNumberFormat()));
        result.put("constant", buildCodeBlock(descriptor.getConstant()));
        result.put("expression", buildCodeBlock(descriptor.getExpression()));
        result.put("defaultExpression", buildCodeBlock(descriptor.getDefaultExpression()));
        result.put("ignore", CodeBlock.builder().add("$L", descriptor.isIgnore()).build());
        result.put("qualifiedBy", descriptor.getQualifiedByClassNameList() == null ? null : CodeBlock.builder().add("$L",
                descriptor.getQualifiedByClassNameList().stream()
                        .map(type -> CodeBlock.of("$T.class", type))
                        .collect(CodeBlock.joining(",", "{", "}"))
        ).build());
        result.put("qualifiedByName", descriptor.getQualifiedByName() == null ? null : CodeBlock.builder().add("$L",
                Arrays.stream(descriptor.getQualifiedByName())
                        .map(type -> CodeBlock.of("$T.$L", String.class, type))
                        .collect(CodeBlock.joining(",", "{", "}"))
        ).build());
        result.put("resultType",
                descriptor.getResultTypeTypeName() == null ? null : CodeBlock.builder()
                        .add("$T.class", descriptor.getResultTypeTypeName())
                        .build()
        );
        result.put("dependsOn", descriptor.getDependsOn() == null ? null : CodeBlock.builder().add("$L",
                Arrays.stream(descriptor.getDependsOn())
                        .map(type -> CodeBlock.of("$T.$L", String.class, type))
                        .collect(CodeBlock.joining(",", "{", "}"))
        ).build());

        result.put("defaultValue", buildCodeBlock(descriptor.getDefaultValue()));

        result.put("nullValueCheckStrategy",
                descriptor.getNullValueCheckStrategy() == null ? null :
                        CodeBlock.builder()
                                .add("$T.$L", NullValueCheckStrategy.class, descriptor.getNullValueCheckStrategy())
                                .build()
        );
        result.put("nullValuePropertyMappingStrategy",
                descriptor.getNullValuePropertyMappingStrategy() == null ? null :
                        CodeBlock.builder()
                                .add("$T.$L", NullValuePropertyMappingStrategy.class, descriptor.getNullValuePropertyMappingStrategy())
                                .build());
        result.put("mappingControl", descriptor.getMappingControl() == null ? null : CodeBlock.builder()
                .add("$T.class", descriptor.getMappingControl())
                .build());

        if (descriptor.getExpression() != null) {
            result.remove("source");
        }

        List<String> valueIsNullKey = result.entrySet().stream().filter(c -> c.getValue() == null).map(c -> c.getKey()).collect(Collectors.toList());
        valueIsNullKey.forEach(c -> result.remove(c));

        return result;
    }

    private static CodeBlock buildCodeBlock(String str) {
        return str == null ? null : CodeBlock.builder().add("$S", str).build();
    }

    private static List<AnnotationSpec> buildMethodMappingAnnotations(AutoMapMapperDescriptor descriptor){
        List<AutoMapMapperDescriptor.AutoMapFieldDescriptor>
                descriptors = Optional.ofNullable(descriptor.getMapFieldDescriptorList())
                .orElse(new ArrayList<>());

        return descriptors.stream().map(c->buildMethodMappingAnnotationSpec(c)).collect(Collectors.toList());
    }

    private static MethodSpec buildMappingMethods(
            AutoMapMapperDescriptor descriptor
    ) {
        ClassName source = descriptor.getSourceClassName();
        ClassName target = descriptor.getTargetClassName();

        ParameterSpec sourceParameterSpec =
                ParameterSpec.builder(source,"source", FINAL).build();

        return MethodSpec.methodBuilder(
                "map")
                .addAnnotation(Override.class)
                .addAnnotations(buildMethodMappingAnnotations(descriptor))
                .addParameter(sourceParameterSpec)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(target)
                .build();
    }


    private static MethodSpec buildMapTargetMethods(
            AutoMapMapperDescriptor descriptor
    ) {
        ClassName source = descriptor.getSourceClassName();
        ClassName target = descriptor.getTargetClassName();

        ParameterSpec sourceParameterSpec =
                ParameterSpec.builder(source,"source", FINAL).build();

        ParameterSpec targetParameterSpec =
                ParameterSpec.builder(target,"target", FINAL)
                        .addAnnotation(MappingTarget.class)
                        .build();

        return MethodSpec.methodBuilder(
                "mapTarget")
                .addAnnotation(Override.class)
                .addAnnotations(buildMethodMappingAnnotations(descriptor))
                .addParameter(sourceParameterSpec)
                .addParameter(targetParameterSpec)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(target)
                .build();
    }


}
