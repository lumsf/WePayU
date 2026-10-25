package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoExisteException extends WePayUException{
    public EmpregadoNaoExisteException(){
        super("Empregado nao existe.");
    }
}
