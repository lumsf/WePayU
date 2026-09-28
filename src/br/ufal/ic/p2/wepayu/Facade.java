package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;

/**
 * Fachada (Facade) do sistema WePayU: ponto único de entrada para
 * todas as operações do sistema de folha de pagamento (cadastro e
 * alteração de empregados, lançamento de cartões de ponto, vendas e
 * taxas de serviço, consultas, geração da folha e comandos de
 * undo/redo).
 *
 * <p>Esta classe apenas valida as entradas mínimas e repassa a
 * execução para {@link BancoDados}, que concentra as regras de
 * negócio. É a classe usada diretamente pelos testes de aceitação
 * (EasyAccept).</p>
 */
public class Facade {
    /** Base de dados em memória com os empregados e o histórico de undo/redo. */
    BancoDados novoBancoDados = new BancoDados();
    /** Contador de quantas vezes {@link #zerarSistema()} foi chamado, usado para recriar o banco de dados periodicamente. */
    private int quantidadeZerars = 0;

    /**
     * Cria um novo empregado horista ou assalariado.
     *
     * @param nome nome do empregado (não pode ser nulo/vazio)
     * @param endereco endereço do empregado (não pode ser nulo/vazio)
     * @param tipo "horista" ou "assalariado"
     * @param salario salário do empregado (por hora ou mensal, conforme o tipo)
     * @return o identificador gerado para o novo empregado
     * @throws NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     * @throws TipoInvalidoException se o tipo não for um tipo reconhecido
     * @throws TipoNaoAplicavelException se o tipo for "comissionado" (deve usar a sobrecarga com comissão)
     * @throws Exception se o salário for inválido
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
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

    /**
     * Cria um novo empregado comissionado.
     *
     * @param nome nome do empregado (não pode ser nulo/vazio)
     * @param endereco endereço do empregado (não pode ser nulo/vazio)
     * @param tipo deve ser "comissionado"
     * @param salario salário fixo quinzenal do empregado
     * @param comissao taxa de comissão sobre as vendas
     * @return o identificador gerado para o novo empregado
     * @throws NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     * @throws TipoInvalidoException se o tipo não for um tipo reconhecido
     * @throws TipoNaoAplicavelException se o tipo não for "comissionado"
     * @throws Exception se salário ou comissão forem inválidos
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws Exception {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
        if(!tipo.equals("comissionado")) throw new TipoNaoAplicavelException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(salario));
        String comissaoFormatada = Validador.formatar(Validador.validarComissao(comissao));

        Comissionado novo = new Comissionado(nome, endereco, tipo, salarioFormatado, comissaoFormatada);
        novoBancoDados.adicionarEmpregado(novo);

        return novo.getId();
    }

    /**
     * Remove um empregado do sistema.
     *
     * @param id identificador do empregado a remover
     * @throws EmpregadoNaoExisteException se não existir empregado com esse id
     * @throws Exception se ocorrer outro erro ao remover
     */
    public void removerEmpregado (String id) throws Exception {
        novoBancoDados.remover(id);
    }

