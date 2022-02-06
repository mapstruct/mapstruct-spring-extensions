package org.mapstruct.extensions.spring.example.arrays;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

@Mapper(config = SpringMapperConfig.class)
public interface BlobToByteArrayMapper extends Converter<Blob, byte[]> {
    @Override
    default byte[] convert(Blob blob) {
        try (BufferedInputStream bis = new BufferedInputStream(blob.getBinaryStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(baos)) {
            byte[] buffer = new byte[4096];
            boolean keepReading;
            do {
                int bytesRead = bis.read(buffer);
                keepReading = bytesRead != -1;
                if(keepReading) {
                    bos.write(buffer, 0, bytesRead);
                }
            } while (keepReading);

            return baos.toByteArray();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
