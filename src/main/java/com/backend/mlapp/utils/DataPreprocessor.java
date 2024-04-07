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

    /**
     * Applies a standard set of preprocessing filters to make the dataset
     * compatible with most algorithms.
     *
     * @param dataset The dataset to preprocess.
     * @return The preprocessed dataset.
     * @throws Exception If an error occurs during preprocessing.
     */
    public static Instances preprocess(Instances dataset) throws Exception {
        MultiFilter multiFilter = new MultiFilter();

        // Prepare filters to be applied
        Filter[] filters = new Filter[]{
                createStringToNominalFilter(dataset),
                createNumericToNominalFilter(dataset),
                new Normalize(), // Optionally, use new Standardize() instead of Normalize()
        };

        multiFilter.setFilters(filters);
        multiFilter.setInputFormat(dataset);

        // Apply all filters
        return Filter.useFilter(dataset, multiFilter);
    }

    private static Filter createStringToNominalFilter(Instances dataset) throws Exception {
        StringToNominal stringToNominal = new StringToNominal();
        stringToNominal.setInputFormat(dataset);
        return stringToNominal;
    }

    private static Filter createNumericToNominalFilter(Instances dataset) throws Exception {
        // Identify which attributes are numeric, for potential conversion to nominal
        String indices = getNumericAttributeIndices(dataset);
        if (!indices.isEmpty()) {
            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices(indices);
            numericToNominal.setInputFormat(dataset);
            return numericToNominal;
        } else {
            // Return a dummy filter when there are no numeric attributes to convert
            return new Filter() {
                @Override
                public boolean setInputFormat(Instances instanceInfo) throws Exception {
                    return false;
                }

                @Override
                public boolean input(Instance instance) {
                    return false;
                }

                @Override
                public boolean batchFinished() throws Exception {
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
