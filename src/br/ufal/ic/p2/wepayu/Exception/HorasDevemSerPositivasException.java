package br.ufal.ic.p2.wepayu.Exception;

public class HorasDevemSerPositivasException extends RuntimeException {
    public HorasDevemSerPositivasException() {
        super("Horas devem ser positivas.");
    }
}
