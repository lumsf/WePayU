package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;

public class Banco {
    public List<Empregado> listaEmpregados = new ArrayList<>();
    int i = 0;

    public void adicionar(Empregado novoEmpregado){
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
}
