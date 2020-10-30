/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shortest_reducts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import static shortest_reducts.ShortestReducts.afterUsedMem;

/**
 *
 * @author maximus
 */
public class QNCG {

    /**
     * Se construye la ultima combinación que es posible formar a partir de los
     * indices de los grupos de atributos formados
     *
     * @param s tamaño de la última combinación a formar
     * @return la ultima combinación de tamaño s que se puede formar
     */
    static Combination buildLastCombinationInQuee(int s, QFC.Parameters p) {
        if (s <= 0) {
            return null;
        }
        int n = Combination.featureCount;
        int parameter_k = p.k;
        int last_candidate_indx = p.indexs[parameter_k - 1];
        if (n - s < last_candidate_indx) {
            return null;
        }
        int firstF = n - s;
        lastCombination = new Combination();
        lastCombination.add(firstF);
        for (int i = 1; i < s; i++) {
            lastCombination.add(firstF + i);
        }
        return lastCombination;
    }

    static Combination buildLastCombinationInGroup(int s, Combination current) {
        return current.createLastGroup();
    }

    static Combination nexCombinationInK(int reducts_size, int fistF, boolean[][] bm) {
        if (current == null) {
            current = new Combination(fistF, reducts_size);
//            System.out.println(currentCombinationSet);
            return current;
        }
        int index = current.getK() - 1;
        boolean spaceFound = false;
        while (!spaceFound && index >= 0) {
            int currF = current.get(index);
            if (currF < lastCombination.get(index)) {
                int nextF = currF + 1;
                current.set(index, nextF);
                for (int i = index + 1; i < current.getK(); i++) {
                    current.set(i, current.get(i - 1) + 1);
                }
                spaceFound = true;
            }
            index--;
        }
        if (!spaceFound) {
            current = null;
            return null;
        }
        return current;
    }

    public static Combination nexCombinationInSet(Combination current) {
        if (current == null) {
            current = new Combination();
            current.initialiceSet(QNCG.current);
        }
        int index = current.getK() - 1;
        boolean spaceFound = false;
        while (!spaceFound && index >= Combination.coreSize) {
            int currF = current.get(index);
            if (currF < lastCombinationSet.get(index)) {
                int nextF = currF + 1;
                current.set(index, nextF, QNCG.current);//Se forma la proxima combinación
                //para esto primero se busca el grupo al que apunta index y el elemento dentro del grupo al que hace referencia nextF
                for (int i = index + 1; i < current.getK(); i++) {
                    int currentValue = current.get(i) + 1;
                    if (currentValue < QNCG.current.getGroupSize(i)) {
                        current.set(i, currentValue, QNCG.current);
                    } else {
                        current.set(i, 0, QNCG.current);
                    }
                }
                spaceFound = true;
            }
            index--;
        }
        if (!spaceFound) {
            return null;
        }
        return current;
    }

    static Combination lastCombination = null;
    static Combination lastCombinationSet = null;//Ultima combinación en el grupo que se este procesando
    static Combination current = null;
    static Combination tmp;
    static Combination tmpComb;
    static boolean sReductFound = false;//Si se ha encontrado un super-reducto
    static int reductsSize = Integer.MAX_VALUE;//Tamaño inicial de los reductos
    static ArrayList<Combination> reductsFound = new ArrayList<>();
    static QFC.Parameters lastParameter = null;
    static int[] featureMapping;
    static int candidates_size;
    static final double EPSILON = 10000;
    static long queueSize = Long.MAX_VALUE;
    static int first_feature_indx = -1;

