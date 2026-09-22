package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;

public class Banco {
    public List<Empregado> listaEmpregados = new ArrayList<>();
    int i = 0;

    public void adicionarEmpregado(Empregado novoEmpregado){
        i++;

        String id = "id" + i;
        novoEmpregado.setId(id);

        listaEmpregados.add(novoEmpregado);

    }

    public void remover(String id) throws EmpregadoNaoExisteException {
        for (int j = 0; j < listaEmpregados.size(); j++) {
            if (listaEmpregados.get(j).getId().equals(id)) {
                listaEmpregados.remove(j);
                return;
            }
        }

        throw new EmpregadoNaoExisteException();
    }

    public Empregado buscar(String id) throws EmpregadoNaoExisteException{
        for(Empregado empregado : listaEmpregados){
            if(empregado.getId().equals(id)){
                return empregado;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public void lancaCartao(String id, String data, String horas) throws EmpregadoNaoExisteException{
        for(int j = 0; j < listaEmpregados.size(); j++){
            if(listaEmpregados.get(j).getId().equals(id)){
                CartaoDePonto cartao = new CartaoDePonto(data, horas);
                listaEmpregados.get(j).adicionarCartao(cartao);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public void lancaVenda(String id, String data, String valor) throws EmpregadoNaoExisteException{
        for(int j = 0; j < listaEmpregados.size(); j++){
            if(listaEmpregados.get(j).getId().equals(id)){
                Venda venda = new Venda(data, valor);
                listaEmpregados.get(j).adicionarVenda(venda);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }
}
