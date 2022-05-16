package fhirspark.adapter;

import java.util.List;

import fhirspark.settings.Regex;

public final class RegexAdapter {
    public static String applyRegexToCbioportal(List<Regex> regex, String input) {
        String output = input;
        for (Regex r : regex) {
            output = output.replaceAll(r.getHis(), r.getCbio());
        }
        return output;
    }

    public static String applyRegexFromCbioportal(List<Regex> regex, String input) {
        String output = input;
        for (Regex r : regex) {
            output = output.replaceAll(r.getCbio(), r.getHis());
        }
        return output;
    }
}
