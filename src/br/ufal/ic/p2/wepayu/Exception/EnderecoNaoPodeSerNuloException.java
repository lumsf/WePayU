package br.ufal.ic.p2.wepayu.Exception;

public class EnderecoNaoPodeSerNuloException extends RuntimeException {
    public EnderecoNaoPodeSerNuloException() {
        super("Endereco nao pode ser nulo.");
    }
}
