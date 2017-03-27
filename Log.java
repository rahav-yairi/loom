


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

class Appearance {

    public Appearance(String word, Date date) {
        this.word = word;
        this.date = date;
    }

    String word;
    Date date;
}

public class Log {

    static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    static HashMap<String, ArrayList<Appearance>> patterns = new HashMap<>();

    static public void readLog(String logFile) throws IOException {
        try (Stream<String> istream = Files.lines(Paths.get(logFile))) {
            istream.forEach(Log::addRecord);
        }
    }

    static private void addRecord(String record) {

        // removing unnecessary white spaces 
        record = record.replaceAll("\\s+", " ");
        ParsePosition pos = new ParsePosition(0);
        Date date = DATE_FORMAT.parse(record, pos);
        String rest = record.substring(pos.getIndex());
        String[] parts = rest.split(" ");

        for (int i = 0; i < parts.length; i++) {
            String temp = parts[i];
            parts[i] = "%s";
            String template = String.join(" ", parts);

            ArrayList<Appearance> appearances = patterns.get(template);
            if (appearances == null) {
                appearances = new ArrayList<>();
                patterns.put(template, appearances);
            }
            appearances.add(new Appearance(temp, date));
            parts[i] = temp;
        }
    }

    static public void writeGroups(String fileName) throws FileNotFoundException, IOException {

        FileOutputStream ostream = new FileOutputStream(new File(fileName), false);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ostream))) {

            for (Map.Entry<String, ArrayList<Appearance>> entry : patterns.entrySet()) {
                String template = entry.getKey();
                ArrayList<Appearance> appearances = entry.getValue();
                int size = appearances.size();

                if (size == 1) {
                    continue;
                }

                String[] changedWords = new String[size];

                for (int i = 0; i < size; i++) {
                    Appearance appearance = appearances.get(i);
                    writer.write(DATE_FORMAT.format(appearance.date) + " " + String.format(template, appearance.word));
                    writer.newLine();
                    changedWords[i] = appearance.word;
                }

                writer.write("The changing word was: " + String.join(", ", changedWords));
                writer.newLine();
                writer.newLine();
            }
        }
    }

    static public void main(String[] av) {
        try {

            readLog(av[0]);
            writeGroups(av[1]);

        } catch (IOException ex) {
            System.err.println("Exception - " + ex.getMessage());
        }
    }
}
