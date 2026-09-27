package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoEhHoristaException extends RuntimeException {
    public EmpregadoNaoEhHoristaException() {
        super("Empregado nao eh horista.");
    }
}
