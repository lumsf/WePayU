package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoEmpregadoNaoPodeSerNulaException extends WePayUException {
    public IdentificacaoDoEmpregadoNaoPodeSerNulaException() {
        super("Identificacao do empregado nao pode ser nula.");
    }
}
