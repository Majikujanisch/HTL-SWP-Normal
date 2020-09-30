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
        int dayi = today.getDayOfMonth();
        int monthi = today.getMonthValue();
        int yeari = today.getYear();
        //Definierung der Feiertage
        ArrayList<LocalDate> freedays = new ArrayList<>();
        do {
            try (Scanner scanner = new Scanner(new File("D:\\Schule\\Vierte+\\Programmieren\\Rubner\\Normal\\FreedayCalc\\FreeDays.csv"))) {
                while (scanner.hasNextLine()) {
                    freedays.add(getRecordFromLine(scanner.nextLine(), year));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            year++;
        }while(year < today.getYear() + 10);
        //überprüfung welche Tage frei sind
        for(int  i = 0; i < freedays.size(); i++){
            LocalDate date = freedays.get(i);
            DayOfWeek dayS = date.getDayOfWeek();
            switch (dayS) {
                case MONDAY -> monday++;
                case TUESDAY -> thuesday++;
                case WEDNESDAY -> wednesday++;
                case THURSDAY -> thursday++;
                case FRIDAY -> friday++;
                default -> System.out.println("None day");

            }


            dayi++;
            if(dayi > getDaysofMonth(monthi)){
                dayi = 1;
                monthi++;
            }
            if(monthi > 12) {
                monthi = 1;
                yeari++;
            }

        }
        System.out.println(monday);
        System.out.println(thuesday);
        System.out.println(wednesday);
        System.out.println(thursday);
        System.out.println(friday);


    }

    public static LocalDate getRecordFromLine(String line, int year) {
        LocalDate values = LocalDate.MIN;
        int month, day;
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter("#");
            while (rowScanner.hasNextInt()) {
                month = rowScanner.nextInt();
                day = rowScanner.nextInt();

                values = LocalDate.of(year, month, day);
            }
        }
        return values;
    }
    public static int getDaysofMonth(int month){

        int days = switch (month) {
            case 2 -> 28;   //Februar
            case 4, 6, 9, 11 -> 30;     //Monate mit 30 tagen
            default -> 31;      //Monate mit 31 Tagen
        };
        return days;
    }


}
