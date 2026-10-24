package br.ufal.ic.p2.wepayu.Exception;

public class EnderecoNaoPodeSerNuloException extends WePayUException {
    public EnderecoNaoPodeSerNuloException() {
        super("Endereco nao pode ser nulo.");
    }
}
