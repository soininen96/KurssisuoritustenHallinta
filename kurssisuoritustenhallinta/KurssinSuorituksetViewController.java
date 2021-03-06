package kurssisuoritustenhallinta;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class 
 * Ikkuna jossa naytetaan yksittaisen kurssin suoritukset
 * ja jossa voidaan lisata uusia tai muokata suorituksia kyseiselle kursille
 * @author Patrik Soininen
 */
public class KurssinSuorituksetViewController implements Initializable {

    @FXML
    private Button btnMuokkaaSuoritusta;
    @FXML
    private Button btnTallenna;
    @FXML
    private Button btnLisaaSuoritus;
    @FXML
    private Button btnPeruuta;
    @FXML
    private Button btnPoista;
    @FXML
    private ComboBox<Integer> cboSuorittaneetOpiskelijat;
    @FXML
    private ComboBox<Integer> cboArvosana;
    @FXML
    private ComboBox<Integer> cboOpiskelijaNumero;
    @FXML
    private DatePicker dteSuoritusPvm;
    @FXML
    private TextField txtNimi;

    private static int opiskelijaID;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection conn = TietokantaYhteys.openConnection();
            TietokantaYhteys.useDatabase(conn);
            //Haetaan suorituksen omaavien opiskelijoiden ID numerot comboboxiin
            cboSuorittaneetOpiskelijat.getItems().addAll(haeSuorittaneetOpiskelijat(conn));
            //Jos suorituksia on olemassa asetetaan comboxiin esille listan ensimm??inen opiskelija
            if (cboSuorittaneetOpiskelijat.getItems().size() > 0) {
                cboSuorittaneetOpiskelijat.setValue(cboSuorittaneetOpiskelijat.getItems().get(0));
                //ja haetaan sen opiskelijan tiedot ja laitetaan paikoilleen
                haeOpiskelijoidenTiedot(conn);
            }
            cboArvosana.setDisable(true);
        } catch (SQLException ex) {
            Logger.getLogger(KurssinSuorituksetViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return opiskelijan ID numero
     */
    protected static int getOpiskelijaID() {
        return opiskelijaID;
    }

    /**
     * hakee kaikkien niiden opiskelijoiden ID numeron joilla on suoritus talla
     * kursilla
     *
     * @param c connection
     * @return lista jossa opiskelijoiden ID numerot
     * @throws SQLException
     */
    private static ArrayList haeSuorittaneetOpiskelijat(Connection c) throws SQLException {

        ArrayList opiskIDeet = new ArrayList();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT opiskelijaID "
                + "FROM Suoritukset, Kurssit "
                + "WHERE Kurssit.nimi = '" + SuoritustietokantaViewController.getKurssiNimi() + "' AND Suoritukset.kurssiID = Kurssit.kurssiID;"
        );
        while (rs.next()) {
            opiskIDeet.add(rs.getInt("Suoritukset.opiskelijaID"));
        }
        return opiskIDeet;
    }

    /**
     * hakee valitun opiskelijan ID numeron ja nimen sek?? suorituksen arvosanan
     * ja suoritus paivamaaran ja asettaa ne niille kuuluviin paikkoihin
     *
     * @param c connection
     * @throws SQLException
     */
    private void haeOpiskelijoidenTiedot(Connection c) throws SQLException {

        btnPoista.setDisable(false);
        btnMuokkaaSuoritusta.setDisable(false);
        btnPoista.setVisible(true);
        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);

        Statement stmt = conn.createStatement();
        ResultSet opiskTiedot = stmt.executeQuery("SELECT Opiskelijat.opiskelijaID, Opiskelijat.nimi, arvosana, pvm "
                + "FROM Opiskelijat, Suoritukset "
                + "WHERE Opiskelijat.opiskelijaID = " + cboSuorittaneetOpiskelijat.getValue()
                + " AND Suoritukset.opiskelijaID = " + cboSuorittaneetOpiskelijat.getValue()
                + " AND Suoritukset.KurssiID = "
                + SuoritustietokantaViewController.haeKurssiID() + ";"
        );
        while (opiskTiedot.next()) {
            cboOpiskelijaNumero.setValue(opiskTiedot.getInt("opiskelijaID"));
            txtNimi.setText(opiskTiedot.getString("nimi"));
            cboArvosana.setValue(opiskTiedot.getInt("arvosana"));
            dteSuoritusPvm.setValue(opiskTiedot.getDate("pvm").toLocalDate());
        }
    }

    /**
     * hakee cboSuorittaneetOpiskelijoista valitun opiskelijan tiedot ja asettaa
     * ne paikoilleen
     *
     * @throws SQLException
     */
    @FXML
    private void opiskelijaValittu(ActionEvent event) throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        haeOpiskelijoidenTiedot(conn);
    }

    /**
     * hakee cboOpiskelijaNumerosta valitun opiskelijan tiedot ja asettaa ne
     * paikoilleen
     *
     * @throws SQLException
     */
    @FXML
    private void vapaaOpiskelijaValittu(ActionEvent event) throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        Statement stmt = conn.createStatement();
        ResultSet haeOpisk = stmt.executeQuery(
                "SELECT nimi "
                + "FROM Opiskelijat "
                + "WHERE opiskelijaID = " + cboOpiskelijaNumero.getValue() + ";"
        );
        while (haeOpisk.next()) {
            txtNimi.setText(haeOpisk.getString("nimi"));
        }
    }

    /**
     * asettaa kaikki suorituksen lisaykseen tarvittavat controllit toimivaksi
     * ja asettaa valittavat arvosanat cboArvosanoihin
     */
    @FXML
    private void LisaaSuoritus(ActionEvent event) throws SQLException {

        btnMuokkaaSuoritusta.setDisable(true);
        btnTallenna.setVisible(true);
        btnLisaaSuoritus.setDisable(true);
        btnPeruuta.setVisible(true);
        cboOpiskelijaNumero.setValue(null);
        cboSuorittaneetOpiskelijat.setDisable(true);
        cboArvosana.setDisable(false);
        //tyhjennet????n ennen uusien arvosanojen lis????mist?? jotta samat luvut eiv??t tule kuin yhden kerran
        cboArvosana.getItems().clear();
        cboArvosana.getItems().addAll(1, 2, 3, 4, 5);
        cboOpiskelijaNumero.getItems().clear();
        //Haetaan t??h??n comboboxiin kaikki opiskelijat joilla ei ole viel?? suoritusta t??ll?? kurssilla
        cboOpiskelijaNumero.getItems().addAll(haeVapaatOpiskelijat());
        cboOpiskelijaNumero.setDisable(false);
        dteSuoritusPvm.setDisable(false);
        //Laitetaan oletusp??iv??ksi t??m??nhetkinen, mutta sen voi muuttaa itse.
        dteSuoritusPvm.setValue(LocalDate.now());
        txtNimi.clear();
    }

    /**
     * asettaa suorituksen muokkaukseen tarvittavat controllit toimivaksi
     * ja asettaa valittavat arvosanat cboArvosanoihin
     */
    @FXML
    private void MuokkaaSuoritusta(ActionEvent event) {

        btnLisaaSuoritus.setDisable(true);
        btnTallenna.setVisible(true);
        btnPeruuta.setVisible(true);
        btnMuokkaaSuoritusta.setDisable(true);
        cboArvosana.setDisable(false);
        //tyhjennet????n ennen uusien arvosanojen lis????mist?? jotta samat luvut eiv??t tule kuin yhden kerran
        cboArvosana.getItems().clear();
        cboArvosana.getItems().addAll(1, 2, 3, 4, 5);
        cboSuorittaneetOpiskelijat.setDisable(true);
        dteSuoritusPvm.setDisable(false);
    }

    /**
     * asettaa kaikki suorituksen lisaykseen tai muokkaukseen tarvittavat
     * controllit toimimattomaksi
     */
    @FXML
    private void Peruuta(ActionEvent event) throws SQLException {

        btnTallenna.setVisible(false);
        btnPeruuta.setVisible(false);
        btnMuokkaaSuoritusta.setDisable(false);
        btnTallenna.setVisible(false);
        btnLisaaSuoritus.setDisable(false);
        btnPeruuta.setVisible(false);
        cboOpiskelijaNumero.setDisable(true);
        cboSuorittaneetOpiskelijat.setDisable(false);
        cboArvosana.getItems().clear();
        cboArvosana.setDisable(true);
        dteSuoritusPvm.setDisable(true);
        txtNimi.setEditable(false);
        txtNimi.clear();
        opiskelijaValittu(event);//t??m?? jotta kent??t eiv??t j???? perumisen j??lkeen tyhjiksi
    }

    /**
     * hakee kaikkien niiden opiskelijoiden ID numerot joilla ei ole suoritusta
     * talla kurssilla
     */
    private ArrayList haeVapaatOpiskelijat() throws SQLException {

        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        ArrayList opiskIDeet = new ArrayList();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT Opiskelijat.opiskelijaID "
                + "FROM Opiskelijat "
                + "WHERE opiskelijaID NOT IN "
                + "("
                + "SELECT opiskelijaID "
                + "FROM Suoritukset "
                + "WHERE Suoritukset.kurssiID = " + SuoritustietokantaViewController.getOpiskID()
                + ");"
        );
        while (rs.next()) {
            opiskIDeet.add(rs.getInt("opiskelijaID"));
        }
        return opiskIDeet;
    }

    /**
     * tallentaa suorituksen tiedot tietokantaan ja tekee kaikki suorituksen
     * lisaykseen tai muokkaukseen tarvittavat controllit toimimattomaksi
     */
    @FXML
    private void tallenna(ActionEvent event) throws SQLException {
        /*
        *T??ll?? metodilla tallennetaan uudet sek?? muokatut suoritukset
        */
        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Suoritukset (opiskelijaID, kurssiID, arvosana, opintopisteet, pvm) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                + "arvosana = ?, pvm = ?"
        );
        ps.setInt(1, cboOpiskelijaNumero.getValue());
        ps.setInt(2, SuoritustietokantaViewController.haeKurssiID());
        ps.setInt(3, cboArvosana.getValue());
        ps.setInt(4, SuoritustietokantaViewController.haeOpintoPisteet());
        ps.setDate(5, Date.valueOf(dteSuoritusPvm.getValue()));
        ps.setInt(6, cboArvosana.getValue());
        ps.setDate(7, Date.valueOf(dteSuoritusPvm.getValue()));
        ps.execute();
        btnTallenna.setVisible(false);
        btnPeruuta.setVisible(false);
        btnLisaaSuoritus.setDisable(false);
        btnMuokkaaSuoritusta.setDisable(false);
        cboArvosana.getItems().clear();
        //Tyhjennet????n ja t??ytet????n opiskelijalista jotta sinne ilmestyy my??s vasta lis??tty
        cboSuorittaneetOpiskelijat.getItems().clear();
        cboSuorittaneetOpiskelijat.getItems().addAll(haeSuorittaneetOpiskelijat(conn));
        cboSuorittaneetOpiskelijat.setValue(cboOpiskelijaNumero.getValue());
        cboOpiskelijaNumero.setDisable(true);
        cboSuorittaneetOpiskelijat.setDisable(false);
        cboArvosana.setDisable(true);
        dteSuoritusPvm.setDisable(true);
        txtNimi.setEditable(false);
    }

    /**
     * avaa PoistonVarmistusView ja sulkee taman ikkunan
     */
    @FXML
    private void poistaSuoritus(ActionEvent event) throws IOException {
        /*
        *Suljetaan nykyinen ikkuna jotta sen voi avata uudestaa kunhan poisto on tehty
        *t??m?? sen takia jotta opiskelija listasta h??vi???? poistettu opiskelija
        */
        Stage stageTama = (Stage) btnPoista.getScene().getWindow();
        /*
        *Otetaan opiskelijan nimi tekstikent??st?? ja kurssin nimi SuoritusTietoKantaViewControllerista ja asetetaan se poistettavan nimeksi
        *lis??t????n alkuun sana "Suoritus" jotta PoistonVarmistusViewControllerissa osataan tehd?? oikentyyppinen poisto
        */
        SuoritustietokantaViewController.setPoistettavanNimi("Suoritus " + txtNimi.getText() + " " + SuoritustietokantaViewController.getKurssiNimi());
        opiskelijaID = cboOpiskelijaNumero.getValue();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoistonVarmistus.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(txtNimi.getText() + " " + SuoritustietokantaViewController.getKurssiNimi() + " suorituksen poisto");
        stage.show();
        stageTama.close();
    }
}
