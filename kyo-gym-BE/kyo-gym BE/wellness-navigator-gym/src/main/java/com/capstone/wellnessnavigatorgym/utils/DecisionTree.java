package com.capstone.wellnessnavigatorgym.utils;

import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DecisionTree {

    private List<TrackDataAi> trackDataAiData;
    private Map<String, Double> informationGainCache = new ConcurrentHashMap<>();
    private Map<Integer, Double> entropyCache = new ConcurrentHashMap<>();

    public DecisionTree(List<TrackDataAi> trackDataAis) {
        this.trackDataAiData = new ArrayList<>(trackDataAis);
    }

    public List<TrackDataAi> getTrackDataAiData() {
        return trackDataAiData;
    }

    public double calculateInformationGain(String attributeName) {
        return informationGainCache.computeIfAbsent(attributeName, this::computeInformationGain);
    }

    private double computeInformationGain(String attributeName) {
        double totalSize = trackDataAiData.size();
        if (totalSize == 0) return 0;

        double entropyS = calculateEntropyForWholeDataSet();
        double informationGain = entropyS;

        Map<Object, List<TrackDataAi>> subsets = getSubsetsByAttributeValue(attributeName);
        for (List<TrackDataAi> subset : subsets.values()) {
            double subsetEntropy = calculateEntropyForSubset(subset);
            informationGain -= (subset.size() / totalSize) * subsetEntropy;
        }
        return informationGain;
    }

    public Map<Object, List<TrackDataAi>> getSubsetsByAttributeValue(String attributeName) {
        return trackDataAiData.stream().collect(Collectors.groupingBy(e -> e.getAttributeValue(attributeName)));
    }

    private double calculateEntropyForWholeDataSet() {
        return calculateEntropyForSubset(trackDataAiData);
    }

    private double calculateEntropyForSubset(List<TrackDataAi> subset) {
        int hashCode = subset.hashCode();
        return entropyCache.computeIfAbsent(hashCode, k -> calculateEntropyForSubsetUncached(subset));
    }

    private double calculateEntropyForSubsetUncached(List<TrackDataAi> subset) {
        int positiveCount = countEffective(subset, true);
        int negativeCount = subset.size() - positiveCount;
        return calculateEntropy(positiveCount, negativeCount);
    }

    private int countEffective(List<TrackDataAi> dataList, boolean effective) {
        return (int) dataList.stream().filter(e -> e.getEffective() == effective).count();
    }

    private double calculateEntropy(int positiveCount, int negativeCount) {
        if (positiveCount == 0 || negativeCount == 0) {
            return 0.0; // No entropy if there's no diversity
        }
        int totalCount = positiveCount + negativeCount;
        double posProb = (double) positiveCount / totalCount;
        double negProb = (double) negativeCount / totalCount;
        return -posProb * Math.log(posProb) / Math.log(2) - negProb * Math.log(negProb) / Math.log(2);
    }

    public double calculateSplitInfo(String attributeName) {
        double totalSize = trackDataAiData.size();
        if (totalSize == 0) return 0;

        Map<Object, List<TrackDataAi>> subsets = getSubsetsByAttributeValue(attributeName);
        double splitInfo = subsets.values().parallelStream()
                .mapToDouble(subset -> {
                    double proportion = subset.size() / totalSize;
                    return proportion == 0 ? 0 : -proportion * Math.log(proportion) / Math.log(2);
                }).sum();

        return splitInfo;
    }

    public double calculateGainRatio(String attributeName) {
        double informationGain = calculateInformationGain(attributeName);
        double splitInfo = calculateSplitInfo(attributeName);

        // Avoid division by zero
        if (splitInfo == 0) {
            return informationGain > 0 ? Double.MAX_VALUE : 0;
        }

        return informationGain / splitInfo;
    }

}