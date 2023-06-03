package clientftp.exceptions;

/**
 * Classe che rappresenta l'eccezione sollevata in caso
 * di problemi durante il caricamento o aggiornamento
 * delle impostazioni dell'utente salvate nel file settings.xml
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPSettingsLoadingException  extends Exception{
    /**
     * Metodo costruttore dell'eccezione
     * @param message messaggio dell'eccezione
     */
    public FTPSettingsLoadingException(String message){
        super(message);
    }
}
