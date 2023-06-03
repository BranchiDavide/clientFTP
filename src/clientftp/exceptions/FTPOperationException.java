package clientftp.exceptions;

/**
 * Classe che rappresenta l'eccezione sollevata in caso
 * di problemi con tutte le operazioni che riguardano
 * download, upload, cancellazione, navigazione, visualizzazione di files e cartelle.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPOperationException extends Exception{
    /**
     * Metodo costruttore dell'eccezione
     * @param message messaggio dell'eccezione
     */
    public FTPOperationException(String message){
        super(message);
    }
}