    public static void findShortestReducts(boolean[][] bm1, String bmName) throws IOException {
        long end;
        long start = System.currentTimeMillis();
        int N = bm1[0].length;
        //Array list where put in the combinations set that conform the reducts

        /**
         * minFeatures : Array que contendrá los atributos que tienen un 1 en la
         * fila de la matriz de discernibilidad con menor cantidad de unos "pues
         * alguno de estos atributos tienen que pertenecer a los reductos"
         */
        ArrayList<Integer> minFeatures = new ArrayList<>();

        //first it is verified if the core exists
        int[] core = Utils.getCore(bm1);
        boolean coreFound = false;
        int coreSize = 0;
        if (core[0] != -1) {
            coreSize = 1;
            minFeatures.add(core[0]);
            coreFound = true;
            for (int i = 1; i < core.length; i++) {
                if (core[i] != -1) {
                    minFeatures.add(core[i]);
                    coreSize++;
                } else {
                    break;
                }
            }
            //si existe el core y es un reducto termina el algoritmo
            if (Utils.isSReduct(bm1, core, coreSize)) {
                Combination reduct = new Combination(core, coreSize, null);
                reductsFound.add(reduct);
                end = System.currentTimeMillis();
                Utils.writeReducts(reductsFound, bmName, (end - start) / 1000.0);
                System.out.println((end - start) / 1000.0);
                return;
            }
            Collections.sort(minFeatures);
        } else {
            boolean[] minRow = Utils.findMinRow(bm1);
            for (int f = 0; f < minRow.length; f++) {
                if (minRow[f]) {
                    minFeatures.add(f);
                }
            }
        }
//        System.out.println(minFeatures);
        Combination.coreSize = coreSize;//Cada combinación estará formada por el 
        //core en caso de que exista y algunos otros atributos
        featureMapping = new int[N];//Se utiliza para mapear la posición inicial
        //de los atributos en la matriz
        /**
         * Se desplazan a la izquierda de la matriz los atributos en minFeatures
         * ya que se parte de estos para formar las combinaciones
         */
        boolean[][] dm = Utils.moveMinFeaturesLeft(bm1, minFeatures, featureMapping);
        //Utils.printArr(featureMapping);
        //Se crean los grupos de atributos, en estos grupos el atributo primario de cada grupo
        //es supercolumna de todos los atributos que pertenecen al mismo grupo
        LinkedList<FeatureSet> listOfFeaturesSets = Utils.createListOfFeaturesSets(dm, minFeatures);

        //Esto solo es para fines explicativos comentar antes de ejecutar el algoritmo
        //Utils.printMatrix(dm);
//        Utils.printGroups(listOfFeaturesSets, featureMapping);
        //System.out.println("Number of Subsets" + listOfFeaturesSets.size() + ", number of attr " + N + ", core " + coreSize);
        Combination.listOfFeaturesSets = listOfFeaturesSets;
        //los otributos que primero se combinan son los atributos primarios de cada grupo
        //por lo que se actualiza la cantidad de caracteristicas
        Combination.featureCount = listOfFeaturesSets.size();//Número de atributos en la matriz básica
        //que son columnas independientes "columnas que no son subcolumnas de otras"
        //Si el tamaño de los reductos predecido es mayor que la cantidad de grupos 
        //formados se actualiza este valor ya que no puede existir un reducto mayor al
        //número total de grupos formados 
        reductsSize=findSizeSuperReduct(coreFound, listOfFeaturesSets, core, dm);
        end = System.currentTimeMillis();
        System.out.println("Predicted size: "+reductsSize+", found in: "+ (end - start) / 1000.0 + " sec");
//        System.exit(0);
        if (Combination.featureCount < reductsSize) {
            reductsSize = Combination.featureCount - 1;
        }
        /**
         * si el valor del tamaño del reducto predicho es menor que el core se
         * fija a core +1 ya que el core por si solo no formaba un reducto
         */
        if (reductsSize < coreSize) {
            reductsSize = coreSize + 1;
        }
//        System.exit(0);
        //Se construye una matriz que nos permitirá podar el espacio de búsqueda
        boolean[][] acm = Utils.buildAcumulativeMatrix(dm);
        //comenzamos a buscar los reductos utilizando una cola de tamaño limitado la cual
        //almacenará algunas combinaciones factibles a ser reductos
        try {
           queueSize = calc_queue_size(Combination.featureCount, reductsSize);
        } catch (NumberFormatException e) {
        }

        System.out.println("Min number of candidates to store: " + queueSize);
        QFC.MAX_SIZE = queueSize;//Tamaño máximo admitido para que se almacenen en la cola combinaciones
        afterUsedMem = ShortestReducts.runtime.totalMemory() - ShortestReducts.runtime.freeMemory();
        boolean allFound = QFC.getShortsReducts(dm, listOfFeaturesSets, acm, coreSize, featureMapping);
        Combination.coreSize = 0;//Esto es porque las combinaciones que se van a formar se van a formar sin
        //tener en cuenta el core ya que este estará contenido en las combinaciones en la cola
        if (allFound) {
            for (int i = 0; i < QFC.reductsFound.size(); i++) {
                current = new Combination();
//                Utils.printArr(QFC.reductsFound.get(i).features);
                current.copy(QFC.reductsFound.get(i));
                reductsFound.add(current.clone(featureMapping));
                if (current.containsAnyGroup()) {
                    lastCombinationSet = current.createLastGroup();
                    Combination currentGroup = nexCombinationInSet(null);
                    while (currentGroup != null) {
                        if (Utils.isSReduct(dm, currentGroup.features, currentGroup.getK())) {
                            reductsFound.add(currentGroup.clone(featureMapping));
                        }
                        currentGroup = nexCombinationInSet(currentGroup);
                    }
                }
            }
            end = System.currentTimeMillis();
            Utils.writeReducts(reductsFound, bmName, (end - start) / 1000.0);
//            System.out.println((end - start) / 1000.0);
            return;
        }
//        Utils.printCandidates();
//        Combination tmp = new Combination();//variable temporal para guardar el estado actual de las combinaciones en cada grupo
        tmp = new Combination();
        tmpComb = new Combination();
        Combination currentComb;
        candidates_size = QFC.queue.getFirst().k;
        if (reductsSize <= candidates_size) {
            reductsSize = candidates_size + 1;
        }
//        boolean startInQueeEnd = false;//Inciciar la busqueda en la cola desde el final
//        boolean sameSizeK;//Si en la cola solo hay combinaciones de un mismo tamaño
        QFC.Parameters parameter = QFC.queue.getLast();//Si todas son del mismo tamaño tomamos la ultima
        int[] featuresCopy = parameter.features.clone();
        int[] indexCopy = parameter.indexs.clone();
        lastParameter = new QFC.Parameters(featuresCopy, indexCopy, candidates_size);
//        sameSizeK = (firstK == lasK);
        System.out.println("Reducts candidates size : " + candidates_size);
        //---------------------------------------------------------------------------------
        //Si no se encontro reductos verificar en el espacio de tamaño reductSize
        int queueIndexFoundReduct = searchFirstReductInQueue(dm, -1);
//        firstReductRestOfCombination(dm);
        //---------------------------------------------------------------------------------

        //---------------------------------------------------------------------------------
        if (!sReductFound)//si no se encontró ningún reducto en el espacio actual se procede a buscar en los espacios consecutivos
        {
            //reductsSize se refiere en este contexto al número 
            while (!sReductFound && ++reductsSize <= Combination.featureCount) {
                queueIndexFoundReduct = searchFirstReductInQueue(dm, -1);
//                firstReductRestOfCombination(dm);
            }
        } else {
            while (sReductFound && --reductsSize >= 0) {
//                System.out.println(reductsSize);
                sReductFound = false;
                int firstReductCount = searchFirstReductInQueue(dm, queueIndexFoundReduct);
                if (sReductFound) {
                    queueIndexFoundReduct = firstReductCount;
                } else {
                    queueIndexFoundReduct = 0;
                }
//                firstReductRestOfCombination(dm);
            }
        }

        ShortestReducts.biforeUsedMem = ShortestReducts.runtime.totalMemory() - ShortestReducts.runtime.freeMemory();
        if (ShortestReducts.biforeUsedMem > ShortestReducts.afterUsedMem) {
            ShortestReducts.afterUsedMem = ShortestReducts.biforeUsedMem;
        }
        //en este punto se tiene en tmpComb el primer reducto encontrado 
        reductsFound.add(tmpComb.clone(featureMapping));
        end = System.currentTimeMillis();
        System.out.println("First reduct found in: " + (end - start) / 1000.0 + " sec, size" + tmpComb.k);
        //Se fija el tamaño de los reductos al tamaño donde se encontro el primer reducto
        reductsSize = tmpComb.k;
        //Se chequea si existen subgrupos que sean reductos
        findAllReductsInSubgroups(tmpComb, dm);
        //Se comienzana buscar los reductos utilizando los elementos que estan en la cola
        //anteriormente se eliminaron de la cola aquellas combinaciones que no pueden ser reductos
        findAllReductInQueue(dm, queueIndexFoundReduct, tmp);
        //Aqui se puede comenzar a buscar en la ultima combinación en caso de que 
        //en la cola no exista ningun reducto
//        if (QFC.queue.isEmpty()) {
//            findAllReductRestOfCombination(dm, tmp);
//        } else {
//            findAllReductRestOfCombination(dm, null);
//        }
        end = System.currentTimeMillis();
        Utils.writeReducts(reductsFound, bmName, (end - start) / 1000.0);
//        System.out.println((end - start) / 1000.0);

    }

