package br.ufal.ic.p2.wepayu.Exception;

public class ValorBooleanoException extends RuntimeException {
    public ValorBooleanoException() {
        super("Valor deve ser true ou false.");
    }
}
