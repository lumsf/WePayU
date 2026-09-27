package br.ufal.ic.p2.wepayu.Exception;

public class BancoNaoPodeSerNuloException extends RuntimeException {
    public BancoNaoPodeSerNuloException() {
        super("Banco nao pode ser nulo.");
    }
}
