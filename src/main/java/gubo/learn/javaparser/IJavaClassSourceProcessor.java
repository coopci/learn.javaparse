package gubo.learn.javaparser;

import java.io.File;
import java.io.FileNotFoundException;

public interface IJavaClassSourceProcessor {
    public String processFile(File file) throws FileNotFoundException;
}
