package kurssisuoritustenhallinta;

import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ohjelman aloitusluokka
 * @author Patrik Soininen
 */
public class Suoritustietokanta extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        Connection conn = TietokantaYhteys.openConnection();    
        TietokantaYhteys.useDatabase(conn);
        Parent root = FXMLLoader.load(getClass().getResource("SuoritustietokantaView.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Suoritustietokanta");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