    private static int searchFirstReductInQueue(boolean[][] bm, int start) {
        Combination currentComb;
        int counter = 0;
        if (QFC.queue.isEmpty()) {
            return -1;
        }
        //---------------------------------------------------------------------------------
        for (int i = 0; i < start; i++) {
            QFC.queue.removeFirst();
        }
        ///--------------------------------------------------------------------------------
//        System.out.println(candidates_size);
        for (QFC.Parameters parameter : QFC.queue) {
            if (candidates_size == reductsSize) {
                return QFC.queue.size();//no hay elementos en la lista factibles a ser reductos
            }
            //arreglo que contiene las filas que son ceros en el candidato actual
            int[] zeroRowsIndexs = Utils.getZeroRows(bm, parameter.features, candidates_size);

            int newReductL = reductsSize - candidates_size;
            int firstFeatureIndex = 0;
            if (first_feature_indx == -1) {
                int lastFeatureIndex = parameter.indexs[candidates_size - 1];
                firstFeatureIndex = lastFeatureIndex + 1;
            } else {
                firstFeatureIndex = first_feature_indx;
            }

            if (firstFeatureIndex + newReductL > Combination.featureCount) {
                return counter;
            }
            current = null;
            lastCombination = buildLastCombinationInQuee(newReductL, parameter);
            nexCombinationInK(newReductL, firstFeatureIndex, bm);//Se construye la primera combinación de tamaño k
//            Utils.printArr(zeroRowsIndexs);
//            System.out.println(newReductL);
            while (current != null) {
                currentComb = parameter.combine(current, 0);
//                Utils.is_super_reduct(bm, currentComb.features,candidates_size,currentComb.k, zeroRowsIndexs)
//                Utils.isSReduct(bm, currentComb.features,currentComb.k)
                if (Utils.is_super_reduct(bm, currentComb.features, candidates_size, currentComb.k, zeroRowsIndexs)) {
                    sReductFound = true;//Si es un reducto o un super reducto hay que verificar que sea el de tamaño mínimo
                    tmp.copy(current);//Se guarda el estado actual del espacio de búsqueda
                    tmpComb.copy(currentComb);
                    first_feature_indx = current.featuresIndex[0];
                    return counter;
                }
                nexCombinationInK(newReductL, 0, bm);
            }
            counter++;
        }
        return QFC.queue.size();
    }

