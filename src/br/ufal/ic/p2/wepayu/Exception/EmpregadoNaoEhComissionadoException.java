package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoEhComissionadoException extends WePayUException {
    public EmpregadoNaoEhComissionadoException() {
        super("Empregado nao eh comissionado.");
    }
}
