package TSP;

import Grafi.Strutture_Dati.Arco;
import Grafi.Strutture_Dati.Grafo.Grafo;
import Grafi.Strutture_Dati.Nodo;
import TSP.TSP_Sotto_Problema.SubProblem_computation;
import TSP.TSP_Sotto_Problema.Exception_Unfeasible_Problem;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TSP_Branching {
    Boolean Passaggi;
    String Log="";
    int nodi =1;
    int HTrovati=0;
    int TTrovati=0;
    int HAnalizzati=0;
    int TAnalizzati=0;
    private final Grafo<Integer, Integer, Integer> grafoPrincipale;
    private final Integer NodoCandidato;
    private ArrayList<SubProblem_computation> ListaSottoProblemi_H;
    private ArrayList<SubProblem_computation> ListaSottoProblemi_T;

    public TSP_Branching(Grafo<Integer, Integer, Integer> grafo, Boolean passaggi) {
        this(grafo, grafo.getNodes().get(0).getKey(),passaggi); //scelgo come nodo candidato quello di indice 0 (il primo)
    }

    public TSP_Branching(Grafo<Integer, Integer, Integer> grafo, Integer candidateNode, Boolean passaggi) {
        this.grafoPrincipale = grafo.clone();
        this.ListaSottoProblemi_H = new ArrayList<>();
        this.ListaSottoProblemi_T = new ArrayList<>();
        this.NodoCandidato = candidateNode;
        this.Passaggi=passaggi;

    }

    public TSP_Results TSP_Resolution(boolean ignoraNodiDegree1) throws Exception_Unfeasible_Problem {
        List<Integer> nodiEliminabili = grafoPrincipale.getNodes()
                                             .stream()
                                             .filter(node -> node.getDegree() < 2)
                                             .map(Nodo::getKey)
                                             .collect(Collectors.toUnmodifiableList());
        String Log_Temp="";
        if (nodiEliminabili.size() > 0) {
            if (ignoraNodiDegree1) {
                nodiEliminabili.forEach(grafoPrincipale::removeNode);
                System.out.println("Rimozione dei nodi {}." + nodiEliminabili.size());
            } else {
                throw new Exception_Unfeasible_Problem(nodiEliminabili);
            }
        }

        TSP_Results MinTSP_Results = new TSP_Results(grafoPrincipale, Integer.MAX_VALUE);//upper bound iniziale + infinito
        SubProblem_computation ProblemaIniziale = new SubProblem_computation(grafoPrincipale, NodoCandidato);
        if (ProblemaIniziale.cicloHamiltoniano()) {
            ListaSottoProblemi_H.add(HTrovati,ProblemaIniziale); HTrovati++;
        } else{ // 1-Tree
            ListaSottoProblemi_T.add(TTrovati,ProblemaIniziale); TTrovati++;
        }
        System.out.println("");
        MinTSP_Results.set_ConteggioNodiTotali(MinTSP_Results.get_ConteggioNodiTotali()+1);// contatore di nodi



        while(HTrovati> HAnalizzati || TTrovati> TAnalizzati) {
            SubProblem_computation problemaCorrente;

            if(HTrovati>HAnalizzati){ //Carico un Sottoproblema aperto dalla lista dei cicliHamiltoniani o 1-tree
                problemaCorrente = ListaSottoProblemi_H.get(HAnalizzati);
                HAnalizzati++;
            } else {
                problemaCorrente = ListaSottoProblemi_T.get(TAnalizzati);
                TAnalizzati++;
            }

            if (problemaCorrente.Feasible()) {  //se feasable
                if (problemaCorrente.cicloHamiltoniano()) {// se contiene ciclo hamiltoniano
                    Log_Temp= "("+problemaCorrente.getNodi()+") Ciclo hamiltoniano: "+problemaCorrente.stampaHamiltoniano()+" Costo="+problemaCorrente.get_LowerBound()+"\nPOSSIBILE SOLUZIONE\n";
                    aggiornaLog(Log_Temp);
                    if(Passaggi==true)
                        System.out.println(Log_Temp); //se contiene ciclo hamiltoniano
                    // se nuova migliore soluzione (lb minore del miglior ciclo H trovato attualmente)
                    if (MinTSP_Results.get_Costo() >= problemaCorrente.get_LowerBound()) {
                        if (MinTSP_Results.get_Costo() > problemaCorrente.get_LowerBound())
                            MinTSP_Results.eliminaVecchiaSoluzioneMigliore(); //se è migliore di tutte le precedenti le cancello
                        MinTSP_Results.nuovaSoluzione(problemaCorrente.get_1Tree(), problemaCorrente.get_LowerBound());
                        // chiudo il nodo per ottimalità
                        MinTSP_Results.set_NodiChiusiPerSoluzioneMigliore(MinTSP_Results.get_NodiChiusiPerSoluzioneMigliore()+1);
                    }
                // siamo in un 1-tree non hamiltoniano, il quale lb risulta minore della migliore soluzione trovata attualmente
                } else if (problemaCorrente.get_LowerBound() < MinTSP_Results.get_Costo()) {
                    Log_Temp= "("+problemaCorrente.getNodi()+") 1-tree: "+ problemaCorrente.get_1Tree()+ " Costo="+problemaCorrente.get_LowerBound();
                    aggiornaLog(Log_Temp);
                    if(Passaggi==true)
                        System.out.println(Log_Temp);
                    //effettuo il braching del nodo in questione (che non posso chiudere)
                    int conteggioNuovoNodo = TSP_Branching.this.branchAndBound(problemaCorrente);
                    MinTSP_Results.set_ConteggioNodiTotali(MinTSP_Results.get_ConteggioNodiTotali()+conteggioNuovoNodo);
                    MinTSP_Results.set_NodiDiBranching(MinTSP_Results.get_NodiDiBranching()+1);
                } else {
                    Log_Temp= "("+problemaCorrente.getNodi()+") 1-tree: "+ problemaCorrente.get_1Tree()+ " Costo="+problemaCorrente.get_LowerBound()+"\nCHIUSO PER BOUND"+"\n";
                    aggiornaLog(Log_Temp);
                    if(Passaggi==true)
                        System.out.println(Log_Temp);
                    // else (1-tree non hamiltoniano, il quale lb risulta maggiore o uguale della migliore soluzione trovata attualmente) chiudo per bound
                    MinTSP_Results.set_NodiChiusiPerBound(MinTSP_Results.get_NodiChiusiPerBound()+1);
                }
            } else {
                // se il nodo non è feasable (impossibile computare un 1-tree per il dato nodo) chiudo per unfeasibility
                Log_Temp= "("+problemaCorrente.getNodi()+") 1-tree: "+ problemaCorrente.get_1Tree()+ " Costo="+problemaCorrente.get_LowerBound()+"\nCHIUSO PER UNFEASIBILITY"+"\n";
                aggiornaLog(Log_Temp);
                if(Passaggi==true)
                    System.out.println(Log_Temp);
                MinTSP_Results.set_NodiChiusiPerUnfeasibility(MinTSP_Results.get_NodiChiusiPerUnfeasibility()+1);
            }
        }

        MinTSP_Results.risultatoFinale();

        return MinTSP_Results;
    }


    private int branchAndBound(SubProblem_computation problemaCorrente) {
       String Log_temp="";
        Log_temp= "PROCEDURA DI BRANCHING";
        aggiornaLog(Log_temp);
        if(Passaggi==true)
            System.out.println(Log_temp);
        HashMap<Integer, Integer> vettorePadri = new HashMap<>();

        // costruzione sottociclo per il branch e la creazione dei nodi figli

        // ricerca in profondità per ottenere il vettore dei padri
        dfs(problemaCorrente.get_1Tree().getNode(NodoCandidato), vettorePadri, problemaCorrente.get_1Tree());
        int conteggioNuoviNodi = 0;
        ArrayList<Arco<Integer, Integer>> sottoCiclo = new ArrayList<>();

        int nodoPartenza = NodoCandidato;
        int nodoArrivo = Integer.MAX_VALUE;
        // partendo dal nodo candidato n e, procedendo con i vettori dei padri a ritroso, si individua il ciclo
        while (nodoArrivo != NodoCandidato) { //fin quando il nodo di partenza non è il candidato
            nodoArrivo = vettorePadri.get(nodoPartenza);// nodoPartenza chiave hash map e nodoArrivo valore
            sottoCiclo.add(problemaCorrente.get_1Tree().getEdge(nodoArrivo, nodoPartenza)); // aggiunge l'arco nodoArrivo to Node al sottociclo
            nodoPartenza = nodoArrivo; //aggiorna il nodoPartenza
        }
        sottoCiclo=riordinaSottoCiclo(sottoCiclo);

        Log_temp= "sottociclo: "+ stampaSottoCiclo(sottoCiclo);
        aggiornaLog(Log_temp);
        if(Passaggi==true)
            System.out.println(Log_temp);

        ArrayList<Arco<Integer, Integer>> archiObbligatori = problemaCorrente.get_ArchiObbligatori();
        ArrayList<Arco<Integer, Integer>> archiProibiti = problemaCorrente.get_ArchiProibiti();
        // cicla su ogni arco del sottociclo
        for (Arco<Integer, Integer> integerIntegerArco : sottoCiclo) {

            // costruzione nodi figli (subproblem) con i relativi associati insiemi E0 e E1
            if (!(problemaCorrente.get_ArchiObbligatori().contains(integerIntegerArco) ||  //controllo se l'arco del sottociclo non è contenuto nella lista degli archi mandatory
                  problemaCorrente.get_ArchiObbligatori().contains(integerIntegerArco.inverse()))) {
                nodi++;
                archiProibiti.add(integerIntegerArco); // aggiunge l'arco alla lista forbidden (inizializzo EO)
                // creazione di un nuovo subproblem (nodo figlio)
                SubProblem_computation sottoProblema = new SubProblem_computation(grafoPrincipale,
                                                           new ArrayList<>(archiObbligatori),
                                                           new ArrayList<>(archiProibiti),
                                                           NodoCandidato,
                                                          problemaCorrente.get_LivelloAlberoSottoProblemi() + 1,
                                                          nodi);

                if (sottoProblema.cicloHamiltoniano()) {
                    ListaSottoProblemi_H.add(HTrovati,sottoProblema);
                    HTrovati++;
                } else{
                    ListaSottoProblemi_T.add(TTrovati,sottoProblema);
                    TTrovati++;
                }

                Log_temp= "("+ sottoProblema.getNodi()+") EO:"+ stampaArchi(archiProibiti) +" E1:"+ stampaArchi(archiObbligatori);
                aggiornaLog(Log_temp);
                if(Passaggi==true)
                    System.out.println(Log_temp);
                conteggioNuoviNodi++;
                archiProibiti.remove(integerIntegerArco);// elimino l'arco del sottociclo dagli archi forbidden (update EO per il nodo figlio successivo)
                archiObbligatori.add(integerIntegerArco); // aggiungo quell'arco negli archi mandatory  (update E1 per il nodo figlio successivo)
            }
        }

        Log_temp= "CALCOLO DEI NUOVI GRAFI\n";
        aggiornaLog(Log_temp);
        if(Passaggi==true)
            System.out.println(Log_temp);

        return conteggioNuoviNodi;
    }

    public void aggiornaLog(String aggiornamento){
        this.Log= Log+"\n"+aggiornamento;
    }

    public void StampaLog(String nomeGrafo){
        String percorsoSoluzione="/Users/itsallmacman/Downloads/OC_tesina 8/implementazione/results";

        try {
            FileWriter file = new FileWriter(percorsoSoluzione+"/"+nomeGrafo+"Solution.csv");
            BufferedWriter output = new BufferedWriter(file);
            output.write(Log);
            output.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }


    public  ArrayList<Arco<Integer, Integer>> riordinaSottoCiclo(ArrayList<Arco<Integer, Integer>> sottoCiclo){
        ArrayList<Arco<Integer, Integer>> risultato = new  ArrayList<Arco<Integer, Integer>>();
        int i=0;
        int MinimoD=Integer.MAX_VALUE;
        int MinimoA=Integer.MAX_VALUE;
        int pos=0;
        int posD=0;
        int posA=0;
        int g=0;
        String CD;
        String CA;
        String[] componente;
        String[] Da ;
        String[] A   ;
        String[] Etichetta;
        String da;
        String a;
        String etichetta;

        while (i<sottoCiclo.size()){
            componente = sottoCiclo.get(i).toString().split(",");
            Da  = componente[0].split("=");
            A    = componente[1].split("=");
            CD = Da[1];
            CA = A[1];
            if(Integer.parseInt(CD)<MinimoD){
                MinimoD=Integer.parseInt(CD); posD=i;
            }
            if(Integer.parseInt(CA)<MinimoA){
                MinimoA=Integer.parseInt(CA); posA=i;
            }
            i++;
        }

        if(MinimoD<= MinimoA){
            pos=posD;
            componente = sottoCiclo.get(pos).toString().split(",");
            Da  = componente[0].split("=");
            A    = componente[1].split("=");
            Etichetta = componente[2].split("=");
            da = Da[1];
            a = A[1];
            etichetta=Etichetta[1].substring(0, Etichetta[1].length() - 1);
            risultato.add(new Arco(Integer.parseInt(da),Integer.parseInt(a),Integer.parseInt(etichetta)));
        }else {
            pos=posA;
            componente = sottoCiclo.get(pos).toString().split(",");
            Da  = componente[0].split("=");
            A    = componente[1].split("=");
            Etichetta = componente[2].split("=");
            da = A[1];
            a = Da[1];
            etichetta=Etichetta[1].substring(0, Etichetta[1].length() - 1);
            risultato.add(new Arco(Integer.parseInt(da),Integer.parseInt(a),Integer.parseInt(etichetta)));
        }

        i=0;
        while (i<sottoCiclo.size()){
            if(i!=pos){
                componente = sottoCiclo.get(i).toString().split(",");
                Da  = componente[0].split("=");
                A    = componente[1].split("=");
                Etichetta = componente[2].split("=");
                CD = Da[1];
                CA = A[1];
                etichetta=Etichetta[1].substring(0, Etichetta[1].length() - 1);

                if(a.equals(CD)){
                    risultato.add(new Arco(Integer.parseInt(CD),Integer.parseInt(CA),Integer.parseInt(etichetta)));
                    a= CA; g++;
                }else if(a.equals(CA)){
                    risultato.add(new Arco(Integer.parseInt(CA),Integer.parseInt(CD),Integer.parseInt(etichetta)));
                    a= CD; g++;
                }
            }
            i++;
            if(i>=sottoCiclo.size() && g<sottoCiclo.size()-1)
                i=0;
        }

        return risultato;
    }




    public String stampaSottoCiclo(ArrayList<Arco<Integer, Integer>> sottoCiclo ){
        ArrayList archi = sottoCiclo;
        int i=0;
        int g=0;
        String da ="";
        String a = "";
        String CD="";
        String CA="";
        String[] componente = archi.get(i).toString().split(",");
        String[] Da  = componente[0].split("=");
        String[] A    = componente[1].split("=");
        String[] Etichetta = componente[2].split("=");
        da = Da[1];
        a = A[1];
        i++;
        String percorso = "("+da+")--("+a+")";
        while (i<archi.size()){
            componente = archi.get(i).toString().split(",");
            Da  = componente[0].split("=");
            A    = componente[1].split("=");
            CD = Da[1];
            CA = A[1];

            if(a.equals(CD)){
                percorso= percorso+"--("+CA+")";
                a= CA; g++;
            }else if(a.equals(CA)){
                percorso= percorso+"--("+CD+")";
                a= CD; g++;
            }
            i++;

            if(i==archi.size() && g!=archi.size()-1){
                i=1;
            }
        }

        return percorso;
    }

    public String stampaArchi(ArrayList<Arco<Integer, Integer>> Arco){
        ArrayList Archi = Arco;
        int i=0;
        String CD="";
        String CA="";
        String[] componente ;
        String[] Da  ;
        String[] A  ;
        String percorso = "[";
        while (i<Archi.size()){
            componente = Archi.get(i).toString().split(",");
            Da  = componente[0].split("=");
            A    = componente[1].split("=");
            CD = Da[1];
            CA = A[1];
            percorso= percorso + "("+CD+","+CA+")";
            i++;
        }

        return percorso+"]";
    }


    // ricerca Depth first per l'individuazione dei vettori padri del sottocircuito da calcolare
    private void dfs(Nodo<Integer, Integer, Integer> nodoCorrente,
                     HashMap<Integer, Integer> vettorePadri,
                     Grafo<Integer, Integer, Integer> grafo) {
        for (Arco<Integer, Integer> arcoUscente : nodoCorrente.getEdges()) { //scorre la lista di adiacenze del nodo corrente(candidato)  e per ogni arco di questa lista
            if (!vettorePadri.containsKey(arcoUscente.getTo())) { // se il vettore dei padri non contiene il nodo d'arrivo dell'arco analizzato e se non contiene il nodo corrente o
                if (!vettorePadri.containsKey(nodoCorrente.getKey()) || //se il nodo corrente non è uguale al nodo d'arrivo dell'arco analizzato
                    !vettorePadri.get(nodoCorrente.getKey()).equals(arcoUscente.getTo())) {

                    vettorePadri.put(arcoUscente.getTo(), nodoCorrente.getKey());           //inserisco questo nodo d'arrivo all'hash map
                    dfs(grafo.getNode(arcoUscente.getTo()), vettorePadri, grafo);           //effettuo una chiamata ricorsiva alla dfs, analizzando questo nodo d'arrivo (come nuovo nodo corrente)
                }
            }
        }
    }

}
