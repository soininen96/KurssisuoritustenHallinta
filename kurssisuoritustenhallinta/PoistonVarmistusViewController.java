package kurssisuoritustenhallinta;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * ikkuna jossa varmistetaan joko kurssin, opiskelijan tai suorituksen
 * poistaminen
 * @author Patrik Soininen
 */
public class PoistonVarmistusViewController implements Initializable {

    @FXML
    private Button btnLopullinenPoista;
    @FXML
    private Button btnPeruutus;
    @FXML
    private Label lblPoistettavanNimi;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblPoistettavanNimi.setText(SuoritustietokantaViewController.getPoistettavanNimi());
    }

    /**
     * poistetaan haluttu kurssi, opiskelija tai suoritus sen perusteella 
     * mita lblPoistettavanNimessa lukee ja avaa joko SuoritusHallintaView 
     * tai KurssiSuoritusHallintaView ja sulkee taman ikkunan
     * @throws SQLException
     * @throws IOException
     */
    @FXML
    protected void poistaLopullisesti(ActionEvent event) throws SQLException, IOException {
        Connection conn = TietokantaYhteys.openConnection();
        Stage stageTama = (Stage) btnPeruutus.getScene().getWindow();
        TietokantaYhteys.useDatabase(conn);

        if (lblPoistettavanNimi.getText().contains("Kurssi")) {
            PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Kurssit WHERE nimi = ?;");
            preStmt.setString(1, SuoritustietokantaViewController.getKurssiNimi());
            preStmt.execute();
            avaaSuoritusHallinta();
        } else if (lblPoistettavanNimi.getText().contains("Opiskelija")) {
            PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Opiskelijat WHERE opiskelijaID = ?;");
            preStmt.setInt(1, SuoritustietokantaViewController.haeOpiskID());
            preStmt.execute();
            avaaSuoritusHallinta();
        } else if (lblPoistettavanNimi.getText().contains("Suoritus")) {
            PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Suoritukset WHERE opiskelijaID = ? "
                    + "AND kurssiID = ?");
            preStmt.setInt(1, KurssinSuorituksetViewController.getOpiskelijaID());
            preStmt.setInt(2, SuoritustietokantaViewController.haeKurssiID());
            preStmt.execute();
            avaaKurssiSuoritusHallinta();
        }
        stageTama.close();
    }
    
    /**
     * sulkee taman ikkunan
     */
    @FXML
    private void peruPoisto(ActionEvent event) {
        Stage stage = (Stage) btnPeruutus.getScene().getWindow();
        stage.close();
    }

    /**
     * avaa SuoritusHallintaView
     * @throws IOException
     */
    protected void avaaSuoritusHallinta() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("SuoritustietokantaView.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    /**
     * avaa KurssiSuorituksetView
     * @throws IOException
     */
    protected void avaaKurssiSuoritusHallinta() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("KurssinSuorituksetView.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(SuoritustietokantaViewController.getKurssiNimi());
        stage.show();
    }
}
