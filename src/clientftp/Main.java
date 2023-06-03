package clientftp;
import clientftp.gui.ConnectionGui;

/**
 * Classe Main del client FTP, questa classe contiene il metodo main
 * in cui viene istanziata la classe ConnectionGui che genera la finestra
 * per effettuare una nuova connessione a un server FTP.
 * Dopo che l'utente avrà inserito tutti i dati nella finestra di connessione
 * verrà effettuata una nuova connessione e login al server FTP, successivamente
 * sarà generata un'altra GUI dove verranno visualizzati tutti i files e cartelle
 * presenti sul server FTP, da lì l'utente potrà interagire con essi, scaricandoli,
 * navigando fra le cartelle, cancellandoli ecc.
 * Il programma utilizza anche delle classi e metodi provenienti da una libreria
 * esterna (org.apache.commons.net) situata in un archivio jar nella cartella lib/
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class Main {
    public static void main(String[] args) {
       new ConnectionGui();
    }
}
