package br.ufal.ic.p2.wepayu.Exception;

public class NomeNaoPodeSerNuloException extends WePayUException {
    public NomeNaoPodeSerNuloException() {
        super("Nome nao pode ser nulo.");
    }
}
