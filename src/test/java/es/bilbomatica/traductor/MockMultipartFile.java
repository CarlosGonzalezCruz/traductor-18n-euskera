package es.bilbomatica.traductor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public class MockMultipartFile implements MultipartFile {

    private String name;
    private String contents;

    public MockMultipartFile(String name, String contents) {
        this.name = name;
        this.contents = contents;
    }

    @Override
    public @NonNull String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public String getOriginalFilename() {
        return this.name;
    }

    @Override
    @Nullable
    public String getContentType() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentType'");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Unimplemented method 'getSize'");
    }

    @Override
    public @NonNull byte[] getBytes() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'getBytes'");
    }

    @Override
    public @NonNull InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.contents.getBytes());
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Unimplemented method 'transferTo'");
    }
            
}