    private static void findAllReductInQueue(boolean[][] bm, int start, Combination startComb) {
        Combination currentComb;
        current = startComb;
        if (QFC.queue.isEmpty()) {
            return;
        }
        //---------------------------------------------------------------------------------
        for (int i = 0; i < start; i++) {
            QFC.queue.removeFirst();
        }
        ///--------------------------------------------------------------------------------
        for (QFC.Parameters parameter : QFC.queue) {
            if (candidates_size == reductsSize) {
                return;//no hay elementos en la lista factibles a ser reductos
            }
            int[] zeroRowsIndexs = Utils.getZeroRows(bm, parameter.features, candidates_size);
            int newReductL = reductsSize - candidates_size;
            int lastFeatureIndex = parameter.indexs[candidates_size - 1];
            int firstFeatureIndex = lastFeatureIndex + 1;
            if (firstFeatureIndex + newReductL > Combination.featureCount) {
                return;
            }
            lastCombination = buildLastCombinationInQuee(newReductL, parameter);
            nexCombinationInK(newReductL, firstFeatureIndex, bm);//Se construye la primera combinación de tamaño k
            if (current == null || lastCombination == null) {
                return;
            }
//            Utils.isSReduct(bm, currentGroup.features, currentGroup.getK())
//Utils.is_super_reduct(bm, currentComb.features,candidates_size,currentComb.k, zeroRowsIndexs)
            while (current != null) {
                currentComb = parameter.combine(current, 0);
                if (Utils.is_super_reduct(bm, currentComb.features, candidates_size, currentComb.k, zeroRowsIndexs)) {
                    reductsFound.add(currentComb.clone(featureMapping));
                    findAllReductsInSubgroups(currentComb, bm);
                }
                nexCombinationInK(newReductL, 0, bm);

            }
        }
    }

