package br.ufal.ic.p2.wepayu.Exception;

public class SistemaEncerradoException extends RuntimeException {
    public SistemaEncerradoException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }
}
