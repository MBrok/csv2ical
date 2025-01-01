package de.brodb.feuerwehr.createDienstplan;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

    public class Main {

        private static final Map<String,String> categoryMap = Map.of(
                "Übungsdienst","uebung",
                "Öffentliche Veranstaltung","veranstaltung_oeff",
                "Feuerwehrinterne Veranstaltung","veranstaltung_ffw",
                "Kommandositzung","twimc",
                "AGT-Strecke","twimc"
        );

        private static final DateFormat df = new SimpleDateFormat("EEE, dd.MM.yyyy", Locale.GERMAN);
        private static final DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);


        public static void main(String[] args) throws Exception {
            if (args.length < 1) {
                throw new Exception("Mindestens eine Datei zum einlesen angeben!");
            }
            String fName = args[0];


            System.out.println("Converting " + fName);
            FileInputStream in = new FileInputStream(fName);
            OutputStream out = System.out;
            if (args.length == 2) {
                out = new FileOutputStream(new File(args[1]));
            }
            convert(in, out);
        }

        private static void convert(FileInputStream in, OutputStream out) throws IOException, URISyntaxException, ParseException {
            try {
                writeHeader(out);
                writeContent(in, out);
                writeFooter(out);
            } finally {
                out.flush();
                out.close();
                in.close();
            }
        }

        private static void writeContent(FileInputStream in, OutputStream out) throws IOException, ParseException {
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bReader = new BufferedReader(reader);
            String s = bReader.readLine();
            int count = 0;
            while (s != null) {
                count++;
                writeEvent(s, out);
                s = bReader.readLine();
            }
            System.out.println(String.format("Wrote %d events", count));
        }

        private static void writeEvent(String s, OutputStream out) throws IOException, ParseException {
            String[] parts = s.split(";");
            if(parts.length < 4){
                return;
            }
            StringBuffer buffer = new StringBuffer();
            Date date = isoFormat.parse(parts[0].trim());
            String time = parts[1];
            String text = replaceUmlauts(parts[2]);
            String category = parts[3];
            String cssClass = categoryMap.get(category);

            buffer.append("<tr class=\"tr_"+cssClass+"\">\n");
            buffer.append("<td class=\"td_"+cssClass+"\">");
            buffer.append(df.format(date));
            if(time != null && time.trim().length() > 0) {
                buffer.append("; ");
                buffer.append(time);
            }
            buffer.append("</td>\n");
            buffer.append("<td class=\"td_"+cssClass+"\" colspan=3>");
            buffer.append(text);
            buffer.append("</td>\n");
            buffer.append("</tr>\n");

            out.write(buffer.toString().getBytes(Charset.forName("utf-8")));
        }

        private static String replaceUmlauts(String part) {
            String replaced = part.replace("Ä","&Auml;");
            replaced = replaced.replace("Ü","&Uuml;");
            replaced = replaced.replace("Ö","&Ouml;");
            replaced = replaced.replace("ä","&auml;");
            replaced = replaced.replace("ü","&uuml;");
            replaced = replaced.replace("ö","&ouml;");
            replaced = replaced.replace("ß","&szlig;");
            return replaced;
        }

        private static void writeFooter(OutputStream out) throws IOException, URISyntaxException {
            copyFileToOut(out,"/dienstplanVorlage/post.txt");
        }

        private static void writeHeader(OutputStream out) throws IOException, URISyntaxException {
            copyFileToOut(out,"/dienstplanVorlage/pre.txt");
        }

        private static void copyFileToOut(OutputStream stream, String resourceName) throws URISyntaxException, IOException {
            File inFile = new File(Main.class.getResource(resourceName).toURI());
            stream.write(Files.readAllBytes(inFile.toPath()));
        }

    }


