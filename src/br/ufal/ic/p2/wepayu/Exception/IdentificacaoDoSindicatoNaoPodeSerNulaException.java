package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoSindicatoNaoPodeSerNulaException extends RuntimeException {
    public IdentificacaoDoSindicatoNaoPodeSerNulaException() {
        super("Identificacao do sindicato nao pode ser nula.");
    }
}
