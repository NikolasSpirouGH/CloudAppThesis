package com.backend.mlapp.utils;

import org.springframework.stereotype.Component;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.Normalize;

@Component
public class DataPreprocessor {

    public static Instances preprocess(Instances dataset) throws Exception {
        MultiFilter multiFilter = new MultiFilter();

        Filter[] filters = new Filter[]{
                createStringToNominalFilter(dataset),
                createNumericToNominalFilter(dataset),
                new Normalize(),
        };

        multiFilter.setFilters(filters);
        multiFilter.setInputFormat(dataset);
        return Filter.useFilter(dataset, multiFilter);
    }

    private static Filter createStringToNominalFilter(Instances dataset){
        StringToNominal stringToNominal = new StringToNominal();
        try {
            stringToNominal.setInputFormat(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stringToNominal;
    }

    private static Filter createNumericToNominalFilter(Instances dataset) {
        String indices = getNumericAttributeIndices(dataset);
        if (!indices.isEmpty()) {
            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices(indices);
            try {
                numericToNominal.setInputFormat(dataset);
            } catch (Exception e) {

                throw new RuntimeException(e);
            }
            return numericToNominal;
        } else {
            return new Filter() {
                @Override
                public boolean setInputFormat(Instances instanceInfo) {
                    return false;
                }

                @Override
                public boolean input(Instance instance) {
                    return false;
                }

                @Override
                public boolean batchFinished(){
                    return false;
                }

                @Override
                public Instance output() {
                    return null;
                }

                @Override
                public Instance outputPeek() {
                    return null;
                }
            };
        }
    }

    private static String getNumericAttributeIndices(Instances dataset) {
        StringBuilder indices = new StringBuilder();
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (dataset.attribute(i).isNumeric()) {
                if (indices.length() > 0) indices.append(",");
                indices.append(i + 1); // WEKA uses 1-based indices
            }
        }
        return indices.toString();
    }
}