    private static void findAllReductsInSubgroups(Combination currentCombination, boolean[][] bm) {
        Combination c = null;
        if (current != null) {
            c = new Combination();
            c.copy(current);
        }
        current = currentCombination;
        if (current.containsAnyGroup()) {
            lastCombinationSet = current.createLastGroup();
            Combination currentGroup = nexCombinationInSet(null);
            if (currentGroup == null || lastCombinationSet == null) {
                return;
            }
            if (!lastCombinationSet.equals(currentGroup)) {
                while (currentGroup != null) {
//                    Utils.is_super_reduct(bm, currentComb.features,candidates_size,currentComb.k, zeroRowsIndexs)
//                    Utils.isSReduct(bm, currentGroup.features, currentGroup.getK())
                    if (Utils.isSReduct(bm, currentGroup.features, currentGroup.getK())) {
                        reductsFound.add(currentGroup.clone(featureMapping));
//                        System.out.println(currentGroup.clone(featureMapping));
                    }
                    currentGroup = nexCombinationInSet(currentGroup);
                }
            }
        }
        current = c;
    }

    static long binomial(final long N, final long K) {
        long ret = 1;
        long max_value = Long.MAX_VALUE / 10000;
        for (int k = 0; k < K; k++) {
            ret *= (N - k);
            ret /= (k + 1);
        }
        if (ret < 0 || ret > max_value) {
            return max_value;
        }
        return ret;
    }

    static long calc_queue_size(final long N, final long K) {
//        System.out.println("Número de subconjuntos: "+Combination.featureCount);
//        System.out.println(binomial(N, K));
        long min_value = binomial(Combination.featureCount, 2);
        long k_value = (long) (binomial(N, K) / EPSILON);
        if (min_value > k_value) {
            return min_value;
        }
        return k_value;
    }

