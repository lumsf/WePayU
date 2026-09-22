package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facade {
    Banco novoBanco = new Banco();

    public void zerarSistema(){
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        String id;

        if(tipo.equals("Horista")){
            Horista novoHorista = new Horista(nome, endereco, tipo, salario);
            novoBanco.adicionarEmpregado(novoHorista);
        }

        else {
            Assalariado novoAssalariado = new Assalariado(nome, endereco, tipo, salario);
            novoBanco.adicionarEmpregado(novoAssalariado);
        }

        id = novoBanco.listaEmpregados.getLast().getId();

        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        String id;

        Comissionado novoComissionado = new Comissionado(nome, endereco, tipo, salario, comissao);
        novoBanco.adicionarEmpregado(novoComissionado);

        id = novoBanco.listaEmpregados.getLast().getId();

        return id;
    }

    public void removerEmpregado (String id) throws EmpregadoNaoExisteException {
        novoBanco.remover(id);
    }

    public void lancaCartao(String id, String data, String horas) throws EmpregadoNaoExisteException{
        novoBanco.lancaCartao(id, data, horas);
    }

    public void lancaVenda(String id, String data, String valor) throws EmpregadoNaoExisteException{
        novoBanco.lancaVenda(id, data, valor);
    }

    public String getAtributoEmpregado(String id,String atributo) throws EmpregadoNaoExisteException {
        throw new EmpregadoNaoExisteException();
    }
}
