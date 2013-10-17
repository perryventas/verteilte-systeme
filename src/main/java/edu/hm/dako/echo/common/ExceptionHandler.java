package edu.hm.dako.echo.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ExceptionHandler {

    private final static Log log = LogFactory.getLog(ExceptionHandler.class);

    public static void logExceptionAndTerminate(Exception exception) {
        handleException(exception, true);
    }

    public static void logException(Exception exception) {
        handleException(exception, false);
    }

    private static void handleException(Exception exception, boolean terminateVm) {
        try {
            throw exception;
        } catch (SocketException e) {
            log.error("Exception bei der DatagramSocket-Erzeugung: " + e);
        } catch (UnknownHostException e) {
            log.error("Exception bei Adressebelegung: " + e);
        } catch (IOException e) {
            log.error("Senden oder Empfangen von Nachrichten nicht moeglich: " + e);
        } catch (InterruptedException e) {
            log.error("Sleep unterbrochen");
        } catch (ClassNotFoundException e) {
            log.error("Empfangene Objektklasse nicht bekannt:" + e);
        } catch (Exception e) {
            log.error("Schwerwiegender Fehler");
        }
        // exception.printStackTrace();
        if (terminateVm) {
            System.exit(1);
        }
    }
}
