package br.ufal.ic.p2.wepayu.Exception;

public class ValorDeveSerPositivoException extends RuntimeException {
    public ValorDeveSerPositivoException() {
        super("Valor deve ser positivo.");
    }
}
