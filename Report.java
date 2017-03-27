
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

class Variant {

    public Variant(String word, Date date) {
        this.word = word;
        this.date = date;
    }

    String word;
    Date date;
}

public class Report {

    static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    static HashMap<String, ArrayList<Variant>> patterns = new HashMap<>();

    static public void readLog(String logFile) throws IOException {
        try (Stream<String> istream = Files.lines(Paths.get(logFile))) {
            istream.forEach(Report::addRecord);

        }
    }

     static private void addRecord(String record) {
        ParsePosition pos = new ParsePosition(0);
        Date date = DATE_FORMAT.parse(record, pos);
        String rest = record.substring(pos.getIndex());
        String[] parts = rest.split(" ");

        for (int i = 0; i < parts.length; i++) {
            String temp = parts[i];
            parts[i] = "%s";
            String template = String.join(" ", parts);

            ArrayList<Variant> variants = patterns.get(template);
            if (variants == null) {
                variants = new ArrayList<>();
                patterns.put(template, variants);
            }
            variants.add(new Variant(temp, date));
            parts[i] = temp;
        }
    }
    static public void writeGroups(String fileName) throws FileNotFoundException, IOException {

        FileOutputStream ostream = new FileOutputStream(new File(fileName), false);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ostream))) {

            for (Map.Entry<String, ArrayList<Variant>> entry : patterns.entrySet()) {
                String template = entry.getKey();
                ArrayList<Variant> variants = entry.getValue();
                int size = variants.size();

                if (size == 1) {
                    continue;
                }

                String[] changedWords = new String[size];

                for (int i = 0; i < size; i++) {
                    Variant variant = variants.get(i);
                    writer.write(DATE_FORMAT.format(variant.date) + " " + String.format(template, variant.word));
                    writer.newLine();
                    changedWords[i] = variant.word;
                }

                writer.write("The changing word was: " + String.join(", ", changedWords));
                writer.newLine();
                writer.newLine();
            }
        }
    }

   

    static public void main(String[] av) {
        try {

            readLog("C:/temp/log.txt");
            writeGroups("C:/temp/log.out");

        } catch (IOException ex) {
            System.err.println("Exception - " + ex.getMessage());
        }
    }
}
