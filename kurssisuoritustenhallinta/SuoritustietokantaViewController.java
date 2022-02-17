package kurssisuoritustenhallinta;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * Toimii tietokannan paavalikkona
 * @author Patrik Soininen
 */
public class SuoritustietokantaViewController implements Initializable {

    @FXML
    private Button btnKurssiSuoritukset;
    @FXML
    private Button btnKurssiMuokkaus;
    @FXML
    private Button btnLKurssiLisaa;
    @FXML
    private Button btnOpiskSuoritukset;
    @FXML
    private Button btnOpiskMuokkaus;
    @FXML
    private Button btnOpiskLisaa;
    @FXML
    private Button btnKurssiTallennus;
    @FXML
    private Button btnOpiskTallennus;
    @FXML
    private Button btnUusiOpiskTallenna;
    @FXML
    private Button btnUusiKurssiTallenna;
    @FXML
    private Button btnKurssiPeru;
    @FXML
    private Button btnOpiskPeru;
    @FXML
    private Button BtnPoistaKurssi;
    @FXML
    private Button btnPoistaOpiskelija;
    @FXML
    private ComboBox<Integer> cboKurssiOpintoPisteet;
    @FXML
    protected ComboBox<Integer> cboOpiskelijat;
    @FXML
    protected ComboBox<String> cboKurssit;
    @FXML
    private Label lblOpiskNum;
    @FXML
    private Label lblKurssiVirheIlmoitus;
    @FXML
    private Label lblOpiskVirheIlmoitus;
    @FXML
    private Label lblKurssiOpaste;
    @FXML
    private Label lblOpisNimiOpaste;
    @FXML
    private Label lblOpiskSpostiOpaste;
    @FXML
    private Label lblOpiskPuhnumOpaste;
    @FXML
    private TextField txtKurssiNimi;
    @FXML
    private TextField txtOpiskNimi;
    @FXML
    private TextField txtOpiskSposti;
    @FXML
    private TextField txtOpiskPuhNum;
    
    private static int kurssiID;
    private static int opintopisteet;
    private static int opiskID;

