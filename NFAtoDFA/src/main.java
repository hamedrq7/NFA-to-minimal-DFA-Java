import java.util.*;

public class main {
    public static void main(String[] args) {
        int nStates, nAlphabet, nTransitions;
        int nFinalStates, startingState;

        Scanner scanner = new Scanner(System.in);

        System.out.println("enter number of states...");
        nStates = scanner.nextInt();
        //q0 .... qn-1

        System.out.println("enter size of alphabet...");
        nAlphabet = scanner.nextInt();

        ArrayList<Character> alphabet = new ArrayList<>();
        System.out.println("enter alphabet characters...");
        for(int i = 0; i < nAlphabet; i++) {
            alphabet.add(scanner.next().charAt(0));
        }
        /////////////adding lambda
        alphabet.add('e');
        nAlphabet = alphabet.size();

        System.out.println(alphabet);

        System.out.println("enter size of final states...");
        nFinalStates = scanner.nextInt();
        ArrayList<Integer> nfaFinalStates = new ArrayList<>();
        System.out.println("enter #final states...  (*" + nFinalStates + ")");
        for(int i = 0; i < nFinalStates; i++) {
            nfaFinalStates.add(scanner.nextInt());
        }

        System.out.println("enter #starting state...");
        startingState = scanner.nextInt();

        System.out.println("enter number of transitions...");
        nTransitions = scanner.nextInt();

        //making NFATable
        HashMap<Integer, HashMap<Character, ArrayList<Integer>>> NFATable = new HashMap<>();
        //init
        for(int currState = 0; currState < nStates; currState++) {
            HashMap<Character, ArrayList<Integer>> initMap = new HashMap<>();
            for(Character c : alphabet) {
                ArrayList<Integer> initArrayList = new ArrayList<>();
                initMap.put(c, initArrayList);
            }
            NFATable.put(currState, initMap);
        }

        System.out.println("enter transition functions like this: qi character qj (as S(qi, character) = qj)...");
        for(int i = 0; i < nTransitions; i++) {
            int qi, qj;
            Character ch;
            qi = scanner.nextInt();
            ch = scanner.next().charAt(0);
            qj = scanner.nextInt();

            NFATable.get(qi).get(ch).add(qj);
        }

        ///////////////////////////////////  NFA -> DFA
        //subset construction:

        DFA dfa = nfaToDfa(startingState, nfaFinalStates, alphabet, NFATable);

        for(int currStateKey: dfa.DFATable.keySet()) {
            System.out.print("Sate: " + currStateKey);
            if(currStateKey==dfa.getStartingState()) System.out.print(" -start- ");
            if(dfa.DFATable.get(currStateKey).isFinal()) System.out.println(" (FINAL) ");
            else System.out.println();
            for(Character c : dfa.DFATable.get(currStateKey).dfaMoves.keySet()) {
                System.out.println("\tChar-"+c+": "+dfa.DFATable.get(currStateKey).dfaMoves.get(c));
            }
            System.out.print("NFA states that they contain...\t");
            for(int x : dfa.DFATable.get(currStateKey).getNfaStates()) {
                System.out.print(x + ", ");
            }
            System.out.println();
            System.out.println("-------");
        }


        /////////////////////////////////// DFA -> MINIMAL DFA
        //

        DFA minimalDfa = toMinimalDfa(dfa, alphabet);

        System.out.println("MINIMAL DFA _________________________________:");
        for(int currStateKey: minimalDfa.DFATable.keySet()) {
            System.out.print("Sate: " + currStateKey);
            if(currStateKey==minimalDfa.getStartingState()) System.out.print(" -start- ");
            if(minimalDfa.DFATable.get(currStateKey).isFinal()) System.out.println(" (FINAL) ");
            else System.out.println();
            for(Character c : minimalDfa.DFATable.get(currStateKey).dfaMoves.keySet()) {
                System.out.println("\tChar-"+c+": "+minimalDfa.DFATable.get(currStateKey).dfaMoves.get(c));
            }
            System.out.print("NFA states that they contain...\t");
            for(int x : minimalDfa.DFATable.get(currStateKey).getNfaStates()) {
                System.out.print(x + ", ");
            }
            System.out.println();
            System.out.println("-------");
        }
        System.out.println(minimalDfa.finalStates);

        System.out.println("enter the string...");
        String test = scanner.next();

        Integer currState = minimalDfa.getStartingState();
        for(Character stringInput : test.toCharArray()) {
            currState = minimalDfa.DFATable.get(currState).dfaMoves.get(stringInput);
        }
        if(minimalDfa.DFATable.get(currState).isFinal()) {
            System.out.println("Accepted.");
        } else System.out.println("Not Accepted.");




    }