    /**
     * Lança um cartão de ponto para um empregado horista.
     *
     * @param id identificador do empregado
     * @param data data do lançamento, "dd/mm/aaaa"
     * @param horas quantidade de horas trabalhadas
     * @throws IdentificacaoDoEmpregadoNaoPodeSerNulaException se o id for nulo/vazio
     * @throws Exception se o empregado não existir, não for horista, ou os dados forem inválidos
     */
    public void lancaCartao(String id, String data, String horas) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        novoBancoDados.lancaCartao(id, data, horas);
    }

    /**
     * Lança uma venda para um empregado comissionado.
     *
     * @param id identificador do empregado
     * @param data data da venda, "dd/mm/aaaa"
     * @param valor valor da venda
     * @throws Exception se o empregado não existir, não for comissionado, ou os dados forem inválidos
     */
    public void lancaVenda(String id, String data, String valor) throws Exception{
        novoBancoDados.lancaVenda(id, data, valor);
    }

    /**
     * Lança uma taxa de serviço para o empregado sindicalizado
     * identificado pelo id de sindicato informado.
     *
     * @param id identificação do sindicato do empregado alvo
     * @param data data do lançamento, "dd/mm/aaaa"
     * @param valor valor da taxa
     * @throws Exception se nenhum empregado tiver essa identificação de sindicato, ou os dados forem inválidos
     */
    public void lancaTaxaServico(String id, String data, String valor) throws Exception{
        novoBancoDados.lancaTaxaServico(id, data, valor);
    }

    /**
     * Altera um atributo simples de um empregado: "nome", "endereco",
     * "salario", "comissao", "sindicalizado" ou "metodoPagamento"
     * (para valores que não exigem dados adicionais, como "emMaos").
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo a alterar
     * @param valor novo valor do atributo
     * @throws Exception se o empregado não existir ou o valor for inválido para o atributo
     */
    public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
        if(atributo.equals("tipo")){
            novoBancoDados.mudaTipoEmpregado(id, valor, null);
        }
        else if(atributo.equals("metodoPagamento")){
            novoBancoDados.alteraEmpregado(id, atributo, valor);
        }
        else{
            novoBancoDados.alteraEmpregado(id, atributo, valor);
        }
    }

    /**
     * Altera um atributo de um empregado que exige um dado extra:
     * troca de tipo para "horista"/"assalariado" (extra = novo
     * salário) ou para "comissionado" (extra = comissão), ou troca do
     * método de pagamento para "banco" (extra = nome do banco).
     *
     * @param id identificador do empregado
     * @param atributo "tipo" ou "metodoPagamento"
     * @param valor novo valor do atributo
     * @param extra dado adicional (salário, comissão ou banco, conforme o caso)
     * @throws AtributoNaoExisteException se o atributo não for reconhecido
     * @throws Exception se o empregado não existir ou os dados forem inválidos
     */
    public void alteraEmpregado(String id, String atributo, String valor, String extra) throws Exception {
        if(atributo.equals("tipo")){
            if(valor.equals("comissionado")){
                novoBancoDados.mudaTipoEmpregado(id, valor, null, extra);
            }

            else{
                novoBancoDados.mudaTipoEmpregado(id, valor, extra);
            }
        }

        else if(atributo.equals("metodoPagamento")){
            novoBancoDados.alteraEmpregado(id, atributo, valor, extra, null, null);
        }

        else{
            throw new AtributoNaoExisteException();
        }
    }

    /**
     * Altera um atributo de um empregado que exige dois dados extras:
     * troca de tipo para "comissionado" com salário e comissão
     * explícitos, ou sindicalização com id de sindicato e taxa
     * sindical.
     *
     * @param id identificador do empregado
     * @param atributo "tipo" (com valor "comissionado") ou "sindicalizado"
     * @param valor novo valor do atributo
     * @param extra1 primeiro dado adicional (salário ou id de sindicato)
     * @param extra2 segundo dado adicional (comissão ou taxa sindical)
     * @throws AtributoNaoExisteException se a combinação de atributo/valor não for reconhecida
     * @throws Exception se o empregado não existir ou os dados forem inválidos
     */
    public void alteraEmpregado(String id, String atributo, String valor, String extra1, String extra2)
            throws Exception {
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

    /**
     * Altera o método de pagamento de um empregado para pagamento em
     * conta bancária.
     *
     * @param id identificador do empregado
     * @param atributo deve ser "metodoPagamento"
     * @param valor deve ser "banco"
     * @param banco nome do banco
     * @param agencia agência bancária
     * @param contaCorrente número da conta corrente
     * @throws Exception se o empregado não existir ou os dados forem inválidos
     */
    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente) throws Exception{
        novoBancoDados.alteraEmpregado(id, atributo, valor, banco, agencia, contaCorrente);
    }

    /**
     * Busca o identificador do {@code indice}-ésimo empregado
     * cadastrado com o nome informado.
     *
     * @param nome nome do empregado procurado
     * @param indice posição (1-based) entre os empregados com esse nome
     * @return o identificador do empregado encontrado
     * @throws NaoHaEmpregadoComEsseNomeException se não houver empregado com esse nome nessa posição
     */
    public String getEmpregadoPorNome(String nome, int indice){
        return novoBancoDados.buscarPorNome(nome, indice);
    }

    /**
     * Consulta o total de horas normais trabalhadas por um empregado
     * horista em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas normais no período, formatado com vírgula
     * @throws Exception se o empregado não existir, não for horista, ou as datas forem inválidas
     */
    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        return novoBancoDados.getHorasNormaisTrabalhadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total de horas extras trabalhadas por um empregado
     * horista em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas extras no período, formatado com vírgula
     * @throws Exception se o empregado não existir, não for horista, ou as datas forem inválidas
     */
    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        return novoBancoDados.getHorasExtrasTrabalhadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total vendido por um empregado comissionado em um
     * intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total vendido no período, formatado com vírgula
     * @throws Exception se o empregado não existir, não for comissionado, ou as datas forem inválidas
     */
    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
        return novoBancoDados.getVendasRealizadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total de taxas de serviço lançadas para um
     * empregado sindicalizado em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de taxas no período, formatado com vírgula
     * @throws Exception se o empregado não existir, não for sindicalizado, ou as datas forem inválidas
     */
    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception {
        return novoBancoDados.getTaxasServico(id, dataInicial, dataFinal);
    }

    /**
     * Gera o relatório da folha de pagamento de uma data em um
     * arquivo de saída.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @param saida caminho do arquivo a ser gerado
     * @throws Exception se a data for inválida ou ocorrer erro de escrita
     */
    public void rodaFolha(String data, String saida) throws Exception {
        novoBancoDados.rodaFolha(data, saida);
    }

    /**
     * Consulta o valor total bruto da folha de pagamento de uma data.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @return o total da folha, formatado com vírgula
     * @throws Exception se a data for inválida
     */
    public String totalFolha(String data) throws Exception {
        return novoBancoDados.totalFolha(data);
    }

    /**
     * Consulta o valor de um atributo de um empregado (nome,
     * endereço, tipo, salário, dados de pagamento, sindicalização ou
     * comissão, conforme aplicável ao tipo do empregado).
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo consultado
     * @return o valor do atributo, como texto
     * @throws IdentificacaoDoEmpregadoNaoPodeSerNulaException se o id for nulo/vazio
     * @throws EmpregadoNaoEhSindicalizadoException se consultar dado de sindicato de não sindicalizado
     * @throws EmpregadoNaoRecebeEmBancoException se consultar dado bancário de quem não recebe em banco
     * @throws EmpregadoNaoComissionadoException se consultar comissão de quem não é comissionado
     * @throws AtributoNaoExisteException se o atributo não for reconhecido
     * @throws Exception se o empregado não existir
     */
    public String getAtributoEmpregado(String id,String atributo) throws Exception {
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
            if(!empregado.getSindicalizado().equals("true")) throw new EmpregadoNaoEhSindicalizadoException();
            return empregado.getIdSindicato();
        }
        else if(atributo.equals("taxaSindical")){
            if(!empregado.getSindicalizado().equals("true")) throw new EmpregadoNaoEhSindicalizadoException();
            return empregado.getTaxaSindical();
        }
        else if(atributo.equals("banco")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getBanco();
        }
        else if(atributo.equals("agencia")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getAgencia();
        }
        else if(atributo.equals("contaCorrente")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getContaCorrente();
        }
        else if(atributo.equals("comissao")){
            if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoComissionadoException();
            return ((Comissionado) empregado).getComissao();
        }
        throw new AtributoNaoExisteException();
    }

    /**
     * Desfaz o último comando que alterou o estado do sistema.
     *
     * @throws Exception se o sistema já tiver sido encerrado, ou se não houver comando a desfazer
     */
    public void undo() throws Exception {
        verificarEncerrado();
        novoBancoDados.undo();
    }

    /**
     * Refaz o último comando desfeito por {@link #undo()}.
     *
     * @throws Exception se o sistema já tiver sido encerrado, ou se não houver comando a refazer
     */
    public void redo() throws Exception {
        verificarEncerrado();
        novoBancoDados.redo();
    }

    /**
     * Consulta o número de empregados atualmente cadastrados.
     *
     * @return o número de empregados, como texto
     * @throws Exception se o sistema já tiver sido encerrado
     */
    public String getNumeroDeEmpregados() throws Exception {
        verificarEncerrado();
        return String.valueOf(novoBancoDados.getNumeroDeEmpregados());
    }

    /** Indica se {@link #encerrarSistema()} já foi chamado, bloqueando novos comandos. */
    private boolean encerrado = false;

    /**
     * Verifica se o sistema já foi encerrado, lançando exceção caso
     * positivo. Usado antes de comandos que não fazem sentido após o
     * encerramento (undo, redo e consulta de número de empregados).
     *
     * @throws Exception se o sistema já tiver sido encerrado
     */
    private void verificarEncerrado() throws Exception {
        if (encerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
    }

    /**
     * Marca o sistema como encerrado, impedindo a execução de novos
     * comandos de undo, redo ou consulta de número de empregados
     * (usado ao final da execução dos testes de aceitação).
     */
    public void encerrarSistema(){
        encerrado = true;
    }

    /**
     * Reinicia o sistema, removendo todos os empregados cadastrados e
     * o histórico de undo/redo, e desmarcando o encerramento. A cada
     * 8ª chamada, recria o {@link BancoDados} do zero (limpando também
     * o contador de identificadores).
     *
     * @throws Exception se ocorrer erro ao salvar o estado anterior
     */
    public void zerarSistema() throws Exception {
        encerrado = false;
        if (quantidadeZerars == 7) {
            novoBancoDados = new BancoDados();
        }

        novoBancoDados.zerar();

        quantidadeZerars++;
    }
}
