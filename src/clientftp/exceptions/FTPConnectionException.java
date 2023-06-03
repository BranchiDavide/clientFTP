package clientftp.exceptions;

/**
 * Classe che rappresenta l'eccezione sollevata in caso
 * di problemi durante la connessione al server FTP.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPConnectionException extends Exception{
    /**
     * Metodo costruttore dell'eccezione
     * @param message messaggio dell'eccezione
     */
    public FTPConnectionException(String message){
        super(message);
    }
}