    public static DFA toMinimalDfa(DFA dfa, ArrayList<Character> alphabet) {
        //unreachable stuff:

        HashSet<Integer> allStates = new HashSet<>();
        for(int x : dfa.DFATable.keySet()) allStates.add(x);
        HashSet<Integer> reachable_states = new HashSet<>();
        reachable_states.add(dfa.getStartingState());

        HashSet<Integer> new_states = new HashSet<>();
        new_states.add(dfa.getStartingState());

        do {
            HashSet<Integer> temp = new HashSet<>();
            //for each q in new_states do
            Iterator<Integer> it3 = new_states.iterator();
            while(it3.hasNext()) {
                int q = it3.next();
                for(Character c : alphabet) {
                    if(c == 'e') continue;
                    temp.add(dfa.DFATable.get(q).dfaMoves.get(c));
                }
            }

            //new_states := temp \ reachable_states;
            new_states.clear();
            new_states.addAll(temp);
            new_states.removeAll(reachable_states);
            new_states.remove(null);

            //reachable_states := reachable_states ∪ new_states;
            reachable_states.addAll(new_states);

        } while(!new_states.isEmpty());

        //unreachable_states := Q \ reachable_states;
        HashSet<Integer> unreachable_states = new HashSet<>();
        unreachable_states.addAll(allStates);
        unreachable_states.removeAll(reachable_states);

        //System.out.println("unreachable states: " + unreachable_states);
        //System.out.println("reachable states: " + reachable_states);


        ////////////////////////main part of minimization:
        //extracting finalStates and nonFinalStates of dfa
        ArrayList<Integer> fs = new ArrayList<>();
        ArrayList<Integer> nfs = new ArrayList<>();
        for(int x : dfa.DFATable.keySet()) {
            if(unreachable_states.contains(x)) continue;
            if(dfa.DFATable.get(x).isFinal()) fs.add(x);
            else nfs.add(x);
        }

        HashSet<Integer> f = new HashSet<>();
        for(int i : fs) f.add(i);
        HashSet<Integer> q_f = new HashSet<>();
        for(int i : nfs) q_f.add(i);

        //P := {F, Q \ F};
        HashSet<HashSet<Integer>> P = new HashSet<>();
        P.add(f);
        P.add(q_f);
        //W := {F, Q \ F};
        HashSet<HashSet<Integer>> W = new HashSet<>();
        W.add(f);
        W.add(q_f);

        System.out.println("P first: " + P);
        //System.out.println("W first: " + W);

        int xxx = 0;
        while(!W.isEmpty()) {
            xxx++;
            Iterator<HashSet<Integer>> it = W.iterator();
            HashSet<Integer> A = it.next();
            W.remove(A);

            //System.out.println("W first of "+xxx+" iteration1:"+W);

            for(Character c : alphabet) {
                if(c=='e') continue;
                HashSet<Integer> X = new HashSet<>();
                for(int currState : dfa.DFATable.keySet()) {
                    if(A.contains(dfa.DFATable.get(currState).dfaMoves.get(c))) {
                        X.add(currState);
                    }
                }

                Iterator<HashSet<Integer>> PIterator = P.iterator();
                HashSet<HashSet<Integer>> removeFromP = new HashSet<>();
                HashSet<HashSet<Integer>> addToP = new HashSet<>();

               
                while(PIterator.hasNext()) {
                    HashSet<Integer> Y = PIterator.next();

                    //System.out.println("x:"+X+",y:"+Y);
                    HashSet<Integer> xyIntersect = new HashSet<>();
                    xyIntersect.addAll(X);
                    xyIntersect.retainAll(Y);
                    //System.out.println("xyIntersect:"+xyIntersect);

                    HashSet<Integer> yxDiff = new HashSet<>();
                    yxDiff.addAll(Y);
                    yxDiff.removeAll(X);
                    //System.out.println("yxDiff:"+yxDiff);

                    if((!xyIntersect.isEmpty()) && (!yxDiff.isEmpty())) {
                        
                        removeFromP.add(Y);
                        addToP.add(xyIntersect);
                        addToP.add(yxDiff);

                        if(W.contains(Y)) {
                            W.remove(Y);
                            W.add(xyIntersect);
                            W.add(yxDiff);
                        }
                        else {
                            if(xyIntersect.size() <= yxDiff.size()) {
                                W.add(xyIntersect);
                            } else {
                                W.add(yxDiff);
                            }
                        }
                    }
                }

                //remove from P
                P.removeAll(removeFromP);
                //add to P
                P.addAll(addToP);
            }
        }
        System.out.println("Final P: " + P);
        System.out.println("original dfa fs:" + dfa.finalStates);
        //P
        DFA minimalDfa = new DFA();

        Iterator<HashSet<Integer>> pIterator = P.iterator();
        int currMinimalDfaStateIndex = 0;
        while(pIterator.hasNext()) {
            ArrayList<Integer> currPartition = new ArrayList<Integer>();

            HashSet<Integer> currPartitionSet = pIterator.next();
            //////****
            if(currPartitionSet.isEmpty()) continue;
            Iterator<Integer> tempIt = currPartitionSet.iterator();
            while(tempIt.hasNext()) currPartition.add(tempIt.next());

            DFAState newMinimalDfaState = new DFAState(true, currPartition);

            for(int x : currPartition) {
                if(dfa.finalStates.contains(x)) newMinimalDfaState.setFinal(true);
                if(x==dfa.getStartingState()) minimalDfa.setStartingState(currMinimalDfaStateIndex);
            }
            minimalDfa.DFATable.put(currMinimalDfaStateIndex, newMinimalDfaState);

            currMinimalDfaStateIndex++;
        }

        //DFA_MOVES in DFAState
        //finalStates[] in DFA class
        for(int x : minimalDfa.DFATable.keySet()) {

            //DFA_Moves in DFAState
            for(Character c : alphabet) {
                if(c=='e') continue;
                int oneOfTheStatesInOriginalDfa =minimalDfa.DFATable.get(x).getNfaStates().get(0);
                int nextStateInOriginalDfa = dfa.DFATable.get(oneOfTheStatesInOriginalDfa).dfaMoves.get(c);
                int nextStateInMinimalDfa = -1;
                for(int k : minimalDfa.DFATable.keySet()) {
                    if(minimalDfa.DFATable.get(k).getNfaStates().contains(nextStateInOriginalDfa)) {
                        nextStateInMinimalDfa = k;
                    }
                }
                ///T(x, c) = nextStateInMinimalDfa
                if(nextStateInMinimalDfa==-1) System.out.println("ERROR-42");
                minimalDfa.DFATable.get(x).dfaMoves.put(c, nextStateInMinimalDfa);
            }

            //finalStates[] in DFA CLass:
            for(int k : minimalDfa.DFATable.get(x).getNfaStates()) {
                if(dfa.finalStates.contains(k)) {
                    minimalDfa.finalStates.add(x);
                }
            }
        }


        return minimalDfa;
    }
    public static DFA nfaToDfa(int startingState, ArrayList<Integer> nfaFinalStates, ArrayList<Character> alphabet,
                                 HashMap<Integer, HashMap<Character, ArrayList<Integer>>> NFATable) {
        DFA dfa = new DFA();
        int currentDFAStateNumber = 0;

        //making starting Sate:
        //startingState of dfa is EClosure of nfa-starting state
        ArrayList<Integer> startingStates = new ArrayList<>();
        startingStates.add(startingState);

        DFAState dfaStartingState = new DFAState(false, getEClosure(startingStates, NFATable));
        System.out.print("E-closure(IO) = ");
        System.out.print(getEClosure(startingStates, NFATable));
        System.out.print(" = " + currentDFAStateNumber + "\n\n");

        dfa.DFATable.put(currentDFAStateNumber, dfaStartingState);
        dfa.setStartingState(currentDFAStateNumber);
        currentDFAStateNumber++;


        //
        while(!dfa.allDfaStatesMarked()) {
            int currUnmarkedDfaState = dfa.getUnMarkedDfaState();
            dfa.DFATable.get(currUnmarkedDfaState).setMarked(true);
            System.out.println("\nMark " + currUnmarkedDfaState);

            for(Character currChar : alphabet) {
                if(currChar=='e') continue;
                ArrayList<Integer> non_e_reachableStates = getNon_e_ReachableStates(currChar, dfa.DFATable.get(currUnmarkedDfaState).getNfaStates(), NFATable);
                ArrayList<Integer> e_reachableStates = getEClosure(non_e_reachableStates, NFATable);

                //pretty print the move if it is not empty
                if(!non_e_reachableStates.isEmpty()){
                    System.out.print(dfa.DFATable.get(currUnmarkedDfaState).getNfaStates());
                    System.out.print("--" +currChar + "--> ");
                    System.out.print(non_e_reachableStates);
                    System.out.println();
                    System.out.print("E-closure");
                    System.out.print(non_e_reachableStates);
                    System.out.print(" = ");
                    System.out.print(e_reachableStates);
                    System.out.print(" = ");
                }

                int newPossibleDfaState = isInDFA(e_reachableStates, dfa);
                if(newPossibleDfaState==-1) {
                    //it does not exist in dfa, so you have to make one

                    //if e_reachableStates is EMPTY, it means there is no possible state
                    //from currUnmarkedState with currChar, so we set nextState from currState with currChar to -1
                    if(e_reachableStates.isEmpty()) {
                        dfa.DFATable.get(currUnmarkedDfaState).dfaMoves.put(currChar, -1);
                    }
                    else {
                        //we make a new DFA State

                        System.out.println(currentDFAStateNumber);
                        DFAState newDfaState = new DFAState(false, e_reachableStates);

                        //adding it to table
                        dfa.DFATable.put(currentDFAStateNumber, newDfaState);

                        //updating currUnmarked transition func
                        dfa.DFATable.get(currUnmarkedDfaState).dfaMoves.put(currChar, currentDFAStateNumber);

                        //
                        currentDFAStateNumber++;

                    }
                }
                else {
                    //its already in dfa
                    System.out.println(newPossibleDfaState);
                    dfa.DFATable.get(currUnmarkedDfaState).dfaMoves.put(currChar, newPossibleDfaState);
                }

            }
        }

        dfa.setDfaFinalStates(nfaFinalStates);
        System.out.println();

        ////////making the "dummy state" (state -1):
        ArrayList<Integer> temp = new ArrayList<>();
        temp.add(-1);
        DFAState dummyState = new DFAState(true, temp);
        int dummyNumber = dfa.DFATable.size();
        for(Character c : alphabet) {
            if(c=='e') continue;
            dummyState.dfaMoves.put(c, dummyNumber);
        }
        dfa.DFATable.put(dummyNumber, dummyState);

        //updating transition function for other states:
        for(int currState : dfa.DFATable.keySet()) {
            for(Character c : alphabet) {
                if(c == 'e') continue;
                if(dfa.DFATable.get(currState).dfaMoves.get(c)==-1) {
                    dfa.DFATable.get(currState).dfaMoves.put(c, dummyNumber);
                }
            }
        }

        return dfa;
    }


