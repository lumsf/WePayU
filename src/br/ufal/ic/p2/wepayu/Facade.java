package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.*;

public class Facade {
    BancoDados novoBancoDados = new BancoDados();

    public void zerarSistema(){
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        String id;

        if(tipo.equals("Horista")){
            Horista novoHorista = new Horista(nome, endereco, tipo, salario);
            novoBancoDados.adicionarEmpregado(novoHorista);
        }

        else {
            Assalariado novoAssalariado = new Assalariado(nome, endereco, tipo, salario);
            novoBancoDados.adicionarEmpregado(novoAssalariado);
        }

        id = novoBancoDados.listaEmpregados.getLast().getId();

        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        String id;

        Comissionado novoComissionado = new Comissionado(nome, endereco, tipo, salario, comissao);
        novoBancoDados.adicionarEmpregado(novoComissionado);

        id = novoBancoDados.listaEmpregados.getLast().getId();

        return id;
    }

    public void removerEmpregado (String id) throws EmpregadoNaoExisteException {
        novoBancoDados.remover(id);
    }

    public void lancaCartao(String id, String data, String horas) throws EmpregadoNaoExisteException{
        novoBancoDados.lancaCartao(id, data, horas);
    }

    public void lancaVenda(String id, String data, String valor) throws EmpregadoNaoExisteException{
        novoBancoDados.lancaVenda(id, data, valor);
    }

    public void lancaTaxaServico(String id, String data, String valor) throws EmpregadoNaoExisteException{
        novoBancoDados.lancaTaxaServico(id, data, valor);
    }

    public String getAtributoEmpregado(String id,String atributo) throws EmpregadoNaoExisteException {
        throw new EmpregadoNaoExisteException();
    }
}
