package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoMembroNaoPodeSerNulaException extends RuntimeException {
    public IdentificacaoDoMembroNaoPodeSerNulaException() {
        super("Identificacao do membro nao pode ser nula.");
    }
}
