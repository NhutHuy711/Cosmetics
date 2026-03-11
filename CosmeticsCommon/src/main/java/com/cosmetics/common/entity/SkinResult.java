package com.cosmetics.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class SkinResult {

    @JsonProperty("final_skin_type")
    private String finalLabel;

    @JsonProperty("final_probs")
    private List<Float> finalProbs;

    @JsonProperty("region_labels")
    private Map<String, String> regionLabels;

    @JsonProperty("region_probs")
    private Map<String, List<Float>> regionProbs;

    @JsonProperty("patches_base64")
    private Map<String, String> patches;

    // GETTER + SETTER

    public String getFinalLabel() {
        return finalLabel;
    }

    public void setFinalLabel(String finalLabel) {
        this.finalLabel = finalLabel;
    }

    public List<Float> getFinalProbs() {
        return finalProbs;
    }

    public void setFinalProbs(List<Float> finalProbs) {
        this.finalProbs = finalProbs;
    }

    public Map<String, String> getRegionLabels() {
        return regionLabels;
    }

    public void setRegionLabels(Map<String, String> regionLabels) {
        this.regionLabels = regionLabels;
    }

    public Map<String, List<Float>> getRegionProbs() {
        return regionProbs;
    }

    public void setRegionProbs(Map<String, List<Float>> regionProbs) {
        this.regionProbs = regionProbs;
    }

    public Map<String, String> getPatches() {
        return patches;
    }

    public void setPatches(Map<String, String> patches) {
        this.patches = patches;
    }
}
