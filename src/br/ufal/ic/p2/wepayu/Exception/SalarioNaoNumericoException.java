package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNaoNumericoException extends WePayUException {
    public SalarioNaoNumericoException() {
        super("Salario deve ser numerico.");
    }
}
