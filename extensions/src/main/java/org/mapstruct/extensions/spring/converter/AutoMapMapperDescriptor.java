package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.control.MappingControl;
import org.mapstruct.extensions.spring.AutoMapField;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AutoMapMapperDescriptor {

    public ClassName getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(ClassName sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    private ClassName sourceClassName;

    public ClassName getTargetClassName() {
        return targetClassName;
    }

    public void setTargetClassName(ClassName targetClassName) {
        this.targetClassName = targetClassName;
    }

    private ClassName targetClassName;

    private List<ClassName> usesClassNameList;

    public List<ClassName> getUsesClassNameList() {
        return usesClassNameList;
    }

    public void setUsesClassNameList(List<ClassName> usesClassNameList) {
        this.usesClassNameList = usesClassNameList;
    }

    public List<AutoMapFieldDescriptor> getMapFieldDescriptorList() {
        return mapFieldDescriptorList;
    }

    public void setMapFieldDescriptorList(List<AutoMapFieldDescriptor> mapFieldDescriptorList) {
        this.mapFieldDescriptorList = mapFieldDescriptorList;
    }

    private List<AutoMapFieldDescriptor> mapFieldDescriptorList;


    public String sourcePackageName(){
        return sourceClassName.packageName();
    }

    public String mapperName(){
        return sourceClassName.simpleName()+"To"+ targetClassName.simpleName()+"Mapper";
    }



    public static class AutoMapFieldDescriptor{
        private String source;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public String getNumberFormat() {
            return numberFormat;
        }

        public void setNumberFormat(String numberFormat) {
            this.numberFormat = numberFormat;
        }

        public String getConstant() {
            return constant;
        }

        public void setConstant(String constant) {
            this.constant = constant;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getDefaultExpression() {
            return defaultExpression;
        }

        public void setDefaultExpression(String defaultExpression) {
            this.defaultExpression = defaultExpression;
        }

        public boolean isIgnore() {
            return ignore;
        }

        public void setIgnore(boolean ignore) {
            this.ignore = ignore;
        }

        public String[] getQualifiedByName() {
            return qualifiedByName;
        }

        public void setQualifiedByName(String[] qualifiedByName) {
            this.qualifiedByName = qualifiedByName;
        }

        public String[] getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(String[] dependsOn) {
            this.dependsOn = dependsOn;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public NullValueCheckStrategy getNullValueCheckStrategy() {
            return nullValueCheckStrategy;
        }

        public void setNullValueCheckStrategy(NullValueCheckStrategy nullValueCheckStrategy) {
            this.nullValueCheckStrategy = nullValueCheckStrategy;
        }

        public NullValuePropertyMappingStrategy getNullValuePropertyMappingStrategy() {
            return nullValuePropertyMappingStrategy;
        }

        public void setNullValuePropertyMappingStrategy(NullValuePropertyMappingStrategy nullValuePropertyMappingStrategy) {
            this.nullValuePropertyMappingStrategy = nullValuePropertyMappingStrategy;
        }

        public List<ClassName> getQualifiedByClassNameList() {
            return qualifiedByClassNameList;
        }

        public void setQualifiedByClassNameList(List<ClassName> qualifiedByClassNameList) {
            this.qualifiedByClassNameList = qualifiedByClassNameList;
        }

        public TypeName getResultTypeTypeName() {
            return resultTypeTypeName;
        }

        public void setResultTypeTypeName(TypeName resultTypeTypeName) {
            this.resultTypeTypeName = resultTypeTypeName;
        }

        public ClassName getMappingControl() {
            return mappingControl;
        }

        public void setMappingControl(ClassName mappingControl) {
            this.mappingControl = mappingControl;
        }

        private String target;
        private String dateFormat;
        private String numberFormat;
        private String constant;
        private String expression;
        private String defaultExpression;
        private boolean ignore;
        private String[] qualifiedByName;
        private String[] dependsOn;
        private String defaultValue;
        private NullValueCheckStrategy nullValueCheckStrategy;
        private NullValuePropertyMappingStrategy nullValuePropertyMappingStrategy;

        private List<ClassName> qualifiedByClassNameList;
        private TypeName resultTypeTypeName;
        private ClassName mappingControl;

        public static AutoMapFieldDescriptor ofAutoMapField(AutoMapField autoMapField) {

            AutoMapFieldDescriptor descriptor = new AutoMapFieldDescriptor();
            descriptor.target = fillString(autoMapField.target());
            descriptor.dateFormat = fillString(autoMapField.dateFormat());
            descriptor.numberFormat = fillString(autoMapField.numberFormat());
            descriptor.constant = fillString(autoMapField.constant());
            descriptor.expression = fillString(autoMapField.expression());
            descriptor.defaultExpression = fillString(autoMapField.defaultExpression());
            descriptor.ignore = autoMapField.ignore();
            descriptor.qualifiedByName = autoMapField.qualifiedByName().length == 0 ? null : autoMapField.qualifiedByName();
            ;
            descriptor.dependsOn = autoMapField.dependsOn().length == 0 ? null : autoMapField.dependsOn();
            descriptor.defaultValue = fillString(autoMapField.defaultValue());

            descriptor.nullValueCheckStrategy = autoMapField.nullValueCheckStrategy() == NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
                    ? null : autoMapField.nullValueCheckStrategy();
            descriptor.nullValuePropertyMappingStrategy = autoMapField.nullValuePropertyMappingStrategy()
                    == NullValuePropertyMappingStrategy.SET_TO_NULL ? null : autoMapField.nullValuePropertyMappingStrategy();


            fillResultType(descriptor, autoMapField);
            fillQualifiedBy(descriptor, autoMapField);
            fillMappingControl(descriptor, autoMapField);
            return descriptor;
        }

        private static String fillString(String value) {
            final String defStr = "";
            return value.equals(defStr) ? null : value;
        }


        private static void fillMappingControl(AutoMapFieldDescriptor descriptor, AutoMapField autoMapField) {
            try {
                Class<?> resultType = autoMapField.mappingControl();
                if (resultType == MappingControl.class) {
                    descriptor.mappingControl = null;
                } else {
                    descriptor.mappingControl = ClassName.get(resultType);
                }

            } catch (MirroredTypeException mte) {
                TypeMirror typeMirror = mte.getTypeMirror();
                ClassName ctrlClassName = (ClassName) ClassName.get(typeMirror);
                ClassName def = ClassName.get(MappingControl.class);
                if (ctrlClassName.equals(def)) {
                    descriptor.mappingControl = null;
                } else {
                    descriptor.mappingControl = ctrlClassName;
                }


            }

        }

        private static void fillResultType(AutoMapFieldDescriptor descriptor, AutoMapField autoMapField) {
            try {
                Class<?> resultType = autoMapField.resultType();
                if (resultType.equals(void.class)) {
                    descriptor.resultTypeTypeName = null;
                }
                {
                    descriptor.resultTypeTypeName = TypeName.get(resultType);
                }

            } catch (MirroredTypeException mte) {
                TypeMirror typeMirror = mte.getTypeMirror();
                TypeName tn = ClassName.get(typeMirror);
                TypeName def = TypeName.get(void.class);
                if (tn.equals(def)) {
                    descriptor.resultTypeTypeName = null;
                } else {
                    descriptor.resultTypeTypeName = tn;
                }

            }

        }

        private static void fillQualifiedBy(AutoMapFieldDescriptor descriptor, AutoMapField autoMapField) {
            try {
                Class<?>[] resultType = autoMapField.qualifiedBy();
                if (resultType.length == 0) {
                    descriptor.qualifiedByClassNameList = null;
                } else {
                    descriptor.qualifiedByClassNameList = Arrays.stream(resultType)
                            .map(c -> ClassName.get(c))
                            .collect(Collectors.toList());
                }

            } catch (MirroredTypesException mte) {
                List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
                if (typeMirrors == null || typeMirrors.size() == 0) {
                    descriptor.qualifiedByClassNameList = null;
                } else {
                    descriptor.qualifiedByClassNameList =
                            typeMirrors.stream()
                                    .map(c -> (ClassName) ClassName.get(c))
                                    .collect(Collectors.toList());
                }
            }

        }
    }
}
