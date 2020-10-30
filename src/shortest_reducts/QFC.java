/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shortest_reducts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author maximus
 */
public class QFC {

    static long MAX_SIZE = Long.MAX_VALUE;

    static class Parameters {

        int[] features;//indice del atributo en la matriz
        int[] indexs;//indices de los subconjuntos al cual pertenece el atributo
        //en la partición del cjto de atributos
        int k;

        public Parameters(int[] f, int[] i, int k) {
            features = f;
            indexs = i;
            this.k = k;
        }

        public Combination combine(Combination current, int numFeat) {
            int[] f = new int[Combination.featureCount];
            int[] index = new int[Combination.featureCount];
            System.arraycopy(features, 0, f, 0, k - numFeat);
            System.arraycopy(indexs, 0, index, 0, k - numFeat);

            System.arraycopy(current.features, 0, f, k - numFeat, current.k);
            System.arraycopy(current.featuresIndex, 0, index, k - numFeat, current.k);

            return new Combination(f, index, (k - numFeat) + current.k);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for (int f = 0; f < k - 1; f++) {
                sb.append("X").append(features[f]).append(1).append(",");
            }
            sb.append("X").append(features[k - 1]).append(1).append(">");
            return sb.toString();
        }

    }

    static boolean[][] buildFCA(boolean[][] bm) {
        boolean[][] fca = new boolean[bm.length][bm[0].length];
        for (int i = 0; i < bm.length; i++) {
            for (int j = 0; j < bm[i].length; j++) {
                boolean found = false;
                for (int k = j; !found && k < bm[i].length; k++) {
                    if (bm[i][k]) {
                        found = true;
                    }
                }
                fca[i][j] = found;
            }
        }
        return fca;
    }

    public static boolean hasFuture(Parameters p, int newFeatureIndex, boolean[][] bm, boolean[][] fca) {
        for (int r = 0; r < bm.length; r++) {
            boolean allZeros = true;
            for (int i = 0; allZeros && i < p.k; i++) {
                if (bm[r][p.features[i]]) {
                    allZeros = false;
                }
            }
            if (allZeros) {
                if (fca[r][newFeatureIndex]) {
                    allZeros = false;
                }
            }
            if (allZeros) {
                return false;
            }
        }
        return true;
    }

    static ArrayDeque<Parameters> queue = new ArrayDeque<>();
    static ArrayList<Combination> reductsFound = new ArrayList();
    static float acum_porcent=10;

    public static boolean getShortsReducts(boolean[][] bm, LinkedList<FeatureSet> listOfFeaturesSets, boolean[][] fca, int coreSize, int[] featureMapping) {
        int N = listOfFeaturesSets.size();
        int reductsLength = 0;
        if (coreSize == 0) {
//            System.out.println("min f: "+Combination.minFeaturesCount);
            for (int mf = 0; mf < Combination.minFeaturesCount; mf++) {
                int[] features = new int[N];
                int[] indexs = new int[N];
                features[0] = listOfFeaturesSets.get(mf).getFirstFeature();
                indexs[0] = mf;
                if (Utils.isSReduct(bm, features, 1)) {
                    reductsLength = 1;
                    reductsFound.add(new Combination(features, indexs, 1));
                    queue.clear();
                } else if (reductsLength == 0) {
                    queue.offer(new Parameters(features, indexs, 1));
                }
            }
            System.out.println("The core not exist, the min features set size is: "
                    + Combination.minFeaturesCount);
        } else {
            int[] features = new int[N];
            int[] indexs = new int[N];
            for (int i = 0; i < coreSize; i++) {
                features[i] = listOfFeaturesSets.get(i).getFirstFeature();
                indexs[i] = i;
            }
            System.out.println("The core exist, and its size is: " + coreSize);
            queue.offer(new Parameters(features, indexs, coreSize));
        }
        boolean dif_size = false;
        while (!queue.isEmpty()) {
            Parameters p = queue.poll();
            if (reductsLength > 0 && p.k >= reductsLength) {
                queue.clear();
            } else {
                if (dif_size && queue.size() > MAX_SIZE && p.k == queue.getLast().k) {
//                    dif_size = false;
//                    long parte = (long) queue.size() + 1;
//                    long k = (long) p.k;
//                    long total = Utils.binomial(N, k);
//                    float porcent = (float) (parte * 100) / (float) total;
//                    System.out.println("------------------------------------------------");
//                    System.out.println("número de candidatos : " + parte);
//                    System.out.println("de un total de " + total + " combinaciones ");
//                    System.out.println("k: " + k + " N: " + N);
//                    System.out.println("porciento: " + porcent);
//                    System.out.println("------------------------------------------------");
//                    System.out.println("");
//                    if(porcent>acum_porcent){
//                        queue.addFirst(p);
//                        return false;
//                    }
//                    acum_porcent/=2;
                    queue.addFirst(p);
                    return false;
                }
//                if (queue.size() > MAX_SIZE && p.k == queue.getLast().k) {
//                    queue.addFirst(p);
//                    return false;
//                }
                int nextGroup = p.indexs[p.k - 1] + 1;
                int[] zeroRowsIndexs = Utils.getZeroRows(bm, p.features, p.k);
                for (int f = nextGroup; f < N; f++) {
                    int[] features = p.features.clone();
                    int[] indexs = p.indexs.clone();
                    int nextF = listOfFeaturesSets.get(f).getFirstFeature();
                    features[p.k] = nextF;
                    indexs[p.k] = f;
//                    Utils.is_super_reduct(bm, currentComb.features, candidates_size, currentComb.k, zeroRowsIndexs)
//                    Utils.isSReduct(bm, features, p.k + 1)
                    if (Utils.is_super_reduct(bm, features, p.k, p.k + 1, zeroRowsIndexs)) {
                        reductsLength = p.k + 1;
                        reductsFound.add(new Combination(features, indexs, p.k + 1));
                    } else if (reductsLength == 0) {
                        /**
                         * Si la supercolumna de un grupo(nextF) no contribuye
                         * no hay que verificar por ningún otro elemento del
                         * mismo grupo de (nextF)
                         */
                        if (Utils.contributes(bm, zeroRowsIndexs, nextF) && hasFuture(p, f, bm, fca)) {
                            queue.offer(new Parameters(features, indexs, p.k + 1));
                            dif_size = true;
                        }
                    }
                }
            }
        }
        return true;
    }

}
