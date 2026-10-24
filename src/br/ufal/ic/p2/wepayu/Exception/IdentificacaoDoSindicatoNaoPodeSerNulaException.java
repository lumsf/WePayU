package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoSindicatoNaoPodeSerNulaException extends WePayUException {
    public IdentificacaoDoSindicatoNaoPodeSerNulaException() {
        super("Identificacao do sindicato nao pode ser nula.");
    }
}
