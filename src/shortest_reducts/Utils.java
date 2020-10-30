package shortest_reducts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import static shortest_reducts.ShortestReducts.bytesToMegabytes;

public class Utils {

    static Logger logger;

    /**
     * esta función cheque que los atributos de la combinación formada se
     * complementen, es decir sean compatibles para formar la combinación
     *
     * @param combination combinación a verificar
     * @param contributes matriz que contine la comparación entre las columnas
     * en BM
     * @return true en caso de que la combinación pueda ser prospecto a reducto
     * false en otro caso
     */
    static boolean contribute(Combination combination, boolean[][] contributes) {
        for (int i = 0; i < combination.getK() - 1; i++) {
            int f = combination.getIndex(i);//Indices reales de la segunda matriz
            //la resultante al realizar los corrimientos a la izquierda de las
            //columnas pertenecientes a la fila con menor cantidad de unos
            for (int j = i + 1; j < combination.getK(); j++) {
                int g = combination.getIndex(j);
                if (!contributes[f][g] || !contributes[g][f]) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean contributes(boolean[][] bm, int[] features, int k, int newFeature) {
        int zeros1 = 0;
        for (boolean[] row : bm) {
            if (isZeroRow(row, features, k)) {
                zeros1++;
            }
        }
        int zeros2 = 0;
        features[k] = newFeature;
        for (boolean[] row : bm) {
            if (isZeroRow(row, features, k + 1)) {
                zeros2++;
            }
        }
        features[k] = 0;
        return zeros2 < zeros1;
    }
    
    static boolean contributes(boolean[][] bm, int[] zeroRows, int newFeature) {
        for (int i = 0; zeroRows[i] != -1; i++) {
            if(bm[zeroRows[i]][newFeature])
                return true;
        }
        return false;
    }

    static int countZeroRows(boolean[][] bm, int feature) {
        int zeroRows = 0;
        for (boolean[] bm1 : bm) {
            if (!bm1[feature]) {
                zeroRows++;
            }
        }
        return zeroRows;
    }

    static int[] onesInCols(boolean[][] bm) {
        int[] ones = new int[bm[0].length];
        for (int i = 0; i < ones.length; i++) {
            ones[i] = contOnesInCol(bm, i);
        }
        return ones;
    }

    static int contOnesInCol(boolean[][] bm, int col) {
        int count = 0;
        for (boolean[] bm1 : bm) {
            if (bm1[col]) {
                count++;
            }
        }
        return count;
    }

    static int sumFeatures(int[] features, int k, int[] onesInCol) {
        int c = 0;
        for (int i = 0; i < k; i++) {
            c += onesInCol[features[i]];
        }
        return c;
    }

    static int countZeroRows(boolean[][] bm, int feature1, int feature2) {
        int zeroRows = 0;
        for (boolean[] bm1 : bm) {
            if (!bm1[feature1] && !bm1[feature2]) {
                zeroRows++;
            }
        }
        return zeroRows;
    }

    static int countOnes(boolean[] row) {
        int c = 0;
        for (boolean b : row) {
            if (b) {
                c++;
            }
        }
        return c;
    }

    static boolean[] findMinRow(boolean[][] bm) {
        boolean[] minRow = bm[0];
        int minCount = countOnes(minRow);
        for (int r = 1; r < bm.length; r++) {
            int count = countOnes(bm[r]);
            if (count < minCount) {
                minRow = bm[r];
                minCount = count;
            }
        }
        return minRow;
    }

    public static void copyColumn(boolean[][] dst, boolean[][] src, int dCol, int sCol) {
        for (int row = 0; row < src.length; row++) {
            dst[row][dCol] = src[row][sCol];
        }
    }

    public static void cloneFeaturesIndex(int N, int[] core, int[] mbIndexMapping, int coreSize) {
        int index = 0;
        for (int i = 0, f = 0; i < N; i++) {
            if (index >= coreSize) {
                return;
            }
            if (core[index] != i) {
                mbIndexMapping[f++] = i;
            } else {
                index++;
            }
        }
    }

    public static boolean[][] moveMinFeaturesLeft(boolean[][] bm, ArrayList<Integer> minFeatures, int[] featureMapping) {
        boolean[][] bm1 = new boolean[bm.length][bm[0].length];
        int dColMin = 0;
        int dColRest = minFeatures.size();
        int f = minFeatures.get(dColMin);
        for (int sCol = 0; sCol < bm[0].length; sCol++) {
            if (sCol == f) {
                copyColumn(bm1, bm, dColMin, sCol);
                featureMapping[dColMin] = sCol;
                if (++dColMin < minFeatures.size()) {
                    f = minFeatures.get(dColMin);
                }
            } else {
                copyColumn(bm1, bm, dColRest, sCol);
                featureMapping[dColRest++] = sCol;
            }
        }
        return bm1;//al principio de la matriz se van a a encontrar las columnas 
        //que estan contenidas en la fila con menor cantidad de unos
        //de la matriz
        //al principio de featureMapping se encontraran dichas columnas y 
        //luego las columnas que se encontraban al principio
    }

    public static boolean[][] moveMinFeaturesLeft(boolean[][] bm, LinkedList<FeatureSet> featureSet, int[] featureMapping) {
        boolean[][] bm1 = new boolean[bm.length][bm[0].length];
        int dCol = 0;
        for (FeatureSet featureSet1 : featureSet) {
            for (Integer sCol : featureSet1.getFeaturesSet()) {
                copyColumn(bm1, bm, dCol, sCol);
                featureMapping[dCol] = sCol;
                dCol++;
            }
        }
        return bm1;//al principio de la matriz se van a a encontrar las columnas 
        //que estan contenidas en la fila con menor cantidad de unos
        //de la matriz
        //al principio de featureMapping se encontraran dichas columnas y 
        //luego las columnas que se encontraban al principio
    }

    static boolean isZeroRow(boolean[] bmRow, int[] features, int k) {
        for (int i = 0; i < k; i++) {
            int f = features[i];
            if (bmRow[f]) {
                return false;
            }
        }
        return true;
    }

    static int countZeroRows(boolean[][] bm, int[] zeroRows , int newF) {
        int zero_rows=0;
        int indx=0;
        for (int i = 0; zeroRows[i] != -1; i++) {
          if(!bm[zeroRows[i]][newF]){
            zero_rows++;
          }  
        }
        return zero_rows;
    }
    
    static int[] getZeroRows(boolean[][] bm, int[] features, int k) {
        int[] zeroRows = new int[bm.length];
        int i = 0;
        int row_index = 0;
        for (boolean[] row : bm) {
            if (isZeroRow(row, features, k)) {
                zeroRows[i++] = row_index;
            }
            ++row_index;
        }
        zeroRows[i] = -1;
        return zeroRows;
    }
    
    static int[] updateZeroRows(boolean[][] bm, int[] zeroRows , int newF) {
        int []zero_rows=new int[zeroRows.length];
        int indx=0;
        for (int i = 0; zeroRows[i] != -1; i++) {
          if(!bm[zeroRows[i]][newF]){
            zero_rows[indx++]=zeroRows[i];
          }  
        }
        zero_rows[indx]=-1;
        return zero_rows;
    }

    static boolean isSReduct(boolean[][] bm, int[] features, int k) {
        for (boolean[] row : bm) {
            if (isZeroRow(row, features, k)) {
                return false;
            }
        }
        return true;
    }

    static boolean is_super_reduct(boolean[][] bm, int[] features, int start, int k, int[] zero_rows) {
        int[] feaut = new int[k - start];
//        System.out.println(start+" "+k);
        System.arraycopy(features, start, feaut, 0, feaut.length);
        for (int i = 0; zero_rows[i] != -1; i++) {
            if (isZeroRow(bm[zero_rows[i]], feaut, feaut.length)) {
                return false;
            }
        }
        return true;
    }

    static int[] getCore(boolean[][] bm) {
        int[] core = new int[bm[0].length];
        for (int i = 0; i < bm[0].length; i++) {
            core[i] = -1;
        }
        int index = 0;
        int part_of_core = -1;
        int numberOfOnes;
        for (boolean[] bs : bm) {
            numberOfOnes = 0;
            for (int col = 0; col < core.length; col++) {
                if (bs[col]) {
                    part_of_core = col;
                    ++numberOfOnes;
                }
                if (numberOfOnes > 1) {
                    break;
                }
            }
            if (numberOfOnes == 1) {
                core[index++] = part_of_core;
            }
        }
        return core;
    }

    static int relationCols(boolean[][] bm, int c1, int c2) {
        int relation = -1;//No existe ninguna relación entre las columnas
        for (boolean[] row : bm) {
            if (row[c1] && !row[c2]) {
                if (relation == 1) {
                    return -1;
                } else {
                    relation = 0;
                }
            } else if (!row[c1] && row[c2]) {
                if (relation == 0) {
                    return -1;
                } else {
                    relation = 1;
                }
            }
        }
        return relation;
    }

    static LinkedList<FeatureSet> createListOfFeaturesSets(boolean[][] bm, ArrayList<Integer> minFeatures) {
        LinkedList<FeatureSet> listOfFeaturesSets = new LinkedList<>();
        int minFCount = 0;
        LinkedList<Integer> minF = new LinkedList<>();
        LinkedList<Integer> minFtmp;
        LinkedList<Integer> listOfIndex = new LinkedList();
        for (int i = 0; i < bm[0].length; i++) {
            listOfIndex.add(i);
        }
//        System.out.println(minFeatures);
        for (int i = 0; i < minFeatures.size(); i++) {
            minF.add(i);
        }
        boolean b;
        while (!listOfIndex.isEmpty()) {
            b = false;
            int c1 = listOfIndex.removeFirst();
            FeatureSet f = new FeatureSet(c1);
            for (Iterator i = listOfIndex.iterator(); i.hasNext();) {
                int c2 = (int) i.next();
                int value = relationCols(bm, c1, c2);
                if (value == 0) {
                    f.addFeatureLast(c2);
                    i.remove();
                } else if (value == 1) {
                    f.addFeatureFirst(c2);
                    c1 = c2;
                    i.remove();
                }
            }
            boolean more = false;
            minFtmp = (LinkedList<Integer>) minF.clone();
            for (Integer attr : minF) {
                if (f.contain(attr)) {
                    if (!more) {
                        minFCount++;
                        more = true;
                    }
                    minFtmp.remove(attr);
                }
            }
            listOfFeaturesSets.add(f);
        }
        Combination.minFeaturesCount = minFCount;
        return listOfFeaturesSets;
    }

    /**
     *
     * @param reductsFound los reductos encontrados por el algoritmo
     * @param bmName Nombre del dataset
     * @param time tiempo que demor el algorimo en encontrar todos los reductos
     * más cortos
     * @throws IOException la información de: 'tiempo de ejecución, número de
     * reductos, tamaño de los reductos y memoria utilizada' se guarda en un
     * fichero .log los reductos en un fichero .RED
     */
    public static void writeReducts(ArrayList<Combination> reductsFound, String bmName, Double time) throws IOException {
        for (int i = 0; i < reductsFound.size(); i++) {
            Arrays.sort(reductsFound.get(i).features);
        }
        reductsFound.sort((Combination o1, Combination o2) -> {
            int c;
            int[] features1 = o1.features;
            int[] features2 = o2.features;
            if (features1[0] == features2[0]) {
                c = 0;
            } else if (features1[0] > features2[0]) {
                c = 1;
            } else {
                c = -1;
            }
            for (int i = 1; i < o1.getK() && c == 0; i++) {
                if (c == 0) {
                    if (features1[i] > features2[i]) {
                        c = 1;
                    } else if (features1[i] < features2[i]) {
                        c = -1;
                    }
                }

            }
            return c;
        });

        String itFile = ShortestReducts.output_dir + bmName + ".RED";
        BufferedWriter bw = new BufferedWriter(new FileWriter(itFile));
        bw.append("Min Reducts found: " + reductsFound.size());
        System.out.println("Min Reducts found: " + reductsFound.size());
        bw.newLine();
        for (Combination combination : reductsFound) {
            bw.append(combination.toString());
            bw.newLine();
        }
        bw.close();
        try {
            boolean append = true;
            FileHandler fh = new FileHandler(ShortestReducts.output_dir + bmName + ".log", append);
            //fh.setFormatter(new XMLFormatter());
            fh.setFormatter(new SimpleFormatter());
            logger = Logger.getLogger(bmName);
            logger.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String txtLog = "Time: " + time + ", size: " + reductsFound.get(0).k
                + ", number of reducts : " + reductsFound.size() + ", Used memory in megabytes: " + String.format("%.5g%n", bytesToMegabytes(ShortestReducts.afterUsedMem));
//        System.out.println(txtLog);
        logger.info(txtLog);
    }

    public static void printMatrix(boolean[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]) {
                    System.out.print(1 + " ");
                } else {
                    System.out.print(0 + " ");
                }
            }
            System.out.println("");
        }
    }

    public static void printGroups(LinkedList<FeatureSet> listOfFeaturesSets, int[] featureMapping) {
        listOfFeaturesSets.stream().map((features) -> {
            System.out.print("{");
            return features;
        }).map((features) -> features.getFeaturesSet()).map((val) -> {
            for (int i = 0; i < val.size() - 1; i++) {
                System.out.print("X" + (featureMapping[val.get(i)] + 1) + ";");
            }
            return val;
        }).map((val) -> {
            System.out.print("X" + (featureMapping[val.get(val.size() - 1)] + 1) + "}");
            return val;
        }).forEachOrdered((_item) -> {
            System.out.println("");
        });
    }

    /**
     * Cantidad de atributos que pertenecen a grupos diferentes y estan
     * contenidos en la fila con menor cantidad de unos, algunos de estos
     * atributos tienen que pertenecer a los reductos por lo que solo se
     * consideran las combinaciones de atributos que contengan dichos atributos.
     *
     * @param numberFeatures
     * @return
     */
    static int getMinFeaturesCountInGrup(ArrayList<Integer> minFeatures) {
        int numFeatures = 0;
        int index = 0;
        boolean b = true;
        for (int i = 0; i < minFeatures.size(); i++) {
            LinkedList a = Combination.listOfFeaturesSets.get(index).getFeaturesSet();
            if (a.contains(minFeatures.get(i))) {
                if (b) {
                    numFeatures++;
                    b = false;
                }
            } else {
                index++;
                i--;
                b = true;
            }
        }
        return numFeatures;
    }

    static boolean[][] buildAcumulativeMatrix(boolean[][] bm) {
        int numFeatures = Combination.featureCount;
        boolean[][] fca = new boolean[bm.length][numFeatures];
        for (int i = 0; i < bm.length; i++) {
            for (int j = 0; j < numFeatures; j++) {
                boolean found = false;
                for (int k = j; !found && k < numFeatures; k++) {
                    int col = Combination.listOfFeaturesSets.get(k).getFirstFeature();
                    if (bm[i][col]) {
                        found = true;
                    }
                }
                fca[i][j] = found;
            }
        }
        return fca;
    }

    public static boolean hasFuture(Combination p, int newFeature, int index, boolean[][] bm, boolean[][] fca) {
        for (int r = 0; r < bm.length; r++) {
            boolean allZeros = true;
            for (int i = 0; allZeros && i < p.k; i++) {
                if (i != index && bm[r][p.features[i]]) {
                    allZeros = false;
                }
            }
            if (allZeros) {
                if (fca[r][newFeature]) {
                    allZeros = false;
                }
            }
            if (allZeros) {
                return false;
            }
        }
        return true;
    }

    public static void printArr(int[] args) {
        System.out.print("[");
        for (int i = 0; i < args.length - 1; i++) {
            System.out.print(args[i] + ",");
        }
        System.out.print(args[args.length - 1] + "]");
    }

    public static void printCols(int[] cols, boolean[][] bm, int k) {
        for (int i = 0; i < k; i++) {
            for (boolean[] bs : bm) {
                if (bs[cols[i]]) {
                    System.out.print(1 + " ");
                } else {
                    System.out.print(0 + " ");
                }
            }
            System.out.println("");
        }
    }

    static long binomial(final long N, final long K) {
        long ret = 1;
        for (int k = 0; k < K; k++) {
            ret *= (N - k);
            ret /= (k + 1);
        }
        return ret;
    }

    static void printCandidates() {
        for (QFC.Parameters parameter : QFC.queue) {
            Combination comb =new Combination(parameter.features, parameter.indexs, parameter.k);
            Combination clone=comb.clone(QNCG.featureMapping);
            System.out.println(comb.toString()+"->"+clone.toString());
        }
    }

}
