package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNegativoException extends WePayUException {
    public SalarioNegativoException() {
        super("Salario deve ser nao-negativo.");
    }
}
