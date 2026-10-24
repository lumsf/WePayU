package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoMembroNaoPodeSerNulaException extends WePayUException {
    public IdentificacaoDoMembroNaoPodeSerNulaException() {
        super("Identificacao do membro nao pode ser nula.");
    }
}
