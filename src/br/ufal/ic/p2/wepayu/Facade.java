package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;

public class Facade {
    BancoDados novoBancoDados = new BancoDados();

    public void zerarSistema(){
        novoBancoDados.zerar();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) throw new TipoInvalidoException();
        if(tipo.equals("comissionado")) throw new TipoNaoAplicavelException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(salario));

        Empregado novo;

        if(tipo.equals("horista")){
            novo = new Horista(nome, endereco, tipo, salarioFormatado);
        }

        else{
            novo = new Assalariado(nome, endereco, tipo, salarioFormatado);
        }

        novoBancoDados.adicionarEmpregado(novo);

        return novo.getId();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) throw new TipoInvalidoException();
        if(!tipo.equals("comissionado")) throw new TipoNaoAplicavelException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(salario));
        String comissaoFormatada = Validador.formatar(Validador.validarComissao(comissao));

        Comissionado novo = new Comissionado(nome, endereco, tipo, salarioFormatado, comissaoFormatada);
        novoBancoDados.adicionarEmpregado(novo);

        return novo.getId();
    }

    public void removerEmpregado (String id) throws EmpregadoNaoExisteException {
        novoBancoDados.remover(id);
    }

    public void lancaCartao(String id, String data, String horas) throws EmpregadoNaoExisteException{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        novoBancoDados.lancaCartao(id, data, horas);
    }

    public void lancaVenda(String id, String data, String valor) throws EmpregadoNaoExisteException{
        novoBancoDados.lancaVenda(id, data, valor);
    }

    public void lancaTaxaServico(String id, String data, String valor) throws EmpregadoNaoExisteException{
        novoBancoDados.lancaTaxaServico(id, data, valor);
    }

    public void alteraEmpregado(String id, String atributo, String valor) throws EmpregadoNaoExisteException {
        if(atributo.equals("tipo")){
            novoBancoDados.mudaTipoEmpregado(id, valor, null);
        }
        else{
            novoBancoDados.alteraEmpregado(id, atributo, valor);
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String salario) throws EmpregadoNaoExisteException {
        if(atributo.equals("tipo")){
            novoBancoDados.mudaTipoEmpregado(id, valor, salario);
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String extra1, String extra2) throws EmpregadoNaoExisteException {
        if(atributo.equals("tipo") && valor.equals("comissionado")){
            novoBancoDados.mudaTipoEmpregado(id, valor, extra1, extra2);
        }
        else if(atributo.equals("sindicalizado")){
            novoBancoDados.alteraEmpregado(id, atributo, valor, extra1, extra2);
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws EmpregadoNaoExisteException{
        novoBancoDados.alteraEmpregado(id, atributo, valor, banco, agencia, contaCorrente);
    }

    public String getEmpregadoPorNome(String nome, int indice){
        return novoBancoDados.buscarPorNome(nome, indice);
    }

    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException{
        return novoBancoDados.getHorasNormaisTrabalhadas(id, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException{
        return novoBancoDados.getHorasExtrasTrabalhadas(id, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        return novoBancoDados.getVendasRealizadas(id, dataInicial, dataFinal);
    }

    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        return novoBancoDados.getTaxasServico(id, dataInicial, dataFinal);
    }

    public String getAtributoEmpregado(String id,String atributo) throws EmpregadoNaoExisteException {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = novoBancoDados.buscar(id);

        if(atributo.equals("nome")){
            return empregado.getNome();
        }
        else if(atributo.equals("endereco")){
            return empregado.getEndereco();
        }
        else if(atributo.equals("tipo")){
            return empregado.getTipo();
        }
        else if(atributo.equals("salario")){
            return empregado.getSalario();
        }
        else if(atributo.equals("metodoPagamento")){
            return empregado.getMetodoPagamento();
        }
        else if(atributo.equals("sindicalizado")){
            return empregado.getSindicalizado();
        }
        else if(atributo.equals("idSindicato")){
            return empregado.getIdSindicato();
        }
        else if(atributo.equals("taxaSindical")){
            return empregado.getTaxaSindical();
        }
        else if(atributo.equals("banco")){
            return empregado.getBanco();
        }
        else if(atributo.equals("agencia")){
            return empregado.getAgencia();
        }
        else if(atributo.equals("contaCorrente")){
            return empregado.getContaCorrente();
        }
        else if(atributo.equals("comissao")){
            if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoComissionadoException();
            return ((Comissionado) empregado).getComissao();
        }
        throw new AtributoNaoExisteException();
    }

    public void encerrarSistema(){

    }
}