    private static String kurssiNimi;
    private static String poistettavanNimi;//Tällä saadaan tieto PoistonVarmistusViewControllerille että minkä tyyppinen poisto siellä tehdään
    private static String opiskNimi;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection conn = TietokantaYhteys.openConnection();
            TietokantaYhteys.useDatabase(conn);
            paivitaListat(conn); //Haetaan olemassaolevien kurssien nimet ja opiskelijoiden ID numerot comboboxeihin
            haeOpiskelijanTiedot(conn); //Haetaan ensiksi avautuvan opiskelijan tiedot oikeisiin paikkoihin
            haeKurssinTiedot(conn); //Sama kuin yläpuolella mutta avautuvaan kurssiin
        } catch (SQLException ex) {
            Logger.getLogger(SuoritustietokantaViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return poistettavan kurssin tai opiskelija nimi
     */
    protected static String getPoistettavanNimi() {
        return poistettavanNimi;
    }

    /**
     *
     * @param nimi poistettavan kurssin tai opiskelijan nimi
     */
    protected static void setPoistettavanNimi(String nimi) {
        poistettavanNimi = nimi;
    }

    /**
     *
     * @return opiskelijan nimi
     */
    protected static String getOpiskelijaNimi() {
        return opiskNimi;
    }

    /**
     *
     * @return kurssin nimi
     */
    protected static String getKurssiNimi() {
        return kurssiNimi;
    }

    /**
     *
     * @return opiskelijan ID numero
     */
    public static int getOpiskID() {
        return opiskID;
    }
    
    /**
     * hakee valittuna olevan kurssin ID numeron kurssin nimen perusteella 
     * @return kurssin ID numero
     * @throws SQLException
     */
    protected static int haeKurssiID() throws SQLException {

        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT kurssiID FROM Kurssit WHERE nimi = '" + getKurssiNimi() + "';"
        );
        while (rs.next()) {
            kurssiID = rs.getInt("kurssiID");
        }
        return kurssiID;
    }

    /**
     * hakee valittuna olevan opiskelijan ID numero opiskelija nimen perusteella
     * @return opiskelijan ID numero
     * @throws SQLException
     */
    protected static int haeOpiskID() throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT opiskelijaID FROM Opiskelijat WHERE nimi = '" + getOpiskelijaNimi() + "';"
        );
        while (rs.next()) {
            opiskID = rs.getInt("opiskelijaID");
        }
        return opiskID;
    }

    /**
     * hakee kaikkien tietokannassa olevien opiskelijoiden ID numerot
     * ja lisaa ne combobox cboOpiskelijoihin
     * @param c connection
     * @throws SQLException
     */
    protected void haeOpiskIdeet(Connection c) throws SQLException {

        ArrayList opiskIDeet = new ArrayList();
        Statement stmt = c.createStatement();
        TietokantaYhteys.useDatabase(c);
        ResultSet rs = stmt.executeQuery(
                "SELECT opiskelijaID, nimi FROM Opiskelijat ORDER BY opiskelijaID asc;"
        );
        while (rs.next()) {
            opiskIDeet.add(rs.getInt("opiskelijaID"));
        }
        cboOpiskelijat.getItems().addAll(opiskIDeet);
    }

    /**
     * hakee kaikkien tietokannassa olevien kurssien nimet
     * ja lisaa ne combobox cboKursseihin
     * @param c connection
     * @throws SQLException
     */
    protected void haeKurssiNimet(Connection c) throws SQLException {

        ArrayList kurssiNimet = new ArrayList();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT nimi FROM Kurssit ORDER BY nimi asc;"
        );
        while (rs.next()) {
            kurssiNimet.add(rs.getString("nimi"));
        }
        cboKurssit.getItems().addAll(kurssiNimet);
    }

    /**
     * hakee valittuna olevan kurssin opintopisteet
     * @return valittuna olevan kurssin opintopisteet
     * @throws SQLException
     */
    protected static int haeOpintoPisteet() throws SQLException {

        Connection conn = TietokantaYhteys.openConnection();
        TietokantaYhteys.useDatabase(conn);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT opintopisteet FROM Kurssit WHERE nimi = '" + getKurssiNimi() + "';"
        );
        while (rs.next()) {
            opintopisteet = rs.getInt("opintopisteet");
        }
        return opintopisteet;
    }

    /**
     * tekee samat asiat kuin "haeOpiskIdeet" ja "haeKurssiNimet" 
     * ja asettaa comboboxit cboOpiskelijat ja cboKurssit ensimmaisen alkion
     * kohdalle
     * @param c connection
     * @throws SQLException
     */
    protected void paivitaListat(Connection c) throws SQLException {

        haeOpiskIdeet(c);
        haeKurssiNimet(c);
        cboOpiskelijat.setValue(cboOpiskelijat.getItems().get(0));
        cboKurssit.setValue(cboKurssit.getItems().get(0));
    }
    
    /**
     * hakee valittuna olevan kurssin tiedot ja asettaa ne niille kuuluviin
     * paikkoihin
     * @param c connection
     * @throws SQLException
     */
    private void haeKurssinTiedot(Connection c) throws SQLException {
        
        BtnPoistaKurssi.setDisable(false);
        TietokantaYhteys.useDatabase(c);

        Statement stmt = c.createStatement();
        ResultSet kurssinTiedot = stmt.executeQuery(
                "SELECT nimi, opintopisteet "
                + "FROM Kurssit "
                + "WHERE nimi = '" + cboKurssit.getValue() + "';"
        );
        while (kurssinTiedot.next()) {
            txtKurssiNimi.setText(kurssinTiedot.getString("nimi"));
            cboKurssiOpintoPisteet.setValue(kurssinTiedot.getInt("opintopisteet"));
            cboKurssiOpintoPisteet.setDisable(true);

        }
    }
    
    /**
     * hakee valittuna olevan opiskelijan tiedot ja asettaa ne niille kuuluviin
     * paikkoihin
     * @param c connection
     * @throws SQLException
     */
    private void haeOpiskelijanTiedot(Connection c) throws SQLException {

        btnPoistaOpiskelija.setDisable(false);
        TietokantaYhteys.useDatabase(c);

        Statement stmt = c.createStatement();
        ResultSet opiskTiedot = stmt.executeQuery(
                "SELECT opiskelijaID, nimi,sposti,puhnum "
                + "FROM Opiskelijat "
                + "WHERE opiskelijaID = " + cboOpiskelijat.getValue() + ";"
        );
        while (opiskTiedot.next()) {
            lblOpiskNum.setText(Integer.toString(opiskTiedot.getInt("opiskelijaID")));
            txtOpiskNimi.setText(opiskTiedot.getString("nimi"));
            txtOpiskSposti.setText(opiskTiedot.getString("sposti"));
            txtOpiskPuhNum.setText(opiskTiedot.getString("puhnum"));
        }
        opiskID = Integer.valueOf(lblOpiskNum.getText());
    }
    
    /**
     * hakee cboKurssista valitun kurssin tiedot ja asettaa ne paikoilleen
     * @throws SQLException
     */
    @FXML
    private void kurssiValittu(ActionEvent event) throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        haeKurssinTiedot(conn);
    }
    
     /**
     * hakee cboOpiskelijoista valitun opiskelijan tiedot ja asettaa
     * ne paikoilleen
     * @throws SQLException
     */
    @FXML
    private void opiskelijaValittu(ActionEvent event) throws SQLException {
        Connection conn = TietokantaYhteys.openConnection();
        haeOpiskelijanTiedot(conn);
    }
    
    /**
     * asettaa kaikki kurssin lisaykseen tarvittavat controllit
     * toimivaksi ja asettaa valittavat opintopisteet cboKurssiOpintoPisteisiin
     */
    @FXML
    private void kurssiLisaaKurssi(ActionEvent event) {
        btnUusiKurssiTallenna.setVisible(true);
        btnLKurssiLisaa.setDisable(true);
        btnKurssiPeru.setVisible(true);
        btnKurssiSuoritukset.setDisable(true);
        btnKurssiMuokkaus.setDisable(true);
        cboKurssit.setDisable(true);
        cboKurssiOpintoPisteet.setDisable(false);
        //tyhjennetään ennen uusien pisteiden lisäämistä jotta samat luvut eivät tule kuin yhden kerran
        cboKurssiOpintoPisteet.getItems().clear();
        cboKurssiOpintoPisteet.getItems().addAll(1, 2, 3, 5, 7, 10);
        lblKurssiOpaste.setVisible(true);
        txtKurssiNimi.clear();
        txtKurssiNimi.setEditable(true);
    }
    
    /**
     * asettaa kaikki opiskelijan lisaykseen tarvittavat controllit toimivaksi
     */
    @FXML
    private void opiskLisaaOpiskelija(ActionEvent event) {
        btnUusiOpiskTallenna.setVisible(true);
        btnOpiskLisaa.setDisable(true);
        btnOpiskPeru.setVisible(true);
        btnOpiskSuoritukset.setDisable(true);
        cboOpiskelijat.setDisable(true);
        cboOpiskelijat.getItems().clear();
        lblOpisNimiOpaste.setVisible(true);
        lblOpiskSpostiOpaste.setVisible(true);
        lblOpiskPuhnumOpaste.setVisible(true);
        lblOpiskNum.setText("");
        txtOpiskNimi.setEditable(true);
        txtOpiskPuhNum.setEditable(true);
        txtOpiskSposti.setEditable(true);
        txtOpiskNimi.clear();
        txtOpiskPuhNum.clear();
        txtOpiskSposti.clear();
    }
    
     /**
     * asettaa kaikki kurssin muokkaukseen tarvittavat controllit toimivaksi
     * ja asettaa valittavat opintopisteet cboKurssiOpintoPisteisiin
     */
    @FXML
    private void kurssiMuokkaa(ActionEvent event) {
        btnKurssiMuokkaus.setDisable(true);
        btnKurssiTallennus.setVisible(true);
        btnKurssiPeru.setVisible(true);
        btnKurssiSuoritukset.setDisable(true);
        btnLKurssiLisaa.setDisable(true);
        cboKurssit.setDisable(true);
        cboKurssiOpintoPisteet.setDisable(false);
        //tyhjennetään ennen uusien pisteiden lisäämistä jotta samat luvut eivät tule kuin yhden kerran
        cboKurssiOpintoPisteet.getItems().clear();
        cboKurssiOpintoPisteet.getItems().addAll(1, 2, 3, 5, 7, 10);
        lblKurssiOpaste.setVisible(true);
        txtKurssiNimi.setEditable(true);
    }
    
    /**
     * asettaa kaikki opiskelijan muokkaukseen tarvittavat controllit
     * toimivaksi
     */
    @FXML
    private void opiskelijaMuokkaa(ActionEvent event) {
        btnOpiskMuokkaus.setDisable(true);
        btnOpiskTallennus.setVisible(true);
        btnOpiskPeru.setVisible(true);
        btnOpiskLisaa.setDisable(true);
        btnOpiskSuoritukset.setDisable(true);
        btnOpiskSuoritukset.setDisable(true);
        cboOpiskelijat.setDisable(true);
        lblOpisNimiOpaste.setVisible(true);
        lblOpiskPuhnumOpaste.setVisible(true);
        lblOpiskSpostiOpaste.setVisible(true);
        txtOpiskNimi.setEditable(true);
        txtOpiskPuhNum.setEditable(true);
        txtOpiskSposti.setEditable(true);
    }
    
    /**
     * asettaa kaikki kurssin lisaykseen tai muokkaukseen tarvittavat
     * controllit toimimattomaksi
     */
    @FXML
    private void kurssiPeru(ActionEvent event) throws SQLException {
        btnUusiKurssiTallenna.setVisible(false);
        btnLKurssiLisaa.setDisable(false);
        btnKurssiPeru.setVisible(false);
        btnKurssiMuokkaus.setDisable(false);
        btnKurssiTallennus.setVisible(false);
        btnKurssiSuoritukset.setDisable(false);
        cboKurssit.setDisable(false);
        cboKurssiOpintoPisteet.setDisable(true);
        lblKurssiVirheIlmoitus.setVisible(false);
        lblKurssiOpaste.setVisible(false);
        txtKurssiNimi.clear();
        txtKurssiNimi.setEditable(false);
        kurssiValittu(event);//tämä jotta kentät eivät jää perumisen jälkeen tyhjiksi
    }
    
    /**
     * asettaa kaikki opiskelijan lisaykseen tai muokkaukseen tarvittavat
     * controllit toimimattomaksi
     */
    @FXML
    private void opiskelijaPeru(ActionEvent event) throws SQLException {
        btnUusiOpiskTallenna.setVisible(false);
        btnOpiskPeru.setVisible(false);
        btnOpiskLisaa.setDisable(false);
        btnOpiskMuokkaus.setDisable(false);
        btnOpiskTallennus.setVisible(false);
        btnOpiskSuoritukset.setDisable(false);
        cboOpiskelijat.setDisable(false);
        lblOpisNimiOpaste.setVisible(false);
        lblOpiskPuhnumOpaste.setVisible(false);
        lblOpiskSpostiOpaste.setVisible(false);
        lblOpiskVirheIlmoitus.setVisible(false);
        txtOpiskNimi.setEditable(false);
        txtOpiskPuhNum.setEditable(false);
        txtOpiskSposti.setEditable(false);
        txtOpiskNimi.clear();
        txtOpiskPuhNum.clear();
        txtOpiskSposti.clear();
        opiskelijaValittu(event);//tämä jotta kentät eivät jää perumisen jälkeen tyhjiksi
    }
    
    /**
     * tallentaa uuden kurssin tiedot tietokantaan ja tekee kaikki 
     * kurssin lisaykseen tarvittavat controllit toimimattomaksi
     */
    @FXML
    private void kurssiUusiTallenna(ActionEvent event) throws SQLException {
        /*
        *Uudet kurssit ja opiskelijat tallennetaan eri buttonin ja metodin kautta kuin muokatut
        *koska käyttäjä ei valitse ID numeroa itse, vaan se luodaan automaattisesti
        *kasvattamalla numeroa yhdellä, ja koska se on pääavain uutta kurssia tai opiskelijaa
        *lisätessä ei ole mahdollista luoda kahta samanlaista
        */
        lblKurssiVirheIlmoitus.setVisible(false);
        //Jos syötetty nimi on liian lyhyt, varoitus tulee näkyviin ja palataan edelliseen vaiheeseen
        if (txtKurssiNimi.getText().trim().length() < 2) {
            lblKurssiVirheIlmoitus.setVisible(true);
            kurssiLisaaKurssi(event);
        } else {
            try {
                Connection conn = TietokantaYhteys.openConnection();
                TietokantaYhteys.useDatabase(conn);

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Kurssit (nimi,opintopisteet) "
                        + "VALUES (?,?);"
                );
                ps.setString(1, txtKurssiNimi.getText().trim());
                ps.setInt(2, cboKurssiOpintoPisteet.getValue());
                ps.execute();
                btnLKurssiLisaa.setDisable(false);
                btnKurssiPeru.setVisible(false);
                btnKurssiSuoritukset.setDisable(false);
                btnUusiKurssiTallenna.setVisible(false);
                cboKurssit.getItems().clear();
                cboKurssit.setDisable(false);
                cboKurssiOpintoPisteet.getItems().clear();
                cboKurssiOpintoPisteet.setDisable(true);
                haeKurssiNimet(conn);
            } catch (SQLException s) {
                //Jos nimi on liian pitkä sama juttu kuin ylhäällä
                lblKurssiVirheIlmoitus.setVisible(true);
                kurssiLisaaKurssi(event);
            }
        }
    }
    
    /**
     * tallentaa uuden opiskelijan tiedot tietokantaan ja tekee kaikki 
     * opiskelijan lisaykseen tarvittavat controllit toimimattomaksi
     */
    @FXML
    private void opiskelijaUusiTallenna(ActionEvent event) throws SQLException {
        //testataan onko syötetyt tarpeeksi pitkiä, ja jos eivät ole palataan edelliseen vaiheeseen
        lblOpiskVirheIlmoitus.setVisible(false);
        if (txtOpiskNimi.getText().trim().length() < 2) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);
        } else if (txtOpiskSposti.getText().trim().length() < 5) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);                    //varmistetaan että puhelinnumero sisältää vain numeroita 
        } else if (txtOpiskPuhNum.getText().trim().length() < 3 || !txtOpiskPuhNum.getText().matches("[0-9]+")) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);
        } else {
            try {
                Connection conn = TietokantaYhteys.openConnection();
                TietokantaYhteys.useDatabase(conn);

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Opiskelijat (nimi,sposti,puhnum) "
                        + "VALUES (?,?,?);"
                );
                ps.setString(1, txtOpiskNimi.getText().trim());
                ps.setString(2, txtOpiskSposti.getText().trim());
                ps.setString(3, txtOpiskPuhNum.getText().trim());
                ps.execute();
                opiskNimi = txtOpiskNimi.getText();
                btnUusiOpiskTallenna.setVisible(false);
                btnOpiskLisaa.setDisable(false);
                btnOpiskPeru.setVisible(false);
                btnOpiskSuoritukset.setDisable(false);
                cboOpiskelijat.getItems().clear();
                cboOpiskelijat.setDisable(false);
                cboOpiskelijat.setValue(haeOpiskID());
                lblOpisNimiOpaste.setVisible(false);
                lblOpiskPuhnumOpaste.setVisible(false);
                lblOpiskSpostiOpaste.setVisible(false);
                txtOpiskNimi.setEditable(false);
                txtOpiskPuhNum.setEditable(false);
                txtOpiskSposti.setEditable(false);
                haeOpiskIdeet(conn);
            } catch (SQLException s) {
                //tarkistetaan taas ettei syötteet ole liian pitkiä
                lblOpiskVirheIlmoitus.setVisible(true);
                opiskLisaaOpiskelija(event);
            }
        }
    }
    
    /**
     * tallentaa muokatun kurssin tiedot tietokantaan ja tekee kaikki 
     * kurssin muokkaukseen tarvittavat controllit toimimattomaksi
     */
    @FXML
    private void kurssiMuokkausTallenna(ActionEvent event) throws SQLException {
        
        lblKurssiVirheIlmoitus.setVisible(false);
        if (txtKurssiNimi.getText().trim().length() < 2) {
            lblKurssiVirheIlmoitus.setVisible(true);
            kurssiLisaaKurssi(event);
        } else {
            try {
                Connection conn = TietokantaYhteys.openConnection();
                TietokantaYhteys.useDatabase(conn);

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Kurssit "
                        + "SET nimi = ?,opintopisteet = ?"
                        + " WHERE nimi = ?;"
                );
                ps.setString(1, txtKurssiNimi.getText().trim());
                ps.setInt(2, cboKurssiOpintoPisteet.getValue());
                ps.setString(3, cboKurssit.getValue());
                ps.execute();
                btnKurssiMuokkaus.setDisable(false);
                btnKurssiTallennus.setVisible(false);
                btnKurssiPeru.setVisible(false);
                btnKurssiSuoritukset.setDisable(false);
                btnLKurssiLisaa.setDisable(false);
                cboKurssit.getItems().clear();
                cboKurssit.setDisable(false);
                cboKurssiOpintoPisteet.setDisable(true);
                lblKurssiOpaste.setVisible(false);
                txtKurssiNimi.setEditable(false);
                haeKurssiNimet(conn);
            } catch (SQLException s) {
                lblKurssiVirheIlmoitus.setVisible(true);
                kurssiLisaaKurssi(event);
            }
        }
    }
    
    /**
     * tallentaa muokatun opiskelijan tiedot tietokantaan ja tekee kaikki 
     * opiskelijan muokkaukseen tarvittavat controllit toimimattomaksi
     */
    @FXML
    private void opiskelijaMuokkausTallenna(ActionEvent event) throws SQLException {

        lblOpiskVirheIlmoitus.setVisible(false);
        if (txtOpiskNimi.getText().trim().length() < 2) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);
        } else if (txtOpiskSposti.getText().trim().length() < 5) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);
        } else if (txtOpiskPuhNum.getText().trim().length() < 3 || !txtOpiskPuhNum.getText().matches("[0-9]+")) {
            lblOpiskVirheIlmoitus.setVisible(true);
            opiskLisaaOpiskelija(event);
        } else {
            try {
                Connection conn = TietokantaYhteys.openConnection();
                TietokantaYhteys.useDatabase(conn);

                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Opiskelijat "
                        + "SET nimi = ?,sposti = ?, puhnum = ?"
                        + " WHERE opiskelijaID = ?;"
                );
                ps.setString(1, txtOpiskNimi.getText().trim());
                ps.setString(2, txtOpiskSposti.getText().trim());
                ps.setString(3, txtOpiskPuhNum.getText().trim());
                ps.setInt(4, cboOpiskelijat.getValue());
                ps.execute();
                btnOpiskMuokkaus.setDisable(false);
                btnOpiskTallennus.setVisible(false);
                btnOpiskPeru.setVisible(false);
                btnOpiskLisaa.setDisable(false);
                btnOpiskSuoritukset.setDisable(false);
                cboOpiskelijat.setDisable(false);
                lblOpisNimiOpaste.setVisible(false);
                lblOpiskPuhnumOpaste.setVisible(false);
                lblOpiskSpostiOpaste.setVisible(false);
                txtOpiskNimi.setEditable(false);
                txtOpiskPuhNum.setEditable(false);
                txtOpiskSposti.setEditable(false);
                haeOpiskIdeet(conn);
            } catch (SQLException s) {
                lblOpiskVirheIlmoitus.setVisible(true);
                opiskLisaaOpiskelija(event);
            }
        }
    }
    
    /**
     * avaa KurssinSuorituksetView 
     */
    @FXML
    private void kurssiNaytaSuoritukset(ActionEvent event) throws IOException {

        kurssiNimi = cboKurssit.getValue();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("KurssinSuorituksetView.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(getKurssiNimi());
        stage.show();
    }
    
     /**
     * avaa OpiskelijanSuorituksetView 
     */
    @FXML
    private void opiskelijaNaytaSuoritukset(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("OpiskelijanSuorituksetView.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(cboOpiskelijat.getValue().toString() + ", " + txtOpiskNimi.getText());
        opiskID = cboOpiskelijat.getValue();
        opiskNimi = txtOpiskNimi.getText();
        stage.show();
    }
    
     /**
     * avaa PoistonVarmistusView ja sulkee taman ikkunan
     */
    @FXML
    private void poistaKurssi(ActionEvent event) throws IOException, SQLException {
        /*
        *Suljetaan ikkuna jotta sen voi avata uudestaa kunhan poisto on tehty
        *tämä sen takia jotta kurssi listasta häviää poistettu kurssi
        */ 
        Stage stageTama = (Stage) btnPoistaOpiskelija.getScene().getWindow();
        /*
        *Otetaan kurssin nimi tekstikentästä ja asetetaan se poistettavan nimeksi
        *lisätään alkuun sana suoritus jotta PoistonVarmistusViewControllerissa osataan tehdä oikentyyppinen poisto
        */
        poistettavanNimi = "Kurssi " + txtKurssiNimi.getText();
        kurssiNimi = txtKurssiNimi.getText();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoistonVarmistus.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Kurssin " + txtKurssiNimi.getText() + " poisto");
        stage.show();
        stageTama.close();
    }
    
    /**
     * avaa PoistonVarmistusView ja sulkee taman ikkunan
     */
    @FXML
    private void poistaOpiskelija(ActionEvent event) throws IOException, SQLException {
        /*
        *Suljetaan nykyinen ikkuna jotta sen voi avata uudestaa kunhan poisto on tehty
        *tämä sen takia jotta opiskelija listasta häviää poistettu opiskelija
        */
        Stage stageTama = (Stage) btnPoistaOpiskelija.getScene().getWindow();
        /*
        *Otetaan opiskelijan nimi tekstikentästä ja asetetaan se poistettavan nimeksi
        *lisätään alkuun sana "Opiskelija" jotta PoistonVarmistusViewControllerissa osataan tehdä oikentyyppinen poisto
        */
        poistettavanNimi = "Opiskelija " + txtOpiskNimi.getText();
        opiskID = Integer.valueOf(lblOpiskNum.getText());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoistonVarmistus.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Opiskelijan " + lblOpiskNum.getText() + ", " + txtOpiskNimi.getText() + " poisto");
        stage.show();
        stageTama.close();
    }
    
    /**
     * sulkee ohjelman
     */
    @FXML
    private void close(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
    
    /**
     * nayttaa ohjelman nimen ja tekijan
     */
    @FXML
    private void about(ActionEvent event) {
        Alert about = new Alert(Alert.AlertType.INFORMATION, "(c) Patrik Soininen", ButtonType.CLOSE);
        about.setTitle("About");
        about.setHeaderText("Suoritustietokanta");
        about.show();
    }
}
