package sample;


import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Main extends Application{
    static int monday = 0, thuesday = 0, wednesday = 0, thursday = 0, friday = 0;
    public static void main(String[] args){
        //Variablen
        int year = 2000, durationYears, fixedyear;

        ArrayList<LocalDate> freedays = new ArrayList<>();
        Scanner User = new Scanner(System.in);

        //eingabe durch den User
        System.out.println("Startjahr angeben: ");
        year = User.nextInt();
        fixedyear = year;
        System.out.println("Dauer in Jahren angeben: ");
        durationYears = User.nextInt();

        //Definierung der Feiertage
        do {
            try (Scanner scanner = new Scanner(new File("D:\\Schule\\Vierte+\\Programmieren\\Rubner\\Normal\\FreedayCalc\\src\\sample\\FreeDays.csv"))) {
                while (scanner.hasNextLine()) {
                    freedays.add(getRecordFromLine(scanner.nextLine(), year));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            year++;
        }while(year < fixedyear + durationYears);

        //überprüfung welche Tage frei sind
        for(LocalDate c:freedays){
            DayOfWeek dayS = c.getDayOfWeek();
            switch (dayS) {
                case MONDAY -> monday++;
                case TUESDAY -> thuesday++;
                case WEDNESDAY -> wednesday++;
                case THURSDAY -> thursday++;
                case FRIDAY -> friday++;

            }

        }
        monday += 2*durationYears;
        thursday += 2*durationYears;


        //Ausgabe der Daten
        writeDays(monday,thuesday,wednesday,thursday,friday);
        //JavaFX
        launch(args);


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
    public static void writeDays(int mon, int thue, int wed, int thur, int fri){
        System.out.print(mon);
        System.out.println(" mondays");
        System.out.print(thue);
        System.out.println(" thuesdays");
        System.out.print(wed);
        System.out.println(" wednesdays");
        System.out.print(thur);
        System.out.println(" thursdays");
        System.out.print(fri);
        System.out.println(" fridays");
    }
    @Override
    public void start(Stage primaryStage)throws Exception {
        try{
            // Angeben wie die Achsen sein sollen
            final NumberAxis xAxis = new NumberAxis();
            final CategoryAxis yAxis = new CategoryAxis();

            // Anlegen der BarChart und angabe wie die Anordnung
            final BarChart<String, Number> barChart = new BarChart<String, Number>(yAxis, xAxis);
            barChart.setTitle("Freie Tage im Vergleich");
            xAxis.setLabel("Tage");
            yAxis.setLabel("Wochentage");

            // Anlegen einer Serie mit den jeweiligen Werten und dem Item
            // dem sie zugeordnet werden.
            XYChart.Series<String, Number> series1 = new XYChart.Series<String,Number>();
            series1.setName("Days of week");
            series1.getData().add(new XYChart.Data("Mondays",monday));
            series1.getData().add(new XYChart.Data("tuesdays",thuesday));
            series1.getData().add(new XYChart.Data("wednesday",wednesday));
            series1.getData().add(new XYChart.Data("thursdays",thursday ));
            series1.getData().add(new XYChart.Data("fridays",friday));


            barChart.getData().add(series1);

            primaryStage.setTitle("Freie Tage im Vergleich");
            Scene scene = new Scene(barChart, 400,200);

            primaryStage.setScene(scene);
            primaryStage.setHeight(300);
            primaryStage.setWidth(400);

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();

        }
    }

}