    public static int isInDFA(ArrayList<Integer> nfaStatesCorrespondingToDFAState, DFA dfa) {

        for(int x : dfa.DFATable.keySet()) {
            DFAState currDfaState = dfa.DFATable.get(x);
            //compare the two vectors
            if(arrayListsEquals(nfaStatesCorrespondingToDFAState, currDfaState.getNfaStates())) {
                return x;
            }
        }

        return -1;
    }

    public static ArrayList<Integer> getNon_e_ReachableStates(Character c, ArrayList<Integer> nfaStates, HashMap<Integer, HashMap<Character, ArrayList<Integer>>> NFATable) {
        ArrayList<Integer> rs = new ArrayList<>();

        for(Integer currState : nfaStates) {
            for(Integer reachableState: NFATable.get(currState).get(c)) {
                if(!rs.contains(reachableState)) rs.add(reachableState);
            }
        }

        Collections.sort(rs);
        return rs;
    }
    public static ArrayList<Integer> getEClosure(ArrayList<Integer> nfaStates, HashMap<Integer, HashMap<Character, ArrayList<Integer>>> NFATable) {
        ArrayList<Integer> eClosure = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();

        for(int x : nfaStates) {
            stack.push(x);
            eClosure.add(x);
        }

        while(!stack.empty()) {
            int currState = stack.pop();
            for(int nextState : NFATable.get(currState).get('e')) {
                if(!eClosure.contains(nextState)) {
                    eClosure.add(nextState);
                    stack.push(nextState);
                }
            }
        }
        //
        Collections.sort(eClosure);
        return eClosure;
    }

