package gubo.learn.javaparser;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class GlobCaller {


    PathMatcher matcher;
    public GlobCaller(String pattern) {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    public void process(File f, IJavaClassSourceProcessor processor, boolean doReplace) throws IOException {
        if (f.isFile() && f.toString().endsWith(".java")) {
            String output = processor.processFile(f);
            if (output == null) {
                System.out.println("No change: " + f.toString());
            } else {
                System.out.println(f.toString() + " changed:");
                System.out.println(output);
            }

            if (doReplace) {
                // Replace in file.
                File fout = f;
                FileOutputStream fos = new FileOutputStream(fout);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                // Affected fields and methods are changed to be indented by tab, change them back.
                // bw.write(output.replace("\t", "    "));
                bw.close();
            }
        } else if (f.isDirectory()) {

            List<Path> paths = Files.walk(Paths.get(f.toString()))
                    .filter(Files::isRegularFile)
                    .filter(p1 -> {
                        return p1.toString().endsWith(".java");
                    })
                    .collect(Collectors.toList());

            for (Path p : paths) {
                process(new File(p.toString()), processor, doReplace);
            }
        }
    }

}
