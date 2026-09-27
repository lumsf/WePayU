package br.ufal.ic.p2.wepayu.Exception;

public class ContaCorrenteNaoPodeSerNuloException extends RuntimeException {
    public ContaCorrenteNaoPodeSerNuloException() {
        super("Conta corrente nao pode ser nulo.");
    }
}
