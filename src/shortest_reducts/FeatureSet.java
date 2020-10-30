/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shortest_reducts;

import java.util.LinkedList;

/**
 *
 * @author maximus
 */
public class FeatureSet {

    private int currentFeature;
    private int indexOfCurrentFeature = 0;//guarda el indice de la caracteristica que se esta procesando en el grupo
    private LinkedList<Integer> featuresSet;

    public FeatureSet(LinkedList<Integer> featuresSet) {
        this.featuresSet = featuresSet;
        this.currentFeature = featuresSet.getLast();
    }

    public FeatureSet(int currentFeatutet) {
        this.featuresSet = new LinkedList<>();
        this.featuresSet.add(currentFeatutet);
        this.currentFeature = currentFeatutet;
    }

    public int getCurrentFeature() {
        return currentFeature;
    }

    public int getFeature(int index) {
        return featuresSet.get(index);
    }

    public int getFirstFeature() {
        return featuresSet.getFirst();
    }

    public int getLastFeature() {
        return featuresSet.getLast();
    }

    public boolean onlyOne() {
        return this.featuresSet.size() == 1;
    }

    public void addFeatureFirst(int currentFeatute) {
        this.currentFeature = currentFeatute;
        this.featuresSet.addFirst(currentFeatute);
    }

    public LinkedList<Integer> getFeaturesSet() {
        return featuresSet;
    }

    public void setFeaturesSet(LinkedList<Integer> featuresSet) {
        this.featuresSet = featuresSet;
    }

    public void addFeatureLast(int feature) {
        this.featuresSet.addLast(feature);
    }

    public int size() {
        return featuresSet.size();
    }

    public int nextFeature() {
        this.indexOfCurrentFeature++;
        this.currentFeature = featuresSet.get(indexOfCurrentFeature);
        return currentFeature;
    }

    public boolean contain(int element) {
        return featuresSet.contains(element);
    }
//    public void addFeatureBeforeLast(int feature) {
//            int last_inddex=this.featuresSet.size()-1;
//            this.featuresSet.add(last_inddex,feature);
//    }

    @Override
    public String toString() {
        String str = "";
        str += "{";
        str = featuresSet.stream().map((feature) -> "[" + "X" + (feature + 1) + "]").reduce(str, String::concat);
        str += "}";
        return str;
    }

}
