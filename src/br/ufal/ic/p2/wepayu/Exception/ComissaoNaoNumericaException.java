package br.ufal.ic.p2.wepayu.Exception;

public class ComissaoNaoNumericaException extends WePayUException {
    public ComissaoNaoNumericaException() {
        super("Comissao deve ser numerica.");
    }
}
