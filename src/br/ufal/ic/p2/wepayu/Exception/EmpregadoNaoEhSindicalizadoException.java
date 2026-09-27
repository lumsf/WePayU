package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoEhSindicalizadoException extends RuntimeException {
    public EmpregadoNaoEhSindicalizadoException() {
      super("Empregado nao eh sindicalizado.");
    }
}
