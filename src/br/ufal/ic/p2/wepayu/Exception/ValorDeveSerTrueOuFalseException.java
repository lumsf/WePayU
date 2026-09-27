package br.ufal.ic.p2.wepayu.Exception;

public class ValorDeveSerTrueOuFalseException extends RuntimeException {
    public ValorDeveSerTrueOuFalseException() {
        super("Valor deve ser true ou false.");
    }
}