    static int findSizeSuperReduct(boolean coreFound, LinkedList<FeatureSet> listOfFeaturesSets, int[] core, boolean[][] dm) {
        int[] features = new int[Combination.featureCount];
        int k = 0;
        if (coreFound) {
            for (int i = 0; i < Combination.coreSize; i++) {
                features[k++] = listOfFeaturesSets.get(i).getFirstFeature();
            }
        } else {
            features[k++] = listOfFeaturesSets.get(0).getFirstFeature();
        }
        LinkedList<Integer> free_attr = new LinkedList<>();
        if (listOfFeaturesSets.size()==1)
            return k;
        for (int i = k; i < listOfFeaturesSets.size(); i++) {
            free_attr.add(listOfFeaturesSets.get(i).getFirstFeature());
        }
        int [] rows_zero = Utils.getZeroRows(dm, features, k);
        int number_zero_rows = Integer.MAX_VALUE;
        do {
            int best_attr = -1;
            int indx = -1;
            for (int i = 0; i < free_attr.size(); i++) {
                int attr = free_attr.get(i);
                int c = Utils.countZeroRows(dm, rows_zero, attr);
                if (c == 0) {
                    return ++k;
                } else if (c <number_zero_rows) {
                    number_zero_rows = c;
                    best_attr = attr;
                    indx = i;
                }
            }
            free_attr.remove(indx);
            rows_zero = Utils.updateZeroRows(dm, rows_zero, best_attr);
            features[k++] = best_attr;
        } while (true);
    }
//    private static void firstReductRestOfCombination(boolean[][] bm) {
//        if (sReductFound) {
//            return;
//        }
//        current = null;
//        int newCombSize = (reductsSize - lastParameter.k) + 1;
//        int fistFeature = lastParameter.indexs[lastParameter.k - 1] + 1;
//        if ((fistFeature + newCombSize) > Combination.featureCount) {
//            return;
//        }
//        Combination currentComb;
//        lastCombination = buildLastCombinationInQuee(newCombSize);
//        current = nexCombinationInK(newCombSize, fistFeature, bm);
//        if (current == null || lastCombination == null) {
//            return;
//        }
//        if (!lastCombination.equals(current)) {
//            while (current != null) {
//                currentComb = lastParameter.combine(current, 1);
//                if (Utils.isSReduct(bm, currentComb.features, currentComb.getK())) {
//                    sReductFound = true;//Si es un reducto o un super reducto hay que verificar que sea el de tamaño mínimo
//                    tmp.copy(current);//Se guarda el estado actual del espacio de búsqueda
//                    tmpComb.copy(currentComb);
//                    return;//Al encontrar el primer reducto se procede a verificar que no exista otro reducto o super reducto en el espacio k-1               
//                }
//                current = nexCombinationInK(newCombSize, 0, bm);
//            }
//        }
//
//        currentComb = lastParameter.combine(lastCombination, 1);
//        if (Utils.isSReduct(bm, currentComb.features, currentComb.k)) {
//            sReductFound = true;//Si es un reducto o un super reducto hay que verificar que sea el de tamaño mínimo
//            tmp.copy(lastCombination);//Se guarda el estado actual del espacio de búsqueda
//            tmpComb.copy(currentComb);
//        }
//
//    }
//
//    private static void findAllReductRestOfCombination(boolean[][] bm, Combination startComb) {
//        current = startComb;
//        int newCombSize = (reductsSize - lastParameter.k) + 1;
//        int fistFeature = lastParameter.indexs[lastParameter.k - 1] + 1;
//        if ((fistFeature + newCombSize) > Combination.featureCount) {
//            return;
//        }
//        Combination currentComb;
//        lastCombination = buildLastCombinationInQuee(newCombSize);
//        current = nexCombinationInK(newCombSize, fistFeature, bm);
//        if (current == null || lastCombination == null) {
//            return;
//        }
//        if (!lastCombination.equals(current)) {
//            while (current != null) {
//                currentComb = lastParameter.combine(current, 1);
//                if (Utils.isSReduct(bm, currentComb.features, currentComb.getK())) {
//                    reductsFound.add(currentComb.clone(featureMapping));
//                    findAllReductsInSubgroups(currentComb, bm);
//                }
//                current = nexCombinationInK(newCombSize, 0, bm);
//            }
//        }
//        if (lastCombination != null) {
//            currentComb = lastParameter.combine(lastCombination, 1);
//            if (Utils.isSReduct(bm, currentComb.features, currentComb.k)) {
//                reductsFound.add(currentComb.clone(featureMapping));
//            }
//        }
//    }
//    public static void main(String[] args) {
//        System.out.println(binomial(40, 4));
//        System.out.println(calc_queue_size(40, 17));
//    }
}
