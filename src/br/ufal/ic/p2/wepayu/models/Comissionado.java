package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

public class Comissionado extends Empregado{
    private String comissao;

    public Comissionado(){
        super();
    }

    public Comissionado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);
        this.comissao = comissao;
    }

    public String getComissao() {
        return comissao;
    }
    public void setComissao(String comissao){
        this.comissao = comissao;
    }
}
