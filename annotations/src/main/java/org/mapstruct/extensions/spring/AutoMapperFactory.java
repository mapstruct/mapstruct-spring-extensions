package org.mapstruct.extensions.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class AutoMapperFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ConcurrentHashMap<String, BaseAutoMapper<?,?>> beanMap = new ConcurrentHashMap<>();

    public <S, D> BaseAutoMapper<S, D> find(Class<?> source, Class<?> destinationCls) {
        String mapKey = key(source,destinationCls);

        if (beanMap.containsKey(mapKey)){
            return (BaseAutoMapper<S, D>) beanMap.get(mapKey);
        }

        BaseAutoMapper<?,?> mapper = findMapper(source, destinationCls);
        if (mapper!=null){
            beanMap.put(mapKey, mapper);
            return (BaseAutoMapper<S, D>)mapper;
        }

        return  null;
    }

    private BaseAutoMapper<?,?> findMapper(Class<?> source, Class<?> destinationCls){
        try {
            ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                    BaseAutoMapper.class, source, destinationCls);
            String[] beanNamesForType = applicationContext.getBeanNamesForType(resolvableType);
            return (BaseAutoMapper<?,?>)applicationContext.getBean(beanNamesForType[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private String key(Class<?> cls1, Class<?> cls2){
        String sourceName = cls1.getName();
        String targetName = cls2.getName();
        return sourceName+"_"+targetName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
