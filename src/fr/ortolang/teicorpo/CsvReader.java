package fr.ortolang.teicorpo;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CsvReader {
    public static void main(String[] args) {
        try {
            CSVReader reader = null;
                reader = new CSVReader(new FileReader(args[0]));
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                System.out.println(nextLine.length + " " + nextLine[0] + nextLine[1] + "etc...");
            }
        } catch (FileNotFoundException e) {
            System.out.printf("File %s not found. Stop.", args[0]);
        } catch (IOException e) {
            System.out.printf("Error reading file %s. Stop. Error is: //%s//%n", args[0], e.toString());
        } catch (CsvValidationException e) {
            System.out.printf("Csv validation error in file %s. Stop. Error is: //%s//%n", args[0], e.toString());
//            e.printStackTrace();
        }
    }

    public static List<String[]> load(String filename, char delimiter) {
        CSVReader reader = null;
        try {
            if (delimiter != 0) {
                CSVParser cpb = new CSVParserBuilder().withSeparator(delimiter).build();
                // reader = new CSVReader(new FileReader(filename), 0, cpb);
                reader = new CSVReaderBuilder(new FileReader(filename)).withCSVParser(cpb).build();
            }
            else {
                reader = new CSVReader(new FileReader(filename));
            }
            List<String[]> myEntries = reader.readAll();
            return myEntries;
        } catch (FileNotFoundException e) {
            System.out.printf("File %s not found. Stop.", filename);
        } catch (IOException e) {
            System.out.printf("Error reading file %s. Stop. Error is: //%s//%n", filename, e.toString());
        } catch (CsvValidationException e) {
            System.out.printf("Csv validation error in file %s. Stop. Error is: //%s//%n", filename, e.toString());
//            e.printStackTrace();
        } catch (CsvException e) {
            System.out.printf("Csv error in file %s. Stop. Error is: //%s//%n", filename, e.toString());
//           e.printStackTrace();
        }
        return null;
    }

    CsvReader() {

    }
}
