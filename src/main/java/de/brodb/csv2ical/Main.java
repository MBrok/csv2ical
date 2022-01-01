package de.brodb.csv2ical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class Main {

	private static final String PRODID = "-//Feuerwehr Doeteberg//Dienstplan 2022//DE";

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

	private static void convert(FileInputStream in, OutputStream out) throws IOException {
		try {
			writeHeader(out, PRODID);
			writeContent(in, out);
			writeFooter(out);
		} finally {
			out.flush();
			out.close();
			in.close();
		}
	}

	private static void writeContent(FileInputStream in, OutputStream out) throws IOException {
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

	private static void writeEvent(String s, OutputStream out) throws IOException {
		String[] parts = s.split(";");
		StringBuffer buffer = new StringBuffer();
		buffer.append("BEGIN:VEVENT\n");
		buffer.append("UID:");
		buffer.append(UUID.randomUUID().toString());
		buffer.append("\n");
		buffer.append("DTSTART:");
		String dateString = parts[0].replace("-","").trim(); 
		buffer.append(dateString);
		boolean hasTime = parts[1] != null && parts[1].trim().length() > 0;
		if (hasTime) {
			buffer.append("T");
			buffer.append(parts[1].replace(":", "").trim());
			buffer.append("00");
		}
		buffer.append("\n");
		if (hasTime) {
			buffer.append("DTEND:");
			buffer.append(dateString);
			buffer.append("T");
			buffer.append("230000\n");
		}
		buffer.append("SUMMARY:");
		buffer.append(parts[2]);
		buffer.append("\n");
		buffer.append("DESCRIPTION:");
		buffer.append("Feuerwehr Döteberg: ");
		buffer.append(parts[3]);
		buffer.append(" - ");
		buffer.append(parts[2]);
		buffer.append("\n");
		buffer.append("CATEGORY:");
		buffer.append(parts[3]);
		buffer.append("\n");
		buffer.append("END:VEVENT\n");
		out.write(buffer.toString().getBytes(Charset.forName("utf-8")));
	}

	private static void writeFooter(OutputStream out) throws IOException {
		String footer = "END:VCALENDAR\n";
		out.write(footer.getBytes(Charset.forName("utf-8")));
	}

	private static void writeHeader(OutputStream out, String prodId) throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("BEGIN:VCALENDAR\n");
		buffer.append("VERSION:2.0\n");
		buffer.append("PRODID:");
		buffer.append(prodId);
		buffer.append("\n");
		buffer.append("METHOD:PUBLISH\n");
		out.write(buffer.toString().getBytes(Charset.forName("utf-8")));
	}

}
