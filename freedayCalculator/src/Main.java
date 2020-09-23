import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int monday = 0, thuesday = 0, wednesday = 0, thursday = 0, friday = 0;
        //Definierung der Feiertage
        List<List<String>> freedays = new ArrayList<>();
        do {
            try (Scanner scanner = new Scanner(new File("Freedays.csv"));) {
                while (scanner.hasNextLine()) {
                    freedays.add(getRecordFromLine(scanner.nextLine(), year));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            year++;
        }while(year < today.getYear() + 10);

        //überprüfung welche Tage frei sind
        for(int i = 0; i < 365 * 10; i++){
            //index daten
            int dayi = today.getDayOfMonth();
            int monthi = today.getMonthValue();
            int yeari = today.getYear();
            LocalDate queryDate = LocalDate.of(yeari,monthi,dayi);
            for(int x = 0; x< freedays.size();x++){
                if(freedays.equals(queryDate)){
                    DayOfWeek dayS = queryDate.getDayOfWeek();
                    switch(dayS){
                        case MONDAY -> monday++;
                        case TUESDAY -> thuesday++;
                        case WEDNESDAY -> wednesday++;
                        case THURSDAY -> thursday++;
                        case FRIDAY -> friday++;
                    }

                }
                dayi++;
                if(dayi > 31){
                    dayi = 1;
                    monthi++;
                }
                if(monthi > 12) {
                    monthi = 1;
                    yeari++;
                }
            }

        }
        System.out.println(monday);
        System.out.println(thuesday);
        System.out.println(wednesday);
        System.out.println(thursday);
        System.out.println(friday);


    }

    private static List<String> getRecordFromLine(String line, int year) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(year + rowScanner.next());
            }
        }
        return values;
    }


}