    public static boolean arrayListsEquals(ArrayList<Integer> l1, ArrayList<Integer> l2) {
        for(int i : l1) {
            boolean flag = false;
            for(int j : l2) {
                if(i == j) flag = true;
            }
            if(flag==false) return false;
        }
        for(int i : l2) {
            boolean flag = false;
            for(int j : l1) {
                if(i == j) flag = true;
            }
            if(flag==false) return false;
        }
        if(l1.size()!=l2.size()) return false;
        return true;
    }
}

class DFA {
    private int startingState;
    public ArrayList<Integer> finalStates = new ArrayList<>();
    public HashMap<Integer, DFAState> DFATable = new HashMap<>();

    public int getStartingState() {
        return startingState;
    }

    public void setStartingState(int startingState) {
        this.startingState = startingState;
    }

    public boolean allDfaStatesMarked() {
        for(Integer x : DFATable.keySet()) {
            if(DFATable.get(x).isMarked()==false) return false;
        }
        return true;
    }

    public int getUnMarkedDfaState() {
        for(Integer x : DFATable.keySet()) {
            if(DFATable.get(x).isMarked()==false) return x;
        }
        return -1;
    }

    public void setDfaFinalStates(ArrayList<Integer> nfaFinalStates) {
        for(int currDfaState : DFATable.keySet()) {
            for(int currNfaState : DFATable.get(currDfaState).getNfaStates()) {
                if(nfaFinalStates.contains(currNfaState)) {
                    DFATable.get(currDfaState).setFinal(true);
                    this.finalStates.add(currDfaState);
                    break;
                }
            }
        }
    }
}
class DFAState {
    private boolean marked;
    private boolean isFinal = false;
    private ArrayList<Integer> nfaStates = new ArrayList<>();
    public HashMap<Character, Integer> dfaMoves = new HashMap<>();

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public DFAState(boolean marked, ArrayList<Integer> nfaStates) {
        this.marked = marked;
        this.nfaStates = nfaStates;
    }

    public boolean isMarked() {
        return marked;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public ArrayList<Integer> getNfaStates() {
        return nfaStates;
    }
}
