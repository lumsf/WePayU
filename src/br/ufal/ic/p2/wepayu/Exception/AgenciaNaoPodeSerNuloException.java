package br.ufal.ic.p2.wepayu.Exception;

public class AgenciaNaoPodeSerNuloException extends RuntimeException {
    public AgenciaNaoPodeSerNuloException() {
        super("Agencia nao pode ser nulo.");
    }
}
