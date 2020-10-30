package shortest_reducts;

import java.util.Arrays;
import java.util.LinkedList;

public class Combination {

    static int featureCount;//Numero de características
//    static int nextMin = 0;
    static LinkedList<FeatureSet> listOfFeaturesSets = null;
//    static int indexProcessed = 0;
    static int minFeaturesCount = 0;//Tamaño de el array de características con menor cantidad de unos 
    int features[];//array con las caractersticas
    int featuresIndex[];//array con los indices de las características
    int k;
    boolean isCompatible = true;
    static int coreSize = 0;

    public Combination() {
        this.features = new int[featureCount];
        this.featuresIndex = new int[featureCount];
        for (int i = 0; i < featureCount; i++) {
            this.features[i] = Integer.MAX_VALUE - 1;
            this.featuresIndex[i] = Integer.MAX_VALUE - 1;

        }
        this.k = 0;
    }

    public Combination(int[] features, int[] featuresIndex, int k) {
        this.features = features;
        this.featuresIndex = featuresIndex;
        this.k = k;
    }

    public Combination(int f0, int k) {
        this.features = new int[featureCount];
        this.featuresIndex = new int[featureCount];
        for (int i = 0; i < k; i++) {
            this.features[i] = listOfFeaturesSets.get(i + f0).getFirstFeature();//Se obtine el atributo representativo de cada grupo
            this.featuresIndex[i] = i + f0;
        }
        this.k = k;
    }

    public Combination(int f0) {
        this.features = new int[featureCount];
        this.featuresIndex = new int[featureCount];

        for (int i = 0; i < coreSize; i++) {
            this.features[i] = listOfFeaturesSets.get(i).getFirstFeature();
            this.featuresIndex[i] = i;
        }
        this.features[coreSize] = listOfFeaturesSets.get(f0).getFirstFeature();
        this.featuresIndex[coreSize] = f0;
        this.k = coreSize + 1;
    }

    public Combination(int[] features, int k, int[] featureMapping) {
        this.k = k;
        if (featureMapping != null) {
            this.features = new int[features.length];
            for (int i = 0; i < this.k; i++) {
                int dCol = features[i];
                int sCol = featureMapping[dCol];
                this.features[i] = sCol;
            }

        } else {
            this.features = features;
        }
//		Arrays.sort(this.features);
    }

    public void initialiceSet(Combination currentGroup) {
        this.features = new int[featureCount];
        this.featuresIndex = new int[featureCount];
        System.arraycopy(currentGroup.features, 0, features, 0, currentGroup.getK());
        this.k = currentGroup.getK();
    }

    public int getK() {
        return this.k;
    }

    public void prevSpace() {
        if (--k <= 0) {
            return;
        }
        this.features = new int[featureCount];
        for (int i = 0; i < k; i++) {
            this.featuresIndex[i] = i;
            this.features[i] = listOfFeaturesSets.get(i).getFirstFeature();
        }
    }

    public void add(int f) {
        if (this.k == featureCount) {
            return;
        }
        this.features[k] = listOfFeaturesSets.get(f).getFirstFeature();
        this.featuresIndex[k++] = f;
    }

    public int get(int index) {
        if (index < 0 || index >= this.k) {
            return -1;
        }
        return this.featuresIndex[index];
    }

    public int getIndex(int index) {
        if (index < 0 || index >= this.k) {
            return -1;
        }
        return this.features[index];
    }

    public void set(int index, int f) {
        if (index < 0 || index >= this.k) {
            return;
        }
        this.features[index] = listOfFeaturesSets.get(f).getFirstFeature();
        this.featuresIndex[index] = f;
    }

    public void set(int index, int f, Combination currentCombSet) {
        if (index < 0 || index >= this.k) {
            return;
        }
        int currentSet = currentCombSet.featuresIndex[index];
//        System.out.println("este es el indice"+f);
        this.features[index] = listOfFeaturesSets.get(currentSet).getFeature(f);
        this.featuresIndex[index] = f;
    }

    public Combination createLastGroup() {
        Combination lastGroup = new Combination();
        lastGroup.k = this.k;
        for (int i = coreSize; i < this.getK(); i++) {
            lastGroup.featuresIndex[i] = listOfFeaturesSets.get(featuresIndex[i]).size() - 1;
            lastGroup.features[i] = listOfFeaturesSets.get(featuresIndex[i]).getLastFeature();
        }
        return lastGroup;
    }

    public int getGroupSize(int i) {
        return listOfFeaturesSets.get(featuresIndex[i]).size();
    }

    public boolean containsAnyGroup() {
        for (int i = 0; i < k; i++) {
            if (getGroupSize(i) > 1) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAny(boolean[] b) {
        for (int i = 0; i < k; i++) {
            if (b[features[i]]) {
                return true;
            }
        }
        return false;
    }

    public Combination clone(int[] featureMapping) {
        Combination clone = new Combination();
        clone.k = this.k;
        for (int i = 0; i < this.k; i++) {
            int dCol = this.features[i];
            int sCol = featureMapping[dCol];
            clone.features[i] = sCol;
        }
//        Arrays.sort(clone.features);
        return clone;
    }

    public void copy(Combination src) {
        this.k = src.k;
        System.arraycopy(src.features, 0, features, 0, src.k);
        System.arraycopy(src.featuresIndex, 0, featuresIndex, 0, src.k);
        this.isCompatible = src.isCompatible;
    }

    @Override
    public String toString() {
        String str = "";
        for (int f = 0; f < this.k - 1; f++) {
            str += "X" + (this.features[f] + 1) + ",";
        }
        str += "X" + (this.features[k - 1] + 1);
        return str;
    }

    public String toStringIndex() {
        String str = "";
        for (int f = 0; f < this.k - 1; f++) {
            str += (this.featuresIndex[f]) + ",";
        }
        str += (this.featuresIndex[k - 1]);
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combination)) {
            return false;
        }
        Combination cmb = (Combination) o;
        return this.k == cmb.k && Arrays.equals(this.features, cmb.features);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Arrays.hashCode(this.features);
        hash = 67 * hash + this.k;
        return hash;
    }

}
