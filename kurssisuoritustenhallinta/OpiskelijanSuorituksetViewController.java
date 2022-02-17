package kurssisuoritustenhallinta;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 * ikkuna jossa naytetaan yksittaisen opiskelijan suoritukset
 * @author Patrik Soininen
 */
public class OpiskelijanSuorituksetViewController implements Initializable {
    /*
    *Tämä luokka on ainoastaan yksittäisen opiskelijan suoritusten tarkastelua varten
    *tässä ei ole mahdollista muokata tai lisätä suorituksia
    */
    @FXML
    private ComboBox<String> cboSuoritukset;
    @FXML
    private DatePicker dteSuoritusPvm;
    @FXML
    private Label lblArvosana;
    @FXML
    private Label lblOpintoPisteet;
    @FXML
    private Label lblKurssiID;
    @FXML
    private TextField txtKurssiNimi;

    private int kurssiID;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection conn = TietokantaYhteys.openConnection();
            //Haetaan kaikki kurssit joissa tällä opiskelijalla on suoritus
            cboSuoritukset.getItems().addAll(haeOpiskelijanKurssit(conn));
            //Jos suorituksia on olemassa asetetaan comboxiin esille listan ensimmäinen kurssi
            if (cboSuoritukset.getItems().size() > 0) {
                cboSuoritukset.setValue(cboSuoritukset.getItems().get(0));
                //ja haetaan sen opiskelijan tiedot ja laitetaan paikoilleen
                haeKurssiTiedot(conn);
            }
        } catch (SQLException ex) {
            Logger.getLogger(KurssinSuorituksetViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * hakee valitun kurssin ID numeron sen nimen perusteella
     * @param c connection
     * @return kurssin ID numero
     * @throws SQLException
     */
    protected int getKurssiID(Connection c) throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT kurssiID FROM Kurssit WHERE nimi = '" + cboSuoritukset.getValue() + "';"
        );
        while (rs.next()) {
            kurssiID = rs.getInt("kurssiID");
        }
        return kurssiID;
    }
    
    /**
     * hakee kaikkien niiden kurssien nimet joilla talla opiskelijalla 
     * on suoritus
     * @param c connection
     * @return lista jossa kurssien nimet
     * @throws SQLException
     */
    private static ArrayList haeOpiskelijanKurssit(Connection c) throws SQLException {

        ArrayList kurssiNimet = new ArrayList();
        TietokantaYhteys.useDatabase(c);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT Kurssit.nimi "
                + "FROM Suoritukset, Kurssit, Opiskelijat "
                + "WHERE Suoritukset.opiskelijaID = " + SuoritustietokantaViewController.haeOpiskID()
                + " AND Suoritukset.opiskelijaID = Opiskelijat.opiskelijaID"
                + " AND Suoritukset.kurssiID = Kurssit.kurssiID"
                + " ORDER BY Suoritukset.opiskelijaID;"
        );
        while (rs.next()) {
            kurssiNimet.add(rs.getString("Kurssit.nimi"));
        }
        return kurssiNimet;
    }

    /**
     * hakee valittuna olevan kurssin ja siihen liityvän suorituksen 
     * tiedot ja asettaa ne niille kuuluviin paikkoihin
     * @param c connection
     * @throws SQLException
     */
    protected void haeKurssiTiedot(Connection c) throws SQLException {

        lblArvosana.setText("");
        try (Connection conn = TietokantaYhteys.openConnection()) {
            TietokantaYhteys.useDatabase(conn);
            Statement stmt = conn.createStatement();
            ResultSet kurssiTiedot = stmt.executeQuery(
                    "SELECT Kurssit.nimi, Kurssit.kurssiID, arvosana, Suoritukset.opintopisteet, pvm "
                    + "FROM Kurssit, Suoritukset "
                    + "WHERE Suoritukset.opiskelijaID = " + SuoritustietokantaViewController.haeOpiskID() + " AND Suoritukset.kurssiID = "
                    + getKurssiID(conn) + " AND Suoritukset.kurssiID = Kurssit.kurssiID;"
            );
            while (kurssiTiedot.next()) {
                txtKurssiNimi.setText(kurssiTiedot.getString("Kurssit.nimi"));
                lblKurssiID.setText(Integer.toString(kurssiTiedot.getInt("Kurssit.kurssiID")));
                lblArvosana.setText(String.valueOf(kurssiTiedot.getInt("arvosana")));
                lblOpintoPisteet.setText(String.valueOf(kurssiTiedot.getInt("Suoritukset.opintopisteet")));
                dteSuoritusPvm.setValue(kurssiTiedot.getDate("pvm").toLocalDate());
            }
        }
    }
    
    /**
     * hakee cboSuorituksesta valitun kurssin ja siihen liittyvan suorituksen
     * tiedot ja asettaa ne paikoilleen
     * @throws SQLException
     */
    @FXML
    private void kurssiValittu(ActionEvent event) throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        haeKurssiTiedot(conn);
    }
}
