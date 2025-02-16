import Grafi.Strutture_Dati.Grafo.Grafo;
import TSP.TSP_Branching;
import TSP.TSP_Results;
import TSP.TSP_Sotto_Problema.Exception_Unfeasible_Problem;

import java.io.*;
import java.util.Iterator;

public class Calcolo_Percorso {

    public static void main(String[] args) throws IOException {
        String[] impostazioniGrafo = selezioneGrafo();
        boolean passaggi= Boolean.parseBoolean(impostazioniGrafo[1]);
        boolean csv =Boolean.parseBoolean(impostazioniGrafo[2]);;
        boolean rimozioneNodiInvalidi = Boolean.parseBoolean(impostazioniGrafo[2]);;

        Grafo<Integer, Integer, Integer> graph = caricaGrafo(impostazioniGrafo[0]);
        TSP_Branching branchAndBound_Resolution = new TSP_Branching(graph,passaggi);

        long tempoPartenza = System.currentTimeMillis();

        TSP_Results results = null;
        try {
            results = branchAndBound_Resolution.TSP_Resolution(rimozioneNodiInvalidi);
        } catch (Exception_Unfeasible_Problem e) {
            System.err.println("Alcuni nodi hanno un solo arco incidente " +
                               "La seguente è la lista dei nodi problematici:");
            System.err.println(e.chiaviNodiInvalidi.toString());
            System.exit(1);
        }

        long tempoEsecuzione = System.currentTimeMillis() - tempoPartenza;

        System.out.println(results.toString());
        branchAndBound_Resolution.aggiornaLog(results.toString());
        System.out.println(results.get_Statistiche());
        branchAndBound_Resolution.aggiornaLog(results.get_Statistiche());
        System.out.println("Tempo complessivo di esecuzione: " + tempoEsecuzione + " millisecondi");
        branchAndBound_Resolution.aggiornaLog("Tempo complessivo di esecuzione: " + tempoEsecuzione + " millisecondi");

        if(csv==true)
           branchAndBound_Resolution.StampaLog(nomeGrafo().substring(0, nomeGrafo().length() - 4));

    }


    private static Grafo<Integer, Integer, Integer> caricaGrafo(String percorsoFile) throws IOException {
        File fileGrafo = new File(percorsoFile);
        Grafo<Integer, Integer, Integer> grafo = new Grafo<>(false);

        try (BufferedReader lineReader = new BufferedReader(new FileReader(fileGrafo))) {
            Iterator<String> lineIterator = lineReader.lines().iterator();

            while (lineIterator.hasNext()) {
                String impostazione = lineIterator.next();
                String[] componenti = impostazione.split(" ");
                int da = Integer.parseInt(componenti[0]);
                int a = Integer.parseInt(componenti[1]);
                int peso;

                try {
                    peso = Integer.parseInt(componenti[2]);
                } catch (NumberFormatException e) {
                    peso = (int) (Float.parseFloat(componenti[2]) * 100);
                }
                grafo.addNodesEdge(da, a, peso);

            }
        } catch (FileNotFoundException e) {
            System.out.println("No file exists at the specified path");
            System.exit(1);
        }

        return grafo;
    }

    private static  String[] selezioneGrafo() {
        String percorso="/Users/itsallmacman/Downloads/OC_tesina 8/implementazione/resources/";
        BufferedReader reader;
        String [] results = new String[4];
        try {
            reader = new BufferedReader(new FileReader(
                    "/Users/itsallmacman/Downloads/OC_tesina 8/implementazione/nomeGrafo.txt"));

            percorso = percorso+reader.readLine();
            results[0]= percorso;
            results[1]= reader.readLine();
            results[2]= reader.readLine();
            results[3]= reader.readLine();

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    private static  String nomeGrafo() {
        String percorso="";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "/Users/itsallmacman/Downloads/OC_tesina 8/implementazione/nomeGrafo.txt"));

            percorso = reader.readLine();


            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return percorso;
    }

}
