package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import org.mapstruct.extensions.spring.AutoMap;
import org.mapstruct.extensions.spring.AutoMapField;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

@SupportedAnnotationTypes(AutomapProcessor.AUTO_MAP)
public class AutomapProcessor extends AbstractProcessor {

    public static final String AUTO_MAP = "org.mapstruct.extensions.spring.AutoMap";

    private final AutoMapMapperGenerator autoMapMapperGenerator;
    private final AutoMapSpringConfigGenerator autoMapSpringConfigGenerator;

    public AutomapProcessor() {
        this(new AutoMapSpringConfigGenerator(Clock.systemUTC()),
                new AutoMapMapperGenerator(Clock.systemUTC()));
    }

    AutomapProcessor(
            final AutoMapSpringConfigGenerator configGenerator,
            final AutoMapMapperGenerator autoMapMapperGenerator) {
        super();
        this.autoMapSpringConfigGenerator = configGenerator;
        this.autoMapMapperGenerator = autoMapMapperGenerator;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        boolean hasAutoMap = annotations.stream()
                .anyMatch(this::isAutoMapAnnotation);

        if(hasAutoMap){
            writerAutoMapSpringConfig();
        }

        annotations.stream().filter(this::isAutoMapAnnotation)
                .forEach(
                        annotation ->
                                processAutoMapAnnotation(roundEnv, annotation));;
        return false;
    }

    private boolean isAutoMapAnnotation(TypeElement annotation) {
        return AUTO_MAP.contentEquals(annotation.getQualifiedName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    private void processAutoMapAnnotation(
            final RoundEnvironment roundEnv,
            final TypeElement annotation) {

        roundEnv.getElementsAnnotatedWith(annotation)
                .stream()
                .map(c->toAutoMapMapperDescriptor(c))
                .filter(c->c!=null)
                .forEach(desc-> {
                    writeAutoMapperClassFile(desc);
                });
    }

    private List<AutoMapMapperDescriptor.AutoMapFieldDescriptor> buildFieldDescriptorList(
            TypeElement autoMapElement
    ){
        List<AutoMapMapperDescriptor.AutoMapFieldDescriptor> result = new ArrayList<>();

        if (autoMapElement.getKind() == ElementKind.CLASS) {
            for (Element enclosedElem : autoMapElement.getEnclosedElements()) {
                if (enclosedElem.getKind() == ElementKind.FIELD) {
                    AutoMapField autoFieldAnnotation = enclosedElem.getAnnotation(AutoMapField.class);
                    if (autoFieldAnnotation != null) {

                        AutoMapMapperDescriptor.AutoMapFieldDescriptor fieldDescriptor =
                                AutoMapMapperDescriptor.AutoMapFieldDescriptor.ofAutoMapField(autoFieldAnnotation);
                        fieldDescriptor.setTarget(autoFieldAnnotation.target());
                        fieldDescriptor.setSource(enclosedElem.getSimpleName().toString());
                        result.add(fieldDescriptor);
                    }
                }
            }
        }

        return result;
    }

    private AutoMapMapperDescriptor toAutoMapMapperDescriptor(
            final Element ele
    ){
        AutoMap annotation1 = ele.getAnnotation(AutoMap.class);
        if (annotation1 == null){
            return null;
        }

        ClassName source = ClassName.get((TypeElement) ele);
        ClassName target = targetTypeClassName(annotation1);
        if(target==null){
            return null;
        }

        List<ClassName> usesList = usesClassNameList(annotation1);
        AutoMapMapperDescriptor descriptor = new AutoMapMapperDescriptor();
        descriptor.setSourceClassName(source);
        descriptor.setTargetClassName(target);
        descriptor.setUsesClassNameList(usesList);
        descriptor.setMapFieldDescriptorList(buildFieldDescriptorList((TypeElement) ele));

        return descriptor;
    }

    private ClassName targetTypeClassName(AutoMap autoMapAnnotation){

        try {
            Class<?> targetClass = autoMapAnnotation.targetType();
            return ClassName.get(targetClass);
        } catch (MirroredTypeException mte) {
            TypeMirror targetClazzType = mte.getTypeMirror();
            return (ClassName)ClassName.get(targetClazzType);
        }

    }

    private List<ClassName> usesClassNameList(AutoMap autoMapAnnotation){

        try {
            Class<?>[] usesClass = autoMapAnnotation.uses();
            return Arrays.stream(usesClass)
                    .map(ClassName::get)
                    .collect(Collectors.toList());
        } catch (MirroredTypesException mte) {
            List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
            return typeMirrors.stream().map(typeMirror -> (ClassName)ClassName.get(typeMirror))
                    .collect(toList());
        }

    }

    private void writeAutoMapperClassFile(AutoMapMapperDescriptor descriptor){

        try (final Writer outputWriter =
                     processingEnv
                             .getFiler()
                             .createSourceFile(
                                     descriptor.sourcePackageName() + "."+ descriptor.mapperName())
                             .openWriter()) {
            autoMapMapperGenerator.write(descriptor, outputWriter);
        } catch (IOException e) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            ERROR,
                            "Error while opening "
                                    + descriptor.mapperName()
                                    + " output file: "
                                    + e.getMessage());
        }

    }


    private void writerAutoMapSpringConfig(){
        Filer filer = processingEnv
                .getFiler();
        try (final Writer outputWriter =
                     filer
                             .createSourceFile("org.mapstruct.extensions.spring"+ ".AutoMapSpringConfig")
                             .openWriter()) {
            autoMapSpringConfigGenerator.write("org.mapstruct.extensions.spring", outputWriter);
        } catch (IOException e) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            WARNING,
                            " while opening "
                                    + "AutoMapSpringConfig"
                                    + " output file: "
                                    + e.getMessage());
        }
    }

}
