package org.mapstruct.extensions.spring;

import java.util.List;

public interface IObjectMapper {

     <S,D> D map(S source, Class<?> destinationCls);
     <S,D> D map(S source, D destination);

    <S,D> List<D> mapList(List<S> source, Class<?> destinationCls);

}
