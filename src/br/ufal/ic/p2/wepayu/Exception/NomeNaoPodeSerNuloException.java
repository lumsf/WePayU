package br.ufal.ic.p2.wepayu.Exception;

public class NomeNaoPodeSerNuloException extends RuntimeException {
    public NomeNaoPodeSerNuloException() {
        super("Nome nao pode ser nulo.");
    }
}
