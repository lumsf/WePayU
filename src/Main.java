import br.ufal.ic.p2.wepayu.Facade;
import easyaccept.EasyAccept;
import easyaccept.QuitSignalException;
import util.Variables;
import util.VariablesImpl;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception{

        Facade facade = new Facade();
        EasyAccept easyAccept = new EasyAccept();

        executarTeste(easyAccept, facade, "tests/us1.txt");
        executarTeste(easyAccept, facade, "tests/us1_1.txt");

        executarTeste(easyAccept, facade, "tests/us2.txt");
        executarTeste(easyAccept, facade, "tests/us2_1.txt");

        executarTeste(easyAccept, facade, "tests/us3.txt");
        executarTeste(easyAccept, facade, "tests/us3_1.txt");

        executarTeste(easyAccept, facade, "tests/us4.txt");
        executarTeste(easyAccept, facade, "tests/us4_1.txt");

        executarTeste(easyAccept, facade, "tests/us5.txt");
        executarTeste(easyAccept, facade, "tests/us5_1.txt");

        executarTeste(easyAccept, facade, "tests/us6.txt");
        executarTeste(easyAccept, facade, "tests/us6_1.txt");

//        EasyAccept.main(new String[]{facade, "tests/us1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us2.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us2_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us3.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us3_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us4.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us4_1.txt"});
//
//        EasyAccept.main(new String[]{facade, "tests/us5.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us5_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us6.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us6_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us7.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us8.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us9.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us9_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us10.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us10_1.txt"});
    }
    private static void executarTeste(EasyAccept easyAccept, Facade facade, String arquivo){
        Variables variables = new VariablesImpl();

        try{
            easyAccept.runAcceptanceTest(facade, arquivo, variables);
        }
        catch (QuitSignalException e){}
        catch (Exception e){
            e.printStackTrace();
        }
    }
}


